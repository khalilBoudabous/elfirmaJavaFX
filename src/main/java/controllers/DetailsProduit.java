package controllers;

import entities.Categorie;
import entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.CategorieService;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetailsProduit {

    private static final Logger logger = Logger.getLogger(DetailsProduit.class.getName());

    @FXML private ImageView imageView;
    @FXML private ImageView imageViewSmall;
    @FXML private Label nomLabel;
    @FXML private Label descLabel;
    @FXML private Label prixLabel;
    @FXML private Label quantiteLabel;
    @FXML private Label catLabel;
    @FXML private Label codePromoLabel;
    @FXML private Label discountLabel;

    private Produit produit;
    private final CategorieService categorieService = new CategorieService();

    public void setProduit(Produit produit) {
        this.produit = produit;
        afficherDetails();
    }

    private void afficherDetails() {
        if (produit != null) {
            // Load image from Cloudinary
            String imageUrl = produit.getImage();
            logger.info("Loading image for product '" + produit.getNom_produit() + "' with URL: " + imageUrl);

            if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("https://res.cloudinary.com")) {
                try {
                    Image image = new Image(imageUrl, true); // Async loading
                    image.progressProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue.doubleValue() == 1.0) {
                            logger.info("Image loaded successfully for product: " + produit.getNom_produit());
                        }
                    });
                    image.errorProperty().addListener((obs, oldError, newError) -> {
                        if (newError) {
                            logger.warning("Failed to load Cloudinary image for product '" + produit.getNom_produit() + "': " + imageUrl);
                            imageView.setImage(loadPlaceholderImage(produit.getNom_produit()));
                            imageViewSmall.setImage(loadPlaceholderImage(produit.getNom_produit()));
                        }
                    });
                    imageView.setImage(image);
                    imageViewSmall.setImage(image);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Exception loading Cloudinary image for product '" + produit.getNom_produit() + "': " + imageUrl, e);
                    imageView.setImage(loadPlaceholderImage(produit.getNom_produit()));
                    imageViewSmall.setImage(loadPlaceholderImage(produit.getNom_produit()));
                }
            } else {
                logger.warning("Invalid or missing Cloudinary URL for product '" + produit.getNom_produit() + "': " + imageUrl);
                imageView.setImage(loadPlaceholderImage(produit.getNom_produit()));
                imageViewSmall.setImage(loadPlaceholderImage(produit.getNom_produit()));
            }

            // Set other product details
            if (nomLabel != null) {
                nomLabel.setText(produit.getNom_produit());
            }
            descLabel.setText("Description: " + produit.getDescription());
            prixLabel.setText(String.format("Prix: %.2f DT", produit.getPrix()));
            quantiteLabel.setText("Quantité: " + produit.getQuantite());

            // Set category
            if (catLabel != null) {
                try {
                    Categorie categorie = categorieService.getById(produit.getCategorie_id());
                    if (categorie != null) {
                        catLabel.setText("Catégorie: " + categorie.getNom_categorie());
                    } else {
                        catLabel.setText("Catégorie: Inconnue");
                    }
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Erreur lors de la récupération de la catégorie pour le produit '" + produit.getNom_produit() + "'", e);
                    catLabel.setText("Catégorie: Erreur de récupération");
                }
            }

            // Set promo code and discount if available
            if (codePromoLabel != null) {
                String codePromo = produit.getCode_promo() != null ? produit.getCode_promo() : "Aucun";
                codePromoLabel.setText("Code Promo: " + codePromo);
            }
            if (discountLabel != null) {
                float discount = produit.getDiscount_percentage();
                discountLabel.setText(String.format("Réduction: %.2f%%", discount));
            }
        }
    }

    private Image loadPlaceholderImage(String productName) {
        String placeholderPath = "/images/placeholder.png";
        try {
            Image placeholder = new Image(placeholderPath);
            logger.info("Placeholder image loaded successfully for product '" + productName + "': " + placeholderPath);
            return placeholder;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load placeholder image for product '" + productName + "': " + placeholderPath, e);
            // Fallback to a default Cloudinary placeholder
            String defaultCloudinaryPlaceholder = "https://res.cloudinary.com/ducdgzame/image/upload/v1/placeholder.png";
            logger.info("Falling back to default Cloudinary placeholder for product '" + productName + "': " + defaultCloudinaryPlaceholder);
            return new Image(defaultCloudinaryPlaceholder);
        }
    }

    @FXML
    private void fermerFenetre() {
        descLabel.getScene().getWindow().hide();
    }
}