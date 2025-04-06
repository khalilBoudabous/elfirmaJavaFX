package services;

import utils.MyDatabase;
import entities.Ticket;
import java.util.*;
import java.sql.*;


public class TicketService implements Service<Ticket> {

    private Connection cnx;
    public TicketService() {cnx = MyDatabase.getInstance().getConnection();}

    @Override
    public void ajouter(Ticket ticket) throws SQLException {
        String sql = "insert into ticket(prix, evenement_id, is_paid)" + "values(?,?,?)";

        try (PreparedStatement ts = cnx.prepareStatement(sql)) {
            ts.setFloat(1, ticket.getPrix());
            ts.setInt(2,ticket.getId_evenement());
            ts.setBoolean(3, ticket.getPayée());
            ts.executeUpdate();
        }
    }

    @Override
    public void supprimer(Ticket ticket) throws SQLException {
        String sql = "delete from ticket where id = ?";
        PreparedStatement ts = cnx.prepareStatement(sql);
        ts.setInt(1, ticket.getId());
        ts.executeUpdate();
    }

    @Override
    public void modifier(Ticket ticket) throws SQLException {
        String sql = "update ticket set prix = ?, evenement_id = ?, is_paid = ? where id = ?";
        PreparedStatement ts = cnx.prepareStatement(sql);
        ts.setFloat(1, ticket.getPrix());
        ts.setInt(2, ticket.getId_evenement());
        ts.setBoolean(3, ticket.getPayée());
        ts.setInt(4, ticket.getId());
        ts.executeUpdate();
    }

    @Override
    public List<Ticket> recuperer() throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "select * from ticket";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int id = rs.getInt("id");
            float prix = rs.getFloat("prix");
            int id_evenement = rs.getInt("evenement_id");
            boolean paye = rs.getBoolean("is_paid");
            Ticket ticket = new Ticket(id, id_evenement, prix, paye);
            tickets.add(ticket);
        }
        return tickets;
    }

}
