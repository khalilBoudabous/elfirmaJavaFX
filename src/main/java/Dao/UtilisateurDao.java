package Dao;

import entities.Utilisateur;
import utils.DBconnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDao {

    // Method to add a user
    public void ajouterUtilisateur(entities.Utilisateur u) {
        String sql = "INSERT INTO mainelfirma.utilisateur (nom, prenom, email, telephone, role, mot_de_passe) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Use a secure hash for the password
            String hashedPassword = hashPassword(u.getMotDePasse());

            stmt.setString(1, u.getNom());
            stmt.setString(2, u.getPrenom());
            stmt.setString(3, u.getEmail());
            stmt.setInt(4, u.getTelephone());
            stmt.setString(5, u.getRole());
            stmt.setString(6, hashedPassword);  // Hashed password

            stmt.executeUpdate();
            System.out.println("Utilisateur ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to retrieve all users
    public List<entities.Utilisateur> afficherUtilisateurs() {
        List<entities.Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur";

        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entities.Utilisateur u = new Utilisateur() {
                    @Override
                    public String getType() {
                        return "";
                    }
                };
                u.setId(rs.getInt("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getInt("telephone"));
                u.setRole(rs.getString("role"));
                u.setMotDePasse(rs.getString("mot_de_passe"));
                utilisateurs.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs :");
            e.printStackTrace();
        }

        return utilisateurs;
    }

    // Method to delete a user
    public void supprimerUtilisateur(int id) {
        String sql = "DELETE FROM mainelfirma.utilisateur WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Utilisateur supprimé avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur :");
            e.printStackTrace();
        }
    }

    // Method to update a user
    public void modifierUtilisateur(entities.Utilisateur u) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, telephone = ?, role = ?, mot_de_passe = ? WHERE id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Hash the password before saving
            String hashedPassword = hashPassword(u.getMotDePasse());

            stmt.setString(1, u.getNom());
            stmt.setString(2, u.getPrenom());
            stmt.setString(3, u.getEmail());
            stmt.setInt(4, u.getTelephone());
            stmt.setString(5, u.getRole());
            stmt.setString(6, hashedPassword);  // Hashed password
            stmt.setInt(7, u.getId());

            stmt.executeUpdate();
            System.out.println("Utilisateur modifié avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'utilisateur :");
            e.printStackTrace();
        }
    }

    // Find a user by their "nom" (used as username)
    public entities.Utilisateur findByUsername(String username) {
        String sql = "SELECT * FROM mainelfirma.utilisateur WHERE nom = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                entities.Utilisateur user = new Utilisateur() {
                    @Override
                    public String getType() {
                        return "";
                    }
                };
                user.setId(rs.getInt("id"));
                user.setNom(rs.getString("nom"));
                user.setMotDePasse(rs.getString("mot_de_passe")); // Password stored as hashed
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No user found with that username
    }

    // Hashing method (SHA-256)
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString(); // Return hashed password as hex string
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";  // Return empty string in case of error (NON-secure, handle properly)
        }
    }

    // Method to validate the entered password with the stored hash
    public boolean isPasswordValid(Utilisateur user, String enteredPassword) {
        String hashedEnteredPassword = hashPassword(enteredPassword);
        return hashedEnteredPassword.equals(user.getMotDePasse());
    }
}
