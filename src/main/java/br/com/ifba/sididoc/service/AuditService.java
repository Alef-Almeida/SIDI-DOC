package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.AuditLog;
import br.com.ifba.sididoc.enums.AuditAction;
import br.com.ifba.sididoc.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    public void log(
            AuditAction action,
            String description,
            String userEmail,
            String ip
    ) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setDescription(description);
        log.setUserEmail(userEmail);
        log.setIpAddress(ip);
        log.setCreatedAt(LocalDateTime.now());

        repository.save(log);
    }

    public List<AuditLog> findAll() {
        return repository.findAll();
    }

    public List<AuditLog>findByPeriod(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return repository.findByCreatedAtBetween(start, end);
    }

}
