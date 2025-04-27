package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.sql.SQLException;
import java.io.IOException;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    private void handleSubmit() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showMessage("Please enter your email", true);
            return;
        }

        try {
            boolean success = userService.handlePasswordResetRequest(email);
            if (success) {
                showMessage("Reset instructions sent to your email", false);
                // Ferme la fenêtre actuelle
                emailField.getScene().getWindow().hide();
                // Ouvre la fenêtre pour saisir le token + nouveau mot de passe
                openResetPasswordWindow();
            } else {
                showMessage("Email not found in our system", true);
            }
        } catch (SQLException e) {
            showMessage("Error processing request", true);
            e.printStackTrace();
        }
    }

    private void openResetPasswordWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ResetPasswordView.fxml")
            );
            Stage resetStage = new Stage();
            resetStage.setScene(new Scene(loader.load()));
            resetStage.setTitle("Réinitialiser le mot de passe");

            resetStage.initModality(Modality.APPLICATION_MODAL);
            resetStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Vous pouvez afficher un alert ici si voulu
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setStyle(isError
                ? "-fx-text-fill: red;"
                : "-fx-text-fill: green;"
        );
    }
}
