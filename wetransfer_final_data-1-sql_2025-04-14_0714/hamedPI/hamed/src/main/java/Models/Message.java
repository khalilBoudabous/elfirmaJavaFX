package Models;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private int emetteurId;
    private int discussionId;
    private String contenu;
    private LocalDateTime dateEnvoi;
    private String image;

    public Message(int id, int emetteurId, int discussionId, String contenu, LocalDateTime dateEnvoi, String image) {
        this.id = id;
        this.emetteurId = emetteurId;
        this.discussionId = discussionId;
        this.contenu = contenu;
        this.dateEnvoi = dateEnvoi;
        this.image = image;
    }

    public Message( int emetteurId, int discussionId, String contenu, String image) {
        this.emetteurId = emetteurId;
        this.discussionId = discussionId;
        this.contenu = contenu;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmetteurId() {
        return emetteurId;
    }

    public void setEmetteurId(int emetteurId) {
        this.emetteurId = emetteurId;
    }

    public int getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(int discussionId) {
        this.discussionId = discussionId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", emetteurId=" + emetteurId +
                ", discussionId=" + discussionId +
                ", contenu='" + contenu + '\'' +
                ", dateEnvoi=" + dateEnvoi +
                ", image='" + image + '\'' +
                '}';
    }
}
