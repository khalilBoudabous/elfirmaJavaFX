package controllers;

import entities.Produit;
import entities.Categorie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import services.ProduitService;
import services.CategorieService;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Affichierproduitagriculteur {

    private static final Logger logger = Logger.getLogger(Affichierproduitagriculteur.class.getName());

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();
    private List<CheckBox> categoryCheckBoxes = new ArrayList<>();
    private List<Produit> filteredProduits = new ArrayList<>();
    private static final int PRODUCTS_PER_PAGE = 10;

    @FXML
    private FlowPane productContainer;
    @FXML
    private AnchorPane scrollPane;
    @FXML
    private TextField searchField;
    @FXML
    private VBox categoryBox;
    @FXML
    private TextField minPriceField;
    @FXML
    private TextField maxPriceField;
    @FXML
    private Button filterButton;
    @FXML
    private Pagination pagination;

    @FXML
    public void initialize() {
        populateCategories();
        chargerProduits();
        productContainer.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(60));
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> updatePage(newIndex.intValue()));

        // Add event handlers programmatically
        searchField.setOnKeyReleased(event -> applyFilters());
        minPriceField.setOnKeyReleased(event -> applyFilters());
        maxPriceField.setOnKeyReleased(event -> applyFilters());
        filterButton.setOnAction(event -> applyFilters());
    }

    private void populateCategories() {
        try {
            List<Categorie> categories = categorieService.recuperer();
            for (Categorie cat : categories) {
                CheckBox checkBox = new CheckBox(cat.getNom_categorie());
                checkBox.setUserData(cat.getId());
                categoryCheckBoxes.add(checkBox);
                categoryBox.getChildren().add(checkBox);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void applyFilters() {
        chargerProduits();
    }

    private void chargerProduits() {
        try {
            List<Produit> produits = produitService.recuperer();
            filteredProduits = filterProduits(produits);
            updatePagination();
            updatePage(0);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors du chargement des produits", e);
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) filteredProduits.size() / PRODUCTS_PER_PAGE);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setCurrentPageIndex(0);
    }

    private void updatePage(int pageIndex) {
        productContainer.getChildren().clear();
        int startIndex = pageIndex * PRODUCTS_PER_PAGE;
        int endIndex = Math.min(startIndex + PRODUCTS_PER_PAGE, filteredProduits.size());

        for (int i = startIndex; i < endIndex; i++) {
            VBox card = createProductCard(filteredProduits.get(i));
            productContainer.getChildren().add(card);
        }
    }

    private List<Produit> filterProduits(List<Produit> produits) {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (!searchTerm.isEmpty()) {
            produits = produits.stream()
                    .filter(p -> p.getNom_produit().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        List<Integer> selectedCategoryIds = categoryCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (Integer) cb.getUserData())
                .collect(Collectors.toList());
        if (!selectedCategoryIds.isEmpty()) {
            produits = produits.stream()
                    .filter(p -> selectedCategoryIds.contains(p.getCategorie_id()))
                    .collect(Collectors.toList());
        }

        try {
            double minPrice = minPriceField.getText().isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minPriceField.getText());
            double maxPrice = maxPriceField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceField.getText());
            produits = produits.stream()
                    .filter(p -> p.getPrix() >= minPrice && p.getPrix() <= maxPrice)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            // Ignore invalid price inputs
        }

        return produits;
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(15));
        card.setMinWidth(220);
        card.setMaxWidth(260);
        card.setPrefWidth(240);
        card.setStyle("""
            -fx-background-color: #C8E6C9;
            -fx-border-color: #388E3C;
            -fx-border-width: 1;
            -fx-border-radius: 15;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
        """);

        card.setOnMouseEntered(e -> card.setStyle("""
            -fx-background-color: #A5D6A7;
            -fx-border-color: #1B5E20;
            -fx-border-width: 1;
            -fx-border-radius: 15;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);
        """));
        card.setOnMouseExited(e -> card.setStyle("""
            -fx-background-color: #C8E6C9;
            -fx-border-color: #388E3C;
            -fx-border-width: 1;
            -fx-border-radius: 15;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);
        """));

        ImageView imageView = new ImageView();
        String imageUrl = produit.getImage();
        logger.info("Loading image for product '" + produit.getNom_produit() + "' with URL: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("https://res.cloudinary.com")) {
            try {
                Image image = new Image(imageUrl, 200, 160, true, true, true); // Async, preserve ratio, smooth scaling
                image.progressProperty().addListener((obs, oldValue, newValue) -> {
                    if (newValue.doubleValue() == 1.0) {
                        logger.info("Image loaded successfully for product: " + produit.getNom_produit());
                    }
                });
                image.errorProperty().addListener((obs, oldError, newError) -> {
                    if (newError) {
                        logger.warning("Failed to load Cloudinary image for product '" + produit.getNom_produit() + "': " + imageUrl);
                        imageView.setImage(loadPlaceholderImage(produit.getNom_produit()));
                    }
                });
                imageView.setImage(image);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception loading Cloudinary image for product '" + produit.getNom_produit() + "': " + imageUrl, e);
                imageView.setImage(loadPlaceholderImage(produit.getNom_produit()));
            }
        } else {
            logger.warning("Invalid or missing Cloudinary URL for product '" + produit.getNom_produit() + "': " + imageUrl);
            imageView.setImage(loadPlaceholderImage(produit.getNom_produit()));
        }

        imageView.setFitWidth(200);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);

        Rectangle clip = new Rectangle(200, 160);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        Label nomLabel = new Label(produit.getNom_produit());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1B5E20;");

        Label descLabel = new Label(produit.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242;");

        Label prixLabel = new Label("Prix: " + produit.getPrix() + " DT");
        prixLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1B5E20; -fx-font-weight: bold;");

        Label quantiteLabel = new Label("Quantité: " + produit.getQuantite());
        quantiteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242;");

        String nomCategorie = "";
        try {
            Categorie cat = categorieService.getById(produit.getCategorie_id());
            if (cat != null) nomCategorie = cat.getNom_categorie();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération de la catégorie", e);
            e.printStackTrace();
        }
        Label catLabel = new Label("Catégorie: " + nomCategorie);
        catLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242;");

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMinHeight(40);

        Button btnDetails = new Button("Détails");
        btnDetails.setPrefWidth(100);
        btnDetails.setStyle("""
            -fx-background-color: #388E3C;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 8 20;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """);
        btnDetails.setOnMouseEntered(e -> btnDetails.setStyle("""
            -fx-background-color: #1B5E20;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 8 20;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """));
        btnDetails.setOnMouseExited(e -> btnDetails.setStyle("""
            -fx-background-color: #388E3C;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 8 20;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """));
        btnDetails.setOnAction(e -> ouvrirFenetreDetails(produit));

        Button btnPayer = new Button("Payer");
        btnPayer.setPrefWidth(100);
        btnPayer.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 8 20;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """);
        btnPayer.setOnMouseEntered(e -> btnPayer.setStyle("""
            -fx-background-color: #2E7D32;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 8 20;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """));
        btnPayer.setOnMouseExited(e -> btnPayer.setStyle("""
            -fx-background-color: #4CAF50;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 8 20;
            -fx-background-radius: 10;
            -fx-cursor: hand;
        """));
        btnPayer.setOnAction(e -> ouvrirFormulairePaiement(produit));

        buttonBox.getChildren().addAll(btnDetails, btnPayer);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(imageView, nomLabel, descLabel, prixLabel, quantiteLabel, catLabel, spacer, buttonBox);

        return card;
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

    private void ouvrirFenetreDetails(Produit produit) {
        try {
            System.out.println("Chargement de DetailsProduit.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsProduit.fxml"));
            if (loader.getLocation() == null) {
                throw new IOException("DetailsProduit.fxml introuvable à l'emplacement /DetailsProduit.fxml");
            }
            Parent root = loader.load();
            System.out.println("DetailsProduit.fxml chargé avec succès.");

            DetailsProduit controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Détails du Produit");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
            System.out.println("Fenêtre DetailsProduit affichée avec succès.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre de détails", e);
            e.printStackTrace();
        }
    }

    private void ouvrirFormulairePaiement(Produit produit) {
        try {
            System.out.println("Tentative de chargement de FormulairePaiement.fxml...");
            String[] possiblePaths = {
                    "/FormulairePaiement.fxml",
                    "/fxml/FormulairePaiement.fxml",
                    "/resources/FormulairePaiement.fxml"
            };

            FXMLLoader loader = null;
            for (String path : possiblePaths) {
                System.out.println("Essai du chemin : " + path);
                loader = new FXMLLoader(getClass().getResource(path));
                if (loader.getLocation() != null) {
                    System.out.println("Chemin trouvé : " + path);
                    break;
                }
            }

            if (loader == null || loader.getLocation() == null) {
                throw new IOException("FormulairePaiement.fxml introuvable après avoir essayé tous les chemins possibles.");
            }

            Parent root = loader.load();
            System.out.println("FormulairePaiement.fxml chargé avec succès.");

            FormulairePaiement controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Formulaire de Paiement");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.showAndWait(); // Wait for the payment form to close

            // Check if the payment was successful
            if (controller.isPaymentSuccessful()) {
                // Save the purchased product to the user's account
                UtilisateurService utilisateurService = new UtilisateurService();
                long userId =FrontOfficeEvenement.instance.getCurrentUserId(); // Replace with the actual method to get the logged-in user's ID
                utilisateurService.addProduitToUser(userId, produit.getId());

                // Refresh the "Produit acheté" page
                FrontOfficeEvenement.refreshEvents(); // Ensure this method refreshes the purchased products list
                System.out.println("Produit ajouté à l'utilisateur avec succès.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de l'ouverture du formulaire de paiement", e);
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir le formulaire de paiement");
            alert.setContentText("Une erreur s'est produite : " + e.getMessage());
            alert.showAndWait();
        }
    }
}