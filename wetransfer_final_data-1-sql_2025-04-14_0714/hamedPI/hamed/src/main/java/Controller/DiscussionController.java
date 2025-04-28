package Controller;

import Models.Discussion;
import Services.DiscussionService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DiscussionController {
    private ListeDiscussionController mainController;
    private Discussion discussion ;

    public void setMainController(ListeDiscussionController mainController) {
        this.mainController = mainController;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }



    @FXML
    private Label titre;

    @FXML
    private Label desc;

    @FXML
    private Label badgeNotif;

    DiscussionService Ds = new DiscussionService();

    @FXML
    void OpenConv(ActionEvent event) {
        mainController.naviguation(2);
        mainController.setCurrentDiscussion(getDiscussion());
        mainController.loadMessages(getDiscussion().getId());
    }

    @FXML
    void DeleteConv(ActionEvent event) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette discussion ?");
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            Ds.deleteDiscussion(getDiscussion().getId());
            mainController.loadDiscussion();
        }
    }

    void setData(Discussion d){
        setDiscussion(d);
        titre.setText(d.getTitre());
        desc.setText(d.getDescription());
        // Animation de survol
        VBox root = (VBox) titre.getParent().getParent();
        root.setOnMouseEntered(e -> {
            root.setStyle("-fx-background-color: #f0f4fa; -fx-effect: dropshadow(gaussian, #4F8CFF, 12, 0.3, 0, 4); -fx-background-radius: 18px; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        root.setOnMouseExited(e -> {
            root.setStyle("");
        });
        // Badge de notification (exemple : 3 messages non lus)
        int nonLus = d.getNbNonLus() != null ? d.getNbNonLus() : 0; // à adapter selon ton modèle
        if (nonLus > 0) {
            badgeNotif.setText(String.valueOf(nonLus));
            badgeNotif.setVisible(true);
        } else {
            badgeNotif.setVisible(false);
        }
    }



}
