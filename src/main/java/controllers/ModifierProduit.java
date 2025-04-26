package controllers;

import entities.Categorie;
import entities.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.CategorieService;
import services.ProduitService;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModifierProduit {
    private static final Logger logger = Logger.getLogger(ModifierProduit.class.getName());

    @FXML
    private ImageView productImage;
    @FXML
    private Button uploadButton;
    @FXML
    private ComboBox<Categorie> categorieComboBox;
    @FXML
    private TextField quantiteField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField nomProduitField;
    @FXML
    private TextField prixField;

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    private File selectedImageFile;
    private Produit produitActuel;
    private Affichierproduit affichageController; // Pour recharger après modif
    @FXML
    private Button ajout;

    public void setAffichageController(Affichierproduit controller) {
        this.affichageController = controller;
    }

    @FXML
    public void initialize() {
        chargerCategories();
        categorieComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categorie categorie) {
                return categorie != null ? categorie.getNom_categorie() : "";
            }

            @Override
            public Categorie fromString(String nom) {
                return categorieComboBox.getItems().stream()
                        .filter(c -> c.getNom_categorie().equals(nom))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieComboBox.getItems().setAll(categories);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
        }
    }
    @FXML
    private void handleCategorieChange(ActionEvent actionEvent) {
        Categorie selected = categorieComboBox.getValue();
        System.out.println("Catégorie sélectionnée : " + (selected != null ? selected.getNom_categorie() : "Aucune"));
    }

    public void setProduit(Produit produit) {
        this.produitActuel = produit;

        nomProduitField.setText(produit.getNom_produit());
        prixField.setText(String.valueOf(produit.getPrix()));
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        descriptionArea.setText(produit.getDescription());

        selectedImageFile = new File(produit.getImage());
        if (selectedImageFile.exists()) {
            productImage.setImage(new Image(selectedImageFile.toURI().toString()));
        }

        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieComboBox.getItems().setAll(categories);
            categories.stream()
                    .filter(c -> c.getId() == produit.getCategorie_id())
                    .findFirst()
                    .ifPresent(categorieComboBox::setValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));
        selectedImageFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedImageFile != null) {
            productImage.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    @FXML
    public void modifierProduit() {
        try {
            // Validation des champs
            if (nomProduitField.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Le nom du produit est obligatoire.").showAndWait();
                return;
            }

            if (prixField.getText().isEmpty() || !prixField.getText().matches("\\d+(\\.\\d{1,2})?")) {
                new Alert(Alert.AlertType.WARNING, "Veuillez entrer un prix valide (ex: 10 ou 10.99).").showAndWait();
                return;
            }

            if (quantiteField.getText().isEmpty() || !quantiteField.getText().matches("\\d+")) {
                new Alert(Alert.AlertType.WARNING, "Veuillez entrer une quantité valide (nombre entier).").showAndWait();
                return;
            }

            if (descriptionArea.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "La description est obligatoire.").showAndWait();
                return;
            }

            if (categorieComboBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une catégorie.").showAndWait();
                return;
            }

            // Mise à jour des données du produit
            produitActuel.setNom_produit(nomProduitField.getText());
            produitActuel.setPrix(Float.parseFloat(prixField.getText()));
            produitActuel.setQuantite(Integer.parseInt(quantiteField.getText()));
            produitActuel.setDescription(descriptionArea.getText());
            produitActuel.setCategorie_id(categorieComboBox.getValue().getId());

            if (selectedImageFile != null) {
                produitActuel.setImage(selectedImageFile.getAbsolutePath());
            }

            // Modification du produit
            produitService.modifier(produitActuel);

            new Alert(Alert.AlertType.INFORMATION, "Produit modifié avec succès").showAndWait();

            // Recharger la liste des produits si le contrôleur d'affichage est défini
            if (affichageController != null) {
                affichageController.initialize();
            }

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) nomProduitField.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    public void ajouterproduit(ActionEvent actionEvent) {
    }
}
