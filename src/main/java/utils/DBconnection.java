package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconnection {

    private static final String URL = "jdbc:mysql://localhost:3306/mainelfirma";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    // Connexion à la base de données
    public static Connection getConnection() {
        if (connection == null || isConnectionClosed()) {
            try {
                // Charger le driver (pas nécessaire avec JDBC 4+, mais bonne pratique)
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données réussie.");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver MySQL introuvable !");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à la base de données !");
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Vérifie si la connexion est fermée
    private static boolean isConnectionClosed() {
        try {
            return connection == null || connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Si une exception est lancée, on suppose que la connexion est fermée
        }
    }

    // Méthode pour fermer la connexion
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Connexion fermée avec succès.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la fermeture de la connexion :");
            e.printStackTrace();
        }
    }
}
