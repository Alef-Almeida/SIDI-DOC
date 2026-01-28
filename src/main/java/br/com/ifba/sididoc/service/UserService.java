package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.*;
import br.com.ifba.sididoc.enums.AuditAction;
import br.com.ifba.sididoc.enums.Role;
import br.com.ifba.sididoc.exception.ResourceAlreadyExistsException;
import br.com.ifba.sididoc.exception.ResourceInactiveException;
import br.com.ifba.sididoc.exception.ResourceNotFoundException;
import br.com.ifba.sididoc.exception.SectorAccessDeniedException;
import br.com.ifba.sididoc.jwt.JwtToken;
import br.com.ifba.sididoc.jwt.JwtUtils;
import br.com.ifba.sididoc.repository.SectorRepository;
import br.com.ifba.sididoc.repository.UserRepository;
import br.com.ifba.sididoc.util.RequestUtils;
import br.com.ifba.sididoc.util.UserUtils;
import br.com.ifba.sididoc.web.dto.*;
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
    private final AuditService auditService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // Registra novo usuário e envia e-mail de ativação
    @Transactional
    public User registerUser(User adminUser, RegisterUserDTO dto) {
        if (adminUser.getRole() != Role.SUPER_ADMIN &&
                adminUser.getRole() != Role.SECTOR_ADMIN) {
            log.error("Você não tem permissão para criar usuários.");
            throw new RuntimeException("Você não tem permissão para criar usuários.");
        }

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("E-mail já cadastrado: {}", dto.email());
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

        if (sectors.size() == 1) {
            user.setCurrentSector(sectors.get(0));
        }

        user = userRepository.save(user);

        log.info("Novo usuário registrado: [{}] [{}]", user.getName(), user.getEmail());

        auditService.log(
                AuditAction.USER_REGISTRATION,
            "Novo usuário registrado: " + user.getEmail(),
            adminUser.getEmail(),
            RequestUtils.getClientIp()
        );

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

        log.info("Enviando e-mail de ativação para: [{}]", user.getEmail());

        auditService.log(
                AuditAction.SEND_ACTIVATION_EMAIL,
            "E-mail de ativação enviado para: " + user.getEmail(),
            "SYSTEM",
            RequestUtils.getClientIp()
        );

        emailService.send(user.getEmail(), "SIDIDOC - Ativação de conta", text);
    }

    // Completa o registro definindo a senha inicial
    @Transactional
    public void completeRegistration(String token, String newPassword) {

        String email = jwtUtils.extractEmailFromActivationToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setIsFirstAccess(false);

        if (user.getCurrentSector() == null &&
                user.getSectors() != null &&
                user.getSectors().size() == 1) {

            user.setCurrentSector(user.getSectors().get(0));
        }

        log.info("Usuário [{}] definiu sua senha e pode acessar o sistema.", user.getEmail());

        auditService.log(
            AuditAction.COMPLETE_REGISTRATION,
            "Usuário completou o registro: " + user.getEmail(),
            user.getEmail(),
            RequestUtils.getClientIp()
        );

        userRepository.save(user);
        //ADD email de confirmação de cadastro
    }

    // Solicita redefinição de senha | usuarios ja registrados
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Erro ao solicitar redefinição de senha: Usuário com email [{}] não encontrado no sistema.", email);
                    return new RuntimeException("Usuário não encontrado.");
                });

        String token = jwtUtils.generateResetPasswordToken(user);
        String link = frontendUrl + "/redefinir-senha?token=" + token;

        String text = """
                Olá, %s!

                Você solicitou a redefinição de senha do SIDIDOC.
                Clique no link abaixo para criar uma nova senha:

                %s

                Se não foi você, ignore este e-mail.
                """.formatted(user.getName(), link);

        log.info("Enviando e-mail de redefinição de senha para: [{}]", user.getEmail());

        auditService.log(
                AuditAction.SEND_PASSWORD_RESET_EMAIL,
            "E-mail de redefinição de senha enviado para: " + user.getEmail(),
            "SYSTEM",
            RequestUtils.getClientIp()
        );

        emailService.send(user.getEmail(), "SIDIDOC - Redefinição de senha", text);
    }

    //Redefinir a senha para usuarios ja registrados
    public void resetPassword(String token, String newPassword) {
        String email = jwtUtils.extractEmailFromResetToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Erro ao redefinir senha: Usuário com email [{}] não encontrado no sistema.", email);
                    return new RuntimeException("Usuário não encontrado.");
                });

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        log.info("Usuário [{}] redefiniu sua senha com sucesso.", user.getEmail());

        auditService.log(
                AuditAction.RESET_PASSWORD,
            "Usuário redefiniu a senha: " + user.getEmail(),
            user.getEmail(),
            RequestUtils.getClientIp()
        );

        userRepository.save(user);
    }

    //Apenas para obter o email do usuario logado
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + email));
    }

    //Listar usuarios que ainda não ativaram a conta
    @Transactional(readOnly = true)
    public List<UserResponseDTO> listPendingUsers() {
        log.info("Listando usuários que ainda não ativaram a conta.");
        return userRepository.findByIsFirstAccessTrue()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    //Listar usuarios que já ativaram a conta
    @Transactional(readOnly = true)
    public List<UserResponseDTO> listActivatedUsers() {
        log.info("Listando usuários que já ativaram a conta.");
        return userRepository.findByIsFirstAccessFalse()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    //Reenviar email caso a conta ainda não tenha sido ativada
    public void resendActivationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Erro ao reenviar e-mail de ativação: Usuário com email [{}] não encontrado no sistema.", email);
                    return new RuntimeException("Usuário não encontrado.");
                });

        if (!Boolean.TRUE.equals(user.getIsFirstAccess())) {
            log.warn("Usuário [{}], já ativou a conta no sistema.", email);
            throw new RuntimeException("Este usuário já ativou a conta.");
        }

        log.info("Reenviando e-mail de ativação para o usuário [{}].", email);
        sendActivationEmail(user);
    }

    @Transactional
    public JwtToken switchSector(String currentToken, Long newSectorId) {
        String cleanToken = currentToken.replace("Bearer ", "");
        String email = jwtUtils.extractUsername(cleanToken);

        log.info("Solicitação de troca de contexto: Usuário [{}] tentando acessar o Setor ID [{}]", email, newSectorId);
        auditService.log(
                AuditAction.SECTOR_SWITCH_ATTEMPT,
            "Tentativa de troca de setor para ID " + newSectorId,
            email,
            RequestUtils.getClientIp()
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Erro crítico na troca de setor: Usuário com email [{}] não encontrado no banco.", email);
                    return new ResourceNotFoundException("Usuário não encontrado.");
                });

        boolean hasAccess = user.getSectors().stream()
                .anyMatch(s -> s.getId().equals(newSectorId));

        if (!hasAccess) {
            log.warn("ACESSO NEGADO: O usuário [{}] tentou acessar o Setor ID [{}] mas não possui permissão.", email, newSectorId);
            auditService.log(
                    AuditAction.SECTOR_ACCESS_DENIED,
                "Tentativa de acesso negado ao setor ID " + newSectorId,
                email,
                RequestUtils.getClientIp()
            );

            throw new SectorAccessDeniedException("Usuário não tem acesso a este setor.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        String newToken = jwtUtils.generateToken(userDetails, newSectorId);

        log.info("Troca de setor realizada com sucesso. Usuário [{}] agora está operando no Setor ID [{}].", email, newSectorId);
        auditService.log(
                AuditAction.SECTOR_SWITCH_SUCCESS,
            "Troca de setor bem sucedida para ID " + newSectorId,
            email,
            RequestUtils.getClientIp()
        );

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

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o email: " + email));
    }

    @Transactional
    public void addToSector(String sectorCode, String userEmail) {
        log.info("Vinculando usuário [{}] ao setor [{}]", userEmail, sectorCode);

        Sector sector = sectorRepository.findByCode(sectorCode)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado com código: " + sectorCode));

        if (!sector.isActive()) {
            throw new ResourceInactiveException("O setor está desativado.");
        }

        User user = findByEmail(userEmail);

        if (user.getSectors().contains(sector)) {
            throw new ResourceAlreadyExistsException("Usuário já está vinculado a este setor.");
        }

        user.getSectors().add(sector);
        sector.getUsers().add(user);

        if (user.getCurrentSector() == null) {
            user.setCurrentSector(sector);
            log.info("Setor [{}] definido como setor atual do usuário [{}].",
                    sector.getCode(), user.getEmail());
            auditService.log(
                    AuditAction.SET_CURRENT_SECTOR,
                "Setor " + sector.getCode() + " definido como setor atual.",
                user.getEmail(),
                RequestUtils.getClientIp()
            );
        }

        userRepository.save(user);
        log.info("Vínculo salvo com sucesso.");
    }

    @Transactional
    public void removeFromSector(String sectorCode, String userEmail) {
        log.info("Removendo vínculo: Usuário [{}] do Setor [{}]", userEmail, sectorCode);

        Sector sector = sectorRepository.findByCode(sectorCode)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado com código: " + sectorCode));

        User user = findByEmail(userEmail);

        if (!user.getSectors().contains(sector)) {
            throw new ResourceNotFoundException("O usuário não pertence a este setor.");
        }

        user.getSectors().remove(sector);
        sector.getUsers().remove(user);

        userRepository.save(user);
        log.info("Vínculo removido com sucesso.");
        auditService.log(
                AuditAction.REMOVE_USER_FROM_SECTOR,
            "Usuário removido do setor " + sector.getCode(),
            "SYSTEM",
            RequestUtils.getClientIp()
        );
    }

    @Transactional(readOnly = true)
    public User me() {
        String email = UserUtils.getAuthenticatedUserEmail();
        return userRepository.findByEmailWithSectors(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }

    //Listar todos os usuarios
    @Transactional(readOnly = true)
    public List<UserResponseDTO> listAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    //Atualizar perfil do próprio usuário
    @Transactional
    public UserResponseDTO updateMyProfile(UpdateMyProfileDTO dto) {

        User user = me();

        if (!user.getEmail().equals(dto.email())
                && userRepository.existsByEmail(dto.email())) {
            log.warn("Esse e-mail ja está em uso.");
            throw new ResourceAlreadyExistsException("Este e-mail já está em uso.");
        }

        user.setName(dto.name());
        user.setEmail(dto.email());

        auditService.log(
                AuditAction.UPDATE_MY_PROFILE,
            "Usuário atualizou seu perfil.",
            user.getEmail(),
            RequestUtils.getClientIp()
        );

        log.info("Atualizando informações do próprio usuário: {}", dto.name());
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    //Faz o update de um usuário sendo Admin, alterando email e role
    @Transactional
    public UserResponseDTO updateUser(Long userId, UpdateUserDTO dto) {

        User user = findById(userId);

        if (!user.getEmail().equals(dto.email())
                && userRepository.existsByEmail(dto.email())) {
            log.warn("Esse e-mail ja está em uso por outro usuário.");
            throw new ResourceAlreadyExistsException("Este e-mail já está em uso.");
        }

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setRole(dto.role());

        auditService.log(
                AuditAction.UPDATE_USER_PROFILE,
            "Administrador atualizou o perfil do usuário ID " + userId,
            UserUtils.getAuthenticatedUserEmail(),
            RequestUtils.getClientIp()
        );

        log.info("Atualizando informações do usuário: {}", dto.name());
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    //Logica de negocio para deletar usuario no sistema
    @Transactional
    public void deleteUser(Long userId) {

        User me = me();
        User userDelete = findById(userId);

        if (me.getId().equals(userId)) {
            auditService.log(
                    AuditAction.DELETE_OWN_USER_ATTEMPT,
                "Tentativa de deletar o próprio usuário.",
                me.getEmail(),
                RequestUtils.getClientIp()
            );
            log.warn("Usuário [{}] tentou deletar o próprio usuário.", me.getName());
            throw new ResourceNotFoundException("Você não pode deletar o próprio usuário.");
        }

        if (userDelete.getRole() == Role.SUPER_ADMIN) {
            auditService.log(
                    AuditAction.DELETE_USER_ADMIN,
                "Tentativa de deletar Administrador: " + userDelete.getEmail(),
                me.getEmail(),
                RequestUtils.getClientIp()
            );
            log.warn("Tentativa de deletar um Administrador [{}]", userDelete.getName());
            throw new ResourceNotFoundException("Não é permitido deletar um Administrador.");
        }

        if (me.getRole() == Role.SECTOR_ADMIN &&
                userDelete.getRole() != Role.OPERATOR) {

            auditService.log(
                    AuditAction.DELETE_USER_PERMISSION_DENIED,
                "Setor Admin tentou deletar usuário com permissão maior ou igual: " + userDelete.getEmail(),
                me.getEmail(),
                RequestUtils.getClientIp()
            );

            log.warn("Setor Admin [{}] tentou deletar usuário com permissão maior ou igual.", me.getName());
            throw new ResourceNotFoundException("Você não tem permissão para deletar este usuário.");
        }

        auditService.log(
                AuditAction.DELETE_USER,
            "Usuário deletado: " + userDelete.getEmail(),
            me.getEmail(),
            RequestUtils.getClientIp()
        );
        log.warn("Usuário [{}] foi deletado do sistema por [{}].", userDelete.getName(), me.getName());
        userRepository.delete(userDelete);
    }


}
