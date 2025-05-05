package entities;

public class Produit {
    private int id, quantite, categorie_id;
    private float prix;
    private String image, description, nom_produit, code_promo; // Nouveau champ
    private float discount_percentage; // Nouveau champ
    private long fournisseurId; // New field for fournisseur ID

    // Constructeur complet
    public Produit(int id, int quantite, int categorie_id, float prix, String image, String description, String nom_produit, String code_promo, float discount_percentage, long fournisseurId) {
        this.id = id;
        this.quantite = quantite;
        this.categorie_id = categorie_id;
        this.prix = prix;
        this.image = image;
        this.description = description;
        this.nom_produit = nom_produit;
        this.code_promo = code_promo;
        this.discount_percentage = discount_percentage;
        this.fournisseurId = fournisseurId;
    }

    // Constructeur pour ajout
    public Produit(int quantite, int categorie_id, float prix, String image, String description, String nom_produit, String code_promo, float discount_percentage, long fournisseurId) {
        this.quantite = quantite;
        this.categorie_id = categorie_id;
        this.prix = prix;
        this.image = image;
        this.description = description;
        this.nom_produit = nom_produit;
        this.code_promo = code_promo;
        this.discount_percentage = discount_percentage;
        this.fournisseurId = fournisseurId;
    }

    // Constructeur sans code promo (pour compatibilité avec les anciens produits)
    public Produit(int quantite, int categorie_id, float prix, String image, String description, String nom_produit) {
        this.quantite = quantite;
        this.categorie_id = categorie_id;
        this.prix = prix;
        this.image = image;
        this.description = description;
        this.nom_produit = nom_produit;
        this.code_promo = null;
        this.discount_percentage = 0;
        this.fournisseurId = 0;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom_produit() {
        return nom_produit;
    }

    public void setNom_produit(String nom_produit) {
        this.nom_produit = nom_produit;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public int getCategorie_id() {
        return categorie_id;
    }

    public void setCategorie_id(int categorie_id) {
        this.categorie_id = categorie_id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getCode_promo() {
        return code_promo;
    }

    public void setCode_promo(String code_promo) {
        this.code_promo = code_promo;
    }

    public float getDiscount_percentage() {
        return discount_percentage;
    }

    public void setDiscount_percentage(float discount_percentage) {
        this.discount_percentage = discount_percentage;
    }

    public long getFournisseurId() {
        return fournisseurId;
    }

    public void setFournisseurId(long fournisseurId) {
        this.fournisseurId = fournisseurId;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", categorie_id=" + categorie_id +
                ", prix=" + prix +
                ", image='" + image + '\'' +
                ", description='" + description + '\'' +
                ", nom_produit='" + nom_produit + '\'' +
                ", code_promo='" + code_promo + '\'' +
                ", discount_percentage=" + discount_percentage +
                ", fournisseurId=" + fournisseurId +
                '}';
    }
}