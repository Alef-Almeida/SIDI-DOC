package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.service.AuthenticationService;
import br.com.ifba.sididoc.service.UserService;
import br.com.ifba.sididoc.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<String> completeRegistration(
            @Valid @RequestBody CompleteRegistrationDTO dto
    ) {
        userService.completeRegistration(dto.token(), dto.newPassword());
        return ResponseEntity.ok("Senha definida com sucesso.");
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(
            @RequestBody ResetPasswordRequestDTO dto
    ) {
        userService.requestPasswordReset(dto.email());
        return ResponseEntity.ok("E-mail de redefinição enviado, se o usuário existir.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordDTO dto
    ) {
        userService.resetPassword(dto.token(), dto.newPassword());
        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }
}
