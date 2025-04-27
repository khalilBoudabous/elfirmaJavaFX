package controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import entities.Categorie;
import entities.Produit;
import io.github.cdimascio.dotenv.Dotenv;
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
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

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
    @FXML private TextField discountPercentageField;
    @FXML private TextField codePromoField;

    // Services
    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    // Cloudinary
    private final Cloudinary cloudinary;

    // Variables
    private File selectedImageFile;
    private String generatedCodePromo;
    @FXML private Button ajout;

    // Constantes pour les validations
    private static final int MAX_QUANTITE = 10000;
    private static final float MAX_PRIX = 1000000.0f;
    private static final int MIN_NOM_LENGTH = 2;
    private static final int MAX_NOM_LENGTH = 100;
    private static final int MIN_DESC_LENGTH = 10;
    private static final int MAX_DESC_LENGTH = 500;
    private static final String CODE_PROMO_PATTERN = "^[A-Z0-9]{8}$"; // 8 caractères alphanumériques
    private static final String NOM_PATTERN = "^[a-zA-Z\\s-]+$"; // Lettres, espaces, tirets (pas de chiffres)

    public Ajouterproduit() {
        // Load environment variables
        Dotenv dotenv = Dotenv.load();
        String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            throw new IllegalStateException("CLOUDINARY_URL not found in .env file");
        }
        cloudinary = new Cloudinary(cloudinaryUrl);
    }

    @FXML
    public void initialize() {
        chargerCategories();

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

        generatedCodePromo = generateCodePromo();
        codePromoField.setText(generatedCodePromo);
    }

    private String generateCodePromo() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieComboBox.getItems().addAll(categories);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
        }
    }

    @FXML
    private void handleCategorieChange(ActionEvent actionEvent) {
        Categorie selected = categorieComboBox.getValue();
        System.out.println("Catégorie sélectionnée : " + (selected != null ? selected.getNom_categorie() : "Aucune"));
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de produit");
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.gif"
        );
        fileChooser.getExtensionFilters().add(imageFilter);
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

    @FXML
    public void ajouterproduit(ActionEvent actionEvent) {
        try {
            // Validation des champs
            String nomProduit = nomProduitField.getText().trim();
            if (nomProduit.isEmpty()) {
                showWarning("Veuillez entrer le nom du produit.");
                return;
            }
            if (nomProduit.length() < MIN_NOM_LENGTH || nomProduit.length() > MAX_NOM_LENGTH) {
                showWarning("Le nom du produit doit contenir entre " + MIN_NOM_LENGTH + " et " + MAX_NOM_LENGTH + " caractères.");
                return;
            }
            if (!Pattern.matches(NOM_PATTERN, nomProduit)) {
                showWarning("Le nom du produit ne doit contenir que des lettres, espaces ou tirets (pas de chiffres).");
                return;
            }

            String description = descriptionArea.getText().trim();
            if (description.isEmpty()) {
                showWarning("Veuillez entrer une description pour le produit.");
                return;
            }
            if (description.length() < MIN_DESC_LENGTH || description.length() > MAX_DESC_LENGTH) {
                showWarning("La description doit contenir entre " + MIN_DESC_LENGTH + " et " + MAX_DESC_LENGTH + " caractères.");
                return;
            }

            int quantite = parseInt(quantiteField.getText(), "quantité");
            if (quantite == -1) return;
            if (quantite < 0) {
                showWarning("La quantité ne peut pas être négative.");
                return;
            }
            if (quantite > MAX_QUANTITE) {
                showWarning("La quantité ne peut pas dépasser " + MAX_QUANTITE + ".");
                return;
            }

            float prix = parseFloat(prixField.getText(), "prix");
            if (prix == -1) return;
            if (prix < 0) {
                showWarning("Le prix ne peut pas être négatif.");
                return;
            }
            if (prix > MAX_PRIX) {
                showWarning("Le prix ne peut pas dépasser " + MAX_PRIX + " DT.");
                return;
            }

            Categorie selectedCategorie = categorieComboBox.getValue();
            if (selectedCategorie == null) {
                showWarning("Veuillez sélectionner une catégorie.");
                return;
            }

            if (selectedImageFile == null) {
                showWarning("Veuillez sélectionner une image pour le produit.");
                return;
            }

            // Validation du pourcentage de réduction
            float discountPercentage;
            try {
                discountPercentage = discountPercentageField.getText().isEmpty() ? 0 : Float.parseFloat(discountPercentageField.getText());
                if (discountPercentage < 0 || discountPercentage > 100) {
                    showWarning("Le pourcentage de réduction doit être entre 0 et 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                showWarning("Veuillez entrer un pourcentage de réduction valide (ex: 10).");
                return;
            }

            // Récupérer et valider le code promo
            String codePromo = codePromoField.getText().trim();
            if (codePromo.isEmpty()) {
                showWarning("Le code promo ne peut pas être vide.");
                return;
            }
            if (!Pattern.matches(CODE_PROMO_PATTERN, codePromo)) {
                showWarning("Le code promo doit contenir exactement 8 caractères alphanumériques.");
                return;
            }

            // Upload image to Cloudinary
            String imageUrl;
            try {
                Map uploadResult = cloudinary.uploader().upload(selectedImageFile, ObjectUtils.asMap(
                        "resource_type", "image",
                        "public_id", "products/" + nomProduit + "_" + System.currentTimeMillis()
                ));
                imageUrl = (String) uploadResult.get("secure_url");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Erreur lors de l'upload de l'image vers Cloudinary", e);
                showError("Erreur lors de l'upload de l'image vers Cloudinary : " + e.getMessage());
                return;
            }

            // Créer le produit et l'ajouter
            Produit produit = new Produit(quantite, selectedCategorie.getId(), prix, imageUrl, description, nomProduit, codePromo, discountPercentage);
            produitService.ajouter(produit);

            showInfo("Produit ajouté avec succès!");
            redirectToProductList();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ajout du produit", e);
            showError("Erreur SQL : " + e.getMessage());
        }
    }

    private int parseInt(String text, String fieldName) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            showError("Le " + fieldName + " doit être un nombre entier valide.");
            return -1;
        }
    }

    private float parseFloat(String text, String fieldName) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            showError("Le " + fieldName + " doit être un nombre décimal valide (ex : 12.5).");
            return -1;
        }
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ajout réussi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToProductList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Affichierproduitagriculteur.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomProduitField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Produits");
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement de la page AffichierProduit", e);
            showError("Erreur de redirection : " + e.getMessage());
        }
    }

    private void clearFields() {
        nomProduitField.clear();
        quantiteField.clear();
        prixField.clear();
        descriptionArea.clear();
        productImage.setImage(null);
        selectedImageFile = null;
        categorieComboBox.getSelectionModel().clearSelection();
        discountPercentageField.clear();
        generatedCodePromo = generateCodePromo();
        codePromoField.setText(generatedCodePromo);
    }
}