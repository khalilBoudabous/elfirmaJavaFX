package entities;

public class Categorie {
    private int id;
    private String nom_categorie,description;
    public Categorie(int id, String nom_categorie, String description) {
        this.id = id;
        this.nom_categorie = nom_categorie;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNom_categorie() {
        return nom_categorie;
    }

    public void setNom_categorie(String nom_categorie) {
        this.nom_categorie = nom_categorie;
    }

    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + id +
                ", nom_categorie='" + nom_categorie + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
