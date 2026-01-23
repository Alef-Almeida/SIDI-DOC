package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.entity.DocumentBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentBatchRepository extends JpaRepository<DocumentBatch, Long> {
    Optional<DocumentBatch> findByCode(String code);
    boolean existsByCode(String code);
}
