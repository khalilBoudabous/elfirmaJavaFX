package controllers;

import entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.ProduitService;

import java.sql.SQLException;

public class FormulairePaiement {

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

    public void setProduit(Produit produit) {
        this.produit = produit;
        finalPrice = produit.getPrix();
        amountLabel.setText(String.format("%.2f DT", finalPrice));
    }

    @FXML
    private void validateCodePromo() {
        String enteredCode = codePromoField.getText().trim();
        if (enteredCode.isEmpty()) {
            codePromoStatusLabel.setText("");
            finalPrice = produit.getPrix();
            amountLabel.setText(String.format("%.2f DT", finalPrice));
            return;
        }

        try {
            Produit produitWithCode = produitService.getProduitByCodePromo(enteredCode);
            if (produitWithCode != null && produitWithCode.getId() == produit.getId()) {
                // Code promo valide pour ce produit
                codePromoStatusLabel.setText("Code promo valide !");
                codePromoStatusLabel.setStyle("-fx-text-fill: green;");
                float discountPercentage = produitWithCode.getDiscount_percentage();
                finalPrice = produit.getPrix() * (1 - discountPercentage / 100);
                amountLabel.setText(String.format("%.2f DT (Réduction de %.0f%%)", finalPrice, discountPercentage));
            } else {
                // Code promo invalide
                codePromoStatusLabel.setText("Code promo invalide.");
                codePromoStatusLabel.setStyle("-fx-text-fill: red;");
                finalPrice = produit.getPrix();
                amountLabel.setText(String.format("%.2f DT", finalPrice));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            codePromoStatusLabel.setText("Erreur lors de la vérification.");
            codePromoStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void processPayment() {
        // Validation simple des champs
        if (cardNameField.getText().isEmpty() ||
                cardNumberField.getText().isEmpty() ||
                expiryDateField.getText().isEmpty() ||
                cvvField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // Ici, vous pouvez ajouter une logique réelle de paiement (par exemple, appeler une API de paiement)
        showAlert("Succès", "Paiement effectué avec succès ! Montant : " + String.format("%.2f DT", finalPrice));
        Stage stage = (Stage) payButton.getScene().getWindow();
        stage.close();
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