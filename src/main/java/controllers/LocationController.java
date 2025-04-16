package controllers ;

import Dao.TerrainDao;
import com.google.protobuf.compiler.PluginProtos;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import Dao.LocationDAO;
import entities.Location;
import entities.Terrain;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class LocationController {

    @FXML
    private TableView<Location> locationTable;
    @FXML
    private TableColumn<Location, Number> colId;
    @FXML
    private TableColumn<Location, LocalDate> colDateDebut;
    @FXML
    private TableColumn<Location, LocalDate> colDateFin;
    @FXML
    private TableColumn<Location, Number> colPrix;
    @FXML
    private TableColumn<Location, String> colMode;
    @FXML
    private TableColumn<Location, Boolean> colPaiement;
    @FXML
    private TableColumn<Location, String> colStatut;

    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private TextField prixField;
    @FXML
    private TextField modePaiementField;
    @FXML
    private TextField statutField;
    @FXML
    private CheckBox paiementCheck;
    @FXML
    private ComboBox<Terrain> terrainCombo;
    @FXML
    private TextField localisationSearchField;

    @FXML
    private GridPane calendarGrid; // The GridPane for the calendar
    @FXML

    private final LocationDAO locationDAO = new LocationDAO();
    private final TerrainDao terrainDAO = new TerrainDao();
    private final ObservableList<Location> locations = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialisation des colonnes du TableView
        colDateDebut.setCellValueFactory(data -> data.getValue().dateDebutProperty());
        colDateFin.setCellValueFactory(data -> data.getValue().dateFinProperty());
        colPrix.setCellValueFactory(data -> data.getValue().prixLocationProperty());
        colMode.setCellValueFactory(data -> data.getValue().modePaiementProperty());
        colPaiement.setCellValueFactory(data -> data.getValue().paiementEffectueProperty());
        colStatut.setCellValueFactory(data -> data.getValue().statutProperty());

        // Charger les terrains dans le ComboBox
        terrainCombo.setItems(FXCollections.observableArrayList(terrainDAO.getAllTerrains()));

        // Charger les locations dans le TableView
        locations.addAll(locationDAO.getAll());
        locationTable.setItems(locations);

        // Populate the calendar with the current month
        populateCalendar(LocalDate.now());

        // Add the button action for contract view navigation
    }

    private void populateCalendar(LocalDate baseDate) {
        calendarGrid.getChildren().clear(); // Clear any previous content

        YearMonth yearMonth = YearMonth.from(baseDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday

        int row = 1; // Start row (0 can be reserved for weekday headers)
        int col = startDayOfWeek - 1;

        // Add weekday headers (Mon-Sun)
        for (int i = 0; i < 7; i++) {
            String dayName = java.time.DayOfWeek.of((i + 1) % 7 == 0 ? 7 : (i + 1))
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault());
            calendarGrid.add(new Label(dayName), i, 0);
        }

        // Add day cells with appropriate style
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = yearMonth.atDay(day);
            StackPane cell = new StackPane(new Label(String.valueOf(day)));
            cell.setStyle("-fx-border-color: #ccc; -fx-padding: 5px;");
            if (current.equals(LocalDate.now())) {
                cell.setStyle("-fx-border-color: blue; -fx-padding: 5px;");
            }
            calendarGrid.add(cell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void ajouterLocation() {
        if (!validateForm()) return;

        Terrain selectedTerrain = terrainCombo.getValue();

        Location loc = new Location();
        loc.setTerrain(selectedTerrain);
        loc.setDateDebut(dateDebutPicker.getValue());
        loc.setDateFin(dateFinPicker.getValue());
        loc.setPrixLocation(Float.parseFloat(prixField.getText()));
        loc.setModePaiement(modePaiementField.getText());
        loc.setStatut(statutField.getText());
        loc.setPaiementEffectue(paiementCheck.isSelected());

        locationDAO.insert(loc);
        locations.add(loc);
    }

    private boolean validateForm() {
        if (terrainCombo.getValue() == null) {
            showErrorAlert("Erreur de sélection", "Veuillez sélectionner un terrain.");
            return false;
        }
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            showErrorAlert("Erreur de date", "Veuillez sélectionner les dates.");
            return false;
        }
        if (prixField.getText().isEmpty()) {
            showErrorAlert("Erreur de prix", "Veuillez entrer le prix.");
            return false;
        }
        return true;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void rechercherParLocalisation() {
        String loc = localisationSearchField.getText();
        if (loc == null || loc.isEmpty()) {
            showErrorAlert("Erreur", "Veuillez entrer une localisation.");
            return;
        }

        List<Terrain> terrains = terrainDAO.getTerrainByLocalisation(loc);
        if (terrains != null && !terrains.isEmpty()) {
            terrainCombo.setItems(FXCollections.observableArrayList(terrains));
            terrainCombo.getSelectionModel().selectFirst();
        } else {
            showErrorAlert("Introuvable", "Aucun terrain trouvé avec la localisation : " + loc);
        }
    }

    @FXML
    private void retourVersTerrain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Terrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
