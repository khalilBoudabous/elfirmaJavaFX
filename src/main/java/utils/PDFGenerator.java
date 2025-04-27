package utils;

import entities.Ticket;
import entities.Utilisateur;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.List;

public class PDFGenerator {
    public static void generateTicketsPDF(List<Ticket> tickets, String filePath) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Participant Details");
                contentStream.newLineAtOffset(0, -25);
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Nom       Prénom       Email");
                contentStream.newLineAtOffset(0, -20);
                
                for (Ticket ticket : tickets) {
                    Utilisateur user = ticket.getUtilisateur();
                    String nom = (user != null && user.getNom() != null) ? user.getNom() : "N/A";
                    String prenom = (user != null && user.getPrenom() != null) ? user.getPrenom() : "N/A";
                    String email = (user != null && user.getEmail() != null) ? user.getEmail() : "N/A";
                    String line = String.format("%-10s %-12s %s", nom, prenom, email);
                    contentStream.showText(line);
                    contentStream.newLineAtOffset(0, -15);
                }
                contentStream.endText();
            }
            document.save(filePath);
        }
    }
}
