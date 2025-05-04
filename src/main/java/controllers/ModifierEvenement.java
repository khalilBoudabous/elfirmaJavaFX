package controllers;

import entities.Evenement;
import entities.LieuEvenement;
import entities.Ticket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import services.EvenementService;
import services.TicketService;
import services.UtilisateurService;
import java.sql.SQLException;

import java.sql.Date;

public class ModifierEvenement {
    @FXML private TextField tfTitre;
    @FXML private TextArea tfDescription;
    @FXML private DatePicker tfDateDebut;
    @FXML private DatePicker tfDateFin;
    @FXML private TextField tfNombrePlaces;
    @FXML private TextField tfPrix;


    private Evenement currentEvent;
    private final EvenementService es = new EvenementService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    @FXML
    private ComboBox tfLieu;

    public void setEvenement(Evenement event) {
        this.currentEvent = event;
        populateFields();
    }

    private void populateFields() {
        tfTitre.setText(currentEvent.getTitre());
        tfDescription.setText(currentEvent.getDescription());
        tfDateDebut.setValue(currentEvent.getDateDebut().toLocalDate());
        tfDateFin.setValue(currentEvent.getDateFin().toLocalDate());
        tfLieu.setValue(LieuEvenement.valueOf(currentEvent.getLieu()));
        tfNombrePlaces.setText(String.valueOf(currentEvent.getNombrePlaces()));
        tfPrix.setText(String.valueOf(currentEvent.getPrix()));
        try {
            currentEvent.setUtilisateur(utilisateurService.getUtilisateurById(currentEvent.getUtilisateur().getId()));
        } catch (SQLException e) {
            afficherAlerte("Erreur", "Échec de récupération de l'utilisateur : " + e.getMessage());
        }
    }

    @FXML
    void initialize() {
        tfLieu.getItems().setAll(LieuEvenement.values());
    }

    @FXML
    public void ModifierEvemenement(ActionEvent actionEvent){
        try {
            validateFields();

            // Sauvegarder anciennes valeurs
            int oldPlaces = currentEvent.getNombrePlaces();
            float oldPrice = currentEvent.getPrix();

            // Récupérer nouvelles valeurs
            int newPlaces = Integer.parseInt(tfNombrePlaces.getText());
            float newPrice = Float.parseFloat(tfPrix.getText());

            // Mettre à jour l'événement
            currentEvent.setTitre(tfTitre.getText());
            currentEvent.setDescription(tfDescription.getText());
            currentEvent.setDateDebut(Date.valueOf(tfDateDebut.getValue()));
            currentEvent.setDateFin(Date.valueOf(tfDateFin.getValue()));
            currentEvent.setLieu(tfLieu.getValue().toString());
            currentEvent.setNombrePlaces(newPlaces);
            currentEvent.setPrix(newPrice);

            // Gestion des tickets
            TicketService ticketService = new TicketService();

            // Mettre à jour le prix des tickets non payés et le titre de l'événement
            if (newPrice != oldPrice || !currentEvent.getTitre().equals(tfTitre.getText())) {
                ticketService.updateAllTicketsForEvent(currentEvent);
            }

            es.modifier(currentEvent);
            fermerFenetre();

        } catch (IllegalArgumentException e) {
            afficherAlerte("Erreur de validation", e.getMessage());
        } catch (Exception e) {
            afficherAlerte("Erreur", "Échec de la modification : " + e.getMessage());
        }
    }

    private void validateFields() {
        if (tfTitre.getText().isEmpty() ||
                tfDescription.getText().isEmpty() ||
                tfDateDebut.getValue() == null ||
                tfDateFin.getValue() == null ||
                tfLieu.getValue() == null ||
                tfNombrePlaces.getText().isEmpty() ||
                tfPrix.getText().isEmpty()) {

            throw new IllegalArgumentException("Tous les champs doivent être remplis");
        }

        if (tfDateDebut.getValue().isAfter(tfDateFin.getValue())) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }

        if (Integer.parseInt(tfNombrePlaces.getText()) < 0) {
            throw new IllegalArgumentException("Le nombre de places ne peut pas être négatif");
        }

        if (Float.parseFloat(tfPrix.getText()) < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        }
    }

    private void fermerFenetre() {
        tfTitre.getScene().getWindow().hide();
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(titre);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
    }
}