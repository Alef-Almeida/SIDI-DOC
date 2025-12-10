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
        com.lowagie.text.Document pdf = new com.lowagie.text.Document();
        PdfWriter.getInstance(pdf, outputStream);

        pdf.open();

        //titulo
        Paragraph title = new Paragraph("Relatório de Documento Digitalizado", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        pdf.add(title);

        //informações principais
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);
        table.setWidths(new float[]{1, 3});

        addTableRow(table, "ID:", String.valueOf(doc.getId()));
        addTableRow(table, "Título:", doc.getTitle());
        addTableRow(table, "Data Upload:", doc.getUploadDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        addTableRow(table, "Tipo:", doc.getType() != null ? doc.getType().name() : "N/A");
        addTableRow(table, "Status:", doc.getStatus() != null ? doc.getStatus().name() : "N/A");
        addTableRow(table, "OCR:", doc.getOcrConfidence() != null ? doc.getOcrConfidence() + "%" : "Não processado");

        pdf.add(table);

        //metadados
        if (!doc.getMetaData().isEmpty()) {
            Paragraph metaHeader = new Paragraph("Metadados do Arquivo:", HEADER_FONT);
            metaHeader.setSpacingAfter(5);
            pdf.add(metaHeader);

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            doc.getMetaData().forEach((key, value) -> addTableRow(metaTable, key, value));
            pdf.add(metaTable);
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