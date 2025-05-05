package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private static Utilisateur loggedInUser;

    public static Utilisateur getLoggedInUser() {
        return loggedInUser;
    }

    private final UtilisateurService userService = new UtilisateurService();
       @FXML
       private void handleLogin() {
           String email = emailField.getText().trim();
           String password = passwordField.getText().trim();

           if (email.isEmpty() || password.isEmpty()) {
               showError("Veuillez remplir tous les champs !");
               return;
           }

           try {
               Utilisateur user = userService.checkLogin(email, password);

               if (user != null) {
                   if (user.isBlocked()) {
                       showError("Compte bloqué, contacter l'administrateur !");
                   } else {
                       loggedInUser = user; // Set the logged-in user
                       if (user.getType().equalsIgnoreCase("admin")) {
                           redirectToListPage();
                       } else if (user.getType().equalsIgnoreCase("fournisseur")) {
                           redirectToDbEvenement(user);
                       } else {
                           redirectToProfile(user);
                       }
                   }
               }
               else
               {
                   showError("Email ou mot de passe incorrect !");
               }
           } catch (SQLException e) {
               e.printStackTrace();
               showError("Erreur de base de données. Veuillez réessayer.");
           }
       }
    private void redirectToListPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherListUtilisateur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setTitle("User List");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void redirectToProfile(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontOfficeEvenement.fxml"));
            Parent root = loader.load();

            FrontOfficeEvenement controller = loader.getController();
            controller.initData(user.getId()); // Appel correct

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Front Office");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page utilisateur.");
        }
    }

    private void redirectToDbEvenement(Utilisateur user) {
        if (user == null) {
            showError("Erreur : utilisateur non connecté.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dbEvenement.fxml"));
            Parent root = loader.load();

            AfficherEvenement controller = loader.getController();
            controller.initData(user); // Pass the logged-in user

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("DB Evenement");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page DB Evenement.");
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
    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Password Reset");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load password reset window");
        }
    }


}