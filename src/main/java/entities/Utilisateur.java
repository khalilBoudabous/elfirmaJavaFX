package entities;

import javafx.beans.property.*;
import java.util.List;

public abstract class Utilisateur {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty nom = new SimpleStringProperty();
    private StringProperty prenom = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();
    private IntegerProperty telephone = new SimpleIntegerProperty();
    private StringProperty role = new SimpleStringProperty();
    private StringProperty motDePasse = new SimpleStringProperty();
    private List<Terrain> terrains; // Changed from Admin.Terrain to Terrain (assuming Terrain is in entities)

    // Constructors
    public Utilisateur() {}

    public Utilisateur(int id, String nom, String prenom, String email, int telephone, String role, String motDePasse) {
        this.id.set(id);
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.email.set(email);
        this.telephone.set(telephone);
        this.role.set(role);
        this.motDePasse.set(motDePasse);
    }

    public Utilisateur(String nom, String prenom, String email, int telephone, String role, String motDePasse) {
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.email.set(email);
        this.telephone.set(telephone);
        this.role.set(role);
        this.motDePasse.set(motDePasse);
    }

    // Property Getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nomProperty() { return nom; }
    public StringProperty prenomProperty() { return prenom; }
    public StringProperty emailProperty() { return email; }
    public IntegerProperty telephoneProperty() { return telephone; }
    public StringProperty roleProperty() { return role; }
    public StringProperty motDePasseProperty() { return motDePasse; }

    // Standard Getters/Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }

    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }

    public int getTelephone() { return telephone.get(); }
    public void setTelephone(int telephone) { this.telephone.set(telephone); }

    public String getRole() { return role.get(); }
    public void setRole(String role) { this.role.set(role); }

    public String getMotDePasse() { return motDePasse.get(); }
    public void setMotDePasse(String motDePasse) { this.motDePasse.set(motDePasse); }

    // Terrain Relationship
    public List<Terrain> getTerrains() { return terrains; }
    public void setTerrains(List<Terrain> terrains) { this.terrains = terrains; }

    @Override
    public String toString() {
        return nom.get() + " " + prenom.get() + " (" + role.get() + ")";
    }

    public abstract String getType();
}