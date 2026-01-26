package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.service.AuditPdfService;
import br.com.ifba.sididoc.service.AuditService;
import br.com.ifba.sididoc.web.dto.AuditLogDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditController {

    private final AuditService auditService;
    private final AuditPdfService auditPdfService;

    @GetMapping
    public List<AuditLogDTO> listAll() {
        return auditService.findAll()
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @GetMapping("/period")
    public List<AuditLogDTO> listByPeriod(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        if (start == null || end == null) {
            return auditService.findAll()
                    .stream()
                    .map(AuditLogDTO::fromEntity)
                    .toList();
        }

        return auditService.findByPeriod(start, end)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .toList();
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {

        byte[] pdf = auditPdfService.exportPdf(start, end);

        String fileName = "auditoria_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm")) +
                ".pdf";

        return ResponseEntity.ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=auditoria.pdf"
                )
                .body(pdf);
    }

}
