package Controllers;

import entities.Evenement;
import entities.Ticket;
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

        // New helper method to supply a valid user id.
        private int getCurrentUserId() {
            // QUICK FIX: Return a valid user id that exists in your 'utilisateur' table.
            // Or implement logic to retrieve the actual logged in user's id.
            return 1; // Ensure a user with id = 1 exists in your database!
        }

    @FXML
    private void handleInscription() {
        if (event.getNombrePlaces() <= 0) {
            showAlert("Erreur", "Aucune place disponible!");
            return;
        }
        try {
            services.TicketService ts = new services.TicketService();
            services.EvenementService es = new services.EvenementService();

            Ticket ticket = new Ticket();
            ticket.setId_evenement(event.getId());
            ticket.setPrix(event.getPrix());
            ticket.setTitreEvenement(event.getTitre());
            ticket.setPayée(false);
            // Use a valid current user id instead of hardcoded value.
            ticket.setUserId(getCurrentUserId());

            ts.ajouter(ticket);

            event.setNombrePlaces(event.getNombrePlaces() - 1);
            es.modifier(event);

            // Optionally update UI info
            txtInfos.setText("Prix: " + event.getPrix() + " € | Places disponibles: " + event.getNombrePlaces());
            
            // Refresh the event grid in FrontOfficeEvenement
            Controllers.FrontOfficeEvenement.refreshEvents();
            Controllers.FrontOfficeEvenement.instance.loadTickets();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription");
            alert.setHeaderText(null);
            alert.setContentText("Participation enregistrée pour l'événement: " + event.getTitre());
            alert.showAndWait();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'inscription: " + e.getMessage());
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