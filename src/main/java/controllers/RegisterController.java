package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import test.MainFX;
import utils.PasswordUtils;

import java.sql.*;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mainelfirma";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Admin", "User");
        roleComboBox.setValue("User");
    }

    @FXML
    private void handleRegister() {
        // Step 1: Validate input fields
        if (!validateInputs()) return;

        // Step 2: Try connecting to the database and register the user
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Step 3: Check if email already exists in the database
            String email = emailField.getText().trim();
            if (emailExists(conn, email)) {
                showError("Email déjà enregistré");
                showAlert(AlertType.ERROR, "Erreur d'inscription", "Email déjà enregistré.");
                return;
            }

            // Step 4: Attempt to register the user
            if (registerUser(conn)) {
                showSuccess("Inscription réussie !");
                showAlert(AlertType.INFORMATION, "Succès", "Inscription réussie !");
                clearFields();
                MainFX.showLoginView();  // Navigate to login view
            } else {
                showError("Échec de l'inscription");
                showAlert(AlertType.ERROR, "Erreur", "Échec de l'inscription.");
            }

        } catch (SQLException e) {
            // Step 5: Catch any SQL exceptions and display error message
            showError("Erreur base de données : " + e.getMessage());
            showAlert(AlertType.ERROR, "Erreur SQL", e.getMessage());
            e.printStackTrace();  // For debugging
        }
    }
    private void showAlert(AlertType type, String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    @FXML
    private void redirectToLogin() {
        MainFX.showLoginView();
    }

    private boolean validateInputs() {
        // Example of input validation, you can extend it as needed
        if (nomField.getText().trim().isEmpty() || prenomField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() || telephoneField.getText().trim().isEmpty() ||
                roleComboBox.getValue() == null || passwordField.getText().trim().isEmpty() ||
                confirmPasswordField.getText().trim().isEmpty()) {
            showError("Tous les champs doivent être remplis");
            return false;
        }

        // Check if passwords match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas");
            return false;
        }

        return true;
    }

    private boolean emailExists(Connection conn, String email) throws SQLException {
        final String SQL = "SELECT 1 FROM utilisateur WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        }
    }

    private boolean registerUser(Connection conn) throws SQLException {
        final String SQL = "INSERT INTO utilisateur (nom, prenom, email, telephone, role, mot_de_passe) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nomField.getText().trim());
            stmt.setString(2, prenomField.getText().trim());
            stmt.setString(3, emailField.getText().trim());
            stmt.setString(4, telephoneField.getText().trim());  // Changed from Integer to String
            stmt.setString(5, roleComboBox.getValue());
            stmt.setString(6, PasswordUtils.hashPassword(passwordField.getText()));

            return stmt.executeUpdate() > 0;
        }
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setText(message);
    }

    private void showSuccess(String message) {
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setText(message);
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}
