package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.DocumentCategory;
import br.com.ifba.sididoc.service.DocumentCategoryService;
import br.com.ifba.sididoc.web.dto.DocumentCategoryRequestDTO;
import br.com.ifba.sididoc.web.dto.DocumentCategoryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents-categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // ajuste depois
public class DocumentCategoryController {

    private final DocumentCategoryService service;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    @PostMapping(value = "/save")
    public ResponseEntity<DocumentCategoryResponseDTO> save(
            @RequestBody @Valid DocumentCategoryRequestDTO dto) {

        DocumentCategoryResponseDTO response = service.save(dto);

        // Retorna status 201 (Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping(value = "/find-all")
    public ResponseEntity<Page<DocumentCategoryResponseDTO>> findAllActive(@PageableDefault(page = 0, size = 24, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DocumentCategory> pageEntities = service.findAllActive(pageable);
        return ResponseEntity.ok(pageEntities.map(DocumentCategoryResponseDTO::fromEntity));
    }


    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    @DeleteMapping(value = "/disable/{name}")
    public ResponseEntity<Void> disableByName(@PathVariable String name) {
        service.disableCategoryByName(name);

        // Retorna 204 (No Content) porque a operação deu certo e não tem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}
