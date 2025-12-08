package br.com.ifba.sididoc.repository;

import br.com.ifba.sididoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //Busca Email
    Optional<User> findByEmail(String email);

    //Retorna se um Email já existe
    boolean existsByEmail(String email);

}
