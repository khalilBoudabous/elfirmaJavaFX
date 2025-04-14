package controllers;

import entities.Produit;
import entities.Categorie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import services.ProduitService;
import services.CategorieService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Affichierproduit {

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();

    @FXML
    private FlowPane productContainer;
    @FXML
    private AnchorPane scrollPane;

    @FXML
    public void initialize() {
        chargerProduits();
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
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setPrefWidth(220);
        card.setStyle("""
            -fx-background-color: #ffffff;
            -fx-border-color: #ccc;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 5);
        """);

        ImageView imageView = new ImageView();
        File imageFile = new File(produit.getImage());
        if (imageFile.exists()) {
            imageView.setImage(new Image(imageFile.toURI().toString()));
        }
        imageView.setFitWidth(180);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);

        Label nomLabel = new Label(produit.getNom_produit());
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label descLabel = new Label(produit.getDescription());
        descLabel.setWrapText(true);
        Label prixLabel = new Label("Prix : " + produit.getPrix() + " DT");
        Label quantiteLabel = new Label("Quantité : " + produit.getQuantite());

        String nomCategorie = "";
        try {
            Categorie cat = categorieService.getById(produit.getCategorie_id());
            if (cat != null) nomCategorie = cat.getNom_categorie();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Label catLabel = new Label("Catégorie : " + nomCategorie);

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        btnModifier.setOnAction(e -> ouvrirFenetreModification(produit));

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        btnSupprimer.setOnAction(e -> {
            try {
                produitService.supprimer(produit);
                chargerProduits(); // Recharge la liste après suppression
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        buttonBox.getChildren().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(imageView, nomLabel, descLabel, prixLabel, quantiteLabel, catLabel, buttonBox);
        return card;
    }

    private void ouvrirFenetreModification(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierProduit.fxml"));
            Parent root = loader.load();

            ModifierProduit controller = loader.getController();
            controller.setProduit(produit);
            controller.setAffichageController(this); // lien pour recharger après modif

            Stage stage = new Stage();
            stage.setTitle("Modifier Produit");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
