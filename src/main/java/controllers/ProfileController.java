package controllers;

import entities.Utilisateur;
import entities.Agriculteur;
import entities.Expert;
import entities.Fournisseur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;

public class ProfileController {

    @FXML private Label nomLabel;
    @FXML private Label prenomLabel;
    @FXML private Label emailLabel;
    @FXML private Label telephoneLabel;
    @FXML private VBox dynamicFields;

    private Utilisateur currentUser;

    public void initData(long userId) {
        UtilisateurService service = new UtilisateurService();
        try {
            currentUser = service.getUtilisateurById(userId);
            displayUserData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayUserData() {
        nomLabel.setText(currentUser.getNom());
        prenomLabel.setText(currentUser.getPrenom());
        emailLabel.setText(currentUser.getEmail());
        telephoneLabel.setText(currentUser.getTelephone());

        dynamicFields.getChildren().clear();

        if (currentUser instanceof Agriculteur) {
            Agriculteur agriculteur = (Agriculteur) currentUser;
            Label adresseLabel = new Label("Adresse d'exploitation: " + agriculteur.getAdresseExploitation());
            dynamicFields.getChildren().add(adresseLabel);
        } else if (currentUser instanceof Expert) {
            Expert expert = (Expert) currentUser;
            Label domaineLabel = new Label("Domaine d'expertise: " + expert.getDomaineExpertise());
            dynamicFields.getChildren().add(domaineLabel);
        } else if (currentUser instanceof Fournisseur) {
            Fournisseur fournisseur = (Fournisseur) currentUser;
            Label entrepriseLabel = new Label("Nom de l'entreprise: " + fournisseur.getNomEntreprise());
            Label idFiscaleLabel = new Label("ID Fiscale: " + fournisseur.getIdFiscale());
            Label categorieLabel = new Label("Catégorie de produit: " + fournisseur.getCategorieProduit());
            dynamicFields.getChildren().addAll(entrepriseLabel, idFiscaleLabel, categorieLabel);
        }
    }

    @FXML
    private void handleModify() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUtilisateur.fxml"));
            Parent root = loader.load();

            ModifierUtilisateurController controller = loader.getController();
            controller.initData(currentUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Utilisateur");

            stage.setOnHidden(event -> {
                initData(currentUser.getId());
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}