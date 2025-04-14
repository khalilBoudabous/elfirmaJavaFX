package Controllers;

import entities.Evenement;
import entities.Ticket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.EvenementService;
import services.TicketService;

import java.io.IOException;
import java.sql.SQLException;

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
    private TableColumn tcTitre;
    @FXML
    private TableColumn tcIdTicket;
    @FXML
    private TableColumn tcPaye;
    @FXML
    private TableView tvTickets;

    private final TicketService ticketService = new TicketService();
    @FXML
    private TableColumn tcPrix;

    @FXML
    void initialize() {
        configureColumns();
        setupActionColumns();
        configureTicketColumns();
        loadData();

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
            // Rafraîchir les événements
            ObservableList<Evenement> data = FXCollections.observableArrayList(es.recuperer());
            tvEvenements.setItems(data);

            // Rafraîchir les tickets si l'onglet est actif
            if(tvTickets != null && tvTickets.getScene() != null) {
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
        tcIdTicket.setCellValueFactory(new PropertyValueFactory<>("id"));
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
    }

    private void loadTicketsData() {
        try {
            ObservableList<Ticket> ticketData = FXCollections.observableArrayList(ticketService.recuperer());
            tvTickets.setItems(ticketData);
        } catch (SQLException e) {
            showAlert("Erreur", "Échec du chargement des tickets : " + e.getMessage());
        }
    }


    @FXML
    public void AfficherTickets(Event event) {
        loadTicketsData();
    }
}