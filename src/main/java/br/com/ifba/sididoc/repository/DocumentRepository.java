package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query(value = """
        SELECT * FROM documents d
        WHERE d.search_vector @@ to_tsquery('portuguese', :termo)
        ORDER BY ts_rank(d.search_vector, to_tsquery('portuguese', :termo)) DESC
    """, nativeQuery = true)
    List<Document> searchByTerm(@Param("termo") String termo);

}
