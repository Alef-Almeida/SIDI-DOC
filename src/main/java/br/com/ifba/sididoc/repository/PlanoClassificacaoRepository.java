package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.PlanoClassificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanoClassificacaoRepository extends JpaRepository<PlanoClassificacao, Long> {
    // Busca um item pelo código do CONARQ (ex: "021.11")
    Optional<PlanoClassificacao> findByCodigo(String codigo);

    // Busca todos os filhos de uma classe (ex: todos os filhos da "020")
    List<PlanoClassificacao> findByPaiCodigo(String paiCodigo);

    // Busca para a IA: Procura por termos no título ou nas notas de escopo
    @Query("SELECT p FROM PlanoClassificacao p WHERE " +
            "LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(p.notasEscopo) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<PlanoClassificacao> buscarPorTermo(String termo);

    // Lista apenas as classes raiz (000, 100, 200...)
    List<PlanoClassificacao> findByPaiIsNull();
}
