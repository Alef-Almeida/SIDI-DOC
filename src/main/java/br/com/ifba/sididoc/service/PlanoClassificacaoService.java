package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.repository.PlanoClassificacaoRepository;
import br.com.ifba.sididoc.web.dto.PlanoClassificacaoDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlanoClassificacaoService {
    private final PlanoClassificacaoRepository repository;

    public List<PlanoClassificacaoDTO> listarTodos() {
        return repository.findAll().stream()
                .map(PlanoClassificacaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PlanoClassificacaoDTO> buscarPorTermo(String termo) {
        return repository.buscarPorTermo(termo).stream()
                .map(PlanoClassificacaoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Aqui entra a lógica que a IA vai usar depois
    public PlanoClassificacaoDTO buscarPorCodigo(String codigo) {
        return repository.findByCodigo(codigo)
                .map(PlanoClassificacaoDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Código CONARQ não encontrado: " + codigo));
    }
}
