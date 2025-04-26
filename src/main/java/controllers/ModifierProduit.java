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

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    private File selectedImageFile;
    private Produit produitActuel;
    private Affichierproduit affichageController;
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
        codePromoField.setText(produit.getCode_promo() != null ? produit.getCode_promo() : "");
        discountPercentageField.setText(String.valueOf(produit.getDiscount_percentage()));

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
            String nomProduit = nomProduitField.getText().trim();
            if (nomProduit.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Le nom du produit est obligatoire.").showAndWait();
                return;
            }
            if (nomProduit.length() < MIN_NOM_LENGTH || nomProduit.length() > MAX_NOM_LENGTH) {
                new Alert(Alert.AlertType.WARNING, "Le nom du produit doit contenir entre " + MIN_NOM_LENGTH + " et " + MAX_NOM_LENGTH + " caractères.").showAndWait();
                return;
            }
            if (!Pattern.matches(NOM_PATTERN, nomProduit)) {
                new Alert(Alert.AlertType.WARNING, "Le nom du produit ne doit contenir que des lettres, espaces ou tirets (pas de chiffres).").showAndWait();
                return;
            }

            if (prixField.getText().isEmpty() || !prixField.getText().matches("\\d+(\\.\\d{1,2})?")) {
                new Alert(Alert.AlertType.WARNING, "Veuillez entrer un prix valide (ex: 10 ou 10.99).").showAndWait();
                return;
            }
            float prix = Float.parseFloat(prixField.getText());
            if (prix < 0) {
                new Alert(Alert.AlertType.WARNING, "Le prix ne peut pas être négatif.").showAndWait();
                return;
            }
            if (prix > MAX_PRIX) {
                new Alert(Alert.AlertType.WARNING, "Le prix ne peut pas dépasser " + MAX_PRIX + " DT.").showAndWait();
                return;
            }

            if (quantiteField.getText().isEmpty() || !quantiteField.getText().matches("\\d+")) {
                new Alert(Alert.AlertType.WARNING, "Veuillez entrer une quantité valide (nombre entier).").showAndWait();
                return;
            }
            int quantite = Integer.parseInt(quantiteField.getText());
            if (quantite < 0) {
                new Alert(Alert.AlertType.WARNING, "La quantité ne peut pas être négative.").showAndWait();
                return;
            }
            if (quantite > MAX_QUANTITE) {
                new Alert(Alert.AlertType.WARNING, "La quantité ne peut pas dépasser " + MAX_QUANTITE + ".").showAndWait();
                return;
            }

            String description = descriptionArea.getText().trim();
            if (description.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "La description est obligatoire.").showAndWait();
                return;
            }
            if (description.length() < MIN_DESC_LENGTH || description.length() > MAX_DESC_LENGTH) {
                new Alert(Alert.AlertType.WARNING, "La description doit contenir entre " + MIN_DESC_LENGTH + " et " + MAX_DESC_LENGTH + " caractères.").showAndWait();
                return;
            }

            if (categorieComboBox.getValue() == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une catégorie.").showAndWait();
                return;
            }

            if (selectedImageFile == null) {
                new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une image pour le produit.").showAndWait();
                return;
            }

            // Validation du pourcentage de réduction
            float discountPercentage;
            try {
                discountPercentage = discountPercentageField.getText().isEmpty() ? 0 : Float.parseFloat(discountPercentageField.getText());
                if (discountPercentage < 0 || discountPercentage > 100) {
                    new Alert(Alert.AlertType.WARNING, "Le pourcentage de réduction doit être entre 0 et 100.").showAndWait();
                    return;
                }
            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.WARNING, "Veuillez entrer un pourcentage de réduction valide.").showAndWait();
                return;
            }

            // Récupérer et valider le code promo
            String codePromo = codePromoField.getText().trim(); // Correction de la faute de frappe
            if (codePromo.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Le code promo ne peut pas être vide.").showAndWait();
                return;
            }
            if (!Pattern.matches(CODE_PROMO_PATTERN, codePromo)) {
                new Alert(Alert.AlertType.WARNING, "Le code promo doit contenir exactement 8 caractères alphanumériques.").showAndWait();
                return;
            }

            // Mise à jour des données du produit
            produitActuel.setNom_produit(nomProduit);
            produitActuel.setPrix(prix);
            produitActuel.setQuantite(quantite);
            produitActuel.setDescription(description);
            produitActuel.setCategorie_id(categorieComboBox.getValue().getId());
            produitActuel.setCode_promo(codePromo);
            produitActuel.setDiscount_percentage(discountPercentage);

            if (selectedImageFile != null) {
                produitActuel.setImage(selectedImageFile.getAbsolutePath());
            }

            // Modification du produit
            produitService.modifier(produitActuel);

            new Alert(Alert.AlertType.INFORMATION, "Produit modifié avec succès").showAndWait();

            if (affichageController != null) {
                affichageController.initialize();
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