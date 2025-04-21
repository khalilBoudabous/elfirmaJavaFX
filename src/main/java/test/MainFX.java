package test;

import entities.Utilisateur;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    private static Stage primaryStage;
    private static Utilisateur currentUser;  // Centralized user session management

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        try {
            // Initial configuration
            stage.setMinWidth(1024);
            stage.setMinHeight(768);

            // Start with login view
            showLoginView();
            stage.show();
        } catch (Exception e) {
            showFatalError("Application initialization failed", e);
        }
    }

    // ====================== User Session Management ======================
    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static void clearCurrentUser() {
        currentUser = null;
    }

    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    // ====================== View Navigation Methods ======================
    public static void showLoginView() {
        loadView("/fxml/Login.fxml", "Connexion - Gestion des Terrains");
    }

    public static void showRegisterView() {
        loadView("/fxml/Register.fxml", "Inscription - Gestion des Terrains");
    }

    public static void showTerrainView() {
        loadView("/fxml/Terrain.fxml", "Gestion des Terrains");
    }

    public static void showUtilisateurView() {
        loadView("/fxml/Utilisateur.fxml", "Gestion des Utilisateurs");  // Fixed typo in filename
    }

    // ====================== Core View Loading Logic ======================
    private static void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainFX.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            showErrorAlert("FXML Loading Error", "Failed to load: " + fxmlPath, e);
        } catch (IllegalStateException e) {
            showErrorAlert("Resource Not Found", "File not found: " + fxmlPath, e);
        } catch (Exception e) {
            showErrorAlert("Unexpected Error", "Error loading view", e);
        }
    }

    // ====================== Logout & Error Handling ======================
    public static void onLogout() {
        clearCurrentUser();
        showLoginView();
    }

    public static void showErrorAlert(String title, String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(e.getClass().getSimpleName() + ": " + e.getMessage());
        alert.showAndWait();
    }

    public static void showFatalError(String message, Exception e) {
        showErrorAlert("Critical Error", message, e);
        System.exit(1);
    }

    // ====================== Utility Methods ======================
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}