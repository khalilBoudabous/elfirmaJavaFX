package controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import entities.Categorie;
import entities.Produit;
import io.github.cdimascio.dotenv.Dotenv;
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
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ModifierProduit {
    private static final Logger logger = Logger.getLogger(ModifierProduit.class.getName());

    @FXML private ImageView productImage;
    @FXML private Button uploadButton;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private TextField quantiteField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField nomProduitField;
    @FXML private TextField prixField;
    @FXML private TextField codePromoField;
    @FXML private TextField discountPercentageField;
    @FXML private Label errorLabel;
    @FXML private Button ajout;

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();
    private final Cloudinary cloudinary;

    private File selectedImageFile;
    private Produit produitActuel;
    private Affichierproduit affichageController;

    // Constantes pour les validations
    private static final int MAX_QUANTITE = 10000;
    private static final float MAX_PRIX = 1000000.0f;
    private static final int MIN_NOM_LENGTH = 2;
    private static final int MAX_NOM_LENGTH = 100;
    private static final int MIN_DESC_LENGTH = 10;
    private static final int MAX_DESC_LENGTH = 500;
    private static final String CODE_PROMO_PATTERN = "^[A-Z0-9]{8}$";
    private static final String NOM_PATTERN = "^[a-zA-Z\\s-]+$";

    public ModifierProduit() {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();
        String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
        System.out.println("CLOUDINARY_URL: " + cloudinaryUrl);
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            throw new IllegalStateException("CLOUDINARY_URL not found in .env file");
        }
        cloudinary = new Cloudinary(cloudinaryUrl);
    }

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
            showError("Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleCategorieChange(ActionEvent actionEvent) {
        Categorie selected = categorieComboBox.getValue();
        System.out.println("Catégorie sélectionnée: " + (selected != null ? selected.getNom_categorie() : "Aucune"));
    }

    public void setProduit(Produit produit) {
        this.produitActuel = produit;
        nomProduitField.setText(produit.getNom_produit());
        prixField.setText(String.valueOf(produit.getPrix()));
        quantiteField.setText(String.valueOf(produit.getQuantite()));
        descriptionArea.setText(produit.getDescription());
        codePromoField.setText(produit.getCode_promo() != null ? produit.getCode_promo() : "");
        discountPercentageField.setText(String.valueOf(produit.getDiscount_percentage()));

        try {
            productImage.setImage(new Image(produit.getImage()));
            selectedImageFile = null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement de l'image", e);
            showError("Erreur lors du chargement de l'image: " + e.getMessage());
        }

        try {
            List<Categorie> categories = categorieService.recuperer();
            categorieComboBox.getItems().setAll(categories);
            categories.stream()
                    .filter(c -> c.getId() == produit.getCategorie_id())
                    .findFirst()
                    .ifPresent(categorieComboBox::setValue);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
            showError("Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif"));
        selectedImageFile = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());

        if (selectedImageFile != null) {
            try {
                productImage.setImage(new Image(selectedImageFile.toURI().toString()));
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Erreur lors du chargement de l'image", e);
                showError("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void modifierProduit() {
        try {
            // Validation des champs
            String nomProduit = nomProduitField.getText().trim();
            if (nomProduit.isEmpty()) {
                showWarning("Le nom du produit est obligatoire.");
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
                showWarning("La description est obligatoire.");
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

            String codePromo = codePromoField.getText().trim();
            if (codePromo.isEmpty()) {
                showWarning("Le code promo ne peut pas être vide.");
                return;
            }
            if (!Pattern.matches(CODE_PROMO_PATTERN, codePromo)) {
                showWarning("Le code promo doit contenir exactement 8 caractères alphanumériques.");
                return;
            }

            // Téléverser une nouvelle image sur Cloudinary si une nouvelle image a été sélectionnée
            String imageUrl = produitActuel.getImage();
            if (selectedImageFile != null) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(selectedImageFile, ObjectUtils.asMap(
                            "resource_type", "image",
                            "public_id", "products/" + nomProduit + "_" + System.currentTimeMillis()
                    ));
                    imageUrl = (String) uploadResult.get("secure_url");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        showError("Erreur lors du téléversement de l'image sur Cloudinary.");
                        return;
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Erreur lors du téléversement de l'image sur Cloudinary", e);
                    showError("Erreur lors du téléversement de l'image: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }

            // Mise à jour des données du produit
            produitActuel.setNom_produit(nomProduit);
            produitActuel.setPrix(prix);
            produitActuel.setQuantite(quantite);
            produitActuel.setDescription(description);
            produitActuel.setCategorie_id(selectedCategorie.getId());
            produitActuel.setCode_promo(codePromo);
            produitActuel.setDiscount_percentage(discountPercentage);
            produitActuel.setImage(imageUrl);

            // Modification du produit
            produitService.modifier(produitActuel);

            showInfo("Produit modifié avec succès!");

            if (affichageController != null) {
                affichageController.initialize();
            }

            Stage currentStage = (Stage) nomProduitField.getScene().getWindow();
            currentStage.close();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la modification du produit", e);
            showError("Erreur SQL: " + e.getMessage());
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
            showError("Le " + fieldName + " doit être un nombre décimal valide (ex: 12.5).");
            return -1;
        }
    }

    private void showWarning(String message) {
        errorLabel.setText(message);
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void showInfo(String message) {
        errorLabel.setText("");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Modification réussie");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Remove unused method
    @FXML
    public void ajouterproduit(ActionEvent actionEvent) {
        // Method not needed for modification
    }
}