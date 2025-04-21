package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart; // Correct!import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import entities.Terrain;
import entities.Location;
import Dao.TerrainDao;
import Dao.LocationDAO;
import java.util.List;

public class DashboardController {

    @FXML
    private Label lblActive, lblOffline, lblExpired;

    @FXML
    private PieChart pieHash, pieKey;  // Ici vous pourriez avoir deux graphiques pour les terrains ou d'autres informations

    private TerrainDao terrainDao;
    private LocationDAO locationDAO;

    public DashboardController() {
        terrainDao = new TerrainDao();
        locationDAO = new LocationDAO();
    }

    @FXML
    private void initialize() {
        loadTerrainData();
        loadLocationData();
        updateCharts();
    }

    private void loadTerrainData() {
        try {
            // Récupérer les données des terrains avec des méthodes optimisées
            long activeCount = terrainDao.countByStatus("Active");
            long offlineCount = terrainDao.countByStatus("Offline");
            long expiredCount = terrainDao.countByStatus("Expired");

            // Mettre à jour l'affichage des statuts
            lblActive.setText(activeCount + " terrains actifs");
            lblOffline.setText(offlineCount + " terrains hors ligne");
            lblExpired.setText(expiredCount + " terrains expirés");
        } catch (Exception e) {
            // Gestion des erreurs (ex : erreur de connexion DB)
            e.printStackTrace();
        }
    }

    private void loadLocationData() {
        try {
            // Chargement des données des locations si nécessaire
            List<Location> locations = locationDAO.getAllLocations();
            // Traitement des données des locations (si applicable)
        } catch (Exception e) {
            e.printStackTrace();
            // Gestion des erreurs pour les locations
        }
    }

    private void updateCharts() {
        try {
            // Mise à jour des graphiques avec des données dynamiques basées sur les terrains
            long activeCount = terrainDao.countByStatus("Disponible");
            long offlineCount = terrainDao.countByStatus("Offline");
            long expiredCount = terrainDao.countByStatus("Expired");

            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Active", activeCount),
                    new PieChart.Data("Offline", offlineCount),
                    new PieChart.Data("Expired", expiredCount)
            );

            pieHash.setData(pieChartData);  // Mettre à jour le PieChart pour les terrains (ici, hash est une mauvaise dénomination)
        } catch (Exception e) {
            e.printStackTrace();
            // Gestion des erreurs pour la mise à jour du graphique
        }
    }

    // Méthode pour actualiser les données et réagir aux changements
    public void refreshDashboard() {
        loadTerrainData();
        loadLocationData();
        updateCharts();
    }
}
