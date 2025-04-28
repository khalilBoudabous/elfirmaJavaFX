package Controller;

import Models.Discussion;
import Models.Message;
import Services.DiscussionService;
import Services.MessageService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeDiscussionController implements Initializable {

    @FXML
    private TextArea DescriptionDiscussion;

    @FXML
    private ColorPicker couleurDiscussion;

    @FXML
    private VBox DiscussionList;

    @FXML
    private VBox MessageContainer;

    @FXML
    private TextField titreDiscussion;

    @FXML
    private VBox DiscussionContainer;

    @FXML
    private VBox AddConvForm;

    @FXML
    private VBox MessageList;

    @FXML
    private TextField messageContent;

    @FXML
    private Label erreurMsg;

    @FXML
    private TextField searchField;

    @FXML
    private TextField searchMessageField;

    @FXML
    private Button themeSwitchButton;

    @FXML
    private ColorPicker userColorPicker;

    @FXML
    private Label typingLabel;

    DiscussionService Ds = new DiscussionService();
    MessageService Ms = new MessageService();

    private Discussion currentDiscussion;
    private List<Discussion> allDiscussions;
    private List<Message> allMessages;

    // Liste des mots interdits
    private static final String[] MOTS_INTERDITS = {
        "pute", "fuck you", "go to hell", "putin", "fils de pute", "va faire foutre", "va faite foudre"
    };

    private boolean contientMotInterdit(String texte) {
        String lower = texte.toLowerCase();
        for (String mot : MOTS_INTERDITS) {
            if (lower.contains(mot)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDarkTheme = false;

    public Discussion getCurrentDiscussion() {
        return currentDiscussion;
    }

    public void setCurrentDiscussion(Discussion currentDiscussion) {
        this.currentDiscussion = currentDiscussion;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        naviguation(1);
        allDiscussions = Ds.getAllDiscussions();
        // Accessibilité : focus sur la zone d'envoi de message si elle existe
        if (messageContent != null) {
            messageContent.setOnKeyPressed(event -> {
                if (event.isControlDown() && event.getCode().toString().equals("ENTER")) {
                    addMsg(null); // Envoie le message
                    event.consume();
                }
            });
            messageContent.setOnKeyTyped(event -> {
                if (typingLabel != null) {
                    if (!messageContent.getText().trim().isEmpty() || !event.getCharacter().trim().isEmpty()) {
                        typingLabel.setText("Quelqu'un est en train d'écrire…");
                        if (!typingLabel.getStyleClass().contains("typing-label"))
                            typingLabel.getStyleClass().add("typing-label");
                        typingLabel.setVisible(true);
                        FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(350), typingLabel);
                        ft.setFromValue(0);
                        ft.setToValue(1);
                        ft.play();
                    } else {
                        if (typingLabel.isVisible()) {
                            FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(350), typingLabel);
                            ft.setFromValue(1);
                            ft.setToValue(0);
                            ft.setOnFinished(e -> typingLabel.setVisible(false));
                            ft.play();
                        }
                    }
                }
            });
        }
    }

    @FXML
    void handleSearch(KeyEvent event) {
        String searchText = searchField.getText().toLowerCase();
        DiscussionContainer.getChildren().clear();
        
        for (Discussion d : allDiscussions) {
            if (d.getTitre().toLowerCase().contains(searchText) || 
                d.getDescription().toLowerCase().contains(searchText)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Discussion.fxml"));
                    VBox discussionItem = loader.load();
                    DiscussionController discussionController = loader.getController();
                    discussionController.setData(d);
                    discussionController.setMainController(this);
                    DiscussionContainer.getChildren().add(discussionItem);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void loadDiscussion(){
        allDiscussions = Ds.getAllDiscussions();
        DiscussionContainer.getChildren().clear();
        for (Discussion d : allDiscussions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Discussion.fxml"));
                VBox discussionItem = loader.load();
                DiscussionController discussionController = loader.getController();
                discussionController.setData(d);
                discussionController.setMainController(this);
                DiscussionContainer.getChildren().add(discussionItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void loadMessages(int idConv){
        allMessages = Ms.getMessagesByDiscussionId(idConv);
        System.out.println(allMessages);
        MessageContainer.getChildren().clear();
        for (Message c : allMessages) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Message.fxml"));
                HBox msgItem = loader.load();
                MessageController messageController = loader.getController();
                messageController.setData(c);
                messageController.setMainController(this);
                // Animation d'apparition
                FadeTransition ft = new FadeTransition(Duration.millis(400), msgItem);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
                MessageContainer.getChildren().add(msgItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (searchMessageField != null) searchMessageField.setText("");
    }

    @FXML
    void handleMessageSearch(KeyEvent event) {
        String searchText = searchMessageField.getText().toLowerCase();
        MessageContainer.getChildren().clear();
        for (Message m : allMessages) {
            if (m.getContenu().toLowerCase().contains(searchText)) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Message.fxml"));
                    HBox msgItem = loader.load();
                    MessageController messageController = loader.getController();
                    messageController.setData(m);
                    messageController.setMainController(this);
                    MessageContainer.getChildren().add(msgItem);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    void naviguateToAdd(ActionEvent event) {
        naviguation(3);
    }
    @FXML
    void naviguateToConvList(ActionEvent event){
        naviguation(1);
    }

    void naviguation(int page){
        AddConvForm.setVisible(false);
        MessageList.setVisible(false);
        DiscussionList.setVisible(false);
        switch (page){
            case(1):
                loadDiscussion();
                DiscussionList.setVisible(true);
                break;
            case(2):
                MessageList.setVisible(true);
                break;
            case (3):
                AddConvForm.setVisible(true);
                break;
        }
    }

    boolean verfifAjout(){
        if(titreDiscussion.getText().trim().isEmpty()){
            erreurMsg.setText("Veuillez saisir le titre de la discussion");
            return false;
        }
        if(DescriptionDiscussion.getText().trim().isEmpty()){
            erreurMsg.setText("Veuillez saisir la description de la discussion");
            return false;
        }
        if(couleurDiscussion.getValue() == null){
            erreurMsg.setText("Veuillez choisir une couleur de discussion");
            return false;
        }
        erreurMsg.setText("");
        return true;
    }

    void fillInput(String text){
        messageContent.setText(text);
    }


    @FXML
    void addMsg(MouseEvent event) {
        String contenu = messageContent.getText().trim();
        if(!contenu.isEmpty()){
            if (contientMotInterdit(contenu)) {
                erreurMsg.setText("Message interdit : langage irrespectueux détecté !");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Message non autorisé");
                alert.setHeaderText(null);
                alert.setContentText("Vous ne pouvez pas envoyer ce message.");
                alert.showAndWait();
                return;
            }
            Message m = new Message(2,getCurrentDiscussion().getId(),contenu,"");
            Ms.createMessage(m);
            loadMessages(getCurrentDiscussion().getId());
            messageContent.setText("");
            erreurMsg.setText("");
            if (typingLabel != null && typingLabel.isVisible()) {
                FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(350), typingLabel);
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(e -> typingLabel.setVisible(false));
                ft.play();
            }
        }
    }

    @FXML
    void AjoutDiscussion(ActionEvent event) {
        if(verfifAjout()){
            String couleur = toHexadecimal(couleurDiscussion.getValue());
            Discussion d = new Discussion(1,titreDiscussion.getText(),DescriptionDiscussion.getText(),couleur);
            System.out.println(d);
            Ds.createDiscussion(d);
            naviguation(1);
        }
    }

    @FXML
    void AnnulerDiscussion(ActionEvent event) {
        titreDiscussion.setText("");
        DescriptionDiscussion.setText("");
        naviguation(1);
    }

    private String toHexadecimal(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    @FXML
    void switchTheme(ActionEvent event) {
        if (themeSwitchButton.getScene() == null) return;
        if (!isDarkTheme) {
            themeSwitchButton.getScene().getStylesheets().remove("/msgStyle.css");
            themeSwitchButton.getScene().getStylesheets().add("/msgStyle-dark.css");
            themeSwitchButton.setText("☀️");
            isDarkTheme = true;
        } else {
            themeSwitchButton.getScene().getStylesheets().remove("/msgStyle-dark.css");
            themeSwitchButton.getScene().getStylesheets().add("/msgStyle.css");
            themeSwitchButton.setText("🌙");
            isDarkTheme = false;
        }
    }

    @FXML
    void handleUserColorChange(ActionEvent event) {
        Color color = userColorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
            (int)(color.getRed()*255),
            (int)(color.getGreen()*255),
            (int)(color.getBlue()*255));
        // Appliquer la couleur à tous les boutons menu-btn
        for (javafx.scene.Node node : ((HBox)themeSwitchButton.getParent()).getChildrenUnmodifiable()) {
            if (node instanceof Button && node.getStyleClass().contains("menu-btn")) {
                node.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-background-radius: 30px; -fx-font-weight: bold;");
            }
        }
        // Appliquer aussi la couleur au bouton de thème si tu veux
        themeSwitchButton.setStyle("-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-background-radius: 18px;");
    }

    @FXML
    void setBold(ActionEvent event) {
        applyStyleToSelection("bold");
    }
    @FXML
    void setItalic(ActionEvent event) {
        applyStyleToSelection("italic");
    }
    @FXML
    void setUnderline(ActionEvent event) {
        applyStyleToSelection("underline");
    }
    private void applyStyleToSelection(String style) {
        int start = messageContent.getSelection().getStart();
        int end = messageContent.getSelection().getEnd();
        if (start == end) return;
        String text = messageContent.getText();
        String before = text.substring(0, start);
        String selected = text.substring(start, end);
        String after = text.substring(end);
        if (style.equals("bold")) {
            selected = "[b]" + selected + "[/b]";
        } else if (style.equals("italic")) {
            selected = "[i]" + selected + "[/i]";
        } else if (style.equals("underline")) {
            selected = "[u]" + selected + "[/u]";
        }
        messageContent.setText(before + selected + after);
        messageContent.positionCaret((before + selected).length());
    }

}
