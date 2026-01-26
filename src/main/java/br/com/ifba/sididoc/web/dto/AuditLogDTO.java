package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.AuditLog;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        String userEmail,
        String action,
        String description,
        String ipAddress,

        @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
        LocalDateTime createdAt
) {
    public static AuditLogDTO fromEntity(AuditLog log) {
        return new AuditLogDTO(
                log.getId(),
                log.getUserEmail(),
                log.getAction().getLabel(),
                log.getDescription(),
                log.getIpAddress(),
                log.getCreatedAt()
        );
    }
}
