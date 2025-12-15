package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //Busca Email
    Optional<User> findByEmail(String email);

    //Retorna se um Email já existe
    boolean existsByEmail(String email);

    //Retornar quem ainda não concluiu o primeiro acesso
    List<User> findByIsFirstAccessTrue();

    //Retornar lista de usuarios por setor
    List<User> findBySectors_Id(Long sectorId);

    //Retornar lista de usuarios cadastrados
    List<User> findByIsFirstAccessFalse();

    @Query("""
    SELECT u
    FROM User u
    LEFT JOIN FETCH u.sectors
    WHERE u.email = :email
""")
    Optional<User> findByEmailWithSectors(@Param("email") String email);
}
