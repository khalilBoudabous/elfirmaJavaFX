package services;

import entities.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.MyDatabase;

public class ProduitService implements Service<Produit> {

    private Connection cnx;

    public ProduitService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Produit produit) throws SQLException {
        String sql = "INSERT INTO produit (nom_produit,description, image, quantite, prix,  categorie_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, produit.getNom_produit());
        ps.setString(2, produit.getDescription());
        ps.setString(3, produit.getImage());
        ps.setInt(4, produit.getQuantite());
        ps.setFloat(5, produit.getPrix());
        ps.setInt(6, produit.getCategorie_id());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Produit produit) throws SQLException {
        String sql = "UPDATE produit SET nom_produit = ?, quantite = ?, prix = ?, image = ?, description = ?, categorie_id = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, produit.getNom_produit());
        ps.setInt(2, produit.getQuantite());
        ps.setFloat(3, produit.getPrix());
        ps.setString(4, produit.getImage());
        ps.setString(5, produit.getDescription());
        ps.setInt(6, produit.getCategorie_id());
        ps.setInt(7, produit.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Produit produit) throws SQLException {
        String sql = "DELETE FROM produit WHERE id = ?";
        PreparedStatement statement = cnx.prepareStatement(sql);
        statement.setInt(1, produit.getId());
        statement.executeUpdate();
    }


    @Override
    public List<Produit> recuperer() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Produit p = new Produit(
                    rs.getInt("id"),
                    rs.getInt("quantite"),
                    rs.getInt("categorie_id"),
                    rs.getFloat("prix"),
                    rs.getString("image"),
                    rs.getString("description"),
                    rs.getString("nom_produit")
            );
            produits.add(p);
        }
        return produits;
    }
}
