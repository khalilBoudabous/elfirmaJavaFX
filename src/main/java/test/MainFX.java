package test;

import entities.Utilisateur;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    private static Stage primaryStage;
    private static entities.Utilisateur currentUser;  // Centralized user session management

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

    /**
     * Gets the currently logged-in user
     * @return The current Utilisateur object or null if no user is logged in
     */
    public static entities.Utilisateur getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the currently logged-in user
     * @param user The Utilisateur object to set as current user
     */
    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    /**
     * Clears the current user session (for logout)
     */
    public static void clearCurrentUser() {
        currentUser = null;
    }

    /**
     * Checks if a user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }

    // View navigation methods
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
        loadView("/fxml/Utilisatur.fxml", "Inscription - Gestion des Utilisaturs");
    }



    private static void loadView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainFX.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
        } catch (Exception e) {
            showFatalError("Failed to load view: " + fxmlPath, e);
        }
    }

    /**
     * Handles successful login
     */
    /**
     * Handles application logout
     */
    public static void onLogout() {
        clearCurrentUser();
        showLoginView();
    }

    /**
     * Displays a fatal error message and exits the application
     */
    public static void showFatalError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Critical Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();

        System.exit(1);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}