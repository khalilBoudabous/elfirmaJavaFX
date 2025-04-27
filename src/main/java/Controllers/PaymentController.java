package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.util.regex.Pattern;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

public class PaymentController {

    // Regex patterns for card info
    private static final String CARD_NUMBER_PATTERN = "\\d{16}";
    private static final String EXPIRY_DATE_PATTERN = "(0[1-9]|1[0-2])/\\d{2}";
    private static final String CVV_PATTERN = "\\d{3,4}";

    @FXML private TextField tfCardNumber;
    @FXML private TextField tfExpiry;
    @FXML private TextField tfCVC;
    @FXML private TextField tfAmount;
    @FXML private Button payButton;

    private boolean paymentSuccess = false;
    private entities.Ticket ticket; // Hold the ticket to pay

    public void setTicket(entities.Ticket ticket) {
        this.ticket = ticket;
        // Pre-fill the amount from the ticket
        tfAmount.setText(String.valueOf(ticket.getPrix()));
    }

    public boolean isPaymentSuccess() {
        return paymentSuccess;
    }

    @FXML
    public void handleSubmitPayment() {
        processPayment();
    }

    private void processPayment() {
        // Validate fields
        if (tfCardNumber.getText().isEmpty() ||
            tfExpiry.getText().isEmpty() ||
            tfCVC.getText().isEmpty() ||
            tfAmount.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }
        
        String cardNumber = tfCardNumber.getText().trim().replaceAll("[\\s-]", "");
        String expiryDate = tfExpiry.getText().trim();
        String cvv = tfCVC.getText().trim();

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
        
        double finalPrice;
        try {
            finalPrice = Double.parseDouble(tfAmount.getText());
        } catch (NumberFormatException ex) {
            showAlert("Erreur", "Montant invalide.");
            return;
        }

        try {
            // Load the API key from .env using java-dotenv
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.load();
            String stripeKey = dotenv.get("STRIPE_API_KEY");
            if (stripeKey == null || stripeKey.isEmpty()) {
                throw new Exception("Stripe API key not set.");
            }
            com.stripe.Stripe.apiKey = stripeKey;
            // Use a Stripe test payment method to simulate payment
            String testPaymentMethodId = "pm_card_visa";

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (finalPrice * 100))
                    .setCurrency("usd")
                    .setDescription("Paiement pour ticket")
                    .setPaymentMethod(testPaymentMethodId)
                    .setConfirm(true)
                    .setReturnUrl("http://localhost:8080/payment/success")
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            if ("succeeded".equals(paymentIntent.getStatus())) {
                showAlert("Succès", "Paiement effectué avec succès ! Montant : " + String.format("%.2f USD", finalPrice));
                paymentSuccess = true;
                Stage stage = (Stage) payButton.getScene().getWindow();
                stage.close();
            } else {
                showAlert("Erreur", "Le paiement a échoué. Statut : " + paymentIntent.getStatus());
            }
        } catch (StripeException e) {
            showAlert("Erreur", "Erreur lors du paiement : " + e.getMessage());
        } catch (Exception ex) {
            showAlert("Erreur", ex.getMessage());
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
