package br.com.ifba.sididoc.service;

import com.spire.pdf.PdfDocument;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.coyote.BadRequestException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_objdetect.QRCodeDetector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExtratorService {
    ITesseract iTesseract = new Tesseract();

    private List<BufferedImage> convertPdf (MultipartFile file) throws IOException { //metodo utilizado para converter arquivos pdf em imagens
        PdfDocument doc = new PdfDocument();
        doc.loadFromBytes(file.getBytes());

        List<BufferedImage> images = new ArrayList<>();

        for (int i=0; i < doc.getPages().getCount(); i++){
            BufferedImage image = doc.saveAsImage(i);
            images.add(image);
        }
        doc.close();
        return images;
    }

    public String extrair (MultipartFile file) throws IOException, TesseractException {
        List<BufferedImage> imagesToProcess = new ArrayList<>();
        //possibilidade de adicionar para um bloco try-catch e evitar o if
        if (isPdf(file)){
            try {
                imagesToProcess = convertPdf(file);
            }catch (IOException e){
                System.out.println("Não foi possível converter o pdf em imagem...");
            }
        }else {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if(img == null)
                throw new BadRequestException("O arquivo informado não é uma imagem válida");
            imagesToProcess.add(img);
        }

        iTesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");//analisar se é possivel deixar essa pasta dentro do diretorio do arquivo
        iTesseract.setLanguage("por");
//        iTesseract.setPageSegMode(3);

        // 2. Definir DPI explicitamente
        iTesseract.setTessVariable("user_defined_dpi", "300");

        StringBuilder allText = new StringBuilder();

        for (BufferedImage image : imagesToProcess){
            //fazer metodo auxiliar de pre-processamento da imagem
            BufferedImage processedImage = preProcessarImagemComOpenCV(image);
            String pageText = iTesseract.doOCR(processedImage);
            allText.append(pageText).append("\n");
        }
        return allText.toString();
    }

    private static boolean isPdf(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()
             ;PDDocument document = PDDocument.load(inputStream)){
            return true;
        }catch (IOException e){
            System.out.println("O documento não é um PDF...");
            return false;
        }
    }

    private BufferedImage preProcessarImagemComOpenCV(BufferedImage javaImage) {
        OpenCVFrameConverter.ToMat cvtToMat = new OpenCVFrameConverter.ToMat();
        Java2DFrameConverter cvtToJava2D = new Java2DFrameConverter();

        Mat mat = cvtToMat.convert(cvtToJava2D.convert(javaImage));

        // 1. Escala de Cinza
        Mat grayMat = new Mat();
        opencv_imgproc.cvtColor(mat, grayMat, opencv_imgproc.COLOR_BGR2GRAY);

        grayMat = removerQrCode(grayMat);

        // 2. Aumento de Escala (Upscaling Simples de 2x)
        // Isso ajuda a separar letras como "r" e "i" que viram "n" ou "m"
        Mat scaledMat = new Mat();
        opencv_imgproc.resize(grayMat, scaledMat, new Size(), 2.0, 2.0, opencv_imgproc.INTER_CUBIC);

        // 3. Binarização (Preto e Branco Absoluto)
        // O THRESH_OTSU calcula automaticamente o melhor divisor entre fundo e texto.
        // Isso remove sombras claras e define bem as letras.
        Mat binaryMat = new Mat();
        opencv_imgproc.threshold(scaledMat, binaryMat, 0, 255, opencv_imgproc.THRESH_BINARY | opencv_imgproc.THRESH_OTSU);

        return cvtToJava2D.convert(cvtToMat.convert(binaryMat));
    }

    private Mat removerQrCode(Mat inputMat) {
        QRCodeDetector qrDetector = new QRCodeDetector();
        Mat points = new Mat();

        // Tenta detectar QR Code na imagem cinza
        if (qrDetector.detect(inputMat, points)) {
            Mat pointsInt = new Mat();
            points.convertTo(pointsInt, opencv_core.CV_32S);
            Rect box = opencv_imgproc.boundingRect(pointsInt);

            // Pinta um retângulo branco sobre o QR Code
            opencv_imgproc.rectangle(inputMat, box, new Scalar(255.0), -1, 8, 0);
        }
        return inputMat;
    }

//    private BufferedImage preProcessarImagemComOpenCV(BufferedImage javaImage) {
//        // Conversores do JavaCV
//        OpenCVFrameConverter.ToMat cvtToMat = new OpenCVFrameConverter.ToMat();
//        Java2DFrameConverter cvtToJava2D = new Java2DFrameConverter();
//
//        // 1. Converte BufferedImage (Java) -> Mat (OpenCV)
//        Mat mat = cvtToMat.convert(cvtToJava2D.convert(javaImage));
//
//        // 2. Aplica Escala de Cinza
//        Mat grayMat = new Mat();
//        opencv_imgproc.cvtColor(mat, grayMat, opencv_imgproc.COLOR_BGR2GRAY);
//
//        // 3. Aplica GaussianBlur (Suavização)
//        Mat blurredMat = new Mat();
//        // O Size(5, 5) deve ser ímpar. O sigmaX = 0 calcula automaticamente.
//        opencv_imgproc.GaussianBlur(grayMat, blurredMat, new org.bytedeco.opencv.opencv_core.Size(5, 5), 0);
//
//        // 4. Converte de volta Mat (OpenCV) -> BufferedImage (Java) para o Tesseract usar
//        return cvtToJava2D.convert(cvtToMat.convert(blurredMat));
//    }

}