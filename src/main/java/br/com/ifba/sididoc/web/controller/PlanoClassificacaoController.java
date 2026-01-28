package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.repository.PlanoClassificacaoRepository;
import br.com.ifba.sididoc.service.PlanoClassificacaoService;
import br.com.ifba.sididoc.web.dto.PlanoClassificacaoDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/plano-classificacao")
@RequiredArgsConstructor
public class PlanoClassificacaoController {
    private final PlanoClassificacaoService service; // Injetando o Service

    @GetMapping
    public ResponseEntity<List<PlanoClassificacaoDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<PlanoClassificacaoDTO>> buscar(@RequestParam String termo) {
        return ResponseEntity.ok(service.buscarPorTermo(termo));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<PlanoClassificacaoDTO> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.buscarPorCodigo(codigo));
    }
}