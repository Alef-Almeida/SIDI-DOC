package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.Sector;
import br.com.ifba.sididoc.service.SectorService;
import br.com.ifba.sididoc.web.dto.SectorCreateDTO;
import br.com.ifba.sididoc.web.dto.SectorResponseDTO;
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
@RequestMapping("/sectors")
@RequiredArgsConstructor
public class SectorController {

    private final SectorService service;

    @GetMapping(value = "/find-all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    public ResponseEntity<Page<SectorResponseDTO>> findAll(@PageableDefault(page = 0, size = 24, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Sector> pageEntities = service.findAll(pageable);
        return ResponseEntity.ok(pageEntities.map(SectorResponseDTO::fromEntity));
    }

    @GetMapping(value = "/find-all-active")
    public ResponseEntity<Page<SectorResponseDTO>> findAllActive(@PageableDefault(page = 0, size = 24, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Sector> pageEntities = service.findAllActive(pageable);
        return ResponseEntity.ok(pageEntities.map(SectorResponseDTO::fromEntity));
    }

    @GetMapping(value = "/find-by-id", params = "id")
    public ResponseEntity<SectorResponseDTO> findById(@RequestParam("id") Long id) {
        Sector sector = service.findById(id);
        return ResponseEntity.ok(SectorResponseDTO.fromEntity(sector));
    }

    @GetMapping("/find-my-sectors")
    public ResponseEntity<Page<SectorResponseDTO>> findMySectors(@PageableDefault(page = 0, size = 24, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Sector> pageEntities = service.findByUser(pageable);
        Page<SectorResponseDTO> pageDtos = pageEntities.map(SectorResponseDTO::fromEntity);
        return ResponseEntity.ok(pageDtos);
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    public ResponseEntity<SectorResponseDTO> create(@RequestBody @Valid SectorCreateDTO dto) {
        Sector sector = service.create(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(SectorResponseDTO.fromEntity(sector));
    }

    @PutMapping(value = "/update", params = "id")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    public ResponseEntity<SectorResponseDTO> update(@RequestParam("id") Long id, @RequestBody @Valid SectorCreateDTO dto) {
        Sector sector = service.update(id, dto.toEntity());
        return ResponseEntity.ok(SectorResponseDTO.fromEntity(sector));
    }

    @DeleteMapping(value = "/soft-delete", params = "id")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    public ResponseEntity<Void> softDelete(@RequestParam("id") Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}