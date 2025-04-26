package services;

import entities.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    private final Connection cnx;

    public UtilisateurService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    public void ajouter(Utilisateur u) {
        String req = "INSERT INTO utilisateur (email, roles, password, nom, prenom, telephone, type, adresse_exploitation, nom_entreprise, id_fiscale, categorie_produit, domaine_expertise, is_blocked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // domain_expertise corrigé

        try {
            PreparedStatement ps = cnx.prepareStatement(req);

            // Remplissage commun
            ps.setString(1, u.getEmail());
            ps.setString(3, u.getPassword());
            ps.setString(4, u.getNom());
            ps.setString(5, u.getPrenom());
            ps.setString(6, u.getTelephone());
            ps.setInt(13, 0);

            // Gestion des rôles et colonnes spécifiques
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

            // Formatage JSON pour les rôles
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

            // Paramètres spécifiques
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
                // Cas par défaut (admin)
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



}