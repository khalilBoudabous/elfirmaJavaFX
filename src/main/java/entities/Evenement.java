package entities;

import java.sql.Date;

public class Evenement {

    private int id, nombrePlaces;
    private String titre, description;
    private Date dateDebut, dateFin;
    private float prix;
    private String lieu;

    public Evenement(String titre, String description, Date dateDebut, Date dateFin, String lieu,int nombrePlaces, float prix) {
        this.nombrePlaces = nombrePlaces;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix = prix;
        this.lieu = lieu;
    }

    public Evenement(int id, String titre, String description, Date dateDebut, Date dateFin, String lieu,int nombrePlaces, float prix) {
        this.id = id;
        this.nombrePlaces = nombrePlaces;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.prix = prix;
        this.lieu = lieu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(int nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", nombrePlaces=" + nombrePlaces +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", prix=" + prix +
                ", lieu='" + lieu + '\'' +
                '}';
    }
}
