package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.DocumentCategory;
import br.com.ifba.sididoc.exception.ResourceNotFoundException;
import br.com.ifba.sididoc.repository.DocumentCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCategoryService {

    private DocumentCategoryRepository repository;

    public DocumentCategory findById(Long id) {
        log.info("Iniciando busca pela categoria de documento com ID: {}", id);

        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Categoria de documento não encontrada no banco de dados. ID solicitado: {}", id);
                    return new ResourceNotFoundException("Categoria de documentos não encontrada.");
                });
    }

}
