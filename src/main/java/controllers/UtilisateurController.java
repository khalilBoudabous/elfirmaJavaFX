package controllers;

import Dao.UtilisateurDao;
import entities.Utilisateur;
import utils.DBconnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UtilisateurController {

    // UI components
    public Button btnGestionTerrain;
    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField telephoneField;
    @FXML
    private TextField roleField;
    @FXML
    private PasswordField mdpField;

    @FXML
    private TableView<Utilisateur> tableView;
    @FXML
    private TableColumn<Utilisateur, String> colNom;
    @FXML
    private TableColumn<Utilisateur, String> colPrenom;
    @FXML
    private TableColumn<Utilisateur, String> colEmail;
    @FXML
    private TableColumn<Utilisateur, Integer> colTel;
    @FXML
    private TableColumn<Utilisateur, String> colRole;

    private final UtilisateurDao utilisateurDao = new UtilisateurDao();
    private ObservableList<Utilisateur> utilisateurs;

    @FXML
    public void initialize() {
        // Set the table columns to match the User properties
        colNom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        colPrenom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrenom()));
        colEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        colTel.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTelephone()));
        colRole.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));

        // Load users into the table
        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        utilisateurs = FXCollections.observableArrayList(utilisateurDao.afficherUtilisateurs());
        tableView.setItems(utilisateurs);
    }

    @FXML
    public void ajouterUtilisateur() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        int telephone = Integer.parseInt(telephoneField.getText());
        String role = roleField.getText();
        String motDePasse = mdpField.getText();

        // Create new user with form data
        Utilisateur utilisateur = new Utilisateur(nom, prenom, email, telephone, role, motDePasse) {
            @Override
            public String getType() {
                return "";
            }
        };

        String sql = "INSERT INTO Utilisateur (nom, prenom, email, telephone, role, mot_de_passe, disc) VALUES (?, ?, ?, ?, ?, ?, 'Agriculteur')";

        try (Connection conn = DBconnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, utilisateur.getNom());
                    stmt.setString(2, utilisateur.getPrenom());
                    stmt.setString(3, utilisateur.getEmail());
                    stmt.setInt(4, utilisateur.getTelephone());
                    stmt.setString(5, utilisateur.getRole());
                    stmt.setString(6, utilisateur.getMotDePasse());

                    stmt.executeUpdate();
                    System.out.println("Utilisateur ajouté avec succès !");
                    chargerUtilisateurs();  // Reload user list
                    clearFields();           // Clear form fields
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("La connexion à la base de données est fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void modifierUtilisateur() {
        Utilisateur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setNom(nomField.getText());
                selected.setPrenom(prenomField.getText());
                selected.setEmail(emailField.getText());
                selected.setTelephone(Integer.parseInt(telephoneField.getText()));
                selected.setRole(roleField.getText());
                selected.setMotDePasse(mdpField.getText());

                utilisateurDao.modifierUtilisateur(selected);
                chargerUtilisateurs();
                clearFields();
            } catch (Exception e) {
                showAlert("Erreur", "Modification échouée.");
            }
        } else {
            showAlert("Avertissement", "Sélectionnez un utilisateur.");
        }
    }

    @FXML
    private void supprimerUtilisateur() {
        Utilisateur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            utilisateurDao.supprimerUtilisateur(selected.getId());
            chargerUtilisateurs();
            clearFields();
        } else {
            showAlert("Avertissement", "Sélectionnez un utilisateur à supprimer.");
        }
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        telephoneField.clear();
        roleField.clear();
        mdpField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleGestionTerrain(javafx.event.ActionEvent actionEvent) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Terrain.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec le root
            Scene scene = new Scene(root);

            // Obtenir le stage courant (ou créer un nouveau stage)
            Stage stage = new Stage();
            stage.setTitle("Gestion des Terrains");  // Titre de la fenêtre
            stage.setScene(scene);  // Définir la scène
            stage.show();  // Afficher la fenêtre

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}