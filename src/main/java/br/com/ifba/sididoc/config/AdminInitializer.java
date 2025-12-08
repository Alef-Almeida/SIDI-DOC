package br.com.ifba.sididoc.config;

import br.com.ifba.sididoc.entity.User;
import br.com.ifba.sididoc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//Classe criada apenas para inicializar o sistema com um usuario Admin

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        //Se já existir pelo menos 1 usuário, não faz nada
        //Como o banco apenas é create, sempre irá criar um user
        if (userRepository.count() > 0) {
            System.out.println("Ja existe um usuario no sistema!");
            return;

        }else {
            System.out.println("Nenhum usuario encontrado no sistema, fazendo o cadastro do Admin");
        }

        User admin = User.builder()
                .name("Administrador")
                .email("admin@ifba.edu.br")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(User.Role.SUPER_ADMIN)
                .isFirstAccess(false)
                .createdDate(LocalDateTime.now())
                .build();

        userRepository.save(admin);

    }
}
