package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.User;
import br.com.ifba.sididoc.service.UserService;
import br.com.ifba.sididoc.web.dto.RegisterUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @AuthenticationPrincipal UserDetails adminDetails,
            @RequestBody RegisterUserDTO dto
    ) {

        User admin = userService.getByEmail(adminDetails.getUsername());

        userService.registerUser(admin, dto);

        return ResponseEntity.ok("Usuário criado com sucesso. E-mail de ativação enviado.");
    }

}
