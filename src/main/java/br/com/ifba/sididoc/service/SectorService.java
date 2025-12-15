package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.Sector;
import br.com.ifba.sididoc.exception.ResourceAlreadyExistsException;
import br.com.ifba.sididoc.exception.ResourceInactiveException;
import br.com.ifba.sididoc.exception.ResourceNotFoundException;
import br.com.ifba.sididoc.repository.SectorRepository;
import br.com.ifba.sididoc.util.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectorService {

    private final SectorRepository repository;

    @Transactional
    public Sector create(Sector sector) {
        log.info("Criando novo setor: {}", sector.getName());

        if (repository.existsByName(sector.getName())) {
            throw new ResourceAlreadyExistsException("Já existe um setor com este nome.");
        }
        if (repository.existsByCode(sector.getCode())) {
            throw new ResourceAlreadyExistsException("Já existe um setor com este código.");
        }

        sector.setActive(true);
        sector.setCode(sector.getCode().toUpperCase());

        return repository.save(sector);
    }

    @Transactional
    public Sector update(Long id, Sector sector) {
        log.info("Atualizando setor ID: {}", id);

        Sector existingSector = findById(id);

        if (repository.existsByName(sector.getName())) {
            throw new ResourceAlreadyExistsException("Já existe outro setor com este nome.");
        }

        if (repository.existsByCode(sector.getCode())) {
            throw new ResourceAlreadyExistsException("Já existe outro setor com este código.");
        }

        existingSector.setName(sector.getName());
        existingSector.setDescription(sector.getDescription());
        existingSector.setCode(sector.getCode().toUpperCase());
        existingSector.setActive(sector.isActive());

        return repository.save(existingSector);
    }

    @Transactional
    public void softDelete(Long id) {
        log.warn("Inativando setor ID: {}", id);

        Sector sector = findById(id);

        if (!sector.isActive()) {
            throw new ResourceInactiveException("Este setor já está inativo.");
        }

        sector.setActive(false);
        repository.save(sector);
    }

    public Page<Sector> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Sector> findAllActive(Pageable pageable) {
        return repository.findAllByActiveTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Sector> findByUser(Pageable pageable) {
        return repository.findAllByUserId(UserUtils.getAuthenticatedUserId(), pageable);
    }

    public Sector findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado."));
    }

    public Sector findReferenceById(Long id) {
        return repository.getReferenceById(id);
    }
}