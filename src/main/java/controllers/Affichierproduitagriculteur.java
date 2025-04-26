package controllers;

import entities.Produit;
import entities.Categorie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import services.ProduitService;
import services.CategorieService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Affichierproduitagriculteur {

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();
    private List<CheckBox> categoryCheckBoxes = new ArrayList<>();
    private List<Produit> filteredProduits = new ArrayList<>();
    private static final int PRODUCTS_PER_PAGE = 6;

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
        File imageFile = new File(produit.getImage());
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
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
            e.printStackTrace();
        }
        Label catLabel = new Label("Catégorie: " + nomCategorie);
        catLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242;");



        HBox buttonBox = new HBox(10); // Ajout d'un espacement entre les boutons
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
        card.getChildren().addAll(imageView, nomLabel, descLabel, prixLabel, quantiteLabel, catLabel,  spacer, buttonBox);

        return card;
    }

    private void ouvrirFenetreDetails(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsProduit.fxml"));
            Parent root = loader.load();

            DetailsProduit controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Détails du Produit");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ouvrirFormulairePaiement(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulairePaiement.fxml"));
            Parent root = loader.load();

            FormulairePaiement controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Formulaire de Paiement");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}