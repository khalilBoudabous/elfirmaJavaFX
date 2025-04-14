package controllers;

import entities.Categorie;
import entities.Produit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ProduitService;
import services.CategorieService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Ajouterproduit {

    private static final Logger logger = Logger.getLogger(Ajouterproduit.class.getName());

    // FXML Elements
    @FXML private ImageView productImage;
    @FXML private Button uploadButton;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private TextField quantiteField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField nomProduitField;
    @FXML private TextField prixField;

    // Services
    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    // Variables
    private File selectedImageFile;
    @FXML
    private Button ajout;

    /**
     * Initialize method to set up the ComboBox and load categories.
     */
    @FXML
    public void initialize() {
        chargerCategories();

        // Configure ComboBox to show category name
        categorieComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Categorie categorie) {
                return (categorie != null) ? categorie.getNom_categorie() : "";
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

    /**
     * Load categories from the service and populate the ComboBox.
     */
    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieComboBox.getItems().addAll(categories);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
        }
    }

    /**
     * Handle category selection change event.
     */
    @FXML
    private void handleCategorieChange(ActionEvent actionEvent) {
        Categorie selected = categorieComboBox.getValue();
        System.out.println("Catégorie sélectionnée : " + (selected != null ? selected.getNom_categorie() : "Aucune"));
    }

    /**
     * Handle image upload for the product.
     */
    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de produit");

        // Add image filter
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.gif"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        // Open file dialog
        selectedImageFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedImageFile != null) {
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                productImage.setImage(image);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors du chargement de l'image", e);
                new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de l'image : " + e.getMessage()).showAndWait();
            }
        }
    }

    /**
     * Add a new product by collecting data from input fields and saving it.
     */
    @FXML
    public void ajouterproduit(ActionEvent actionEvent) {
        try {
            // Get product data
            String nomProduit = nomProduitField.getText();
            String description = descriptionArea.getText();
            int quantite = parseInt(quantiteField.getText(), "quantité");
            float prix = parseFloat(prixField.getText(), "prix");

            if (quantite == -1 || prix == -1) return; // Exit if invalid data

            // Validate category selection
            Categorie selectedCategorie = categorieComboBox.getValue();
            if (selectedCategorie == null) {
                showWarning("Veuillez sélectionner une catégorie.");
                return;
            }

            // Validate image selection
            if (selectedImageFile == null) {
                showWarning("Veuillez sélectionner une image pour le produit.");
                return;
            }

            // Create product and add it
            String imagePath = selectedImageFile.getAbsolutePath();
            Produit produit = new Produit(quantite, selectedCategorie.getId(), prix, imagePath, description, nomProduit);
            produitService.ajouter(produit);

            // Show success message
            showInfo("Produit ajouté avec succès!");

            // Redirect to product list page
            redirectToProductList();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ajout du produit", e);
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    /**
     * Parse integer value from text input with validation.
     */
    private int parseInt(String text, String fieldName) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            showError("Le " + fieldName + " doit être un nombre entier valide.");
            return -1;
        }
    }

    /**
     * Parse float value from text input with validation.
     */
    private float parseFloat(String text, String fieldName) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            showError("Le " + fieldName + " doit être un nombre décimal valide (ex : 12.5).");
            return -1;
        }
    }

    /**
     * Show warning alert with the specified message.
     */
    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    /**
     * Show error alert with the specified message.
     */
    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    /**
     * Show information alert with the specified message.
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ajout réussi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Redirect to the product list page.
     */
    private void redirectToProductList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichierProduit.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomProduitField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Produits");
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement de la page AffichierProduit", e);
            showError("Erreur de redirection : " + e.getMessage());
        }
    }

    /**
     * Clear all input fields.
     */
    private void clearFields() {
        nomProduitField.clear();
        quantiteField.clear();
        prixField.clear();
        descriptionArea.clear();
        productImage.setImage(null);
        selectedImageFile = null;
        categorieComboBox.getSelectionModel().clearSelection();
    }
}
