package Controllers;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javafx.stage.FileChooser;
import java.io.File;

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
        //lblUserName.setText("Name: " + ticket.getNom_user());
        //lblUserEmail.setText("Email: " + ticket.getEmail_user());
        if (event != null) {
            lblEventDates.setText("From: " + event.getDateDebut() + " to " + event.getDateFin());
        } else {
            lblEventDates.setText("Dates: Unknown");
        }
        generateQRCode(ticket, event);
    }

    private void generateQRCode(Ticket ticket, Evenement event) {
        try {
            // Structure QR code content
            StringBuilder qrContent = new StringBuilder();
            qrContent.append("Evenement: ").append(ticket.getTitreEvenement()).append("\n")
                    .append("Prix: ").append(ticket.getPrix()).append("\n")
                    .append("Payement: ").append(ticket.getPayée()).append("\n")
                    .append("Dates: ").append(event != null ?
                            event.getDateDebut() + " au " + event.getDateFin() : "Unconnue").append("\n");

            // Placeholder for user details (to be uncommented when Utilisateur is integrated)
            // qrContent.append("User Name: ").append(ticket.getNom_user()).append("\n")
            //         .append("User Email: ").append(ticket.getEmail_user()).append("\n");

            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent.toString(), BarcodeFormat.QR_CODE, 150, 150);
            qrCodeBufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            Image qrImage = SwingFXUtils.toFXImage(qrCodeBufferedImage, null);
            qrCodeImage.setImage(qrImage);

            // Enable download button if QR code is generated successfully
            btnDownload.setDisable(false);
        } catch (WriterException e) {
            // Show error alert to user
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