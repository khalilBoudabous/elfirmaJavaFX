package entities;

public class Agriculteur extends Utilisateur {

    private String adresseExploitation;

    public String getAdresseExploitation() {
        return adresseExploitation;
    }

    public void setAdresseExploitation(String adresseExploitation) {
        this.adresseExploitation = adresseExploitation;
    }
    @Override
    public String getType() {
        return "agriculteur";
    }
}