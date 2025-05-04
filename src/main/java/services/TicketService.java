package services;

import entities.Evenement;
import utils.MyDatabase;
import entities.Ticket;
import java.util.*;
import java.sql.*;
import services.UtilisateurService;

public class TicketService implements Service<Ticket> {

    private Connection cnx;
    private final UtilisateurService utilisateurService = new UtilisateurService();

    public TicketService() {cnx = MyDatabase.getInstance().getCnx();}

    @Override
    public void ajouter(Ticket ticket) throws SQLException {
        String sql = "INSERT INTO ticket (prix, utilisateur_id, evenement_id, is_paid, Titre_evenement) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ts = cnx.prepareStatement(sql)) {
            ts.setFloat(1, ticket.getPrix());
            ts.setLong(2, ticket.getUtilisateur().getId()); // Set utilisateur_id
            ts.setInt(3, ticket.getId_evenement());
            ts.setBoolean(4, ticket.getPayée());
            ts.setString(5, ticket.getTitreEvenement());
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
        String sql = "update ticket set prix = ?, utilisateur_id = ?, evenement_id = ?, is_paid = ?, Titre_evenement = ? where id = ?";
        PreparedStatement ts = cnx.prepareStatement(sql);
        ts.setFloat(1, ticket.getPrix());
        ts.setInt(2, ticket.getUserId());
        ts.setInt(3, ticket.getId_evenement());
        ts.setBoolean(4, ticket.getPayée());
        ts.setString(5, ticket.getTitreEvenement());
        ts.setInt(6, ticket.getId());
        ts.executeUpdate();
    }

    public void deleteOldestTickets(int eventId, int limit) throws SQLException {
        String sql = "DELETE FROM ticket WHERE evenement_id = ? ORDER BY id ASC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, limit);
            ps.executeUpdate();
        }
    }

    public void updateAllTicketsForEvent(Evenement event) throws SQLException {
        String sql = "UPDATE ticket SET  prix = ?, Titre_evenement = ? WHERE evenement_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setFloat(1, event.getPrix());
            ps.setString(2, event.getTitre());
            ps.setInt(3, event.getId());
            ps.executeUpdate();
        }
    }

    public void updateUnpaidTicketsPrice(int eventId, float newPrice) throws SQLException {
        String sql = "UPDATE ticket SET prix = ? WHERE evenement_id = ? AND is_paid = false";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setFloat(1, newPrice);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        }
    }


    @Override
    public List<Ticket> recuperer() throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM ticket";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Ticket ticket = new Ticket(
                rs.getInt("id"),
                rs.getInt("evenement_id"),
                rs.getString("Titre_evenement"),
                rs.getFloat("prix"),
                rs.getBoolean("is_paid"),
                rs.getInt("utilisateur_id")
            );
            ticket.setUtilisateur(utilisateurService.getUtilisateurById(rs.getInt("utilisateur_id"))); // Fetch and set Utilisateur
            tickets.add(ticket);
        }
        return tickets;
    }

}
