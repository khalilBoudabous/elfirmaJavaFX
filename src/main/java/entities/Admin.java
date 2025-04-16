package entities;

import javafx.beans.property.*;

public class Admin extends Utilisateur {

    @Override
    public String getType() {
        return "admin";
    }


    public static class Terrain {

        // Static counter for auto-incremented IDs
        private static int idCounter = 1;

        // Properties
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final DoubleProperty superficie = new SimpleDoubleProperty();
        private final StringProperty localisation = new SimpleStringProperty();
        private final DoubleProperty latitude = new SimpleDoubleProperty();
        private final DoubleProperty longitude = new SimpleDoubleProperty();
        private final StringProperty typeSol = new SimpleStringProperty();
        private final BooleanProperty irrigationDisponible = new SimpleBooleanProperty();
        private final StringProperty statut = new SimpleStringProperty();
        private final StringProperty image = new SimpleStringProperty(); // ✅ champ image ajouté

        // === CONSTRUCTEURS ===

        public Terrain() {
            this.id.set(idCounter++);
        }

        public Terrain(double superficie, String localisation, double latitude,
                       double longitude, String typeSol, boolean irrigationDisponible, String statut) {
            this.id.set(idCounter++);
            this.superficie.set(superficie);
            this.localisation.set(localisation);
            this.latitude.set(latitude);
            this.longitude.set(longitude);
            this.typeSol.set(typeSol);
            this.irrigationDisponible.set(irrigationDisponible);
            this.statut.set(statut);
        }

        public Terrain(int id, double superficie, String localisation, double latitude,
                       double longitude, String typeSol, boolean irrigationDisponible, String statut) {
            this.id.set(id);
            this.superficie.set(superficie);
            this.localisation.set(localisation);
            this.latitude.set(latitude);
            this.longitude.set(longitude);
            this.typeSol.set(typeSol);
            this.irrigationDisponible.set(irrigationDisponible);
            this.statut.set(statut);
        }

        public Terrain(int id, double superficie, String localisation, double latitude,
                       double longitude, String typeSol, boolean irrigationDisponible, String statut, String image) {
            this(id, superficie, localisation, latitude, longitude, typeSol, irrigationDisponible, statut);
            this.image.set(image);
        }

        // === GETTERS / SETTERS ===

        public int getId() { return id.get(); }
        public void setId(int id) { this.id.set(id); }
        public IntegerProperty idProperty() { return id; }

        public double getSuperficie() { return superficie.get(); }
        public void setSuperficie(double superficie) { this.superficie.set(superficie); }
        public DoubleProperty superficieProperty() { return superficie; }

        public String getLocalisation() { return localisation.get(); }
        public void setLocalisation(String localisation) { this.localisation.set(localisation); }
        public StringProperty localisationProperty() { return localisation; }

        public double getLatitude() { return latitude.get(); }
        public void setLatitude(double latitude) { this.latitude.set(latitude); }
        public DoubleProperty latitudeProperty() { return latitude; }

        public double getLongitude() { return longitude.get(); }
        public void setLongitude(double longitude) { this.longitude.set(longitude); }
        public DoubleProperty longitudeProperty() { return longitude; }

        public String getTypeSol() { return typeSol.get(); }
        public void setTypeSol(String typeSol) { this.typeSol.set(typeSol); }
        public StringProperty typeSolProperty() { return typeSol; }

        public boolean isIrrigationDisponible() { return irrigationDisponible.get(); }
        public void setIrrigationDisponible(boolean value) { this.irrigationDisponible.set(value); }
        public BooleanProperty irrigationDisponibleProperty() { return irrigationDisponible; }

        public String getStatut() { return statut.get(); }
        public void setStatut(String statut) { this.statut.set(statut); }
        public StringProperty statutProperty() { return statut; }

        public String getImage() { return image.get(); }
        public void setImage(String image) { this.image.set(image); }
        public StringProperty imageProperty() { return image; }

        // === toString amélioré ===
        @Override
        public String toString() {
            String loc = (getLocalisation() != null && !getLocalisation().isBlank()) ? getLocalisation() : "inconnue";
            String type = (getTypeSol() != null && !getTypeSol().isBlank()) ? getTypeSol() : "non spécifié";
            String stat = (getStatut() != null && !getStatut().isBlank()) ? getStatut() : "non défini";
            String irrigation = isIrrigationDisponible() ? "oui" : "non";

            return String.format(
                    "Terrain #%d :\n" +
                            "- Localisation : %s\n" +
                            "- Superficie : %.2f m²\n" +
                            "- Latitude : %.6f\n" +
                            "- Longitude : %.6f\n" +
                            "- Type de sol : %s\n" +
                            "- Irrigation disponible : %s\n" +
                            "- Statut : %s\n" +
                            "- Image : %s", // Show the image path or some description
                    getId(),
                    loc,
                    getSuperficie(),
                    getLatitude(),
                    getLongitude(),
                    type,
                    irrigation,
                    stat,
                    (getImage() != null && !getImage().isBlank()) ? getImage() : "Aucune image"
            );
        }
    }
}
