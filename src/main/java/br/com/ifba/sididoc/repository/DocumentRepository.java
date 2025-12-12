package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Busca exata por IDs de relacionamento
    // Ordenado por ordem alfabética
    List<Document> findBySector_IdAndCategory_IdOrderByTitleAsc(Long sectorId, Long categoryId);
}
