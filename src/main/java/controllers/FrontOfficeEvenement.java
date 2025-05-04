package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.scene.control.agenda.Agenda;
import jfxtras.scene.control.agenda.Agenda.AppointmentGroupImpl;
import entities.Ticket;
import entities.Evenement;
import services.EvenementService;
import services.TicketService;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import entities.Utilisateur;
import services.UtilisateurService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class FrontOfficeEvenement {

    public static FrontOfficeEvenement instance;

    @FXML
    private GridPane gridEvents;
    @FXML
    private ListView<Ticket> listTickets;
    @FXML
    private Agenda calendarAgenda;
    @FXML
    private Label lblCurrentMonth;
    @FXML
    private Button btnPreviousMonth;
    @FXML
    private Button btnNextMonth;
    @FXML
    private BorderPane productPane; // Add a placeholder for the product view

    private LocalDate currentMonth;

    private final EvenementService evenementService = new EvenementService();
    private final TicketService ticketService = new TicketService();
    private List<Evenement> eventsCache;
    private Utilisateur loggedInUser;

    @FXML
    public void initialize() {
        instance = this;
        currentMonth = LocalDate.now();
        updateCalendarView();
        setupCalendar();
        // Ne pas charger les tickets ici
    }

    public void initData(long userId) {
        UtilisateurService service = new UtilisateurService();
        try {
            loggedInUser = service.getUtilisateurById(userId);
            loadEvents(); // OK après login
            loadTickets(); // Charger ici après avoir défini loggedInUser
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de récupérer l'utilisateur.");
        }
    }

    private void updateCalendarView() {
        // Update the displayed month label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        lblCurrentMonth.setText(currentMonth.format(formatter));

        // Set the Agenda's displayed range to the current month
        calendarAgenda.setDisplayedLocalDateTime(currentMonth.atStartOfDay());
    }

    @FXML
    private void handlePreviousMonth() {
        currentMonth = currentMonth.minusMonths(1); // Move to the previous month
        updateCalendarView();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1); // Move to the next month
        updateCalendarView();
    }

    public void loadEvents() {
        try {
            List<Evenement> events = evenementService.recuperer();
            eventsCache = events;
            populateEventGrid(events);
        } catch (SQLException e) {
            showAlert("Erreur", "Failed to load events: " + e.getMessage());
        }
    }

    private Evenement getEventById(int eventId) {
        if (eventsCache == null) return null;
        return eventsCache.stream()
                .filter(e -> e.getId() == eventId)
                .findFirst()
                .orElse(null);
    }

    public Long getCurrentUserId() {
        if (loggedInUser != null) {
            return loggedInUser.getId();
        } else {
            showAlert("Erreur", "Utilisateur non connecté.");
            return null;
        }
    }

    public void loadTickets() {
        Long userId = getCurrentUserId();
        if (userId == null) return;

        try {
            List<Ticket> allTickets = ticketService.recuperer();
            List<Ticket> userTickets = new ArrayList<>();

            // Filter tickets for the logged-in user
            for (Ticket ticket : allTickets) {
                if (ticket.getUtilisateur().getId() == userId) {
                    userTickets.add(ticket);
                }
            }

            listTickets.setItems(FXCollections.observableArrayList(userTickets));
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
                        // Label for ticket information
                        Label info = new Label("Event: " + ticket.getTitreEvenement() +
                                " | " + dates + " | Prix: " + ticket.getPrix() + " | Etat de payement: " + ticket.getPayée());
                        info.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;"); // Inline style for label

                        // Buttons
                        Button btnCancel = new Button("Annuler");
                        btnCancel.setStyle("-fx-pref-width: 80px;"); // Fixed width
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
                        btnPayer.setStyle("-fx-pref-width: 80px;"); // Fixed width
                        btnPayer.setOnAction(e -> {
                            if (ticket.getPayée()) {
                                new Alert(Alert.AlertType.INFORMATION, "Ce ticket est déjà payé.").showAndWait();
                                return;
                            }
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PaiementForm.fxml"));
                                Parent paymentRoot = loader.load();
                                PaymentController controller = loader.getController();
                                controller.setTicket(ticket);
                                Stage stage = new Stage();
                                stage.setTitle("Paiement");
                                stage.setScene(new Scene(paymentRoot));
                                stage.showAndWait();
                                if (controller.isPaymentSuccess()) {
                                    ticket.setPayée(true);
                                    ticketService.modifier(ticket);
                                    new Alert(Alert.AlertType.INFORMATION, "Paiement effectué via Stripe.").showAndWait();
                                    loadTickets();
                                }
                            } catch (Exception ex) {
                                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
                            }
                        });

                        Button btnViewDetails = new Button("View Details");
                        btnViewDetails.setStyle("-fx-pref-width: 80px;"); // Fixed width
                        btnViewDetails.setOnAction(e -> {
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/TicketDetails.fxml"));
                                Parent pane = loader.load();
                                TicketDetailsController controller = loader.getController();
                                controller.setTicketData(ticket, event);
                                Stage stage = new Stage();
                                stage.setTitle("Ticket Details");
                                stage.setMaximized(true);
                                stage.setScene(new Scene(pane));
                                stage.show();
                            } catch (IOException ex) {
                                new Alert(Alert.AlertType.ERROR, "Erreur: " + ex.getMessage()).showAndWait();
                            }
                        });

                        // Button container with fixed width
                        HBox buttonContainer = new HBox(10, btnCancel, btnPayer, btnViewDetails);
                        buttonContainer.setStyle("-fx-pref-width: 270px; -fx-alignment: center-right;"); // Fixed width and right alignment

                        // Main container for label and buttons
                        HBox container = new HBox(20, info, buttonContainer);
                        container.setStyle("-fx-padding: 10px; -fx-alignment: center-left;"); // Padding and alignment
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
        if (eventsCache == null) return;
        try {
            for (Evenement event : eventsCache) {
                String translatedTitre = translateText(event.getTitre(), targetLang);
                String translatedLieu = translateText(event.getLieu(), targetLang);
                event.setTitre(translatedTitre);
                event.setLieu(translatedLieu);
            }
            gridEvents.getChildren().clear();
            populateEventGrid(eventsCache);
        } catch (Exception e) {
            showAlert("Erreur", "La traduction des événements a échoué: " + e.getMessage());
        }
    }

    private String translateText(String text, String targetLang) throws Exception {
        String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=fr&tl="
                + targetLang + "&dt=t&q=" + URLEncoder.encode(text, "UTF-8");
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        String resp = response.toString();
        int start = resp.indexOf("\"");
        int end = resp.indexOf("\"", start + 1);
        if (start >= 0 && end > start) {
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

    private void setupCalendar() {
        calendarAgenda.setAllowDragging(false);
        calendarAgenda.setAllowResize(false);

        // Define appointment groups for styling
        AppointmentGroupImpl upcomingGroup = new AppointmentGroupImpl();
        upcomingGroup.setStyleClass("group0"); // Green for upcoming events
        AppointmentGroupImpl pastGroup = new AppointmentGroupImpl();
        pastGroup.setStyleClass("group1"); // Grey for past events

        if (eventsCache != null) {
            for (Evenement event : eventsCache) {
                LocalDateTime start = convertToLocalDateTime(event.getDateDebut());
                LocalDateTime end = convertToLocalDateTime(event.getDateFin());
                calendarAgenda.appointments().add(
                    new Agenda.AppointmentImplLocal()
                        .withStartLocalDateTime(start)
                        .withEndLocalDateTime(end)
                        .withSummary(event.getTitre())
                        .withDescription(event.getLieu())
                        .withAppointmentGroup(
                            start.isBefore(LocalDateTime.now()) ? pastGroup : upcomingGroup
                        )
                );
            }
        }
    }

    private LocalDateTime convertToLocalDateTime(Date date) {
        // Convert java.sql.Date to java.util.Date if necessary
        java.util.Date utilDate = (date instanceof java.sql.Date) ? new java.util.Date(date.getTime()) : date;
        return utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


}