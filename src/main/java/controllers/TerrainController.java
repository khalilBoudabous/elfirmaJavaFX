package controllers ;

import test.MainFX;
import entities.Terrain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.TextField;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.Optional;

public class TerrainController {

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mainelfirma";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Form fields
    @FXML private TextField idField;
    @FXML private TextField superficieField;
    @FXML private TextField localisationField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private ComboBox<String> typeSolCombo;
    @FXML private CheckBox irrigationCheckbox;
    @FXML private ComboBox<String> statutCombo;

    @FXML private Button uploadButton;
    @FXML private ImageView terrainImageView;
    private File selectedImageFile;

    // Table view
    @FXML private TableView<Terrain> terrainTable;
    @FXML private TableColumn<Terrain, Integer> colId;
    @FXML private TableColumn<Terrain, Double> colSuperficie;
    @FXML private TableColumn<Terrain, String> colLocalisation;
    @FXML private TableColumn<Terrain, Double> colLatitude;
    @FXML private TableColumn<Terrain, Double> colLongitude;
    @FXML private TableColumn<Terrain, String> colTypeSol;
    @FXML private TableColumn<Terrain, Boolean> colIrrigation;
    @FXML private TableColumn<Terrain, String> colStatut;

    private final ObservableList<Terrain> terrainList = FXCollections.observableArrayList();


    @FXML
    private void initialize() {
        typeSolCombo.getItems().addAll("Argileux", "Sableux", "Limoneux", "Tourbeux");
        statutCombo.getItems().addAll("Disponible", "Loué", "En maintenance", "Vendu");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSuperficie.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colLatitude.setCellValueFactory(new PropertyValueFactory<>("latitude"));
        colLongitude.setCellValueFactory(new PropertyValueFactory<>("longitude"));
        colTypeSol.setCellValueFactory(new PropertyValueFactory<>("typeSol"));
        colIrrigation.setCellValueFactory(new PropertyValueFactory<>("irrigationDisponible"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        loadTerrains();

        terrainTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) fillFormWithSelectedTerrain(selected);
        });
    }

    private void loadTerrains() {
        if (MainFX.getCurrentUser() == null) {
            showAlert("Error", "No user logged in. Please login again.");
            return;
        }

        terrainList.clear();
        String query = "SELECT * FROM terrain WHERE utilisateur_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, MainFX.getCurrentUser().getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Terrain t = new Terrain(
                        rs.getInt("id"),
                        rs.getDouble("superficie"),
                        rs.getString("localisation"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("type_sol"),
                        rs.getBoolean("irrigation_disponible"),
                        rs.getString("statut"),
                        rs.getString("image")
                );
                terrainList.add(t);
            }

            terrainTable.setItems(terrainList);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load terrains: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleAddTerrain() {
        if (!validateInputs()) return;

        Terrain terrain = new Terrain(
                Double.parseDouble(superficieField.getText()),
                localisationField.getText(),
                Double.parseDouble(latitudeField.getText()),
                Double.parseDouble(longitudeField.getText()),
                typeSolCombo.getValue(),
                irrigationCheckbox.isSelected(),
                statutCombo.getValue()
        );

        if (selectedImageFile != null) {
            try {
                File dest = new File("images/" + selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                terrain.setImage(dest.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Image Error", "Image upload failed: " + e.getMessage());
            }
        }

        String sql = "INSERT INTO terrain (superficie, localisation, latitude, longitude, type_sol, irrigation_disponible, statut, utilisateur_id, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDouble(1, terrain.getSuperficie());
            pstmt.setString(2, terrain.getLocalisation());
            pstmt.setDouble(3, terrain.getLatitude());
            pstmt.setDouble(4, terrain.getLongitude());
            pstmt.setString(5, terrain.getTypeSol());
            pstmt.setBoolean(6, terrain.isIrrigationDisponible());
            pstmt.setString(7, terrain.getStatut());
            pstmt.setInt(8, MainFX.getCurrentUser().getId());
            pstmt.setString(9, terrain.getImage());

            if (pstmt.executeUpdate() > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    terrain.setId(rs.getInt(1));
                    terrainList.add(terrain);
                    showAlert("Success", "Terrain added successfully");
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add terrain: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateTerrain() {
        Terrain selected = terrainTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "No terrain selected");
            return;
        }
        if (!validateInputs()) return;

        selected.setSuperficie(Double.parseDouble(superficieField.getText()));
        selected.setLocalisation(localisationField.getText());
        selected.setLatitude(Double.parseDouble(latitudeField.getText()));
        selected.setLongitude(Double.parseDouble(longitudeField.getText()));
        selected.setTypeSol(typeSolCombo.getValue());
        selected.setIrrigationDisponible(irrigationCheckbox.isSelected());
        selected.setStatut(statutCombo.getValue());

        if (selectedImageFile != null) {
            try {
                File dest = new File("images/" + selectedImageFile.getName());
                Files.copy(selectedImageFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                selected.setImage(dest.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Image Error", "Image upload failed: " + e.getMessage());
            }
        }

        String sql = "UPDATE terrain SET superficie=?, localisation=?, latitude=?, longitude=?, type_sol=?, irrigation_disponible=?, statut=?, image=? WHERE id=? AND utilisateur_id=?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, selected.getSuperficie());
            pstmt.setString(2, selected.getLocalisation());
            pstmt.setDouble(3, selected.getLatitude());
            pstmt.setDouble(4, selected.getLongitude());
            pstmt.setString(5, selected.getTypeSol());
            pstmt.setBoolean(6, selected.isIrrigationDisponible());
            pstmt.setString(7, selected.getStatut());
            pstmt.setString(8, selected.getImage());
            pstmt.setInt(9, selected.getId());
            pstmt.setInt(10, MainFX.getCurrentUser().getId());

            if (pstmt.executeUpdate() > 0) {
                terrainTable.refresh();
                showAlert("Success", "Terrain updated successfully");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update terrain: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteTerrain() {
        Terrain selected = terrainTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "No terrain selected");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Delete Terrain");
        confirmation.setContentText("Are you sure you want to delete this terrain?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM terrain WHERE id=? AND utilisateur_id=?";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selected.getId());
                pstmt.setInt(2, MainFX.getCurrentUser().getId());

                if (pstmt.executeUpdate() > 0) {
                    terrainList.remove(selected);
                    showAlert("Success", "Terrain deleted successfully");
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete terrain: " + e.getMessage());
            }
        }
    }

    @FXML
    private void navigateToLocationPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Location.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(loader.load()));
        stage.show();
    }

    private boolean validateInputs() {
        if (superficieField.getText().isEmpty() || localisationField.getText().isEmpty() ||
                latitudeField.getText().isEmpty() || longitudeField.getText().isEmpty() ||
                typeSolCombo.getValue() == null || statutCombo.getValue() == null) {
            showAlert("Validation Error", "All fields must be filled.");
            return false;
        }
        try {
            Double.parseDouble(superficieField.getText());
            Double.parseDouble(latitudeField.getText());
            Double.parseDouble(longitudeField.getText());
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Superficie, Latitude, and Longitude must be numeric.");
            return false;
        }
        return true;
    }

    private void fillFormWithSelectedTerrain(Terrain terrain) {
        idField.setText(String.valueOf(terrain.getId()));
        superficieField.setText(String.valueOf(terrain.getSuperficie()));
        localisationField.setText(terrain.getLocalisation());
        latitudeField.setText(String.valueOf(terrain.getLatitude()));
        longitudeField.setText(String.valueOf(terrain.getLongitude()));
        typeSolCombo.setValue(terrain.getTypeSol());
        irrigationCheckbox.setSelected(terrain.isIrrigationDisponible());
        statutCombo.setValue(terrain.getStatut());

        if (terrain.getImage() != null) {
            terrainImageView.setImage(new Image("file:" + terrain.getImage()));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleGoToLocation(ActionEvent actionEvent) {
    }
}
