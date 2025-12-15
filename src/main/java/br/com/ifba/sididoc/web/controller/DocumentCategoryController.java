package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.service.DocumentCategoryService;
import br.com.ifba.sididoc.web.dto.DocumentCategoryRequestDTO;
import br.com.ifba.sididoc.web.dto.DocumentCategoryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<DocumentCategoryResponseDTO>> findAllActive() {
        List<DocumentCategoryResponseDTO> list = service.findAllActive();
        return ResponseEntity.ok(list);
    }


    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    @DeleteMapping(value = "/disable/{name}")
    public ResponseEntity<Void> disableByName(@PathVariable String name) {
        service.disableCategoryByName(name);

        // Retorna 204 (No Content) porque a operação deu certo e não tem corpo de resposta
        return ResponseEntity.noContent().build();
    }
}