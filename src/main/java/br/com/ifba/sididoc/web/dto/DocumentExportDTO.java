package br.com.ifba.sididoc.web.dto;
import org.springframework.core.io.Resource;

public record DocumentExportDTO (
    byte [] data,
    String filename,
    String contentType
){}
