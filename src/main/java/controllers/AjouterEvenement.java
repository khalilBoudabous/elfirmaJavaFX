package controllers;

import entities.Evenement;
import entities.LieuEvenement;
import entities.Ticket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.EvenementService;
import services.TicketService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AjouterEvenement implements Initializable {

    @FXML private TextField tfTitre;
    @FXML private DatePicker tfDateFin;
    @FXML private TextField tfNombrePlaces;
    @FXML private TextField tfPrix;
    @FXML private TextArea tfDescription;
    @FXML private ComboBox<LieuEvenement> tfLieu;
    @FXML private DatePicker tfDateDebut;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tfLieu.getItems().setAll(LieuEvenement.values());
    }

    @Deprecated
    public void AfficherEvenement(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/dbEvenement.fxml"));
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    //controle saisie !!

    private boolean validerChampsRequis() {
        if (tfTitre.getText().isEmpty() ||
                tfDescription.getText().isEmpty() ||
                tfDateDebut.getValue() == null ||
                tfDateFin.getValue() == null ||
                tfLieu.getValue() == null ||
                tfNombrePlaces.getText().isEmpty() ||
                tfPrix.getText().isEmpty()) {

            afficherAlerte("Erreur", "Veuillez remplir tous les champs requis.");
            return false;
        }
        return true;
    }

    private boolean validerDates() {
        LocalDate dateDebut = tfDateDebut.getValue();
        LocalDate dateFin = tfDateFin.getValue();

        if (dateDebut.isAfter(dateFin)) {
            afficherAlerte("Erreur", "La date de début doit être antérieure à la date de fin.");
            return false;
        }
        return true;
    }

    private int validerNombreEntier(String entree, String nomChamp) {
        int valeur = Integer.parseInt(entree);
        if (valeur < 0) {
            throw new IllegalArgumentException("Le " + nomChamp + " ne peut pas être négatif");
        }
        return valeur;
    }

    private float validerPrix(String entree) {
        float prix = Float.parseFloat(entree);
        if (prix < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        }
        return prix;
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Deprecated
    public void AjouterEvemenement(ActionEvent actionEvent) {
        if (!validerChampsRequis()) return;
        if (!validerDates()) return;

        EvenementService es = new EvenementService();
        TicketService ts = new TicketService();

        try {
            int nombrePlaces = validerNombreEntier(tfNombrePlaces.getText(), "nombre de places");
            float prix = validerPrix(tfPrix.getText());
            String titreEvenement = tfTitre.getText();

            Evenement evenement = new Evenement(
                    titreEvenement,
                    tfDescription.getText(),
                    Date.valueOf(tfDateDebut.getValue()),
                    Date.valueOf(tfDateFin.getValue()),
                    tfLieu.getValue().name(),
                    nombrePlaces,
                    prix
            );

            // Ajout de l'événement
            es.ajouter(evenement);

            // Vérification de l'ID généré
            if(evenement.getId() == 0) {
                throw new SQLException("Échec de la génération de l'ID de l'événement");
            }


            afficherAlerte("Succès", "Événement ajouté avec succès !");
            reinitialiserChamps();

            // Fermeture de la fenêtre
            Stage stage = (Stage) tfTitre.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            afficherAlerte("Erreur", "Format numérique invalide : " + e.getMessage());
        } catch (SQLException e) {
            afficherAlerte("Erreur SQL", "Erreur base de données : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reinitialiserChamps() {
        tfTitre.clear();
        tfDescription.clear();
        tfDateDebut.setValue(null);
        tfDateFin.setValue(null);
        tfLieu.setValue(null);
        tfNombrePlaces.clear();
        tfPrix.clear();
    }
}