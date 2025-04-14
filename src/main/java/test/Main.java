package test;

import entities.Produit;
import services.ProduitService;
import utils.MyDatabase;

public class Main {
    public static void main(String[] args) {
        ProduitService produitService = new ProduitService();

        Produit nouveauProduit = new Produit(
                169, // id auto-incrémenté
                50, // quantité

                130,  // ID de la catégorie (assurez-vous que la catégorie existe)
                19.99f, // prix
                "chemin/image.jpg", // image
                "Description du produit de test", // description
                "ProduitTest" // nom
        );


        try {
            /*produitService.ajouter(nouveauProduit);
            System.out.println("Produit ajouté avec succès !");*/
            produitService.supprimer(nouveauProduit);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du produit : " + e.getMessage());
            e.printStackTrace();
        }
    }
}