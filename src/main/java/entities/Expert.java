package entities;

public class Expert extends Utilisateur {

    private String domaineExpertise;

    public String getDomaineExpertise() {
        return domaineExpertise;
    }

    public void setDomaineExpertise(String domaineExpertise) {
        this.domaineExpertise = domaineExpertise;
    }

    @Override
    public String getType() {
        return "expert";
    }
}