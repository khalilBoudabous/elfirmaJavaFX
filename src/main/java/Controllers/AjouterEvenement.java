package Controllers;

import entities.Evenement;
import entities.LieuEvenement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import services.EvenementService;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;

public class AjouterEvenement implements Initializable {

    @FXML
    private TextField tfTitre;
    @FXML
    private DatePicker tfDateFin;
    @FXML
    private TextField tfNombrePlaces;
    @FXML
    private TextField tfPrix;
    @FXML
    private TextArea tfDescription;
    @FXML
    private ComboBox<LieuEvenement> tfLieu;
    @FXML
    private DatePicker tfDateDebut;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Populate the ComboBox with enum values
        tfLieu.getItems().setAll(LieuEvenement.values());
    }

    @FXML
    public void AjouterEvemenement(ActionEvent actionEvent) {
        // Validate all fields are filled
        if (tfTitre.getText().isEmpty() || tfDescription.getText().isEmpty() ||
                tfDateDebut.getValue() == null || tfDateFin.getValue() == null ||
                tfLieu.getValue() == null || tfNombrePlaces.getText().isEmpty() ||
                tfPrix.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Convert DatePicker values to java.sql.Date
            Date dateDebut = Date.valueOf(tfDateDebut.getValue());
            Date dateFin = Date.valueOf(tfDateFin.getValue());

            // Parse numerical values
            int nombrePlaces = Integer.parseInt(tfNombrePlaces.getText());
            float prix = Float.parseFloat(tfPrix.getText());

            // Create Evenement object
            Evenement evenement = new Evenement(
                    tfTitre.getText(),
                    tfDescription.getText(),
                    dateDebut,
                    dateFin,
                    tfLieu.getValue().name(),  // Convert enum to String
                    nombrePlaces,
                    prix
            );

            // Add to database
            EvenementService evenementService = new EvenementService();
            evenementService.ajouter(evenement);

            // Clear fields or show success message
            showAlert("Succée", "Evenement ajouté avec succés!");
            clearFields();

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Format de nombre invalide pour les lieux ou les prix.");
        } catch (Exception e) {
            showAlert("Error", "error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        tfTitre.clear();
        tfDescription.clear();
        tfDateDebut.setValue(null);
        tfDateFin.setValue(null);
        tfLieu.setValue(null);
        tfNombrePlaces.clear();
        tfPrix.clear();
    }
}