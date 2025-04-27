package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.UtilisateurService;

public class ResetPasswordController {

    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    private void handleReset() {
        String token = tokenField.getText().trim();
        String pwd   = newPasswordField.getText();
        String conf  = confirmPasswordField.getText();

        if (token.isEmpty() || pwd.isEmpty() || conf.isEmpty()) {
            showMessage("Tous les champs sont obligatoires", true);
            return;
        }
        if (!pwd.equals(conf)) {
            showMessage("Les mots de passe ne correspondent pas", true);
            return;
        }
        try {
            boolean ok = userService.resetPassword(token, pwd);
            if (ok) {
                showMessage("Mot de passe réinitialisé avec succès", false);
            } else {
                showMessage("Token invalide ou expiré", true);
            }
        } catch (Exception e) {
            showMessage("Erreur serveur, réessayez plus tard", true);
            e.printStackTrace();
        }
    }

    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.setVisible(true);
        messageLabel.setStyle(error ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
}
