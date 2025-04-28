package Services;
import Models.Discussion;
import Utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DiscussionService {

    private Connection cnx;

    public DiscussionService() {
        this.cnx = MyDatabase.getInstance().getCnx(); // Get the connection from MyDatabase
    }

    public void createDiscussion(Discussion discussion) {
        String query = "INSERT INTO discussion (createur_id, titre, description, color_code) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, discussion.getCreateurId());
            statement.setString(2, discussion.getTitre());
            statement.setString(3, discussion.getDescription());
            statement.setString(4, discussion.getColorCode());

            statement.executeUpdate();
            System.out.println("Discussion created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating discussion: " + e.getMessage());
        }
    }

    public Discussion getDiscussionById(int id) {
        String query = "SELECT * FROM discussion WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Discussion(
                        resultSet.getInt("id"),
                        resultSet.getInt("createur_id"),
                        resultSet.getString("titre"),
                        resultSet.getDate("date_creation").toLocalDate(),
                        resultSet.getString("description"),
                        resultSet.getString("color_code")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving discussion: " + e.getMessage());
        }
        return null;
    }

    public List<Discussion> getAllDiscussions() {
        List<Discussion> discussions = new ArrayList<>();
        String query = "SELECT * FROM discussion";
        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Discussion d = new Discussion(
                        resultSet.getInt("id"),
                        resultSet.getInt("createur_id"),
                        resultSet.getString("titre"),
                        resultSet.getDate("date_creation").toLocalDate(),
                        resultSet.getString("description"),
                        resultSet.getString("color_code")
                );
                // Calcul du nombre de messages non lus pour l'utilisateur courant (exemple userId=1)
                int userId = 1; // À remplacer par l'utilisateur connecté
                String unreadQuery = "SELECT COUNT(*) FROM message WHERE discussion_id = ? AND emetteur_id != ? AND lu = 0";
                try (PreparedStatement unreadStmt = cnx.prepareStatement(unreadQuery)) {
                    unreadStmt.setInt(1, d.getId());
                    unreadStmt.setInt(2, userId);
                    ResultSet unreadRs = unreadStmt.executeQuery();
                    if (unreadRs.next()) {
                        d.setNbNonLus(unreadRs.getInt(1));
                    }
                } catch (SQLException e) {
                    d.setNbNonLus(0);
                }
                discussions.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all discussions: " + e.getMessage());
        }
        return discussions;
    }

    public void updateDiscussion(Discussion discussion) {
        String query = "UPDATE discussion SET createur_id = ?, titre = ?, date_creation = ?, description = ?, color_code = ? WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, discussion.getCreateurId());
            statement.setString(2, discussion.getTitre());
            statement.setDate(3, Date.valueOf(discussion.getDateCreation()));
            statement.setString(4, discussion.getDescription());
            statement.setString(5, discussion.getColorCode());
            statement.setInt(6, discussion.getId());

            statement.executeUpdate();
            System.out.println("Discussion updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating discussion: " + e.getMessage());
        }
    }

    public void deleteDiscussion(int id) {
        String query = "DELETE FROM discussion WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Discussion deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Error deleting discussion: " + e.getMessage());
        }
    }
}
