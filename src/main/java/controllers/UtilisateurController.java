package controllers;

import entities.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationMessage;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class UtilisateurController {

    @FXML private TextField nomField, prenomField, emailField, telephoneField;
    @FXML private TextField adresseExploitationField, domaineExpertiseField, nomEntrepriseField, idFiscaleField, categorieProduitField;
    @FXML private RadioButton radioAgriculteur, radioExpert, radioFournisseur;
    @FXML private VBox formAgriculteur, formExpert, formFournisseur;
    @FXML private PasswordField passwordField;
    @FXML
    private TableView<Utilisateur> userTable;

    private final ValidationSupport validationSupport = new ValidationSupport();


    @FXML
    private ToggleGroup roleGroup;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {

        // Configuration de la validation
        validationSupport.registerValidator(nomField, Validator.createEmptyValidator("Nom obligatoire"));
        validationSupport.registerValidator(prenomField, Validator.createEmptyValidator("Prénom obligatoire"));


        roleGroup = new ToggleGroup();
        radioAgriculteur.setToggleGroup(roleGroup);
        radioExpert.setToggleGroup(roleGroup);
        radioFournisseur.setToggleGroup(roleGroup);

        roleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            formAgriculteur.setVisible(radioAgriculteur.isSelected());
            formAgriculteur.setManaged(radioAgriculteur.isSelected());

            formExpert.setVisible(radioExpert.isSelected());
            formExpert.setManaged(radioExpert.isSelected());

            formFournisseur.setVisible(radioFournisseur.isSelected());
            formFournisseur.setManaged(radioFournisseur.isSelected());
        });

    }



    public void rafraichirListe() {
        userTable.getItems().clear();
        userTable.getItems().addAll(utilisateurService.getAllUtilisateurs());
    }

    @FXML
    public void ajouterUtilisateur() {
        boolean isValid = true;

        // Validation des champs communs
        StringBuilder errors = new StringBuilder();

        if(nomField.getText().isEmpty()) errors.append("Nom est obligatoire\n");
        if(prenomField.getText().isEmpty()) errors.append("Prénom est obligatoire\n");
        if(emailField.getText().isEmpty()) errors.append("Email est obligatoire\n");
        if(passwordField.getText().isEmpty()) errors.append("Mot de passe est obligatoire\n");
        if(telephoneField.getText().isEmpty()) errors.append("Téléphone est obligatoire\n");

        // Validation format email
        if(!emailField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errors.append("Format email invalide\n");
        }

        // Validation téléphone (10 chiffres)
        if(!telephoneField.getText().matches("\\d{8}")) {
            errors.append("Téléphone doit contenir 8 chiffres\n");
        }

        // Validation rôle spécifique
        if(radioAgriculteur.isSelected() && adresseExploitationField.getText().isEmpty()) {
            errors.append("Adresse exploitation est obligatoire\n");
        }
        else if(radioExpert.isSelected() && domaineExpertiseField.getText().isEmpty()) {
            errors.append("Domaine expertise est obligatoire\n");
        }
        else if(radioFournisseur.isSelected()) {
            if(nomEntrepriseField.getText().isEmpty()) errors.append("Nom entreprise est obligatoire\n");
            if(idFiscaleField.getText().isEmpty()) errors.append("ID fiscale est obligatoire\n");
            if(categorieProduitField.getText().isEmpty()) errors.append("Catégorie produit est obligatoire\n");
        }

        if(errors.length() > 0) {
            new Alert(Alert.AlertType.ERROR, errors.toString()).show();
            return;
        }

        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String telephone = telephoneField.getText();

        Utilisateur u = null;


        if (radioAgriculteur.isSelected()) {
            Agriculteur a = new Agriculteur();
            a.setAdresseExploitation(adresseExploitationField.getText());
            u = a;
        } else if (radioExpert.isSelected()) {
            Expert e = new Expert();
            e.setDomaineExpertise(domaineExpertiseField.getText());
            u = e;
        } else if (radioFournisseur.isSelected()) {
            Fournisseur f = new Fournisseur();
            f.setNomEntreprise(nomEntrepriseField.getText());
            f.setIdFiscale(idFiscaleField.getText());
            f.setCategorieProduit(categorieProduitField.getText());
            u = f;
        }

        if (u != null) {
            u.setNom(nom);
            u.setPrenom(prenom);
            u.setEmail(email);
            u.setPassword(password);
            u.setTelephone(telephone);


            utilisateurService.ajouter(u);
            chargerVueListe();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Utilisateur ajouté avec succès !");
            alert.showAndWait();


            viderChamps();

        }
    }

    private void viderChamps() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        telephoneField.clear();
        adresseExploitationField.clear();
        domaineExpertiseField.clear();
        nomEntrepriseField.clear();
        idFiscaleField.clear();
        categorieProduitField.clear();
    }

    private void chargerVueListe() {

        try {
            // Corrigez l'orthographe si nécessaire (Afficher au lieu de Afficer)
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setContentText("Impossible de charger la liste : " + e.getMessage());
            alert.showAndWait();
        }

    }
    @FXML
    private void goToLoginPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml")); // Adjust path if needed
            Scene scene = new Scene(root);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setContentText("Could not load login page: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
