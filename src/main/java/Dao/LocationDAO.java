package Dao;

import entities.Location;
import entities.Terrain;
import Dao.TerrainDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mainelfirma";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Méthode pour ajouter une location
    public void addLocation(Location location) {
        String query = "INSERT INTO location (date_debut, date_fin, prix_location, mode_paiement, paiement_effectue, statut, terrain_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDate(1, Date.valueOf(location.getDateDebut()));
            pstmt.setDate(2, Date.valueOf(location.getDateFin()));
            pstmt.setFloat(3, location.getPrixLocation());
            pstmt.setString(4, location.getModePaiement());
            pstmt.setBoolean(5, location.isPaiementEffectue());
            pstmt.setString(6, location.getStatut());
            pstmt.setInt(7, location.getTerrain().getId());

            pstmt.executeUpdate();

            // Récupération de l'ID généré pour l'entité
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    location.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout d'une location : " + e.getMessage());
        }
    }

    // Méthode pour mettre à jour une location
    public void updateLocation(Location location) {
        String query = "UPDATE location SET date_debut=?, date_fin=?, prix_location=?, mode_paiement=?, paiement_effectue=?, statut=?, terrain_id=? WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDate(1, Date.valueOf(location.getDateDebut()));
            pstmt.setDate(2, Date.valueOf(location.getDateFin()));
            pstmt.setFloat(3, location.getPrixLocation());
            pstmt.setString(4, location.getModePaiement());
            pstmt.setBoolean(5, location.isPaiementEffectue());
            pstmt.setString(6, location.getStatut());
            pstmt.setInt(7, location.getTerrain().getId());
            pstmt.setInt(8, location.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la location : " + e.getMessage());
        }
    }

    // Méthode pour supprimer une location
    public void deleteLocation(int id) {
        String query = "DELETE FROM location WHERE id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la location : " + e.getMessage());
        }
    }

    // Méthode pour récupérer toutes les locations
    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        String query = "SELECT * FROM location";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                Date dateDebut = rs.getDate("date_debut");
                Date dateFin = rs.getDate("date_fin");
                float prixLocation = rs.getFloat("prix_location");
                String modePaiement = rs.getString("mode_paiement");
                boolean paiementEffectue = rs.getBoolean("paiement_effectue");
                String statut = rs.getString("statut");
                int terrainId = rs.getInt("terrain_id");

                // Recherche du terrain associé à la location
                Terrain terrain = new TerrainDao().getTerrainById(terrainId);

                // Création de l'objet Location
                Location location = new Location(id, terrain, dateDebut.toLocalDate(), dateFin.toLocalDate(), prixLocation, paiementEffectue, modePaiement, statut);
                locations.add(location);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des locations : " + e.getMessage());
        }

        return locations;
    }

    // Méthode pour récupérer une location par son ID
    public Location getLocationById(int id) {
        String query = "SELECT * FROM location WHERE id=?";
        Location location = null;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Date dateDebut = rs.getDate("date_debut");
                Date dateFin = rs.getDate("date_fin");
                float prixLocation = rs.getFloat("prix_location");
                String modePaiement = rs.getString("mode_paiement");
                boolean paiementEffectue = rs.getBoolean("paiement_effectue");
                String statut = rs.getString("statut");
                int terrainId = rs.getInt("terrain_id");

                // Recherche du terrain associé à la location
                Terrain terrain = new TerrainDao().getTerrainById(terrainId);

                // Création de l'objet Location
                location = new Location(id, terrain, dateDebut.toLocalDate(), dateFin.toLocalDate(), prixLocation, paiementEffectue, modePaiement, statut);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la location : " + e.getMessage());
        }

        return location;
    }





    public List<Location> getAll() {
        List<Location> locations = new ArrayList<>();
        String query = "SELECT * FROM location";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                Date dateDebut = rs.getDate("date_debut");
                Date dateFin = rs.getDate("date_fin");
                float prixLocation = rs.getFloat("prix_location");
                String modePaiement = rs.getString("mode_paiement");
                boolean paiementEffectue = rs.getBoolean("paiement_effectue");
                String statut = rs.getString("statut");
                int terrainId = rs.getInt("terrain_id");

                // Récupérer le terrain associé à la location
                Terrain terrain = new TerrainDao().getTerrainById(terrainId);

                // Créer un objet Location et l'ajouter à la liste
                Location location = new Location(id, terrain, dateDebut.toLocalDate(), dateFin.toLocalDate(), prixLocation, paiementEffectue, modePaiement, statut);
                locations.add(location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }

    public void insert(Location loc) {
        String query = "INSERT INTO location (terrain_id, date_debut, date_fin, prix_location, mode_paiement, paiement_effectue, statut) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    }


}
