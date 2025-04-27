package controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.UtilisateurService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListeUtilisateursController {

    @FXML
    private PieChart rolePieChart;

    @FXML
    private TableView<Utilisateur> userTable;

    @FXML
    private TableColumn<Utilisateur, String> nomColumn;

    private final UtilisateurService utilisateurService = new UtilisateurService();

    @FXML
    public void initialize() {
        configureTableColumns();
        chargerUtilisateurs();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        //afficherStatistiquesRole();


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
        private final Button btnModifier = new Button("Modif");
        private final Button btnSupprimer = new Button("Suppr");
        private final Button btnBlock = new Button("Block");

        public ButtonCell() {
            hbox.getChildren().addAll(btnModifier, btnSupprimer, btnBlock);

            // Style buttons to be more compact
            btnModifier.setStyle("-fx-padding: 3 5 3 5; -fx-font-size: 12;");
            btnSupprimer.setStyle("-fx-padding: 3 5 3 5; -fx-font-size: 12;");
            btnBlock.setStyle("-fx-padding: 3 5 3 5; -fx-font-size: 12;");

            btnModifier.setOnAction(event -> {
                Utilisateur user = getTableView().getItems().get(getIndex());
                ouvrirModification(user);
            });

            btnSupprimer.setOnAction(event -> {
                Utilisateur user = getTableView().getItems().get(getIndex());
                supprimerUtilisateur(user);
            });
            btnBlock.setOnAction(event -> {
                Utilisateur user = getTableView().getItems().get(getIndex());
                blockUtilisateur(user);
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

    @FXML
    private void ajouterUtilisateur(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajouterUtilisateur.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void genererPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(userTable.getScene().getWindow());

        if (file != null) {
            try {
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Add title
                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                Paragraph title = new Paragraph("Liste des Utilisateurs", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Create table
                PdfPTable table = new PdfPTable(4); // 4 columns
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                // Add table headers
                String[] headers = {"Nom", "Prénom", "Email", "Rôle"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                // Add user data
                List<Utilisateur> users = userTable.getItems();
                for (Utilisateur user : users) {
                    table.addCell(user.getNom());
                    table.addCell(user.getPrenom());
                    table.addCell(user.getEmail());
                    table.addCell(user.getType());
                }

                document.add(table);
                document.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PDF Généré");
                alert.setHeaderText(null);
                alert.setContentText("La liste des utilisateurs a été exportée avec succès!");
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

    @FXML
    private void trierParNom(ActionEvent event) {
        // Clear existing sort order
        userTable.getSortOrder().clear();

        // Add the "nom" column to the sort order
        userTable.getSortOrder().add(nomColumn);

        // Set ascending sort type
        nomColumn.setSortType(TableColumn.SortType.ASCENDING);

        // Trigger the sort
        userTable.sort();
    }

    private void afficherStatistiquesRole() throws SQLException {
        List<Utilisateur> utilisateurs = userTable.getItems();

        int nbAgriculteurs = 0;
        int nbExperts = 0;
        int nbFournisseurs = 0;

        for (Utilisateur u : utilisateurs) {
            switch (u.getType().toLowerCase()) {
                case "agriculteur":
                    nbAgriculteurs++;
                    break;
                case "expert":
                    nbExperts++;
                    break;
                case "fournisseur":
                    nbFournisseurs++;
                    break;
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Agriculteurs", nbAgriculteurs),
                new PieChart.Data("Experts", nbExperts),
                new PieChart.Data("Fournisseurs", nbFournisseurs)
        );

        rolePieChart.setData(pieChartData);
        rolePieChart.setTitle("Répartition des rôles");
   }
    private void blockUtilisateur(Utilisateur user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setContentText(user.isBlocked() ? "Débloquer " + user.getNom() + "?" : "Bloquer " + user.getNom() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean newStatus = !user.isBlocked();
                    utilisateurService.blockUser(user.getId(), newStatus);
                    user.setBlocked(newStatus);
                    userTable.refresh(); // Refresh the table to update the status
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).show();
                }
            }
        });
    }
}

