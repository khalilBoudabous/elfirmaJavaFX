package entities;

public class Ticket {
    private int id, id_evenement;
    private String titreEvenement;
    private float prix;
    private Boolean payée = false;
    
    // New user id field
    private long userId;
    private String nomUser;
    private String emailUser;
    private Utilisateur utilisateur; // Reference to the user who owns the ticket

    public Ticket(int id, int id_evenement, String titreEvenement, float prix, Boolean payée, int userId) {
        this.id = id;
        this.id_evenement = id_evenement;
        this.titreEvenement = titreEvenement;
        this.prix = prix;
        this.payée = payée;
        this.userId = userId;
    }

    public Ticket() {}

    public int getId_evenement() {
        return id_evenement;
    }

    public void setId_evenement(int id_evenement) {
        this.id_evenement = id_evenement;
    }

    public String getTitreEvenement() {
        return titreEvenement;
    }

    public void setTitreEvenement(String titreEvenement) {
        this.titreEvenement = titreEvenement;
    }

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
        this.payée = payé;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    // Getter and setter for userId
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getNomUser() {
        if (utilisateur != null) {
            return utilisateur.getNom() + " " + utilisateur.getPrenom();
        }
        return nomUser;
    }

    public void setNomUser(String nomUser) {
        this.nomUser = nomUser;
    }

    public String getEmailUser() {
        if (utilisateur != null) {
            return utilisateur.getEmail();
        }
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", id_evenement=" + id_evenement +
                ", titreEvenement='" + titreEvenement + '\'' +
                ", prix=" + prix +
                ", payée=" + payée +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getNom() + " " + utilisateur.getPrenom() : "N/A") +
                '}';
    }
}