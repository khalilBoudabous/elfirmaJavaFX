package controllers;

import entities.Utilisateur;
import test.MainFX;
import utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

import static test.MainFX.setCurrentUser;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // Database configuration (should be moved to config file)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mainelfirma";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

   // private static Utilisateur currentUser;

    @FXML
    private void initialize() {
        // Set initial focus
        emailField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (!validateInputs(email, password)) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Utilisateur user = authenticateUser(conn, email, password);

            if (user != null) {
                // Use the setter method to update currentUser
                setCurrentUser(user);
                redirectToTerrainView();
            }
        } catch (SQLException e) {
            showError("Database connection error. Please try again.");
            System.err.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            showError("An unexpected error occurred");
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    @FXML
    private void redirectToRegister() {
        MainFX.showRegisterView();
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return false;
        }

        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    private Utilisateur authenticateUser(Connection conn, String email, String password) throws SQLException {
        String sql = "SELECT id, nom, prenom, email, telephone, role, mot_de_passe FROM utilisateur WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("mot_de_passe");

                    if (PasswordUtils.verifyPassword(password, storedHash)) {
                        return new Utilisateur(
                                rs.getInt("id"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                rs.getInt("telephone"),
                                rs.getString("role"),
                                storedHash
                        ) {
                            @Override
                            public String getType() {
                                return "";
                            }
                        };
                    } else {
                        showError("Invalid email or password");
                    }
                } else {
                    showError("Account not found");
                }
            }
        }
        return null;
    }

    private void redirectToTerrainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Terrain.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(loader.load());

            stage.setScene(scene);
            stage.setTitle("Terrain Management");
            stage.centerOnScreen();
        } catch (Exception e) {
            MainFX.showFatalError("Failed to load terrain view", e);
        }
    }

    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setText(message);
        passwordField.clear();
        passwordField.requestFocus();
    }


}
