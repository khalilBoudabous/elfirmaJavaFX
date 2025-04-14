package services;

import entities.Categorie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.MyDatabase;

public class CategorieService implements Service<Categorie> {

    private Connection cnx;

    public CategorieService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Categorie categorie) throws SQLException {
        String sql = "INSERT INTO categorie (nom_categorie, description) VALUES (?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, categorie.getNom_categorie());
        ps.setString(2, categorie.getDescription());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Categorie categorie) throws SQLException {
        String sql = "UPDATE categorie SET nom_categorie = ?, description = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, categorie.getNom_categorie());
        ps.setString(2, categorie.getDescription());
        ps.setInt(3, categorie.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Categorie categorie) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, categorie.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Categorie> recuperer() throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorie";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Categorie c = new Categorie(
                    rs.getInt("id"),
                    rs.getString("nom_categorie"),
                    rs.getString("description")
            );
            categories.add(c);
        }
        return categories;
    }
    public Categorie getById(int id) throws SQLException {
        return recuperer().stream().filter(c -> c.getId() == id).findFirst().orElse(null);
    }

}
