package Dao;

import entities.Terrain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static utils.DBconnection.getConnection;
import static utils.DBconnection.getConnection;


public class TerrainDao {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mainelfirma";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Retrieves all terrains from the database
    public List<Terrain> getAllTerrains() {
        List<Terrain> terrains = new ArrayList<>();
        String query = "SELECT * FROM terrain";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Terrain terrain = new Terrain(
                        rs.getInt("id"),
                        rs.getDouble("superficie"),
                        rs.getString("localisation"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("type_sol"),
                        rs.getBoolean("irrigation_disponible"),
                        rs.getString("statut")
                );
                terrains.add(terrain);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return terrains;
    }

    // Adds a new terrain to the database
    public void addTerrain(Terrain terrain) throws SQLException {
        String query = "INSERT INTO terrain (superficie, localisation, latitude, longitude, type_sol, irrigation_disponible, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, terrain.getSuperficie());
            pstmt.setString(2, terrain.getLocalisation());
            pstmt.setDouble(3, terrain.getLatitude());
            pstmt.setDouble(4, terrain.getLongitude());
            pstmt.setString(5, terrain.getTypeSol());
            pstmt.setBoolean(6, terrain.isIrrigationDisponible());
            pstmt.setString(7, terrain.getStatut());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    terrain.setId(rs.getInt(1));
                }
            }
        }
    }

    // Updates an existing terrain in the database
    public void updateTerrain(Terrain terrain) throws SQLException {
        String query = "UPDATE terrain SET superficie=?, localisation=?, latitude=?, longitude=?, type_sol=?, irrigation_disponible=?, statut=? WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, terrain.getSuperficie());
            pstmt.setString(2, terrain.getLocalisation());
            pstmt.setDouble(3, terrain.getLatitude());
            pstmt.setDouble(4, terrain.getLongitude());
            pstmt.setString(5, terrain.getTypeSol());
            pstmt.setBoolean(6, terrain.isIrrigationDisponible());
            pstmt.setString(7, terrain.getStatut());
            pstmt.setInt(8, terrain.getId());

            pstmt.executeUpdate();
        }
    }

    // Deletes a terrain from the database
    public void deleteTerrain(int id) throws SQLException {
        String query = "DELETE FROM terrain WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Retrieves a terrain by its ID
    public Terrain getTerrainById(int id) {
        String query = "SELECT * FROM terrain WHERE id=?";
        Terrain terrain = null;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                terrain = new Terrain(
                        rs.getInt("id"),
                        rs.getDouble("superficie"),
                        rs.getString("localisation"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("type_sol"),
                        rs.getBoolean("irrigation_disponible"),
                        rs.getString("statut")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du terrain : " + e.getMessage());
        }

        return terrain;
    }

    // Retrieves terrains by localisation from the database
    public List<Terrain> getTerrainByLocalisation(String localisation) {
        List<Terrain> terrains = new ArrayList<>();
        String query = "SELECT * FROM terrain WHERE localisation LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + localisation + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Terrain terrain = new Terrain(
                        rs.getInt("id"),
                        rs.getDouble("superficie"),
                        rs.getString("localisation"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("type_sol"),
                        rs.getBoolean("irrigation_disponible"),
                        rs.getString("statut")
                );
                terrains.add(terrain);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return terrains;
    }


    // Méthode pour compter les terrains par statut
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM terrain WHERE statut = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);  // Récupère le nombre de terrains avec le statut donné
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;  // Retourne 0 si une erreur se produit ou si aucun terrain n'est trouvé
    }
}


