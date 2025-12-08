package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}
