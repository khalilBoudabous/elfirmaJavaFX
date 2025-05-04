package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;



public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /*@Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/Affichierproduit.fxml"));

        try {
            Parent root=fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Modifier Produit");
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }*/






    /*
    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/ajouterUtilisateur.fxml"));

        try {
            Parent root=fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Ajouter Utilisateur");
            primaryStage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    */

/*
    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/FrontOfficeEvenement.fxml"));

        try {
            Parent root=fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setTitle("Dashboard Evenement");
            primaryStage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


    */

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/FrontOfficeEvenement.fxml"));

        try {
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setTitle("Welcome User");
            primaryStage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }




}
