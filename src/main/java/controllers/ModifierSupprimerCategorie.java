package controllers;

import entities.Categorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import services.CategorieService;

import java.sql.SQLException;
import java.util.List;

public class ModifierSupprimerCategorie {
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, Integer> colId;
    @FXML private TableColumn<Categorie, String> colNom;
    @FXML private TableColumn<Categorie, String> colDescription;

    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;

    private final CategorieService categorieService = new CategorieService();
    private ObservableList<Categorie> categorieList;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colNom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom_categorie()));
        colDescription.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        tableCategories.setOnMouseClicked(this::onCategorieSelected);
        loadCategories();
    }

    private void loadCategories() {
        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieList = FXCollections.observableArrayList(categories);
            tableCategories.setItems(categorieList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors du chargement des catégories.");
            e.printStackTrace();
        }
    }

    private void onCategorieSelected(MouseEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfNom.setText(selected.getNom_categorie());
            taDescription.setText(selected.getDescription());
        }
    }

    @FXML
    private void modifierCategorie() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une catégorie.");
            return;
        }

        String nouveauNom = tfNom.getText().trim();
        String nouvelleDescription = taDescription.getText().trim();

        if (nouveauNom.isEmpty() || nouvelleDescription.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.");
            return;
        }

        selected.setNom_categorie(nouveauNom);
        selected.setDescription(nouvelleDescription);

        try {
            categorieService.modifier(selected);
            showAlert(Alert.AlertType.INFORMATION, "Catégorie modifiée avec succès.");
            loadCategories(); // rafraîchir
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la modification.");
        }
    }

    @FXML
    private void supprimerCategorie() {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une catégorie.");
            return;
        }

        try {
            categorieService.supprimer(selected);
            showAlert(Alert.AlertType.INFORMATION, "Catégorie supprimée avec succès.");
            loadCategories(); // mise à jour
            tfNom.clear();
            taDescription.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la suppression.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
