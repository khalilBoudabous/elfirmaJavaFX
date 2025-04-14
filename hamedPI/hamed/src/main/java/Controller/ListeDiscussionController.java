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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

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

    DiscussionService Ds = new DiscussionService();
    MessageService Ms = new MessageService();

    private Discussion currentDiscussion;

    public Discussion getCurrentDiscussion() {
        return currentDiscussion;
    }

    public void setCurrentDiscussion(Discussion currentDiscussion) {
        this.currentDiscussion = currentDiscussion;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        naviguation(1);
    }

    void loadDiscussion(){
        List<Discussion> discussions = Ds.getAllDiscussions();
        System.out.println(discussions);
        DiscussionContainer.getChildren().clear();
        for (Discussion c : discussions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Discussion.fxml"));
                VBox categorieItem = loader.load();
                DiscussionController discussionController = loader.getController();
                discussionController.setData(c);
                discussionController.setMainController(this);
                DiscussionContainer.getChildren().add(categorieItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void loadMessages(int idConv){
        List<Message> messages = Ms.getMessagesByDiscussionId(idConv);
        System.out.println(messages);
        MessageContainer.getChildren().clear();
        for (Message c : messages) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Message.fxml"));
                VBox msgItem = loader.load();
                MessageController messageController = loader.getController();
                messageController.setData(c);
                messageController.setMainController(this);
                MessageContainer.getChildren().add(msgItem);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void naviguateToAdd(MouseEvent event) {
        naviguation(3);
    }
    @FXML
    void naviguateToConvList(MouseEvent event){
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
        if(!messageContent.getText().trim().isEmpty()){
            Message m = new Message(2,getCurrentDiscussion().getId(),messageContent.getText().trim(),"");
            Ms.createMessage(m);
            loadMessages(getCurrentDiscussion().getId());
            messageContent.setText("");
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

}
