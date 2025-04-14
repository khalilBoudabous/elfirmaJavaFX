package Controller;


import Models.Message;
import Services.MessageService;
import Services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MessageController {

    private ListeDiscussionController mainController;
    private Message message;

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
    void modifmsg(ActionEvent event) {
        mainController.fillInput(getMessage().getContenu());
        Ms.deleteMessage(getMessage().getId());
    }

    @FXML
    void suppmsg(ActionEvent event) {
        Ms.deleteMessage(getMessage().getId());
        mainController.loadMessages(getMessage().getDiscussionId());
    }

    void setData(Message m){
        setMessage(m);
        username.setText(Us.getUserById(m.getEmetteurId()).getNom());
        msg.setText(m.getContenu());
    }

}
