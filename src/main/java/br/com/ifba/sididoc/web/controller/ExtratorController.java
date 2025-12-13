package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.service.ExtratorService;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/ocr")
public class ExtratorController {
    @Autowired
    private ExtratorService extratorService;//injeção de dependencia

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> ocrXtractor(@RequestPart MultipartFile file) throws TesseractException, IOException {
        return ResponseEntity.ok(extratorService.extrair(file));
    }

}
