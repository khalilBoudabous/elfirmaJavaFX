package controllers;

import entities.Ticket;
import entities.Evenement;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javafx.stage.FileChooser;
import java.io.File;
import services.UtilisateurService;
import entities.Utilisateur;

public class TicketDetailsController {

    @FXML
    private Label lblEventTitle;
    @FXML
    private Label lblEventDates;
    @FXML
    private Label lblPrice;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblUserEmail;
    @FXML
    private ImageView qrCodeImage;
    @FXML
    private Button btnDownload;

    private BufferedImage qrCodeBufferedImage;

    public void setTicketData(Ticket ticket, Evenement event) {
        lblEventTitle.setText(ticket.getTitreEvenement());
        lblPrice.setText("Price: " + ticket.getPrix());

        // Fetch user info by userId
        String userFullName = "";
        String userEmail = "";
        try {
            UtilisateurService utilisateurService = new UtilisateurService();
            Utilisateur user = utilisateurService.getUtilisateurById(ticket.getUserId());
            if (user != null) {
                userFullName = user.getNom() + " " + user.getPrenom();
                userEmail = user.getEmail();
            }
        } catch (Exception e) {
            userFullName = "N/A";
            userEmail = "N/A";
        }

        lblUserName.setText("Name: " + userFullName);
        lblUserEmail.setText("Email: " + userEmail);

        if (event != null) {
            lblEventDates.setText("From: " + event.getDateDebut() + " to " + event.getDateFin());
        } else {
            lblEventDates.setText("Dates: Unknown");
        }
        generateQRCode(ticket, event, userFullName, userEmail);
    }

    // Update QR code generation to use fetched user info
    private void generateQRCode(Ticket ticket, Evenement event, String userFullName, String userEmail) {
        try {
            StringBuilder qrContent = new StringBuilder();
            qrContent.append("Evenement: ").append(ticket.getTitreEvenement()).append("\n")
                    .append("Prix: ").append(ticket.getPrix()).append("\n")
                    .append("Payement: ").append(ticket.getPayée()).append("\n")
                    .append("Dates: ").append(event != null ?
                            event.getDateDebut() + " au " + event.getDateFin() : "Unconnue").append("\n")
                    .append("User Name: ").append(userFullName).append("\n")
                    .append("User Email: ").append(userEmail).append("\n");

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent.toString(), BarcodeFormat.QR_CODE, 150, 150);
            qrCodeBufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image qrImage = SwingFXUtils.toFXImage(qrCodeBufferedImage, null);
            qrCodeImage.setImage(qrImage);

            btnDownload.setDisable(false);
        } catch (WriterException e) {
            showAlert("Error", "Echec de génération du code QR : " + e.getMessage());
            btnDownload.setDisable(true);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void downloadQRCode() {
        if (qrCodeBufferedImage == null) {
            showAlert("Erreur", "QR code non disponible !");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Télécharger le Code QR");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        fileChooser.setInitialFileName("Ticket_" + System.currentTimeMillis() + ".png");

        File file = fileChooser.showSaveDialog(btnDownload.getScene().getWindow());
        if (file != null) {
            try {
                ImageIO.write(qrCodeBufferedImage, "PNG", file);
                showAlert("Succé", "QR code téléchargé avec succé!");
            } catch (IOException e) {
                showAlert("Erreur", "Echec de téléchargement du Code QR: " + e.getMessage());
            }
        }
    }
}