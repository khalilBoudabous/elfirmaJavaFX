package Controllers;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import services.EvenementService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class FrontOfficeEvenement {

    public static FrontOfficeEvenement instance; // new static instance

    @FXML
    private GridPane gridEvents;

    private final EvenementService evenementService = new EvenementService();

    @FXML
    public void initialize() {
        instance = this; // assign instance for later use
        loadEvents();    // load events initially
    }

    // Changed from private to public
    public void loadEvents() {
        try {
            List<Evenement> events = evenementService.recuperer();
            populateEventGrid(events);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load events: " + e.getMessage());
        }
    }

    // New static refresh method
    public static void refreshEvents() {
        if(instance != null) {
            instance.loadEvents();
        }
    }

    private void populateEventGrid(List<Evenement> events) {
        int column = 0;
        int row = 0;
        for (Evenement event : events) {
            AnchorPane eventCard = createEventCard(event);
            gridEvents.add(eventCard, column, row);
            column++;
            if (column == 2) { // 2 columns per row
                column = 0;
                row++;
            }
        }
    }

    private AnchorPane createEventCard(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventCard.fxml"));
            AnchorPane card = loader.load();
            EventCard controller = loader.getController();
            controller.setEventData(event);
            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return new AnchorPane();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
