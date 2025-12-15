package br.com.ifba.sididoc.config;

import br.com.ifba.sididoc.entity.Sector;
import br.com.ifba.sididoc.entity.User;
import br.com.ifba.sididoc.enums.Role;
import br.com.ifba.sididoc.repository.SectorRepository;
import br.com.ifba.sididoc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

//Classe criada apenas para inicializar o sistema com um usuario Admin

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SectorRepository sectorRepository;

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

        //Removido a criação em builder para setar os campos de auditoria
        User admin = new User();
        admin.setName("Administrador");
        admin.setEmail("admin@ifba.edu.br");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRole(Role.SUPER_ADMIN);
        admin.setIsFirstAccess(false);

        admin.setCreatedAt(LocalDateTime.now());
        admin.setLastModifiedAt(LocalDateTime.now());
        admin.setCreatedBy("...");
        admin.setLastModifiedBy("...");

        userRepository.save(admin);

        //Criar setor ao iniciar
        if (sectorRepository.count() == 0) {
            System.out.println("Nenhum setor encontrado. Criando setores padrão...");

            LocalDateTime now = LocalDateTime.now();

            List<Sector> defaultSectors = List.of(
                    createSector("Administração", "SECADM", "Setor Administrativo", now),
                    createSector("Compras", "COM", "Setor de Compras", now),
                    createSector("RH", "RH", "Setor de Recursos Humanos", now),
                    createSector("Tesouraria", "TES", "Setor de Tesouraria", now),
                    createSector("Contabilidade", "SME", "Setor de Contabilidade", now)
            );

            sectorRepository.saveAll(defaultSectors);
        }

    }

    private Sector createSector(String name, String code, String desc, LocalDateTime now) {
        Sector setor = new Sector();
        setor.setName(name);
        setor.setCode(code);
        setor.setDescription(desc);

        setor.setCreatedBy("...");
        setor.setCreatedAt(now);
        setor.setLastModifiedBy("...");
        setor.setLastModifiedAt(now);

        return setor;
    }
}
