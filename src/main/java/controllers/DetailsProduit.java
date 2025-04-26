package controllers;

import entities.Produit;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class DetailsProduit {

    @FXML
    private ImageView imageView;
    @FXML
    private ImageView imageViewSmall;
    @FXML
    private Label nomLabel; // Unused but kept for compatibility
    @FXML
    private Label descLabel;
    @FXML
    private Label prixLabel;
    @FXML
    private Label quantiteLabel;
    @FXML
    private Label catLabel; // Unused but kept for compatibility

    private Produit produit;

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
                imageViewSmall.setImage(image); // Same image for both views
            }
            // nomLabel and catLabel are unused in the current FXML but kept for compatibility
            if (nomLabel != null) {
                nomLabel.setText(produit.getNom_produit());
            }
            descLabel.setText("Description: " + produit.getDescription());
            prixLabel.setText(String.format("$%.2f", produit.getPrix()));
            quantiteLabel.setText("Quantité: " + produit.getQuantite());
            if (catLabel != null) {
                catLabel.setText("Catégorie: " + produit.getCategorie_id());
            }
        }
    }

    @FXML
    private void fermerFenetre() {
        descLabel.getScene().getWindow().hide();
    }

    @FXML
    private void payer() {
        // Placeholder for payment logic (unused since "Payer" button is removed)
        System.out.println("Payer button clicked - Implement payment logic here");
    }

}