package Controllers;

import entities.Evenement;
import entities.Ticket;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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

    @FXML
    private void handleTranslateEn() {
        translateInterface("en");
    }

    @FXML
    private void handleTranslateAr() {
        translateInterface("ar");
    }
    
    private void translateInterface(String targetLang) {
        try {
            String translatedTitre = translateText(txtTitre.getText(), targetLang);
            String translatedDescription = translateText(txtDescription.getText(), targetLang);
            String translatedDates = translateText(txtDates.getText(), targetLang);
            String translatedLieu = translateText(txtLieu.getText(), targetLang);
            String translatedInfos = translateText(txtInfos.getText(), targetLang);
            txtTitre.setText(translatedTitre);
            txtDescription.setText(translatedDescription);
            txtDates.setText(translatedDates);
            txtLieu.setText(translatedLieu);
            txtInfos.setText(translatedInfos);
        } catch (Exception e) {
            showAlert("Erreur", "La traduction a échoué: " + e.getMessage());
        }
    }

    private String translateText(String text, String targetLang) throws Exception {
        // Use the free Google Translate API endpoint.
        String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=fr&tl=" 
                        + targetLang + "&dt=t&q=" + URLEncoder.encode(text, "UTF-8");
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        // The response is a JSON-like array. Extract the translated text.
        // This is a minimal parser; in production use a JSON parser.
        String resp = response.toString();
        // Assume response structure: [[["translated text", "original", ...], ...], ...]
        int start = resp.indexOf("\"");
        int end = resp.indexOf("\"", start + 1);
        if(start >= 0 && end > start) {
            return resp.substring(start + 1, end);
        }
        return text;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}