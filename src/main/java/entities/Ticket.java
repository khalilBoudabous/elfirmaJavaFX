package entities;

public class Ticket {
    private int id, id_evenement;
    private String titreEvenement;
    private float prix;
    private Boolean payée = false;
    
    // New user id field
    private int userId;
    private String nomUser;
    private String emailUser;
    private Utilisateur utilisateur; // Add user entity reference

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
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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
        return "ticket{" +
                "id=" + id +
                ", id_evenement=" + id_evenement +
                ", prix=" + prix +
                ", payée=" + payée +
                ", titreEvenement='" + titreEvenement + '\'' +
                ", userId=" + userId +
                '}';
    }
}