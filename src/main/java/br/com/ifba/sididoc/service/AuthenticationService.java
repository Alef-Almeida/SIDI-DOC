package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.User;
import br.com.ifba.sididoc.enums.AuditAction;
import br.com.ifba.sididoc.jwt.JwtUtils;
import br.com.ifba.sididoc.repository.SectorRepository;
import br.com.ifba.sididoc.repository.UserRepository;
import br.com.ifba.sididoc.util.RequestUtils;
import br.com.ifba.sididoc.web.dto.LoginRequest;
import br.com.ifba.sididoc.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SectorRepository sectorRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        UserDetails principal = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow();

        Long sectorId = user.getCurrentSector() != null
                ? user.getCurrentSector().getId()
                : null;

        String token = jwtUtils.generateToken(principal, sectorId);

        String role = principal.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("UNKNOWN");

        auditService.log(
                AuditAction.LOGIN,
                "Usuário realizou login com sucesso",
                principal.getUsername(),
                RequestUtils.getClientIp()
        );

        return new LoginResponse(token, role, principal.getUsername(), new ArrayList<>());
    }
}