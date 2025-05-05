package controllers;

import entities.Produit;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import services.ProduitService;
import services.UtilisateurService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProduitAcheteController {

    private static final Logger logger = Logger.getLogger(ProduitAcheteController.class.getName());

    @FXML private ScrollPane scrollPane;
    @FXML private VBox productContainer;
    @FXML private TextField searchField;
    @FXML private VBox noProductsPlaceholder;

    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ProduitService produitService = new ProduitService();
    private Long userId;
    private List<Produit> allProducts;

    @FXML
    public void initialize() {
        logger.info("Initializing ProduitAcheteController");
        scrollPane.setFitToWidth(true);
        productContainer.setSpacing(20);
        if (noProductsPlaceholder != null) {
            noProductsPlaceholder.setVisible(false);
            noProductsPlaceholder.setManaged(false);
        }
        // Add listener for search field
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> filterProducts(newValue));
        }
    }

    public void setUserId(Long userId) {
        this.userId = userId;
        logger.info("setUserId called with userId: " + userId);
        loadPurchasedProducts();
    }

    @FXML
    private void refreshProducts() {
        logger.info("Refreshing purchased products for user: " + userId);
        if (searchField != null) {
            searchField.clear();
        }
        loadPurchasedProducts();
    }

    private void loadPurchasedProducts() {
        logger.info("Loading purchased products for user: " + userId);
        productContainer.getChildren().clear();
        allProducts = null;
        if (noProductsPlaceholder != null) {
            noProductsPlaceholder.setVisible(false);
            noProductsPlaceholder.setManaged(false);
        }

        try {
            Utilisateur user = utilisateurService.getUtilisateurById(userId);
            if (user == null) {
                logger.warning("Utilisateur non trouvé pour ID: " + userId);
                Label noUserLabel = new Label("Utilisateur non trouvé.");
                noUserLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff0000; -fx-font-family: 'Arial';");
                productContainer.getChildren().add(noUserLabel);
                return;
            }
            logger.info("Utilisateur trouvé: " + user.getNom() + " (ID: " + userId + ")");
            String produitsString = user.getProduits();
            logger.info("Produits string for user " + userId + ": " + produitsString);
            if (produitsString == null || produitsString.trim().isEmpty()) {
                logger.info("Aucun produit acheté pour l'utilisateur: " + userId);
                if (noProductsPlaceholder != null) {
                    noProductsPlaceholder.setVisible(true);
                    noProductsPlaceholder.setManaged(true);
                } else {
                    Label noProductsLabel = new Label("Aucun produit acheté trouvé pour cet utilisateur.");
                    noProductsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; -fx-font-family: 'Arial';");
                    productContainer.getChildren().add(noProductsLabel);
                }
                return;
            }
            List<String> produitIds = Arrays.asList(produitsString.split(","));
            allProducts = produitIds.stream()
                    .map(id -> {
                        try {
                            return produitService.getProduitById(Integer.parseInt(id.trim()));
                        } catch (NumberFormatException | SQLException e) {
                            logger.warning("Invalid product ID or error: " + id + ", " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(prod -> prod != null)
                    .collect(Collectors.toList());

            if (allProducts.isEmpty()) {
                logger.info("Aucun produit valide trouvé pour l'utilisateur: " + userId);
                if (noProductsPlaceholder != null) {
                    noProductsPlaceholder.setVisible(true);
                    noProductsPlaceholder.setManaged(true);
                } else {
                    Label noValidProductsLabel = new Label("Aucun produit valide trouvé.");
                    noValidProductsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; -fx-font-family: 'Arial';");
                    productContainer.getChildren().add(noValidProductsLabel);
                }
                return;
            }

            for (Produit produit : allProducts) {
                logger.info("Produit trouvé: " + produit.getNom_produit() + " (ID: " + produit.getId() + ")");
                HBox productCard = createProductCard(produit);
                productContainer.getChildren().add(productCard);
            }
        } catch (SQLException e) {
            logger.severe("Erreur lors du chargement des produits: " + e.getMessage());
            Label errorLabel = new Label("Erreur lors du chargement des produits: " + e.getMessage());
            errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ff0000; -fx-font-family: 'Arial';");
            productContainer.getChildren().add(errorLabel);
        }
    }

    private void filterProducts(String searchText) {
        logger.info("Filtering products with search text: " + searchText);
        productContainer.getChildren().clear();
        if (noProductsPlaceholder != null) {
            noProductsPlaceholder.setVisible(false);
            noProductsPlaceholder.setManaged(false);
        }

        if (allProducts == null || allProducts.isEmpty()) {
            if (noProductsPlaceholder != null) {
                noProductsPlaceholder.setVisible(true);
                noProductsPlaceholder.setManaged(true);
            } else {
                Label noProductsLabel = new Label("Aucun produit acheté trouvé.");
                noProductsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; -fx-font-family: 'Arial';");
                productContainer.getChildren().add(noProductsLabel);
            }
            return;
        }

        List<Produit> filteredProducts = allProducts.stream()
                .filter(produit -> searchText == null || searchText.isEmpty() ||
                        produit.getNom_produit().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());

        if (filteredProducts.isEmpty()) {
            Label noMatchLabel = new Label("Aucun produit ne correspond à la recherche.");
            noMatchLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555; -fx-font-family: 'Arial';");
            productContainer.getChildren().add(noMatchLabel);
            return;
        }

        for (Produit produit : filteredProducts) {
            HBox productCard = createProductCard(produit);
            productContainer.getChildren().add(productCard);
        }
    }

    private HBox createProductCard(Produit produit) {
        HBox card = new HBox(10);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-padding: 15; -fx-alignment: center-left; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(produit.getImage(), true));
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");
        } catch (Exception e) {
            logger.warning("Erreur de chargement de l'image pour le produit " + produit.getNom_produit() + ": " + e.getMessage());
            imageView.setImage(new Image("https://via.placeholder.com/100", true));
        }

        VBox details = new VBox(5);
        details.setStyle("-fx-alignment: center-left;");
        Label nameLabel = new Label(produit.getNom_produit());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-family: 'Arial';");
        Label descriptionLabel = new Label(produit.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666; -fx-font-family: 'Arial'; -fx-wrap-text: true; -fx-max-width: 300;");
        Label priceLabel = new Label("Prix: " + produit.getPrix() + " TND");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-family: 'Arial';");
        details.getChildren().addAll(nameLabel, descriptionLabel, priceLabel);

        card.getChildren().addAll(imageView, details);
        return card;
    }
}