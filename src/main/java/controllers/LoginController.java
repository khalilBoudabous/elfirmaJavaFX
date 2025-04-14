package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        Utilisateur user = userService.checkLogin(email, password);
        if (user != null) {
            redirectToListPage();
        } else {
            showError("Invalid email or password!");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void redirectToListPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherListUtilisateur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User List");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void redirectToAjouterUtilisateur() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajouterUtilisateur.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Utilisateur");
            stage.show();

            // Close current window if needed
            ((Stage) emailField.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de charger la page d'ajout");
        }
    }
}