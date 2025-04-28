package Services;

import Models.User;
import Utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection cnx;

    public UserService() {
        this.cnx = MyDatabase.getInstance().getCnx(); // Get the connection from MyDatabase
    }

    public void createUser(User user) {
        String query = "INSERT INTO user (nom, prenom, number, mail, password, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getNumber());
            statement.setString(4, user.getMail());
            statement.setString(5, user.getPassword());
            statement.setString(6, user.getRole());

            statement.executeUpdate();
            System.out.println("User created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
    }

    public User getUserById(int id) {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("prenom"),
                        resultSet.getString("number"),
                        resultSet.getString("mail"),
                        resultSet.getString("password"),
                        resultSet.getString("role")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        try (Statement statement = cnx.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("prenom"),
                        resultSet.getString("number"),
                        resultSet.getString("mail"),
                        resultSet.getString("password"),
                        resultSet.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all users: " + e.getMessage());
        }
        return users;
    }

    public void updateUser(User user) {
        String query = "UPDATE user SET nom = ?, prenom = ?, number = ?, mail = ?, password = ?, role = ? WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setString(1, user.getNom());
            statement.setString(2, user.getPrenom());
            statement.setString(3, user.getNumber());
            statement.setString(4, user.getMail());
            statement.setString(5, user.getPassword());
            statement.setString(6, user.getRole());
            statement.setInt(7, user.getId());

            statement.executeUpdate();
            System.out.println("User updated successfully.");
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    public void deleteUser(int id) {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {

            statement.setInt(1, id);
            statement.executeUpdate();
            System.out.println("User deleted successfully.");
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }
}
