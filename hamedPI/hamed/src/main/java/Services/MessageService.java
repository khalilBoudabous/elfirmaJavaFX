package Services;


import Models.Message;
import Utils.MyDatabase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageService {

    private Connection cnx;

    public MessageService() {
        this.cnx = MyDatabase.getInstance().getCnx(); // Get the connection from MyDatabase
    }

    public void createMessage(Message message) {
        String query = "INSERT INTO message (emetteur_id, discussion_id, contenu, image) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, message.getEmetteurId());
            statement.setInt(2, message.getDiscussionId());
            statement.setString(3, message.getContenu());
            statement.setString(4, message.getImage());

            statement.executeUpdate();
            System.out.println("Message created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating message: " + e.getMessage());
        }
    }

    public Message getMessageById(int id) {
        String query = "SELECT * FROM message WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Message(
                        resultSet.getInt("id"),
                        resultSet.getInt("emetteur_id"),
                        resultSet.getInt("discussion_id"),
                        resultSet.getString("contenu"),
                        resultSet.getTimestamp("date_envoi").toLocalDateTime(),
                        resultSet.getString("image")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving message: " + e.getMessage());
        }
        return null;
    }

    public List<Message> getMessagesByDiscussionId(int discussionId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM message WHERE discussion_id = ?";

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            // Set the discussion_id parameter in the query
            statement.setInt(1, discussionId);

            // Execute the query and process the result set
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                messages.add(new Message(
                        resultSet.getInt("id"),
                        resultSet.getInt("emetteur_id"),
                        resultSet.getInt("discussion_id"),
                        resultSet.getString("contenu"),
                        resultSet.getTimestamp("date_envoi").toLocalDateTime(),
                        resultSet.getString("image")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving messages: " + e.getMessage());
        }

        return messages;
    }


    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM message";
        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                messages.add(new Message(
                        resultSet.getInt("id"),
                        resultSet.getInt("emetteur_id"),
                        resultSet.getInt("discussion_id"),
                        resultSet.getString("contenu"),
                        resultSet.getTimestamp("date_envoi").toLocalDateTime(),
                        resultSet.getString("image")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all messages: " + e.getMessage());
        }
        return messages;
    }

    public void updateMessage(Message message) {
        String query = "UPDATE message SET emetteur_id = ?, discussion_id = ?, contenu = ?, date_envoi = ?, image = ? WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, message.getEmetteurId());
            statement.setInt(2, message.getDiscussionId());
            statement.setString(3, message.getContenu());
            statement.setTimestamp(4, Timestamp.valueOf(message.getDateEnvoi()));
            statement.setString(5, message.getImage());
            statement.setInt(6, message.getId());

            statement.executeUpdate();
            System.out.println("Message updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating message: " + e.getMessage());
        }
    }

    public void deleteMessage(int id) {
        String query = "DELETE FROM message WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("Message deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
        }
    }
}