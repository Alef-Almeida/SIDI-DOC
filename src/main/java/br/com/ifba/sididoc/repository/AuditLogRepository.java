package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );


}
