package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.*;
import br.com.ifba.sididoc.enums.Role;
import br.com.ifba.sididoc.jwt.JwtUtils;
import br.com.ifba.sididoc.repository.SectorRepository;
import br.com.ifba.sididoc.repository.UserRepository;
import br.com.ifba.sididoc.web.dto.RegisterUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

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

}
