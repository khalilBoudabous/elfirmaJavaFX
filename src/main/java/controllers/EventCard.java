package controllers;

import entities.Evenement;
import entities.Ticket;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.EvenementService;
import services.TicketService;
import services.UtilisateurService;

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
	private UtilisateurService utilisateurService = new UtilisateurService();


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
			//stage.setMaximized(true);
			stage.show();

		} catch (IOException e) {
			showAlert("Erreur", "Impossible d'afficher les détails");
		}
	}

	@FXML
	private void handleParticipation() {
		if (event.getNombrePlaces() <= 0) {
			showAlert("Erreur", "Aucune place disponible!");
			return;
		}
		try {
			TicketService ts = new TicketService();
			EvenementService es = new EvenementService();

			Ticket ticket = new Ticket();
			ticket.setId_evenement(event.getId());
			ticket.setPrix(event.getPrix());
			ticket.setTitreEvenement(event.getTitre());
			ticket.setPayée(false);

			Long userId = controllers.FrontOfficeEvenement.instance.getCurrentUserId();
			if (userId == null) return;

			ticket.setUtilisateur(utilisateurService.getUtilisateurById(userId)); // Set Utilisateur

			ts.ajouter(ticket);

			event.setNombrePlaces(event.getNombrePlaces() - 1);
			es.modifier(event);

			txtPlaces.setText("Places restantes: " + event.getNombrePlaces());

			// Refresh the event grid in FrontOfficeEvenement
            controllers.FrontOfficeEvenement.refreshEvents();
            controllers.FrontOfficeEvenement.instance.loadTickets();

			showAlert("Succès", "Participation enregistrée!");
		} catch (Exception e) {
			showAlert("Erreur", "Erreur lors de la participation: " + e.getMessage());
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
