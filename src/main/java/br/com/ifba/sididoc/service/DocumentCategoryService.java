package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.DocumentCategory;
import br.com.ifba.sididoc.exception.ResourceNotFoundException;
import br.com.ifba.sididoc.repository.DocumentCategoryRepository;
import br.com.ifba.sididoc.web.dto.DocumentCategoryRequestDTO;
import br.com.ifba.sididoc.web.dto.DocumentCategoryResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCategoryService {

    private final DocumentCategoryRepository repository;

    public DocumentCategory findById(Long id) {
        log.info("Iniciando busca pela categoria de documento com ID: {}", id);

        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Categoria de documento não encontrada no banco de dados. ID solicitado: {}", id);
                    return new ResourceNotFoundException("Categoria de documentos não encontrada.");
                });
    }

    @Transactional
    public DocumentCategoryResponseDTO save(DocumentCategoryRequestDTO doc) {
        log.info("Tentando criar nova categoria: {}", doc.name());

        if (repository.existsByName(doc.name())) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome.");
        }

        DocumentCategory documentCategory = new DocumentCategory();
        documentCategory.setName(doc.name());
        documentCategory.setDescription(doc.description());
        documentCategory.setActive(true); // Garante que nasce ativa

        DocumentCategory savedCategoryCategory = repository.save(documentCategory);
        log.info("Categoria criada com sucesso: ID {}", savedCategoryCategory.getId());

        return DocumentCategoryResponseDTO.fromEntity(savedCategoryCategory);
    }

    @Transactional(readOnly = true)
    public List<DocumentCategoryResponseDTO> findAllActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream()
                .map(DocumentCategoryResponseDTO::fromEntity)
                .toList(); // .toList() é mais moderno que .collect(Collectors.toList())
    }

    @Transactional
    public void disableCategoryByName(String name) {
        log.info("Solicitação para desativar categoria pelo nome: {}", name);

        //Ache a categoria com tal nome
        DocumentCategory category = repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o nome: " + name));

        // Verificar se já está desativada para evitar update desnecessário
        if (!category.isActive()) {
            log.warn("A categoria '{}' (ID: {}) já estava desativada.", name, category.getId());
            return;
        }

        // Desative (Soft Delete)
        category.setActive(false);

        // Salva a alteração
        repository.save(category);
        log.info("Categoria '{}' (ID: {}) desativada com sucesso.", name, category.getId());
    }

}
