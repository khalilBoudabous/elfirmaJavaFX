package Controllers;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class EventDetails {

    @FXML private Text txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private Text txtDates;
    @FXML private Text txtLieu;
    @FXML private Text txtInfos;
    @FXML private Button btnInscription;

    private Evenement event;

    public void setEventDetails(Evenement event) {
        this.event = event;
        txtTitre.setText(event.getTitre());
        txtDescription.setText(event.getDescription());
        txtDates.setText("Du " + event.getDateDebut().toLocalDate() + " au " + event.getDateFin().toLocalDate());
        txtLieu.setText(event.getLieu());
        txtInfos.setText("Prix: " + event.getPrix() + " € | Places disponibles: " + event.getNombrePlaces());
    }

    @FXML
    private void handleInscription() {
        // Logique d'inscription
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inscription");
        alert.setHeaderText(null);
        alert.setContentText("Inscription à l'événement: " + event.getTitre());
        alert.showAndWait();
    }
}