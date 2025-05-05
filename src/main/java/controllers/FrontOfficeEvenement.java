package controllers;

import entities.Evenement;
import entities.Ticket;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.scene.control.agenda.Agenda;
import jfxtras.scene.control.agenda.Agenda.AppointmentGroupImpl;
import services.EvenementService;
import services.ProduitService;
import services.TicketService;
import services.UtilisateurService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FrontOfficeEvenement {

    private static final Logger logger = Logger.getLogger(FrontOfficeEvenement.class.getName());

    public static FrontOfficeEvenement instance;

    @FXML private GridPane gridEvents;
    @FXML private ListView<Ticket> listTickets;
    @FXML private Agenda calendarAgenda;
    @FXML private Label lblCurrentMonth;
    @FXML private Button btnPreviousMonth;
    @FXML private Button btnNextMonth;
    @FXML private Button btnTranslateEventsEn;
    @FXML private Button btnTranslateEventsAr;
    @FXML private TabPane tabPane;

    private LocalDate currentMonth;
    private final EvenementService evenementService = new EvenementService();
    private final TicketService ticketService = new TicketService();
    private final UtilisateurService utilisateurService = new UtilisateurService();
    private final ProduitService produitService = new ProduitService();
    private List<Evenement> eventsCache;
    private Utilisateur loggedInUser;
    private ProduitAcheteController produitAcheteController;

    @FXML
    public void initialize() {
        logger.info("Initializing FrontOfficeEvenement");
        instance = this;
        currentMonth = LocalDate.now();
        updateCalendarView();
        setupCalendar();
        initializePurchasedProductsTab();
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getText().equals("Produits Achetés")) {
                logger.info("Produits Achetés tab selected");
                if (produitAcheteController == null) {
                    logger.warning("ProduitAcheteController is null, attempting to reinitialize");
                    initializePurchasedProductsTab();
                }
                loadPurchasedProducts();
            }
        });
        tabPane.getTabs().addListener((javafx.collections.ListChangeListener<Tab>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    logger.info("New tabs added, rechecking for Produits Achetés");
                    initializePurchasedProductsTab();
                }
            }
        });
    }

    public void initData(long userId) {
        logger.info("initData called with userId: " + userId);
        try {
            loggedInUser = utilisateurService.getUtilisateurById(userId);
            if (loggedInUser == null) {
                logger.severe("Utilisateur non trouvé pour ID: " + userId);
                showAlert("Erreur", "Utilisateur non trouvé pour ID: " + userId);
                return;
            }
            logger.info("Logged-in user: " + loggedInUser.getNom() + " (ID: " + userId + ")");
            loadEvents();
            loadTickets();
            if (produitAcheteController != null) {
                produitAcheteController.setUserId(userId);
            } else {
                logger.warning("ProduitAcheteController is null during initData, attempting to reinitialize");
                initializePurchasedProductsTab();
                if (produitAcheteController != null) {
                    produitAcheteController.setUserId(userId);
                }
            }
        } catch (SQLException e) {
            logger.severe("SQLException in initData: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de récupérer l'utilisateur: " + e.getMessage());
        }
    }

    private void initializePurchasedProductsTab() {
        logger.info("Initializing Produits Achetés tab");
        if (tabPane == null) {
            logger.severe("TabPane is null");
            showAlert("Erreur", "TabPane non initialisé.");
            return;
        }
        Tab produitAcheteTab = tabPane.getTabs().stream()
                .filter(tab -> tab.getText() != null && tab.getText().equals("Produits Achetés"))
                .findFirst()
                .orElse(null);
        if (produitAcheteTab == null) {
            logger.warning("Tab 'Produits Achetés' not found in TabPane. Available tabs: " +
                    tabPane.getTabs().stream().map(Tab::getText).collect(Collectors.joining(", ")));
            return;
        }
        if (produitAcheteTab.getContent() == null || produitAcheteController == null) {
            try {
                logger.info("Attempting to load ProduitAchete.fxml from path: /ProduitAchete.fxml");
                URL fxmlUrl = getClass().getResource("/ProduitAchete.fxml");
                if (fxmlUrl == null) {
                    logger.severe("ProduitAchete.fxml not found in resources");
                    showAlert("Erreur", "Fichier ProduitAchete.fxml introuvable dans les ressources.");
                    return;
                }
                logger.info("FXML URL resolved: " + fxmlUrl.toString());
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent content = loader.load();
                produitAcheteController = loader.getController();
                if (produitAcheteController == null) {
                    logger.severe("ProduitAcheteController is null after loading FXML");
                    showAlert("Erreur", "Échec de l'initialisation du contrôleur ProduitAchete.");
                    return;
                }
                produitAcheteTab.setContent(content);
                logger.info("ProduitAcheteController initialized successfully");
                if (loggedInUser != null) {
                    produitAcheteController.setUserId(loggedInUser.getId());
                }
            } catch (IOException e) {
                logger.severe("Failed to load ProduitAchete.fxml: " + e.getMessage());
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger ProduitAchete.fxml: " + e.getMessage());
            }
        }
    }

    private void loadPurchasedProducts() {
        logger.info("Loading purchased products");
        if (loggedInUser == null) {
            logger.warning("No logged-in user");
            showAlert("Erreur", "Utilisateur non connecté.");
            return;
        }
        if (produitAcheteController == null) {
            logger.severe("ProduitAcheteController is null");
            showAlert("Erreur", "Contrôleur ProduitAchete non initialisé.");
            return;
        }
        logger.info("Calling setUserId for user: " + loggedInUser.getId());
        produitAcheteController.setUserId(loggedInUser.getId());
    }

    private void updateCalendarView() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        lblCurrentMonth.setText(currentMonth.format(formatter));
        calendarAgenda.setDisplayedLocalDateTime(currentMonth.atStartOfDay());
    }

    @FXML
    private void handlePreviousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendarView();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendarView();
    }

    public void loadEvents() {
        try {
            List<Evenement> events = evenementService.recuperer();
            eventsCache = events;
            populateEventGrid(events);
            setupCalendar();
        } catch (SQLException e) {
            logger.severe("Failed to load events: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
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
            throw new IllegalStateException("Utilisateur non connecté.");
        }
    }

    public void loadTickets() {
        Long userId = getCurrentUserId();
        if (userId == null) return;

        try {
            List<Ticket> allTickets = ticketService.recuperer();
            List<Ticket> userTickets = new ArrayList<>();

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
                        // Create labels for ticket information
                        Evenement event = getEventById(ticket.getId_evenement());
                        String dates = event != null ? "Du " + event.getDateDebut() + " au " + event.getDateFin() : "Dates inconnues";

                        Label titleLabel = new Label(ticket.getTitreEvenement());
                        titleLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                        Label dateLabel = new Label(dates);
                        dateLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-text-fill: #666666;");

                        Label priceLabel = new Label("Prix: " + ticket.getPrix());
                        priceLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-text-fill: #666666;");

                        Label paymentLabel = new Label("État: " + (ticket.getPayée() ? "Payé" : "Non payé"));
                        paymentLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-text-fill: " +
                                (ticket.getPayée() ? "#2ecc71" : "#e74c3c") + ";");

                        // Organize info in a VBox
                        VBox infoVBox = new VBox(5, titleLabel, dateLabel, priceLabel, paymentLabel);
                        infoVBox.setStyle("-fx-padding: 15px;");

                        // Create buttons with wider widths and modern styling
                        Button btnCancel = new Button("Annuler");
                        btnCancel.setStyle(
                                "-fx-pref-width: 100px; " +
                                        "-fx-background-color: #e74c3c; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-family: 'Segoe UI'; " +
                                        "-fx-font-size: 12px; " +
                                        "-fx-padding: 8px; " +
                                        "-fx-background-radius: 5px; " +
                                        "-fx-cursor: hand;"
                        );
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
                        btnPayer.setStyle(
                                "-fx-pref-width: 100px; " +
                                        "-fx-background-color: #3498db; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-family: 'Segoe UI'; " +
                                        "-fx-font-size: 12px; " +
                                        "-fx-padding: 8px; " +
                                        "-fx-background-radius: 5px; " +
                                        "-fx-cursor: hand;"
                        );
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

                        Button btnViewDetails = new Button("Détails");
                        btnViewDetails.setStyle(
                                "-fx-pref-width: 100px; " +
                                        "-fx-background-color: #2ecc71; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-family: 'Segoe UI'; " +
                                        "-fx-font-size: 12px; " +
                                        "-fx-padding: 8px; " +
                                        "-fx-background-radius: 5px; " +
                                        "-fx-cursor: hand;"
                        );
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

                        // Organize buttons in a VBox with spacing
                        VBox buttonVBox = new VBox(10, btnCancel, btnPayer, btnViewDetails);
                        buttonVBox.setStyle("-fx-padding: 15px; -fx-alignment: center;");

                        // Create main HBox with card-like styling
                        HBox container = new HBox(20, infoVBox, buttonVBox);
                        container.setStyle(
                                "-fx-background-color: #ffffff; " +
                                        "-fx-border-color: #dddddd; " +
                                        "-fx-border-radius: 10px; " +
                                        "-fx-background-radius: 10px; " +
                                        "-fx-padding: 15px; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
                        );

                        setGraphic(container);
                    }
                }
            });
            loadEvents();
        } catch (Exception ex) {
            logger.severe("Failed to load tickets: " + ex.getMessage());
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
            logger.severe("Failed to translate events: " + e.getMessage());
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
            instance.loadTickets(); // Reload tickets to reflect payment updates
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
            logger.severe("Failed to create event card: " + e.getMessage());
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

        AppointmentGroupImpl upcomingGroup = new AppointmentGroupImpl();
        upcomingGroup.setStyleClass("group0");
        AppointmentGroupImpl pastGroup = new AppointmentGroupImpl();
        pastGroup.setStyleClass("group1");

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
        java.util.Date utilDate = (date instanceof java.sql.Date) ? new java.util.Date(date.getTime()) : date;
        return utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}