package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.Document;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DocumentExportService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11);

    public void generatePdfReport(Document doc, OutputStream outputStream) throws DocumentException {
            System.out.println("--- INICIANDO GERAÇÃO DO PDF ---");
             com.lowagie.text.Document pdf = new com.lowagie.text.Document();

             try {
                 PdfWriter.getInstance(pdf, outputStream);
                 pdf.open();

                 //titulo
                 Paragraph title = new Paragraph("Relatório de documento digitalizado", TITLE_FONT);
                 title.setAlignment(Element.ALIGN_CENTER);
                 title.setSpacingAfter(20);
                 pdf.add(new Paragraph("Relatório do documento ID: " + doc.getId()));

                 //informações principais
                 PdfPTable table = new PdfPTable(2);
                 table.setWidthPercentage(100);
                /* table.setSpacingAfter(15);
                 table.setWidths(new float[]{1, 3});*/

                 System.out.println("Escrevendo dados...");
                 addTableRow(table, "ID:", String.valueOf(doc.getId()));
                 addTableRow(table, "Título:", doc.getTitle());
                 addTableRow(table, "Data Upload:", doc.getUploadDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                 addTableRow(table, "Tipo:", doc.getType() != null ? doc.getType().name() : "N/A");
                 addTableRow(table, "Status:", doc.getStatus() != null ? doc.getStatus().name() : "N/A");
                 addTableRow(table, "OCR:", doc.getOcrConfidence() != null ? doc.getOcrConfidence() + "%" : "Não processado");

                 pdf.add(table);

                 //metadados
                 System.out.println("Escrevendo metadados...");
                 if (doc.getMetaData() != null) {
                     for (var entry : doc.getMetaData().entrySet()) {
                         pdf.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
                     }
                 }

                 //conteudo extraido
                 pdf.add(new Paragraph("Conteúdo Extraído (OCR):", HEADER_FONT));
                 String textContent = doc.getExtractedText() != null && !doc.getExtractedText().isBlank()
                         ? doc.getExtractedText()
                         : "[Nenhum texto extraído ou OCR pendente]";

                 Paragraph textPara = new Paragraph(textContent, NORMAL_FONT);
                 textPara.setSpacingBefore(10);
                 pdf.add(textPara);

                 pdf.close();

            } catch (Exception e) {
                 System.out.println("ERRO FATAL: " + e.getMessage());
                 e.printStackTrace();

                 try {
                     pdf.add(new Paragraph("ERRO AO GERAR: " + e.getMessage()));
                 } catch (Exception ex) {
                 }
             } finally {
                 if (pdf.isOpen()) {
                     pdf.close();
                     System.out.println("PDF Fechado.");
                 }
             }
         }

    public void generateZipExport(List<Document> documents, OutputStream outputStream) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            for (Document doc : documents) {
                //nome do arquivo
                String safeTitle = doc.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_");
                String entryName = safeTitle + "_" + doc.getId() + ".pdf";

                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);

                ByteArrayOutputStream pdfBuffer = new ByteArrayOutputStream();
                try {
                    generatePdfReport(doc, pdfBuffer);
                    zos.write(pdfBuffer.toByteArray());
                } catch (DocumentException e) {
                    zos.write(("Erro ao gerar PDF para ID " + doc.getId()).getBytes());
                }

                zos.closeEntry();
            }
            zos.finish();
        }
    }

    private void addTableRow(PdfPTable table, String header, String value) {
        PdfPCell headerCell = new PdfPCell(new Phrase(header, HEADER_FONT));
        headerCell.setBackgroundColor(Color.LIGHT_GRAY);
        headerCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setPadding(5);

        table.addCell(headerCell);
        table.addCell(valueCell);
    }
}