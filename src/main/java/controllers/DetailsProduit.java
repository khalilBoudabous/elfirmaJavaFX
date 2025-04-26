package controllers;

import entities.Categorie;
import entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import services.CategorieService;

import java.io.File;
import java.sql.SQLException;

public class DetailsProduit {

    @FXML private ImageView imageView;
    @FXML private ImageView imageViewSmall;
    @FXML private Label nomLabel;
    @FXML private Label descLabel;
    @FXML private Label prixLabel;
    @FXML private Label quantiteLabel;
    @FXML private Label catLabel;
    @FXML private Label codePromoLabel; // Ajouté pour correspondre à la version précédente
    @FXML private Label discountLabel; // Ajouté pour correspondre à la version précédente

    private Produit produit;
    private final CategorieService categorieService = new CategorieService(); // Ajout du service

    public void setProduit(Produit produit) {
        this.produit = produit;
        afficherDetails();
    }

    private void afficherDetails() {
        if (produit != null) {
            File imageFile = new File(produit.getImage());
            if (imageFile.exists()) {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
                imageViewSmall.setImage(image);
            }
            if (nomLabel != null) {
                nomLabel.setText(produit.getNom_produit());
            }
            descLabel.setText("Description: " + produit.getDescription());
            prixLabel.setText(String.format("Prix: %.2f DT", produit.getPrix()));
            quantiteLabel.setText("Quantité: " + produit.getQuantite());

            // Récupérer le nom de la catégorie à partir de l'ID
            if (catLabel != null) {
                try {
                    Categorie categorie = categorieService.getById(produit.getCategorie_id());
                    if (categorie != null) {
                        catLabel.setText("Catégorie: " + categorie.getNom_categorie());
                    } else {
                        catLabel.setText("Catégorie: Inconnue");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    catLabel.setText("Catégorie: Erreur de récupération");
                }
            }
        }
    }

    @FXML
    private void fermerFenetre() {
        descLabel.getScene().getWindow().hide();
    }
}