package controllers;



import entities.Utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;


public class ListeUtilisateursController {

    @FXML
    private TableView<Utilisateur> userTable;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        configureTableColumns();
        chargerUtilisateurs();

    }

    private void chargerUtilisateurs() {
        userTable.getItems().clear();
        userTable.getItems().addAll(utilisateurService.getAllUtilisateurs());
    }

    @FXML
    private void retourAjout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajouterUtilisateur.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configureTableColumns() {
        userTable.getColumns().forEach(column -> {
            if ("Action".equals(column.getText())) {
                TableColumn<Utilisateur, Void> actionCol = (TableColumn<Utilisateur, Void>) column;
                actionCol.setCellFactory(param -> new ButtonCell());
            }
        });
    }

    private class ButtonCell extends TableCell<Utilisateur, Void> {
        private final HBox hbox = new HBox(5);
        private final Button btnModifier = new Button("Modifier");
        private final Button btnSupprimer = new Button("Supprimer");

        public ButtonCell() {
            hbox.getChildren().addAll(btnModifier, btnSupprimer);

            btnModifier.setOnAction(event -> {
                Utilisateur user = getTableView().getItems().get(getIndex());
                ouvrirModification(user);
            });

            btnSupprimer.setOnAction(event -> {
                Utilisateur user = getTableView().getItems().get(getIndex());
                supprimerUtilisateur(user);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(hbox);
            }
        }
    }
    private void ouvrirModification(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUtilisateur.fxml"));
            Parent root = loader.load();

            ModifierUtilisateurController controller = loader.getController();
            controller.initData(user);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Utilisateur");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void supprimerUtilisateur(Utilisateur user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setContentText("Supprimer " + user.getNom() + " ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    utilisateurService.supprimer(user.getId());
                    userTable.getItems().remove(user);
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
                }
            }
        });
    }

}