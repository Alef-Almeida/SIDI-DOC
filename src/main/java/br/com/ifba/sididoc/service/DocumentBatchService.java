package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.DocumentBatch;
import br.com.ifba.sididoc.exception.ResourceAlreadyExistsException;
import br.com.ifba.sididoc.exception.ResourceNotFoundException;
import br.com.ifba.sididoc.repository.DocumentBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentBatchService {
    private final DocumentBatchRepository repository;

    @Transactional(readOnly = true)
    public DocumentBatch findByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Lote não encontrado com o código: " + code));
    }

    @Transactional
    public DocumentBatch create(String code, String description) {
        if (repository.existsByCode(code)) {
            throw new ResourceAlreadyExistsException("Já existe um lote com o código: " + code);
        }

        DocumentBatch batch = DocumentBatch.builder()
                .code(code)
                .description(description)
                .build();

        return repository.save(batch);
    }
}
