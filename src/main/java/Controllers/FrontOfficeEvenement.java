package Controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import entities.Ticket;
import services.EvenementService;
import services.TicketService;
import entities.Evenement;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent; // Added import
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class FrontOfficeEvenement {

    public static FrontOfficeEvenement instance;

    @FXML
    private GridPane gridEvents;
    @FXML
    private ListView<Ticket> listTickets;

    private final EvenementService evenementService = new EvenementService();
    private final TicketService ticketService = new TicketService();
    private List<Evenement> eventsCache;

    @FXML
    public void initialize() {
        instance = this;
        loadEvents();
        loadTickets();
    }

    public void loadEvents() {
        try {
            List<Evenement> events = evenementService.recuperer();
            eventsCache = events;
            populateEventGrid(events);
        } catch (SQLException e) {
             showAlert("Error", "Failed to load events: " + e.getMessage());
        }
    }

    private Evenement getEventById(int eventId) {
        if (eventsCache == null) return null;
        return eventsCache.stream()
                .filter(e -> e.getId() == eventId)
                .findFirst()
                .orElse(null);
    }

    public void loadTickets() {
        try {
            listTickets.setItems(FXCollections.observableArrayList(ticketService.recuperer()));
            listTickets.setCellFactory(lv -> new ListCell<Ticket>() {
                @Override
                protected void updateItem(Ticket ticket, boolean empty) {
                    super.updateItem(ticket, empty);
                    if (empty || ticket == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        String dates;
                        Evenement event = getEventById(ticket.getId_evenement());
                        if (event != null) {
                            dates = "Du " + event.getDateDebut() + " au " + event.getDateFin();
                        } else {
                            dates = "Dates inconnues";
                        }
                        Label info = new Label("Event: " + ticket.getTitreEvenement() +
                                " | " + dates + " | Prix: " + ticket.getPrix() + " | Etat de payement: " + ticket.getPayée());
                        Button btnCancel = new Button("Cancel");
                        btnCancel.setOnAction(e -> {
                            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                                    "Voulez-vous vraiment annuler votre participation ?", ButtonType.YES, ButtonType.NO);
                            Optional<ButtonType> result = conf.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.YES) {
                                try {
                                    ticketService.supprimer(ticket);
                                    if (event != null) {
                                        event.setNombrePlaces(event.getNombrePlaces() + 1);
                                        evenementService.modifier(event);
                                    }
                                    loadTickets();
                                } catch (Exception ex) {
                                    new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
                                }
                            }
                        });
                        Button btnPayer = new Button("Payer");
                        btnPayer.setOnAction(e -> {
                            if (ticket.getPayée()) {
                                new Alert(Alert.AlertType.INFORMATION, "Ce ticket est déjà payé.").showAndWait();
                                return;
                            }
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaiementForm.fxml"));
                                Parent paymentRoot = loader.load();
                                PaymentController controller = loader.getController();
                                // Pass ticket data to the payment form
                                controller.setTicket(ticket);
                                Stage stage = new Stage();
                                stage.setTitle("Paiement");
                                stage.setScene(new Scene(paymentRoot));
                                stage.showAndWait();
                                // After form closes, check if payment succeeded
                                if(controller.isPaymentSuccess()){
                                    ticket.setPayée(true);
                                    ticketService.modifier(ticket);
                                    new Alert(Alert.AlertType.INFORMATION, "Paiement effectué via Stripe.").showAndWait();
                                    loadTickets();
                                }
                            } catch(Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
                            }
                        });
                        Button btnViewDetails = new Button("View Details");
                        btnViewDetails.setOnAction(e -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/TicketDetails.fxml"));
                                Parent pane = loader.load(); // Changed to Parent
                                TicketDetailsController controller = loader.getController();
                                controller.setTicketData(ticket, event);
                                Stage stage = new Stage();
                                stage.setTitle("Ticket Details");
                                stage.setScene(new Scene(pane));
                                stage.setMaximized(true);
                                stage.show();
                            } catch (IOException ex) {
                                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
                            }
                        });
                        HBox container = new HBox(10, info, btnCancel, btnPayer, btnViewDetails);
                        setGraphic(container);
                    }
                }
            });
            loadEvents();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement des tickets: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleTranslateEventsEn() {
        translateEvents("en");
    }

    @FXML
    private void handleTranslateEventsAr() {
        translateEvents("ar");
    }

    private void translateEvents(String targetLang) {
        if(eventsCache == null) return;
        try {
            for(Evenement event : eventsCache) {
                String translatedTitre = translateText(event.getTitre(), targetLang);
                String translatedLieu = translateText(event.getLieu(), targetLang);
                event.setTitre(translatedTitre);
                event.setLieu(translatedLieu);
                // Optionally, translate other fields as needed
            }
            gridEvents.getChildren().clear();
            populateEventGrid(eventsCache);
        } catch(Exception e) {
            showAlert("Erreur", "La traduction des événements a échoué: " + e.getMessage());
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
        while((line = reader.readLine()) != null){
            response.append(line);
        }
        reader.close();
        String resp = response.toString();
        int start = resp.indexOf("\"");
        int end = resp.indexOf("\"", start + 1);
        if(start >= 0 && end > start) {
            return resp.substring(start + 1, end);
        }
        return text;
    }

    public static void refreshEvents() {
        if (instance != null) {
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
            if (column == 2) {
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