package entities;

import entities.Terrain;
import javafx.beans.property.*;

import java.time.LocalDate;

public class Location {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final ObjectProperty<Terrain> terrain = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateDebut = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateFin = new SimpleObjectProperty<>();
    private final FloatProperty prixLocation = new SimpleFloatProperty();
    private final BooleanProperty paiementEffectue = new SimpleBooleanProperty();
    private final StringProperty modePaiement = new SimpleStringProperty();
    private final StringProperty statut = new SimpleStringProperty();

    // Constructeurs
    public Location() {}

    public Location(int id, Terrain terrain, LocalDate dateDebut, LocalDate dateFin,
                    float prixLocation, boolean paiementEffectue, String modePaiement, String statut) {
        this.id.set(id);
        this.terrain.set(terrain);
        this.dateDebut.set(dateDebut);
        this.dateFin.set(dateFin);
        this.prixLocation.set(prixLocation);
        this.paiementEffectue.set(paiementEffectue);
        this.modePaiement.set(modePaiement);
        this.statut.set(statut);
    }

    public Location(Terrain t1, LocalDate now, LocalDate localDate, float v, boolean b, String cb, String validée) {
            
    }


    // Getters et Setters

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public Terrain getTerrain() {
        return terrain.get();
    }

    public void setTerrain(Terrain terrain) {
        this.terrain.set(terrain);
    }

    public ObjectProperty<Terrain> terrainProperty() {
        return terrain;
    }

    public LocalDate getDateDebut() {
        return dateDebut.get();
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut.set(dateDebut);
    }

    public ObjectProperty<LocalDate> dateDebutProperty() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin.get();
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin.set(dateFin);
    }

    public ObjectProperty<LocalDate> dateFinProperty() {
        return dateFin;
    }

    public float getPrixLocation() {
        return prixLocation.get();
    }

    public void setPrixLocation(float prixLocation) {
        this.prixLocation.set(prixLocation);
    }

    public FloatProperty prixLocationProperty() {
        return prixLocation;
    }

    public boolean isPaiementEffectue() {
        return paiementEffectue.get();
    }

    public void setPaiementEffectue(boolean paiementEffectue) {
        this.paiementEffectue.set(paiementEffectue);
    }

    public BooleanProperty paiementEffectueProperty() {
        return paiementEffectue;
    }

    public String getModePaiement() {
        return modePaiement.get();
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement.set(modePaiement);
    }

    public StringProperty modePaiementProperty() {
        return modePaiement;
    }

    public String getStatut() {
        return statut.get();
    }

    public void setStatut(String statut) {
        this.statut.set(statut);
    }

    public StringProperty statutProperty() {
        return statut;
    }

    // Optionnel : méthode toString pour affichage dans ComboBox ou logs
    @Override
    public String toString() {
        return "Location #" + getId() + " - " + getStatut();
    }
}
