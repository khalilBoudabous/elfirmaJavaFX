package controllers;

import entities.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ModifierUtilisateurController implements Initializable {

    @FXML private TextField nomField, prenomField, emailField, telephoneField;
    @FXML private TextField adresseExploitationField, domaineExpertiseField;
    @FXML private TextField nomEntrepriseField, idFiscaleField, categorieProduitField;
    @FXML private PasswordField passwordField;
    @FXML private VBox formAgriculteur, formExpert, formFournisseur;

    private Utilisateur currentUser;
    private final UtilisateurService utilisateurService = new UtilisateurService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialisation commune
    }

    public void initData(Utilisateur user) {
        this.currentUser = user;
        peuplerChamps();
    }

    private void peuplerChamps() {
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        telephoneField.setText(currentUser.getTelephone());
        passwordField.setText(currentUser.getPassword());

        if(currentUser instanceof Agriculteur) {
            formAgriculteur.setVisible(true);
            adresseExploitationField.setText(((Agriculteur) currentUser).getAdresseExploitation());
        }
        else if(currentUser instanceof Expert) {
            formExpert.setVisible(true);
            domaineExpertiseField.setText(((Expert) currentUser).getDomaineExpertise());
        }
        else if(currentUser instanceof Fournisseur) {
            formFournisseur.setVisible(true);
            nomEntrepriseField.setText(((Fournisseur) currentUser).getNomEntreprise());
            idFiscaleField.setText(((Fournisseur) currentUser).getIdFiscale());
            categorieProduitField.setText(((Fournisseur) currentUser).getCategorieProduit());
        }
    }

    @FXML
    private void sauvegarderModifications() {
        StringBuilder errors = new StringBuilder();

        if(nomField.getText().isEmpty()) errors.append("Nom est obligatoire\n");
        if(prenomField.getText().isEmpty()) errors.append("Prénom est obligatoire\n");
        if(emailField.getText().isEmpty()) errors.append("Email est obligatoire\n");
        if(passwordField.getText().isEmpty()) errors.append("Mot de passe est obligatoire\n");
        if(telephoneField.getText().isEmpty()) errors.append("Téléphone est obligatoire\n");

        if(!emailField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errors.append("Format email invalide\n");
        }

        if(!telephoneField.getText().matches("\\d{8}")) {
            errors.append("Téléphone doit contenir 8 chiffres\n");
        }

        if(currentUser instanceof Agriculteur && adresseExploitationField.getText().isEmpty()) {
            errors.append("Adresse exploitation est obligatoire\n");
        }
        else if(currentUser instanceof Expert && domaineExpertiseField.getText().isEmpty()) {
            errors.append("Domaine expertise est obligatoire\n");
        }
        else if(currentUser instanceof Fournisseur) {
            if(nomEntrepriseField.getText().isEmpty()) errors.append("Nom entreprise est obligatoire\n");
            if(idFiscaleField.getText().isEmpty()) errors.append("ID fiscale est obligatoire\n");
            if(categorieProduitField.getText().isEmpty()) errors.append("Catégorie produit est obligatoire\n");
        }

        if(errors.length() > 0) {
            new Alert(Alert.AlertType.ERROR, errors.toString()).show();
            return;
        }

        try {
            mettreAJourUtilisateur();
            utilisateurService.modifier(currentUser);
            retourListe();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de mise à jour: " + e.getMessage()).show();
        }
    }

    private void mettreAJourUtilisateur() {
        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setTelephone(telephoneField.getText());
        currentUser.setPassword(passwordField.getText());

        if(currentUser instanceof Agriculteur) {
            ((Agriculteur) currentUser).setAdresseExploitation(adresseExploitationField.getText());
        }
        else if(currentUser instanceof Expert) {
            ((Expert) currentUser).setDomaineExpertise(domaineExpertiseField.getText());
        }
        else if(currentUser instanceof Fournisseur) {
            ((Fournisseur) currentUser).setNomEntreprise(nomEntrepriseField.getText());
            ((Fournisseur) currentUser).setIdFiscale(idFiscaleField.getText());
            ((Fournisseur) currentUser).setCategorieProduit(categorieProduitField.getText());
        }
    }

    @FXML
    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherListUtilisateur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}