package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.Sector;
import br.com.ifba.sididoc.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectorService {

    private final SectorRepository repository;

    public Sector findReferenceById(Long id) {
        return repository.getReferenceById(id);
    }

}
