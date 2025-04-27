package controllers;

import entities.Categorie;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.CategorieService;

public class Ajoutercategorie {

    @FXML
    private TextField tfNom;

    @FXML
    private TextArea taDescription;

    private final CategorieService categorieService = new CategorieService();

    @FXML
    private void ajouterCategorie() {
        String nom = tfNom.getText().trim();
        String description = taDescription.getText().trim();

        if (nom.isEmpty() || description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        Categorie categorie = new Categorie(0, nom, description);
        try {
            categorieService.ajouter(categorie);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Catégorie ajoutée avec succès !");
            tfNom.clear();
            taDescription.clear();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        tfNom.clear();
        taDescription.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // no header
        alert.setContentText(message);
        alert.showAndWait();
    }
}
