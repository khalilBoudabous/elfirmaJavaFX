package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private final String url = "jdbc:mysql://localhost:3306/el_firma";
    private final String user = "root";
    private final String password = "";
    private Connection cnx;
    private static MyDatabase instance;

    private MyDatabase() {
        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connected successfully to database");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
            if (instance == null) {
                instance = new MyDatabase();
            }
            return instance;
    }

    public Connection getConnection() { return cnx; }

}
