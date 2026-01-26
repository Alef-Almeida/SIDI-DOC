package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.AuditLog;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AuditPdfService {

    private final AuditService auditService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color PRIMARY_BLUE = new Color(0, 123, 193);
    private static final Color LIGHT_BLUE = new Color(230, 244, 250);
    private static final Color HEADER_GRAY = new Color(240, 240, 240);

    public byte[] exportPdf(LocalDateTime start, LocalDateTime end) {

        List<AuditLog> logs =
                (start != null && end != null)
                        ? auditService.findByPeriod(start, end)
                        : auditService.findAll();

        logs.sort(Comparator.comparing(AuditLog::getCreatedAt));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addHeader(document, start, end);
            addTable(document, logs);
            addFooter(document);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de auditoria", e);
        }

        return out.toByteArray();
    }

    private void addHeader(Document document, LocalDateTime start, LocalDateTime end) throws Exception {

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1, 4});
        header.setSpacingAfter(20);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);

        try {
            Image logo = Image.getInstance(
                    getClass().getResource("/static/logo.png")
            );
            logo.scaleToFit(60, 60);
            logoCell.addElement(logo);
        } catch (Exception ignored) {}

        header.addCell(logoCell);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, PRIMARY_BLUE);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);

        textCell.addElement(new Paragraph("SIDIDOC", titleFont));
        textCell.addElement(new Paragraph("Sistema Integrado de Documentação", subtitleFont));
        textCell.addElement(new Paragraph("Relatório de Auditoria", subtitleFont));

        header.addCell(textCell);

        document.add(header);

        String period =
                (start != null && end != null)
                        ? "Período: " + start.format(DATE_FORMAT) + " até " + end.format(DATE_FORMAT)
                        : "Período: Todos os registros";

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph(period, infoFont));
        document.add(new Paragraph("Gerado em: " + LocalDateTime.now().format(DATE_FORMAT), infoFont));
    }

    private void addTable(Document document, List<AuditLog> logs) {

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);
        table.setWidths(new float[]{3, 3, 6, 3, 3});

        Stream.of("Usuário", "Ação", "Descrição", "IP", "Data")
                .forEach(col -> {
                    PdfPCell cell = new PdfPCell(new Phrase(col, headerFont));
                    cell.setBackgroundColor(PRIMARY_BLUE);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(6);
                    table.addCell(cell);
                });

        boolean alternate = false;

        for (AuditLog log : logs) {
            Color bg = alternate ? LIGHT_BLUE : Color.WHITE;
            alternate = !alternate;

            table.addCell(cell(log.getUserEmail(), bodyFont, bg, Element.ALIGN_LEFT));
            table.addCell(cell(log.getAction().getLabel(), bodyFont, bg, Element.ALIGN_LEFT));
            table.addCell(cell(log.getDescription(), bodyFont, bg, Element.ALIGN_LEFT));
            table.addCell(cell(normalizeIp(log.getIpAddress()), bodyFont, bg, Element.ALIGN_CENTER));
            table.addCell(cell(log.getCreatedAt().format(DATE_FORMAT), bodyFont, bg, Element.ALIGN_CENTER));
        }

        document.add(table);
    }

    private PdfPCell cell(String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }

    private void addFooter(Document document) {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);

        Paragraph footer = new Paragraph(
                "Documento gerado automaticamente pelo SIDIDOC • Uso interno e confidencial",
                footerFont
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);

        document.add(footer);
    }

    private String normalizeIp(String ip) {
        return "0:0:0:0:0:0:0:1".equals(ip) ? "localhost" : ip;
    }

}
