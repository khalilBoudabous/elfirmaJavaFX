package Controller;

import Models.Message;
import Services.MessageService;
import Services.UserService;
import Services.CloudinaryService;
import Services.TranslationService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;

public class MessageController {

    private ListeDiscussionController mainController;
    private Message message;
    private int likeCount = 0;
    private int dislikeCount = 0;
    private boolean hasLiked = false;
    private boolean hasDisliked = false;

    @FXML
    private ImageView messageImage;

    @FXML
    private Button likeButton;

    @FXML
    private Button dislikeButton;

    @FXML
    private Button uploadButton;

    @FXML
    private Button translateButton;

    @FXML
    private Label avatar;

    @FXML
    private Label heure;

    public void setMainController(ListeDiscussionController mainController) {
        this.mainController = mainController;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @FXML
    private Label msg;

    @FXML
    private Label username;

    UserService Us = new UserService();
    MessageService Ms = new MessageService();

    @FXML
    void uploadImage(ActionEvent event) {
        try {
            if (message == null) {
                System.err.println("Erreur : Aucun message sélectionné");
                // TODO: Afficher une alerte à l'utilisateur
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            Stage stage = (Stage) uploadButton.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);
            
            if (selectedFile != null) {
                try {
                    System.out.println("Début de l'upload de l'image : " + selectedFile.getAbsolutePath());
                    String imageUrl = CloudinaryService.uploadImage(selectedFile);
                    
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        System.out.println("Image uploadée avec succès : " + imageUrl);
                        message.setImage(imageUrl);
                        Ms.updateMessage(message);
                        displayImage(imageUrl);
                        System.out.println("Message mis à jour avec la nouvelle image");
                    } else {
                        System.err.println("Erreur : URL de l'image vide après upload");
                    }
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'upload de l'image : " + e.getMessage());
                    e.printStackTrace();
                    // TODO: Afficher une alerte à l'utilisateur
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
            // TODO: Afficher une alerte à l'utilisateur
        }
    }

    private void displayImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                System.out.println("Tentative d'affichage de l'image : " + imageUrl);
                Image image = new Image(imageUrl, true); // true pour le chargement en arrière-plan
                messageImage.setImage(image);
                messageImage.setFitWidth(200);
                messageImage.setFitHeight(150);
                messageImage.setPreserveRatio(true);
                messageImage.setSmooth(true);
                messageImage.setVisible(true);
                System.out.println("Image affichée avec succès");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'affichage de l'image : " + e.getMessage());
                e.printStackTrace();
                messageImage.setVisible(false);
            }
        } else {
            System.out.println("Aucune image à afficher (URL null ou vide)");
            messageImage.setVisible(false);
        }
    }

    @FXML
    void handleLike(ActionEvent event) {
        if (!hasLiked && !hasDisliked) {
            likeCount++;
            hasLiked = true;
            updateLikeButton();
            updateDislikeButton();
        } else if (hasLiked) {
            likeCount--;
            hasLiked = false;
            updateLikeButton();
        } else if (hasDisliked) {
            dislikeCount--;
            likeCount++;
            hasDisliked = false;
            hasLiked = true;
            updateLikeButton();
            updateDislikeButton();
        }
    }

    @FXML
    void handleDislike(ActionEvent event) {
        if (!hasDisliked && !hasLiked) {
            dislikeCount++;
            hasDisliked = true;
            updateDislikeButton();
            updateLikeButton();
        } else if (hasDisliked) {
            dislikeCount--;
            hasDisliked = false;
            updateDislikeButton();
        } else if (hasLiked) {
            likeCount--;
            dislikeCount++;
            hasLiked = false;
            hasDisliked = true;
            updateLikeButton();
            updateDislikeButton();
        }
    }

    private void updateLikeButton() {
        likeButton.setText("👍 " + likeCount);
        if (hasLiked) {
            likeButton.setStyle("-fx-background-color: #45a049;");
        } else {
            likeButton.setStyle("-fx-background-color: #4CAF50;");
        }
    }

    private void updateDislikeButton() {
        dislikeButton.setText("👎 " + dislikeCount);
        if (hasDisliked) {
            dislikeButton.setStyle("-fx-background-color: #da190b;");
        } else {
            dislikeButton.setStyle("-fx-background-color: #f44336;");
        }
    }

    @FXML
    void modifmsg(ActionEvent event) {
        mainController.fillInput(getMessage().getContenu());
        Ms.deleteMessage(getMessage().getId());
    }

    @FXML
    void suppmsg(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce message ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                Ms.deleteMessage(getMessage().getId());
                mainController.loadMessages(getMessage().getDiscussionId());
                System.out.println("Message supprimé avec succès");
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression du message : " + e.getMessage());
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText(null);
                error.setContentText("Une erreur est survenue lors de la suppression du message.");
                error.showAndWait();
            }
        }
    }

    @FXML
    void translateMessage(ActionEvent event) {
        if (message != null && message.getContenu() != null && !message.getContenu().isEmpty()) {
            try {
                String translatedText = TranslationService.translateText(message.getContenu(), "fr");
                msg.setText(translatedText);
                System.out.println("Message traduit avec succès");
            } catch (Exception e) {
                System.err.println("Erreur lors de la traduction : " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de traduction");
                alert.setHeaderText(null);
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    void reportMsg(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Signaler un message");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment signaler ce message comme inapproprié ?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            // Ici tu peux ajouter la logique d'envoi du signalement (BDD, mail, etc.)
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Signalement envoyé");
            info.setHeaderText(null);
            info.setContentText("Merci, le message a été signalé à la modération.");
            info.showAndWait();
        }
    }

    void setData(Message m) {
        setMessage(m);
        String nom = Us.getUserById(m.getEmetteurId()).getNom();
        username.setText(nom);
        msg.setText(m.getContenu());
        updateLikeButton();
        updateDislikeButton();
        displayImage(m.getImage());
        // Affichage de l'heure réelle
        if (heure != null && m.getDateEnvoi() != null) {
            heure.setText(m.getDateEnvoi().toLocalTime().toString().substring(0,5));
        }
        // Avatar : initiale
        if (avatar != null && nom != null && !nom.isEmpty()) {
            avatar.setText(nom.substring(0,1).toUpperCase());
        }
        // Style façon chat
        int currentUserId = 1; // à remplacer plus tard par l'utilisateur connecté
        HBox root = (HBox) avatar.getParent();
        VBox bubble = (VBox) msg.getParent();
        if (m.getEmetteurId() == currentUserId) {
            // Moi : aligné à droite, bulle bleue
            avatar.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            bubble.setStyle("-fx-background-color: #e3f2fd; -fx-background-radius: 18px; -fx-alignment: TOP_RIGHT;");
            root.setAlignment(javafx.geometry.Pos.TOP_RIGHT);
        } else {
            // Les autres : aligné à gauche, bulle grise
            avatar.setStyle("-fx-background-color: #b3c6ff; -fx-text-fill: #333;");
            bubble.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 18px; -fx-alignment: TOP_LEFT;");
            root.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        }
        // Forcer la visibilité et la gestion des boutons
        if (likeButton != null) { likeButton.setVisible(true); likeButton.setManaged(true); }
        if (dislikeButton != null) { dislikeButton.setVisible(true); dislikeButton.setManaged(true); }
        if (uploadButton != null) { uploadButton.setVisible(true); uploadButton.setManaged(true); }
        if (translateButton != null) { translateButton.setVisible(true); translateButton.setManaged(true); }
        // Pour les boutons supp et modif si besoin
        Button suppBtn = null;
        Button modifBtn = null;
        for (javafx.scene.Node node : bubble.getChildren()) {
            if (node instanceof HBox) {
                for (javafx.scene.Node btn : ((HBox)node).getChildren()) {
                    if (btn instanceof Button) {
                        Button b = (Button) btn;
                        if (b.getStyleClass().contains("button-suppr")) { b.setVisible(true); b.setManaged(true); suppBtn = b; }
                        if (b.getStyleClass().contains("button-modif")) { b.setVisible(true); b.setManaged(true); modifBtn = b; }
                    }
                }
            }
        }
        // Animation de survol
        root.setOnMouseEntered(e -> {
            root.setStyle("-fx-background-color: #f0f4fa; -fx-effect: dropshadow(gaussian, #4F8CFF, 12, 0.3, 0, 4); -fx-background-radius: 18px; -fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        root.setOnMouseExited(e -> {
            root.setStyle("");
        });
    }

}
