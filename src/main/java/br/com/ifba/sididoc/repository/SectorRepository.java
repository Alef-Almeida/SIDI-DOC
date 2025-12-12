package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    // JPQL: "Selecione os setores S que estão dentro da lista de setores do usuário U"
    @Query("SELECT Sector FROM User u JOIN u.sectors s WHERE u.id = :userId")
    List<Sector> findAllByUserId(@Param("userId") Long userId);
}
