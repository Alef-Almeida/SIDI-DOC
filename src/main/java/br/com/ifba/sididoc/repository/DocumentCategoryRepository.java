package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {
    // Busca apenas as categorias que não foram "deletadas"
    List<DocumentCategory> findByActiveTrueOrderByNameAsc();

    // Para evitar duplicidade de nomes
    boolean existsByName(String name);

    // O Spring monta: SELECT * FROM tb_categories WHERE name = ?
    Optional<DocumentCategory> findByName(String name);
}
