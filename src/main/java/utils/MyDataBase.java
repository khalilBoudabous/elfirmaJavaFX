package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String url = "jdbc:mysql://localhost:3306/elfirma";
    private final String user = "root";
    private final String password = "";
    private Connection cnx;
    private static MyDataBase instance;


    private MyDataBase(){
        try {
            cnx = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion établie");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance(){
        if(instance == null)
            instance = new MyDataBase();
        return instance;
    }
    public Connection getCnx()  {
        return cnx;
    }
}
