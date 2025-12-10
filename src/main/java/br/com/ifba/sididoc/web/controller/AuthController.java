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

    //Faz o login do usuario ja cadastrado
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    //Recebe o token e valida o cadastro
    @PostMapping("/complete-registration")
    public ResponseEntity<String> completeRegistration(
            @Valid @RequestBody CompleteRegistrationDTO dto
    ) {
        userService.completeRegistration(dto.token(), dto.newPassword());
        return ResponseEntity.ok("Senha definida com sucesso.");
    }

    //Envia o email para redefinir a senha
    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(
            @RequestBody ResetPasswordRequestDTO dto
    ) {
        userService.requestPasswordReset(dto.email());
        return ResponseEntity.ok("E-mail de redefinição enviado, se o usuário existir.");
    }

    //Recebe o token e senha para alteração
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordDTO dto
    ) {
        userService.resetPassword(dto.token(), dto.newPassword());
        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }
}
