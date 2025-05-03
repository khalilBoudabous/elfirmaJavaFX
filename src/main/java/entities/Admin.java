package entities;

public class Admin extends Utilisateur {

    @Override
    public String getType() {
        return "admin";
    }


}
