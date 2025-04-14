package entities;


public class Ticket {
    private int id,id_evenement;
    private String titreEvenement;
    private float prix;
    private Boolean payée = false;

    public Ticket(int id, int id_evenement, String titreEvenement, float prix, Boolean payée) {
        this.id = id;
        this.id_evenement = id_evenement;
        this.titreEvenement = titreEvenement;
        this.prix = prix;
        this.payée = payée;
    }

    public Ticket() {}

    public int getId_evenement() {
        return id_evenement;
    }

    public void setId_evenement(int id_evenement) {
        this.id_evenement = id_evenement;
    }

    public String getTitreEvenement() { return titreEvenement;}

    public void setTitreEvenement(String titreEvenement) {this.titreEvenement = titreEvenement;}

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public Boolean getPayée() {
        return payée;
    }

    public void setPayée(Boolean payé) {
        this.payée = payée;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ticket{" +
                "id=" + id +
                ", id_evenement=" + id_evenement +
                ", prix=" + prix +
                ", payée=" + payée +
                ", titreEvenement='" + titreEvenement +
                '}';
    }
}