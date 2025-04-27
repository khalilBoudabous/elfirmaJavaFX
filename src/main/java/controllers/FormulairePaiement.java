package controllers;

import entities.Produit;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.ProduitService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
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
        // Add a listener to codePromoField to validate the promo code as the user types
        codePromoField.textProperty().addListener((obs, oldValue, newValue) -> {
            System.out.println("Code promo field changed: " + newValue);
            validateCodePromo();
        });
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        finalPrice = produit.getPrix();
        amountLabel.setText(String.format("%.2f USD", finalPrice));
    }

    @FXML
    private void validateCodePromo() {
        String enteredCode = codePromoField.getText().trim();
        System.out.println("Validating promo code: " + enteredCode);

        if (enteredCode.isEmpty()) {
            codePromoStatusLabel.setText("");
            finalPrice = produit.getPrix();
            amountLabel.setText(String.format("%.2f USD", finalPrice));
            System.out.println("Promo code is empty, resetting price to: " + finalPrice + " USD");
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
                showAlert("Succès", "Paiement effectué avec succès ! Montant : " + String.format("%.2f USD", finalPrice));
                Stage stage = (Stage) payButton.getScene().getWindow();
                stage.close();
            } else {
                showAlert("Erreur", "Le paiement a échoué. Statut : " + paymentIntent.getStatus());
            }

        } catch (StripeException e) {
            System.err.println("Erreur lors du traitement du paiement avec Stripe : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du paiement : " + e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}