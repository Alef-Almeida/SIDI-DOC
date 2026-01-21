package br.com.ifba.sididoc.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ImageToPdfService {

    /**
     * Recebe uma lista de imagens (JPG/PNG) e retorna o PDF consolidado em bytes.
     */
    public byte[] convertImagesToPdf(List<MultipartFile> images) throws IOException {
        // Cria um documento PDF em branco na memória
        try (PDDocument doc = new PDDocument()) {

            for (MultipartFile imageFile : images) {
                // Para cada imagem, cria uma nova página A4
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                // Carrega a imagem para o objeto PDFBox
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imageFile.getBytes(), imageFile.getOriginalFilename());

                try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                    // --- Lógica de Redimensionamento (Para caber na folha A4) ---
                    float pageWidth = PDRectangle.A4.getWidth();
                    float pageHeight = PDRectangle.A4.getHeight();

                    float imgWidth = pdImage.getWidth();
                    float imgHeight = pdImage.getHeight();

                    // Calcula a escala para caber na página (sem esticar)
                    float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);

                    // Se a imagem for pequena, não aumente (escala = 1.0), apenas reduza se for grande
                    if (scale > 1.0f) {
                        scale = 1.0f;
                    }

                    float finalWidth = imgWidth * scale;
                    float finalHeight = imgHeight * scale;

                    // Centraliza na página
                    float x = (pageWidth - finalWidth) / 2;
                    float y = (pageHeight - finalHeight) / 2;

                    // Desenha a imagem na página atual
                    contentStream.drawImage(pdImage, x, y, finalWidth, finalHeight);
                }
            }

            // Salva o PDF final em um array de bytes
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}