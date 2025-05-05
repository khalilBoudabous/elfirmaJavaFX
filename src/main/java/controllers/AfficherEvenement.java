package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entities.Evenement;
import entities.Ticket;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.EvenementService;
import services.TicketService;
import services.UtilisateurService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherEvenement {

    @FXML private TableView<Evenement> tvEvenements;
    @FXML private TableColumn<Evenement, Void> colSupprimer;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, Void> colModifier;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, Float> colPrix;
    @FXML private TableColumn<Evenement, Integer> colNombrePlaces;


    private final EvenementService es = new EvenementService();
    @FXML
    private TableColumn<Ticket, String> tcTitre;
    @FXML
    private TableColumn<Ticket, Boolean> tcPaye;
    @FXML
    private TableColumn<Ticket, Float> tcPrix;
    @FXML
    private TableView<Ticket> tvTickets;
    @FXML private TableColumn<Ticket, String> tcNom;
    @FXML private TableColumn<Ticket, String> tcPrenom;
    @FXML private TableColumn<Ticket, String> tcEmail;

    private final TicketService ticketService = new TicketService();
    private final UtilisateurService utilisateurService = new UtilisateurService();

    private Utilisateur loggedInUser;

    public void initData(Utilisateur user) {
        this.loggedInUser = user; // Initialize the logged-in user
    }

    @FXML
    void initialize() {
        // Retrieve the logged-in user from LoginController
        loggedInUser = LoginController.getLoggedInUser();
        if (loggedInUser == null) {
            showAlert("Erreur", "Utilisateur non connecté.");
            return;
        }
        configureColumns();
        setupActionColumns();
        configureTicketColumns();
        loadData();
    }

    public Long getCurrentUserId() {
        if (loggedInUser == null) {
            showAlert("Erreur", "Utilisateur non connecté.");
            return null;
        }
        return loggedInUser.getId();
    }

    private void configureColumns() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colNombrePlaces.setCellValueFactory(new PropertyValueFactory<>("nombrePlaces"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));
    }

    private void setupActionColumns() {
        // Delete Button Column
        colSupprimer.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Supprimer");

            {
                btn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    Evenement eventToDelete = getTableView().getItems().get(getIndex());
                    try {
                        // Check if event can be deleted
                        boolean canDelete = canDeleteEvent(eventToDelete);
                        if (!canDelete) {
                            showAlert("Suppression impossible", "Impossible de supprimer cet événement :\nIl existe des tickets payés et l'événement n'est pas encore terminé.");
                            return;
                        }
                        es.supprimer(eventToDelete);
                        loadData();
                        showAlert("Succès", "Événement supprimé avec succès!");
                    } catch (SQLException e) {
                        showAlert("Erreur", "Échec de la suppression: " + e.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // Modify Button Column
        colModifier.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Modifier");

            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btn.setOnAction(event -> {
                    Evenement eventToModify = getTableView().getItems().get(getIndex());
                    openModificationForm(eventToModify);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void openModificationForm(Evenement event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvenement.fxml"));
            Parent root = loader.load();

            ModifierEvenement controller = loader.getController();
            controller.setEvenement(event);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'événement");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh data after modification
            loadData();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void loadData() {
        try {
            // Filter events by the logged-in Fournisseur
            ObservableList<Evenement> data = FXCollections.observableArrayList(
                es.recuperer().stream()
                    .filter(event -> event.getUtilisateur().getId() == loggedInUser.getId())
                    .toList()
            );
            tvEvenements.setItems(data);

            // Load tickets if the tab is active
            if (tvTickets != null && tvTickets.getScene() != null) {
                loadTicketsData();
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des données: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openAjoutForm() {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getResource("/AjouterEvenement.fxml"));
            Stage stage = new Stage();

            try {
                Parent root=fxmlLoader.load();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setTitle("Ajouter Evenement");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

            // Refresh data after modification
            loadData();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    @FXML
    public void AjouterEvenement(ActionEvent actionEvent) {
        openAjoutForm();
    }

    // Ajouter cette méthode
    private void configureTicketColumns() {
        tcTitre.setCellValueFactory(new PropertyValueFactory<>("titreEvenement"));
        tcPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        tcPaye.setCellValueFactory(new PropertyValueFactory<>("payée"));
        tcPaye.setCellFactory(column -> new TableCell<Ticket, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "payé" : "non payé");
                }
            }
        });
        tcNom.setCellValueFactory(cellData -> {
            Utilisateur utilisateur = cellData.getValue().getUtilisateur();
            return utilisateur != null ? new javafx.beans.property.SimpleStringProperty(utilisateur.getNom()) : null;
        });
        tcPrenom.setCellValueFactory(cellData -> {
            Utilisateur utilisateur = cellData.getValue().getUtilisateur();
            return utilisateur != null ? new javafx.beans.property.SimpleStringProperty(utilisateur.getPrenom()) : null;
        });
        tcEmail.setCellValueFactory(cellData -> {
            Utilisateur utilisateur = cellData.getValue().getUtilisateur();
            return utilisateur != null ? new javafx.beans.property.SimpleStringProperty(utilisateur.getEmail()) : null;
        });
    }

    private void loadTicketsData() {
        try {
            if (loggedInUser == null) {
                showAlert("Erreur", "Utilisateur non connecté.");
                return;
            }

            // Retrieve events created by the current user
            List<Evenement> userEvents = es.recuperer().stream()
                .filter(event -> event.getUtilisateur().getId() == loggedInUser.getId())
                .toList();

            // Retrieve tickets related to those events
            ObservableList<Ticket> ticketData = FXCollections.observableArrayList(
                ticketService.recuperer().stream()
                    .filter(ticket -> userEvents.stream()
                        .anyMatch(event -> event.getId() == ticket.getId_evenement()))
                    .toList()
            );

            tvTickets.setItems(ticketData);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des tickets : " + e.getMessage());
        }
    }


    @FXML
    public void AfficherTickets(Event event) {
        loadTicketsData();
    }

    /**
     * Returns true if the event can be deleted:
     * - All tickets are unpaid OR
     * - The event's end date is before today
     */
    private boolean canDeleteEvent(Evenement event) throws SQLException {
        // If event is finished, allow deletion
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate eventEnd = event.getDateFin().toLocalDate();
        if (eventEnd.isBefore(today)) {
            return true;
        }
        // Otherwise, check if all tickets are unpaid
        List<Ticket> tickets = ticketService.recuperer();
        for (Ticket t : tickets) {
            if (t.getId_evenement() == event.getId() && t.getPayée()) {
                return false; // Found a paid ticket
            }
        }
        return true;
    }
    private void openAjoutProduitForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterProduit.fxml"));
            Parent root = loader.load();

            Ajouterproduit controller = loader.getController();
            controller.initData(loggedInUser); // Pass the logged-in user

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter Produit");
            stage.setMaximized(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout de produit");
        }
    }

    public void AjouterProduit(ActionEvent actionEvent) {
        openAjoutProduitForm();
    }

    @FXML
    private void genererPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(tvEvenements.getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Liste des Tickets", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Create table
                PdfPTable table = new PdfPTable(5); // 5 columns (ID removed)
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Add table headers
                String[] headers = {"Titre Événement", "Prix", "Payé", "Nom Utilisateur", "Email Utilisateur"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // Add ticket data
                List<Ticket> tickets = tvTickets.getItems();
                for (Ticket ticket : tickets) {
                    table.addCell(ticket.getTitreEvenement());
                    table.addCell(String.format("%.2f", ticket.getPrix()));
                    table.addCell(ticket.getPayée() ? "Oui" : "Non");

                    Utilisateur utilisateur = ticket.getUtilisateur();
                    if (utilisateur != null) {
                        table.addCell(utilisateur.getNom());
                        table.addCell(utilisateur.getEmail());
                    } else {
                        table.addCell("N/A");
                        table.addCell("N/A");
                    }
                }

                document.add(table);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Généré");
                alert.setHeaderText(null);
                alert.setContentText("La liste des tickets a été exportée avec succès!");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur s'est produite lors de la génération du PDF.");
                alert.showAndWait();
            }
        }
    }
}