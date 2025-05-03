package services;

import entities.*;
import jakarta.mail.MessagingException;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UtilisateurService {

    private  Connection cnx;

    public UtilisateurService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    private void ensureConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDatabase.getInstance().getCnx();
            if (cnx == null || cnx.isClosed()) {
                throw new SQLException("Échec de reconnexion à la base");
            }
        }
    }
    public void ajouter(Utilisateur u) {
        String req = "INSERT INTO utilisateur (email, roles, password, nom, prenom, telephone, type, adresse_exploitation, nom_entreprise, id_fiscale, categorie_produit, domaine_expertise, is_blocked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // domain_expertise corrigé

        try {
            PreparedStatement ps = cnx.prepareStatement(req);


            ps.setString(1, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getNom());
            ps.setString(5, u.getPrenom());
            ps.setString(6, u.getTelephone());
            ps.setInt(13, 0);


            String role;
            if (u instanceof Agriculteur agriculteur) {
                role = "ROLE_AGRICULTEUR";
                ps.setString(7, "agriculteur");
                ps.setString(8, agriculteur.getAdresseExploitation());
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
                ps.setString(12, null); // domain_expertise
            } else if (u instanceof Fournisseur fournisseur) {
                role = "ROLE_FOURNISSEUR";
                ps.setString(7, "fournisseur");
                ps.setNull(8, Types.VARCHAR);
                ps.setString(9, fournisseur.getNomEntreprise());
                ps.setString(10, fournisseur.getIdFiscale());
                ps.setString(11, fournisseur.getCategorieProduit());
                ps.setString(12, null); // domain_expertise
            } else if (u instanceof Expert expert) {
                role = "ROLE_EXPERT";
                ps.setString(7, "expert");
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
                ps.setString(12, expert.getDomaineExpertise());
            } else {
                role = "ROLE_ADMIN";
                ps.setString(7, "admin");
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
                ps.setString(12, null); // domain_expertise
            }

            ps.setString(2, "[\"" + role + "\"]");

            ps.executeUpdate();
            System.out.println("Utilisateur ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur SQL détaillée : " + e.getMessage());
        }
    }

    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String req = "SELECT * FROM utilisateur";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                String type = rs.getString("type");
                Utilisateur u = createUserByType(type);

                u.setId(rs.getLong("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setPassword(rs.getString("password"));
                u.setBlocked(rs.getBoolean("is_blocked"));

                // Remplir les champs spécifiques
                if(u instanceof Agriculteur a) {
                    a.setAdresseExploitation(rs.getString("adresse_exploitation"));
                }
                else if(u instanceof Fournisseur f) {
                    f.setNomEntreprise(rs.getString("nom_entreprise"));
                    f.setIdFiscale(rs.getString("id_fiscale"));
                    f.setCategorieProduit(rs.getString("categorie_produit"));
                }
                else if(u instanceof Expert e) {
                    e.setDomaineExpertise(rs.getString("domaine_expertise"));
                }


                utilisateurs.add(u);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return utilisateurs;
    }

    private Utilisateur createUserByType(String type) {
        return switch(type.toLowerCase()) {
            case "agriculteur" -> new Agriculteur();
            case "fournisseur" -> new Fournisseur();
            case "expert" -> new Expert();
            case "admin" -> new Admin();

            default -> new Utilisateur() {
                @Override
                public String getType() {
                    return "admin";
                }
            };
        };
    }

    public void supprimer(long id) throws SQLException {
        String req = "DELETE FROM utilisateur WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            throw e; // Propagation de l'exception
        }
    }
    public void modifier(Utilisateur u) throws SQLException {
        String req = "UPDATE utilisateur SET "
                + "email = ?, roles = ?, password = ?, nom = ?, prenom = ?, telephone = ?, "
                + "adresse_exploitation = ?, nom_entreprise = ?, id_fiscale = ?, categorie_produit = ?, domaine_expertise = ? "
                + "WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            // Paramètres communs
            ps.setString(1, u.getEmail());
            ps.setString(2, getRoles(u));
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getNom());
            ps.setString(5, u.getPrenom());
            ps.setString(6, u.getTelephone());


            if(u instanceof Agriculteur a) {
                ps.setString(7, a.getAdresseExploitation());
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
            }
            else if(u instanceof Fournisseur f) {
                ps.setNull(7, Types.VARCHAR);
                ps.setString(8, f.getNomEntreprise());
                ps.setString(9, f.getIdFiscale());
                ps.setString(10, f.getCategorieProduit());
                ps.setNull(11, Types.VARCHAR);
            }
            else if(u instanceof Expert e) {
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
                ps.setString(11, e.getDomaineExpertise());
            }
            else {

                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
            }

            ps.setLong(12, u.getId());
            ps.executeUpdate();
        }
    }

    private String getRoles(Utilisateur u) {
        if(u instanceof Agriculteur) return "[\"ROLE_AGRICULTEUR\"]";
        if(u instanceof Fournisseur) return "[\"ROLE_FOURNISSEUR\"]";
        if(u instanceof Expert) return "[\"ROLE_EXPERT\"]";
        return "[\"ROLE_ADMIN\"]";
    }
    public Utilisateur checkLogin(String email, String password) {
        String req = "SELECT * FROM utilisateur WHERE email = ? AND password = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String type = rs.getString("type");
                Utilisateur u = createUserByType(type);

                // Set common fields
                u.setId(rs.getLong("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setPassword(rs.getString("password"));
                u.setBlocked(rs.getBoolean("is_blocked")); // Ensure blocked status is set


                // Set specific fields
                if (u instanceof Agriculteur a) {
                    a.setAdresseExploitation(rs.getString("adresse_exploitation"));
                } else if (u instanceof Fournisseur f) {
                    f.setNomEntreprise(rs.getString("nom_entreprise"));
                    f.setIdFiscale(rs.getString("id_fiscale"));
                    f.setCategorieProduit(rs.getString("categorie_produit"));
                } else if (u instanceof Expert e) {
                    e.setDomaineExpertise(rs.getString("domaine_expertise"));
                }
                return u;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public Utilisateur getUtilisateurById(long id) throws SQLException {
        String req = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String type = rs.getString("type");
                Utilisateur u = createUserByType(type);

                u.setId(rs.getLong("id"));
                u.setNom(rs.getString("nom"));
                u.setPrenom(rs.getString("prenom"));
                u.setEmail(rs.getString("email"));
                u.setTelephone(rs.getString("telephone"));
                u.setPassword(rs.getString("password"));

                if (u instanceof Agriculteur a) {
                    a.setAdresseExploitation(rs.getString("adresse_exploitation"));
                } else if (u instanceof Fournisseur f) {
                    f.setNomEntreprise(rs.getString("nom_entreprise"));
                    f.setIdFiscale(rs.getString("id_fiscale"));
                    f.setCategorieProduit(rs.getString("categorie_produit"));
                } else if (u instanceof Expert e) {
                    e.setDomaineExpertise(rs.getString("domaine_expertise"));
                }
                return u;
            }
        }
        return null;
    }

    public Utilisateur findByEmail(String email) throws SQLException {
        ensureConnection();
        String sql = "SELECT * FROM utilisateur WHERE email = ?";

        Connection conn = MyDatabase.getInstance().getCnx(); // Ne pas fermer cette connexion

        try ( PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Utilisateur user = createUserByType(rs.getString("type"));
                user.setBlocked(rs.getBoolean("is_blocked")); // Récupérer le statut
                // Création d'une implémentation concrète minimaliste
//                Utilisateur user = new Utilisateur() {
//
//                    @Override
//                    public String getType() throws SQLException {
//                        return rs.getString("type"); // Adaptez selon votre schéma
//                        user.setBlocked(rs.getBoolean("is_blocked")); // Récupérer le statut
//
//                    }
//                };

                // Hydratation des propriétés
                user.setId(rs.getLong("id"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setTelephone(rs.getString("telephone"));
//                user.setBlocked(rs.getBoolean("blocked"));

                return user;
            }
            return null;
        }
    }



    private String generateResetToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void blockUser(long id, boolean blocked) throws SQLException {
        String req = "UPDATE utilisateur SET is_blocked = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setBoolean(1, blocked);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    // Ajoutez ces méthodes
    public boolean handlePasswordResetRequest(String email) throws SQLException {
        Utilisateur user = findByEmail(email);
        if (user == null) return false;

        String token = generateResetToken();
        storeResetToken(user.getId(), token);

        // Envoi d'e-mail
        String subject = "Réinitialisation de mot de passe";
        String content = "Votre code de réinitialisation est : " + token
                + "\nCe code expirera dans 15 minutes.";

        try {
            MailService mailService = new MailService("khllboudabous@gmail.com", "xdlf jcxz ymms pyiy");
            mailService.sendEmail(email, subject, content);
            return true;
        } catch (javax.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeResetToken(long userId, String token) throws SQLException {
        ensureConnection();

        String req = "INSERT INTO reset_password_request (user_id, token, expires_at) VALUES (?, ?, NOW() + INTERVAL 15 MINUTE)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setLong(1, userId); // Paramètre 1: user_id
            ps.setString(2, token); // Paramètre 2: token
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur stockage token: " + e.getMessage());
            throw e;
        }
    }
    public boolean resetPassword(String token, String newPassword) throws SQLException {
        ensureConnection();

        String req = "SELECT user_id FROM reset_password_request "
                + "WHERE token = ? AND expires_at    > NOW()";

        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long userId = rs.getLong("user_id");
                updateUserPassword(userId, newPassword);
                deleteToken(token);
                return true;
            }
        }
        return false;
    }

    private void updateUserPassword(long userId, String newPassword) throws SQLException {
        ensureConnection();
        String req = "UPDATE utilisateur SET password = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, newPassword);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    private void deleteToken(String token) throws SQLException {

        String req = "DELETE FROM reset_password_request WHERE token = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, token);
            ps.executeUpdate();
        }
    }








}
