package br.com.ifba.sididoc.web.dto;

import java.io.InputStream;

public record DownloadDocumentDTO(
        String filename,
        String contentType,
        InputStream inputStream,
        long length
) {}
