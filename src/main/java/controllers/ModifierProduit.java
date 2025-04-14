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
            produitActuel.setNom_produit(nomProduitField.getText());
            produitActuel.setPrix(Float.parseFloat(prixField.getText()));
            produitActuel.setQuantite(Integer.parseInt(quantiteField.getText()));
            produitActuel.setDescription(descriptionArea.getText());
            produitActuel.setCategorie_id(categorieComboBox.getValue().getId());

            if (selectedImageFile != null) {
                produitActuel.setImage(selectedImageFile.getAbsolutePath());
            }

            produitService.modifier(produitActuel);

            new Alert(Alert.AlertType.INFORMATION, "Produit modifié avec succès").showAndWait();

            if (affichageController != null) {
                affichageController.initialize(); // Recharger la liste
            }

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
