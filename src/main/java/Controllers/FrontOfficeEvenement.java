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
// For retrieving event dates
import entities.Evenement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FrontOfficeEvenement {

    public static FrontOfficeEvenement instance; // new static instance

    @FXML
    private GridPane gridEvents;
    @FXML private ListView<Ticket> listTickets;

    private final EvenementService evenementService = new EvenementService();
    private final TicketService ticketService = new TicketService();
    private List<Evenement> eventsCache; // new field to cache events

    @FXML
    public void initialize() {
        instance = this;
        loadEvents();
        loadTickets();
    }
    
    // Modified loadEvents method to cache events
    public void loadEvents() {
        try {
            List<Evenement> events = evenementService.recuperer();
            eventsCache = events; // cache events for lookup
            populateEventGrid(events);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load events: " + e.getMessage());
        }
    }
    
    // New helper method to retrieve event by id from cache
    private Evenement getEventById(int eventId) {
        if (eventsCache == null) return null;
        return eventsCache.stream()
                .filter(e -> e.getId() == eventId)
                .findFirst()
                .orElse(null);
    }
    
    // In loadTickets, update retrieval of event details using getEventById
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
                                " | " + dates + " | Prix: " + ticket.getPrix());
                        Button btnCancel = new Button("Cancel");
                        btnCancel.setOnAction(e -> {
                            Alert conf = new Alert(Alert.AlertType.CONFIRMATION, 
                                    "Voulez-vous vraiment annuler votre participation ?", ButtonType.YES, ButtonType.NO);
                            Optional<ButtonType> result = conf.showAndWait();
                            if(result.isPresent() && result.get() == ButtonType.YES) {
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
                        Button btnPayer = new Button("payer");
                        btnPayer.setOnAction(e -> {
                            if (ticket.getPayée()) {
                                new Alert(Alert.AlertType.INFORMATION, "Ce ticket est déjà payé.").showAndWait();
                                return;
                            }
                            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                                    "Voulez-vous procéder au paiement ?", ButtonType.YES, ButtonType.NO);
                            Optional<ButtonType> result = conf.showAndWait();
                            if(result.isPresent() && result.get() == ButtonType.YES) {
                                try {
                                    ticket.setPayée(true);
                                    ticketService.modifier(ticket);
                                    new Alert(Alert.AlertType.INFORMATION, "Paiement effectué.").showAndWait();
                                    loadTickets();
                                } catch (Exception ex) {
                                    new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
                                }
                            }
                        });
                        HBox container = new HBox(10, info, btnCancel, btnPayer);
                        setGraphic(container);
                    }
                }
            });
            loadEvents();
        } catch(Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement des tickets: " + ex.getMessage()).showAndWait();
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
