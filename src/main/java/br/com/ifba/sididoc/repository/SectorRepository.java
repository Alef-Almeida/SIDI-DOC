package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.Sector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    @Query("SELECT s FROM Sector s JOIN s.users u WHERE u.id = :userId")
    List<Sector> findAllByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT s FROM Sector s JOIN s.users u WHERE u.id = :userId",
            countQuery = "SELECT COUNT(s) FROM Sector s JOIN s.users u WHERE u.id = :userId")
    Page<Sector> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Page<Sector> findAllByActiveTrue(Pageable pageable);

    Optional<Sector> findByCode(String code);
}
