module com.salah.appcrud {
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jbcrypt;
    requires com.google.protobuf;
    requires com.jfoenix;
    requires javafx.controls;
    requires itextpdf;
    requires mysql.connector.j;

    // Ouvre les packages pour l'accès par FXML aux contrôleurs
    opens entities to javafx.fxml;


    // Exporte les packages nécessaires pour qu'ils soient accessibles dans d'autres modules
    exports controllers;
    exports Dao;
    exports entities;
    exports test;
    exports utils;
    opens controllers to javafx.fxml;
    opens utils to javafx.fxml;
    opens Dao to javafx.fxml;
    opens test to javafx.fxml;
}
