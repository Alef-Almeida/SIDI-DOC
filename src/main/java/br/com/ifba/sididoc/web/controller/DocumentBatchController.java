package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.DocumentBatch;
import br.com.ifba.sididoc.service.DocumentBatchService;
import br.com.ifba.sididoc.web.dto.CreateBatchDTO;
import br.com.ifba.sididoc.web.dto.DocumentBatchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class DocumentBatchController {

    private final DocumentBatchService batchService;

    @GetMapping(value = "/find-by-code", params = "code")
    public ResponseEntity<DocumentBatch> findByCode(@RequestParam("code") String code) {
        DocumentBatch batch = batchService.findByCode(code);
        return ResponseEntity.ok(batch);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<DocumentBatch> create(@RequestBody @Valid CreateBatchDTO dto) {
        DocumentBatch created = batchService.create(dto.code(), dto.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping(path = "/find-all")
    public ResponseEntity<Page<DocumentBatchResponse>> findAll(
            @PageableDefault(size = 24, direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.listAll(pageable));
    }
}