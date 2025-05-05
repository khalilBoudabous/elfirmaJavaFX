package controllers;

import entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ProduitService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

public class FormulairePaiement implements Initializable {

    @FXML private TextField cardNameField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private Label amountLabel;
    @FXML private TextField codePromoField;
    @FXML private Label codePromoStatusLabel;
    @FXML private Button payButton;
    @FXML private Button cancelButton;

    private Produit produit;
    private ProduitService produitService = new ProduitService();
    private float finalPrice;
    private String paymentIntentId; // To store the PaymentIntent ID for the invoice
    private boolean paymentSuccessful = false;

    private static final String CARD_NUMBER_PATTERN = "^\\d{16}$";
    private static final String EXPIRY_DATE_PATTERN = "^(0[1-9]|1[0-2])\\/\\d{2}$";
    private static final String CVV_PATTERN = "^\\d{3,4}$";

    public FormulairePaiement() {
        // Load .env file
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();
        System.out.println("Fichier .env chargé avec succès dans FormulairePaiement. Chemin: " + System.getProperty("user.dir"));

        // Retrieve STRIPE_SECRET_KEY
        String stripeSecretKey = dotenv.get("STRIPE_SECRET_KEY");
        System.out.println("STRIPE_SECRET_KEY from .env dans FormulairePaiement: " + (stripeSecretKey != null ? "Clé trouvée" : "Clé non trouvée"));

        if (stripeSecretKey == null || stripeSecretKey.isEmpty()) {
            throw new IllegalStateException("STRIPE_SECRET_KEY n'est pas défini dans le fichier .env");
        }
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set a default value for amountLabel
        if (produit == null) {
            amountLabel.setText("0.00 USD");
        }

        // Add a listener to codePromoField to validate the promo code as the user types
        codePromoField.textProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("Code promo field changed: " + newValue);
            validateCodePromo();
        });
    }

    public void setProduit(Produit produit) {
        if (produit == null) {
            System.out.println("Produit is null in setProduit");
            amountLabel.setText("0.00 USD");
            return;
        }
        this.produit = produit;
        finalPrice = produit.getPrix();
        System.out.println("Setting amount to: " + finalPrice);
        amountLabel.setText(String.format("%.2f USD", finalPrice));
    }

    @FXML
    private void validateCodePromo() {
        String enteredCode = codePromoField.getText().trim();
        System.out.println("Validating promo code: " + enteredCode);

        if (enteredCode.isEmpty()) {
            codePromoStatusLabel.setText("");
            codePromoStatusLabel.setStyle(""); // Clear the style
            if (produit != null) {
                finalPrice = produit.getPrix();
                amountLabel.setText(String.format("%.2f USD", finalPrice));
                System.out.println("Promo code is empty, resetting price to: " + finalPrice + " USD");
            } else {
                System.out.println("Produit is null when resetting promo code");
                amountLabel.setText("0.00 USD");
            }
            return;
        }

        if (produit == null) {
            System.out.println("Produit is null in validateCodePromo");
            codePromoStatusLabel.setText("Erreur : produit non défini.");
            codePromoStatusLabel.setStyle("-fx-text-fill: red;");
            amountLabel.setText("0.00 USD");
            return;
        }

        try {
            Produit produitWithCode = produitService.getProduitByCodePromo(enteredCode);
            System.out.println("Produit with promo code: " + (produitWithCode != null ? produitWithCode.getNom_produit() : "null"));
            System.out.println("Current product ID: " + produit.getId() + ", Promo code product ID: " + (produitWithCode != null ? produitWithCode.getId() : "N/A"));

            if (produitWithCode != null && produitWithCode.getId() == produit.getId()) {
                // Code promo valide pour ce produit
                codePromoStatusLabel.setText("Code promo valide !");
                codePromoStatusLabel.setStyle("-fx-text-fill: green;");
                float discountPercentage = produitWithCode.getDiscount_percentage();
                finalPrice = produit.getPrix() * (1 - discountPercentage / 100);
                amountLabel.setText(String.format("%.2f USD (Réduction de %.0f%%)", finalPrice, discountPercentage));
                System.out.println("Promo code valid, new price: " + finalPrice + " USD, discount: " + discountPercentage + "%");
            } else {
                // Code promo invalide
                codePromoStatusLabel.setText("Code promo invalide.");
                codePromoStatusLabel.setStyle("-fx-text-fill: red;");
                finalPrice = produit.getPrix();
                amountLabel.setText(String.format("%.2f USD", finalPrice));
                System.out.println("Promo code invalid, resetting price to: " + finalPrice + " USD");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            codePromoStatusLabel.setText("Erreur lors de la vérification.");
            codePromoStatusLabel.setStyle("-fx-text-fill: red;");
            System.out.println("SQL error during promo code validation: " + e.getMessage());
            if (produit != null) {
                finalPrice = produit.getPrix();
                amountLabel.setText(String.format("%.2f USD", finalPrice));
            } else {
                amountLabel.setText("0.00 USD");
            }
        }
    }

    @FXML
    private void processPayment() {
        System.out.println("Bouton Payer cliqué.");

        // Validation des champs
        if (cardNameField.getText().isEmpty() ||
                cardNumberField.getText().isEmpty() ||
                expiryDateField.getText().isEmpty() ||
                cvvField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        String cardNumber = cardNumberField.getText().trim().replaceAll("[\\s-]", "");
        String expiryDate = expiryDateField.getText().trim();
        String cvv = cvvField.getText().trim();

        if (!Pattern.matches(CARD_NUMBER_PATTERN, cardNumber)) {
            showAlert("Erreur", "Le numéro de carte doit contenir 16 chiffres.");
            return;
        }
        if (!Pattern.matches(EXPIRY_DATE_PATTERN, expiryDate)) {
            showAlert("Erreur", "La date d'expiration doit être au format MM/YY.");
            return;
        }
        if (!Pattern.matches(CVV_PATTERN, cvv)) {
            showAlert("Erreur", "Le CVV doit contenir 3 ou 4 chiffres.");
            return;
        }

        // Processus de paiement avec Stripe using a test payment method
        try {
            // Use a Stripe test payment method ID to simulate a payment
            String testPaymentMethodId = "pm_card_visa"; // Test payment method for Visa card (Stripe test mode)

            // Créer les paramètres pour le PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (finalPrice * 100)) // Montant en centimes (Stripe utilise les centimes)
                    .setCurrency("USD") // Devise : US Dollar
                    .setDescription("Paiement pour le produit : " + produit.getNom_produit())
                    .setPaymentMethod(testPaymentMethodId)
                    .setConfirm(true)
                    .setReturnUrl("http://localhost:8080/payment/success")
                    .build();

            // Créer et confirmer le PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            System.out.println("Paiement réussi avec Stripe. PaymentIntent ID : " + paymentIntent.getId());

            // Vérifier le statut du paiement
            if ("succeeded".equals(paymentIntent.getStatus())) {
                paymentSuccessful = true; // Set paymentSuccessful to true
                paymentIntentId = paymentIntent.getId(); // Store the PaymentIntent ID for the invoice
                showAlert("Succès", "Paiement effectué avec succès ! Montant : " + String.format("%.2f USD", finalPrice));

                // Ask if the user wants an invoice
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Facture");
                confirmation.setHeaderText("Paiement Confirmé");
                confirmation.setContentText("Voulez-vous avoir une facture ?");
                Optional<ButtonType> result = confirmation.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    generateAndDownloadInvoice();
                }

                // Close the window after payment
                Stage stage = (Stage) payButton.getScene().getWindow();
                stage.close();
            } else {
                paymentSuccessful = false; // Set paymentSuccessful to false
                showAlert("Erreur", "Le paiement a échoué. Statut : " + paymentIntent.getStatus());
            }

        } catch (StripeException e) {
            paymentSuccessful = false; // Set paymentSuccessful to false
            System.err.println("Erreur lors du traitement du paiement avec Stripe : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du paiement : " + e.getMessage());
        }
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    private void generateAndDownloadInvoice() {
        try {
            // Generate a unique transaction ID (in case PaymentIntent ID isn't sufficient)
            String transactionId = UUID.randomUUID().toString();

            // Get current date and time
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String dateTime = now.format(formatter);

            // Prepare invoice data
            String cardholderName = cardNameField.getText().trim();
            String finalAmount = String.format("%.2f USD", finalPrice);
            String promoCode = codePromoField.getText().trim().isEmpty() ? "Aucun" : codePromoField.getText().trim();
            String promoStatus = codePromoStatusLabel.getText().isEmpty() ? "Non appliqué" : codePromoStatusLabel.getText();
            String productName = produit != null ? produit.getNom_produit() : "Produit non spécifié";

            // Show FileChooser to let the user select where to save the PDF
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la facture");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("Facture_" + transactionId.substring(0, 8) + ".pdf");
            java.io.File file = fileChooser.showSaveDialog(payButton.getScene().getWindow());

            if (file != null) {
                // Create a new PDF document
                try (PDDocument document = new PDDocument()) {
                    PDPage page = new PDPage();
                    document.addPage(page);

                    // Start a content stream to write to the page
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        // Set initial position and line spacing
                        float yPosition = 750; // Start near the top of the page
                        float leading = 20; // Line spacing
                        float margin = 50; // Left margin

                        // Begin text
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Facture de Paiement");
                        contentStream.endText();

                        // Move to next line
                        yPosition -= leading;

                        // Add invoice details
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Transaction ID: " + paymentIntentId);
                        contentStream.endText();
                        yPosition -= leading;

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Date et Heure: " + dateTime);
                        contentStream.endText();
                        yPosition -= leading;

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Produit: " + productName);
                        contentStream.endText();
                        yPosition -= leading * 2; // Extra space

                        // Payment details section
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Détails du Paiement");
                        contentStream.endText();
                        yPosition -= leading;

                        // Add payment details
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Nom sur la carte: " + cardholderName);
                        contentStream.endText();
                        yPosition -= leading;

                        // Include last 4 digits of the card number
                        String lastFourDigits = cardNumberField.getText().trim().substring(cardNumberField.getText().length() - 4);
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Carte: **** **** **** " + lastFourDigits);
                        contentStream.endText();
                        yPosition -= leading;

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Montant: " + finalAmount);
                        contentStream.endText();
                        yPosition -= leading;

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Code Promo: " + promoCode);
                        contentStream.endText();
                        yPosition -= leading;

                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Statut du Code Promo: " + promoStatus);
                        contentStream.endText();
                        yPosition -= leading * 2;

                        // Thank you message
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                        contentStream.newLineAtOffset(margin, yPosition);
                        contentStream.showText("Merci pour votre achat !");
                        contentStream.endText();
                    }

                    // Save the document
                    document.save(file);
                }

                showAlert("Succès", "Facture téléchargée avec succès !");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Échec de la génération de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}