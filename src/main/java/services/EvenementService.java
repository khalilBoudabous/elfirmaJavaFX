package services;

import utils.MyDatabase;
import entities.Evenement;
import services.UtilisateurService;

import java.sql.Date;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class EvenementService implements Service<Evenement> {

    private Connection cnx;
    private final UtilisateurService utilisateurService = new UtilisateurService();

    public EvenementService() { cnx = MyDatabase.getInstance().getCnx(); }

    /*
    @Override
    public void ajouter(Evenement event) throws SQLException {
        String sql = "insert into evenement(titre, description, date_debut, date_fin, lieu, nombre_de_palces, prix)" +
                "values('" + event.getTitre() + "','" + event.getDescription() +
                "','" + event.getDateDebut() + "','" +
                "','" + event.getDateFin() + "','" + event.getLieu() +
                "','" + event.getNombrePlaces() + "'," + event.getPrix() + ")";
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);

    }


     */

    @Override
    public void ajouter(Evenement event) throws SQLException {
        String sql = "INSERT INTO evenement (titre, description, date_debut, date_fin, lieu, nombre_de_places, prix, utilisateur_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getTitre());
            ps.setString(2, event.getDescription());
            ps.setDate(3, event.getDateDebut());
            ps.setDate(4, event.getDateFin());
            ps.setString(5, event.getLieu());
            ps.setInt(6, event.getNombrePlaces());
            ps.setFloat(7, event.getPrix());
            ps.setInt(8, (int) event.getUtilisateur().getId()); // Set utilisateur_id
            ps.executeUpdate();

            // Retrieve generated ID and set it back to the event object
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));
                }
            }
        }

    }

    @Override
    public List<Evenement> recuperer() throws SQLException {
        List<Evenement> events = new ArrayList<>();
        String sql = "SELECT * FROM evenement";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Evenement e = new Evenement(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getString("description"),
                rs.getDate("date_debut"),
                rs.getDate("date_fin"),
                rs.getString("lieu"),
                rs.getInt("nombre_de_places"),
                rs.getFloat("prix")
            );
            long utilisateurId = rs.getLong("utilisateur_id");
            e.setUtilisateur(utilisateurService.getUtilisateurById(utilisateurId)); // Fetch and set Utilisateur
            events.add(e);
        }
        return events;
    }

    @Override
    public void modifier(Evenement event) throws SQLException {
        String sql = "update evenement set titre = ?, description = ?, date_debut = ?, date_fin = ?, lieu = ?, nombre_de_places = ?, prix = ? where id = ?";
        PreparedStatement es = cnx.prepareStatement(sql);
        es.setString(1, event.getTitre());
        es.setString(2, event.getDescription());
        es.setDate(3, event.getDateDebut());
        es.setDate(4, event.getDateFin());
        es.setString(5, event.getLieu());
        es.setInt(6, event.getNombrePlaces());
        es.setFloat(7, event.getPrix());
        es.setInt(8, event.getId());
        es.executeUpdate();
    }

    @Override
    public void supprimer(Evenement event) throws SQLException {
        String sql = "delete from evenement where id = ?";
        PreparedStatement es = cnx.prepareStatement(sql);
        es.setInt(1, event.getId());
        es.executeUpdate();
    }

}
