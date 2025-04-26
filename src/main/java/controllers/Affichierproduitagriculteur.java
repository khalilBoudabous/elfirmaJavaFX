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
import javafx.scene.control.Label;
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
import java.util.List;

public class Affichierproduitagriculteur {

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    @FXML
    private FlowPane productContainer;
    @FXML
    private AnchorPane scrollPane;

    @FXML
    public void initialize() {
        chargerProduits();
        // Make FlowPane responsive to window resizing
        productContainer.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(60)); // Subtract padding
    }

    private void chargerProduits() {
        try {
            productContainer.getChildren().clear();
            List<Produit> produits = produitService.recuperer();

            for (Produit p : produits) {
                VBox card = createProductCard(p);
                productContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        // Add hover effect
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

        // Add rounded corners to the image
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

        // Fixed button layout
        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMinHeight(40); // Fixed height for consistency

        Button btnDetails = new Button("Détails");
        btnDetails.setPrefWidth(100); // Fixed width for consistency
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

        buttonBox.getChildren().add(btnDetails);

        // Ensure buttonBox is at the bottom with consistent spacing
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // Push button to bottom
        card.getChildren().addAll(imageView, nomLabel, descLabel, prixLabel, quantiteLabel, catLabel, spacer, buttonBox);

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
            stage.setMaximized(true); // Maximize the window
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}