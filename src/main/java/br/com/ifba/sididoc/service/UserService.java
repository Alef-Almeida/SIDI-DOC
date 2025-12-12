package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.*;
import br.com.ifba.sididoc.enums.Role;
import br.com.ifba.sididoc.exception.ResourceAlreadyExistsException;
import br.com.ifba.sididoc.exception.ResourceInactiveException;
import br.com.ifba.sididoc.exception.ResourceNotFoundException;
import br.com.ifba.sididoc.exception.SectorAccessDeniedException;
import br.com.ifba.sididoc.jwt.JwtToken;
import br.com.ifba.sididoc.jwt.JwtUtils;
import br.com.ifba.sididoc.repository.SectorRepository;
import br.com.ifba.sididoc.repository.UserRepository;
import br.com.ifba.sididoc.util.UserUtils;
import br.com.ifba.sididoc.web.dto.RegisterUserDTO;
import br.com.ifba.sididoc.web.dto.SectorResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // Registra novo usuário e envia e-mail de ativação
    public User registerUser(User adminUser, RegisterUserDTO dto) {
        if (adminUser.getRole() != Role.SUPER_ADMIN &&
                adminUser.getRole() != Role.SECTOR_ADMIN) {
            throw new RuntimeException("Você não tem permissão para criar usuários.");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("E-mail já cadastrado.");
        }

        List<Sector> sectors = sectorRepository.findAllById(dto.sectorIds());

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .role(dto.role())
                .sectors(sectors)
                .isFirstAccess(true)
                .passwordHash(null)
                .build();

        user = userRepository.save(user);

        sendActivationEmail(user);
        return user;
    }

    // Envia e-mail de ativação para novo usuário
    public void sendActivationEmail(User user) {
        String token = jwtUtils.generateActivationToken(user);
        String link = frontendUrl + "/ativar-conta?token=" + token;

        String text = """
                Olá, %s!

                Sua conta no SIDIDOC foi criada pelo administrador.
                Clique no link abaixo para definir sua senha:

                %s

                Este link expira em 24 horas.
                """.formatted(user.getName(), link);

        emailService.send(user.getEmail(), "SIDIDOC - Ativação de conta", text);
    }

    // Completa o registro definindo a senha inicial
    public void completeRegistration(String token, String newPassword) {

        String email = jwtUtils.extractEmailFromActivationToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setIsFirstAccess(false);

        userRepository.save(user);
        //ADD email de confirmação de cadastro
    }

    // Solicita redefinição de senha | usuarios ja registrados
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        String token = jwtUtils.generateResetPasswordToken(user);
        String link = frontendUrl + "/redefinir-senha?token=" + token;

        String text = """
                Olá, %s!

                Você solicitou a redefinição de senha do SIDIDOC.
                Clique no link abaixo para criar uma nova senha:

                %s

                Se não foi você, ignore este e-mail.
                """.formatted(user.getName(), link);

        emailService.send(user.getEmail(), "SIDIDOC - Redefinição de senha", text);
    }

    //Redefinir a senha para usuarios ja registrados
    public void resetPassword(String token, String newPassword) {
        String email = jwtUtils.extractEmailFromResetToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    //Apenas para obter o email do usuario logado
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
    }

    public JwtToken switchSector(String currentToken, Long newSectorId) {
        String cleanToken = currentToken.replace("Bearer ", "");
        String email = jwtUtils.extractUsername(cleanToken);

        log.info("Solicitação de troca de contexto: Usuário [{}] tentando acessar o Setor ID [{}]", email, newSectorId);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Erro crítico na troca de setor: Usuário com email [{}] não encontrado no banco.", email);
                    return new ResourceNotFoundException("Usuário não encontrado.");
                });

        boolean hasAccess = user.getSectors().stream()
                .anyMatch(s -> s.getId().equals(newSectorId));

        if (!hasAccess) {
            log.warn("ACESSO NEGADO: O usuário [{}] tentou acessar o Setor ID [{}] mas não possui permissão.", email, newSectorId);
            throw new SectorAccessDeniedException("Usuário não tem acesso a este setor.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String newToken = jwtUtils.generateToken(userDetails, newSectorId);

        log.info("Troca de setor realizada com sucesso. Usuário [{}] agora está operando no Setor ID [{}].", email, newSectorId);

        return new JwtToken(newToken);
    }

    @Transactional(readOnly = true)
    public List<SectorResponseDTO> findSectorsByUserId(Long userId) {
        log.info("Buscando setores para o usuário ID: {}", userId);

        // Garante que o usuário já existe
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Usuário não encontrado");
        }

        // "SELECT s FROM User u JOIN u.sectors s WHERE u.id = :userId"
        List<Sector> sectors = sectorRepository.findAllByUserId(userId);

        return sectors.stream()
                .map(SectorResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public User me() {
        String email = UserUtils.getAuthenticatedUserEmail();
        return findByEmail(email);
    }
}
