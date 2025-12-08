package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    // Adicionar métodos caso necessário

}
