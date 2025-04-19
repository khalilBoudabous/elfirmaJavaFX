package Controllers;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class EventCard {

    @FXML private Text txtTitre;
    @FXML private Text txtLieu;
    @FXML private Text txtDateDebut;
    @FXML private Text txtDateFin;
    @FXML private Text txtPrix;
    @FXML private Text txtPlaces;
    @FXML private Button btnParticiper;
    @FXML private Button btnDetails;

    private Evenement event;

    public void setEventData(Evenement event) {
        this.event = event;
        txtTitre.setText(event.getTitre());
        txtLieu.setText("Lieu: " + event.getLieu());
        txtDateDebut.setText("Début: " + event.getDateDebut().toLocalDate());
        txtDateFin.setText("Fin: " + event.getDateFin().toLocalDate());
        txtPrix.setText("Prix: " + String.format("%.2f €", event.getPrix()));
        txtPlaces.setText("Places restantes: " + event.getNombrePlaces());
    }

    @FXML
    private void handleShowDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EventDetails.fxml"));
            Parent root = loader.load();

            EventDetails controller = loader.getController();
            controller.setEventDetails(event);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de l'événement");
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'afficher les détails");
        }
    }

    @FXML
    private void handleParticipation() {
        // Logique existante de participation
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
