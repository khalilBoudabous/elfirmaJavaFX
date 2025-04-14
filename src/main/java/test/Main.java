package test;

import entities.Evenement;
import entities.Ticket;
import services.EvenementService;
import services.TicketService;
import utils.MyDatabase;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // MyDatabase db1 = MyDatabase.getInstance();   // CREATION D UN SINGELTON : CREER UNE SEUL INSTANCE D UNE CLASSE DONNEES !!!!
        // MyDatabase db2 = MyDatabase.getInstance();   // pour limiter l'access a une classe

        EvenementService es = new EvenementService();
        Evenement event2 = new Evenement(0,"Agriculture intelligente 4.0", "lorem stecj edc",
                Date.valueOf("2025-06-1"), Date.valueOf("2024-06-5"), "ON_SITE", 20, 55);

        TicketService ts = new TicketService();

       /*
        try {
            /* test EVENEMENT

            //es.ajouter(event1);
            //es.modifier(event1);
            //es.supprimer(event1);
            List<Evenement> eventsList = new ArrayList<>();
            eventsList = es.recuperer();
            eventsList.forEach(event->System.out.println(event));
       */

            /*
            es.ajouter(event2);
            for (int i = 0; i < event2.getNombrePlaces(); i++) {
                Ticket ticket = new Ticket(0, event2.getId(), event2.getPrix(), false,);
                ts.ajouter(ticket);
            }
            */

/*

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    */
        }

}


