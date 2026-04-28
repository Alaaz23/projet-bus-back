package bustrack.example.bustrack.services;

import bustrack.example.bustrack.models.Feedback;
import bustrack.example.bustrack.models.Salarie;
import bustrack.example.bustrack.models.TrajetHistory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(33, 150, 243);  // Material Blue
    private static final DeviceRgb ROW_ALT_COLOR = new DeviceRgb(240, 248, 255); // AliceBlue
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportSalaries(List<Salarie> salaries) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            addTitleAndDate(doc, "Liste des Salariés", salaries.size() + " salarié(s)");

            Table table = new Table(UnitValue.createPercentArray(new float[]{1.5f, 2f, 2f, 2f}))
                    .useAllAvailableWidth();

            addTableHeader(table, "Matricule", "Nom", "Prénom", "Bus assigné");

            boolean alt = false;
            for (Salarie s : salaries) {
                String busName = (s.getBus() != null) ? s.getBus().getDesignation() : "Non assigné";
                addTableRow(table, alt,
                        s.getMatricule(),
                        s.getNom() != null ? s.getNom() : "",
                        s.getPrenom() != null ? s.getPrenom() : "",
                        busName);
                alt = !alt;
            }
            doc.add(table);
            addFooter(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF salariés", e);
        }
        return out.toByteArray();
    }

    public byte[] exportFeedbacks(List<Feedback> feedbacks) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            addTitleAndDate(doc, "Rapport des Feedbacks", feedbacks.size() + " feedback(s)");

            Table table = new Table(UnitValue.createPercentArray(new float[]{3f, 1.5f, 1.5f, 1f}))
                    .useAllAvailableWidth();

            addTableHeader(table, "Description", "Salarié", "Date", "Lu");

            boolean alt = false;
            for (Feedback f : feedbacks) {
                String salarieNom = (f.getSalarie() != null)
                        ? f.getSalarie().getNom() + " " + f.getSalarie().getPrenom()
                        : "Inconnu";
                String dateStr = (f.getTime() != null)
                        ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(f.getTime())
                        : "";
                addTableRow(table, alt,
                        f.getDescription() != null ? f.getDescription() : "",
                        salarieNom,
                        dateStr,
                        f.isChecked() ? "✓" : "✗");
                alt = !alt;
            }
            doc.add(table);
            addFooter(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF feedbacks", e);
        }
        return out.toByteArray();
    }

    public byte[] exportTrajetHistory(List<TrajetHistory> trajets) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf)) {

            addTitleAndDate(doc, "Historique des Trajets", trajets.size() + " trajet(s)");

            Table table = new Table(UnitValue.createPercentArray(new float[]{2f, 2f, 2f, 1.5f, 1.5f}))
                    .useAllAvailableWidth();

            addTableHeader(table, "Date départ", "Bus", "Salarié", "Durée", "Distance");

            boolean alt = false;
            for (TrajetHistory t : trajets) {
                String bus = (t.getBus() != null) ? t.getBus().getDesignation() : "—";
                String salarie = (t.getSalarie() != null)
                        ? t.getSalarie().getNom() + " " + t.getSalarie().getPrenom() : "—";
                String duree = (t.getDuree() != null) ? t.getDuree() + " min" : "—";
                String distance = (t.getDistance() != null)
                        ? String.format("%.1f km", t.getDistance()) : "—";
                String date = (t.getDateDepart() != null) ? t.getDateDepart().format(DT_FMT) : "—";
                addTableRow(table, alt, date, bus, salarie, duree, distance);
                alt = !alt;
            }
            doc.add(table);
            addFooter(doc);

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF historique", e);
        }
        return out.toByteArray();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void addTitleAndDate(Document doc, String title, String subtitle) {
        doc.add(new Paragraph("🚌 Bus Tracking System")
                .setFontSize(10).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.RIGHT));
        doc.add(new Paragraph(title)
                .setFontSize(22).setBold().setFontColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
        doc.add(new Paragraph(subtitle)
                .setFontSize(11).setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
        String date = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
        doc.add(new Paragraph("Généré le " + date)
                .setFontSize(9).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(16));
    }

    private void addTableHeader(Table table, String... headers) {
        for (String h : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(h).setBold().setFontSize(10).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR)
                    .setPadding(6)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }
    }

    private void addTableRow(Table table, boolean alt, String... values) {
        DeviceRgb bg = alt ? ROW_ALT_COLOR : new DeviceRgb(255, 255, 255);
        for (String v : values) {
            Cell cell = new Cell()
                    .add(new Paragraph(v != null ? v : "").setFontSize(9))
                    .setBackgroundColor(bg)
                    .setPadding(5);
            table.addCell(cell);
        }
    }

    private void addFooter(Document doc) {
        doc.add(new Paragraph(" ").setMarginTop(20));
        doc.add(new Paragraph("Bus Tracking System — Rapport confidentiel")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }
}
