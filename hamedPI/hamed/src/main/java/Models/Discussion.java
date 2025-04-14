package Models;

import java.time.LocalDate;

public class Discussion {

    private int id;
    private int createurId;
    private String titre;
    private LocalDate dateCreation;
    private String description;
    private String colorCode;

    public Discussion(int id, int createurId, String titre, LocalDate dateCreation, String description, String colorCode) {
        this.id = id;
        this.createurId = createurId;
        this.titre = titre;
        this.dateCreation = dateCreation;
        this.description = description;
        this.colorCode = colorCode;
    }

    public Discussion(int createurId, String titre, String description, String colorCode) {
        this.createurId = createurId;
        this.titre = titre;
        this.description = description;
        this.colorCode = colorCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreateurId() {
        return createurId;
    }

    public void setCreateurId(int createurId) {
        this.createurId = createurId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    @Override
    public String toString() {
        return "Discussion{" +
                "id=" + id +
                ", createurId=" + createurId +
                ", titre='" + titre + '\'' +
                ", dateCreation=" + dateCreation +
                ", description='" + description + '\'' +
                ", colorCode='" + colorCode + '\'' +
                '}';
    }
}
