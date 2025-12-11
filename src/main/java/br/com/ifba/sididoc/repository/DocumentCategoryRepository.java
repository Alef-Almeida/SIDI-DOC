package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {

}
