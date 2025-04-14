package Controller;

import Models.Discussion;
import Services.DiscussionService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

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

    DiscussionService Ds = new DiscussionService();

    @FXML
    void OpenConv(ActionEvent event) {
        mainController.naviguation(2);
        mainController.setCurrentDiscussion(getDiscussion());
        mainController.loadMessages(getDiscussion().getId());
    }

    @FXML
    void DeleteConv(ActionEvent event) {
        Ds.deleteDiscussion(getDiscussion().getId());
        mainController.loadDiscussion();
    }

    void setData(Discussion d){
        setDiscussion(d);
        titre.setText(d.getTitre());
        desc.setText(d.getDescription());
    }



}
