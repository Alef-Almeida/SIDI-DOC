package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.User;
import br.com.ifba.sididoc.service.UserService;
import br.com.ifba.sididoc.web.dto.RegisterUserDTO;
import br.com.ifba.sididoc.web.dto.SectorResponseDTO;
import br.com.ifba.sididoc.web.dto.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me() {
        User user = userService.me();
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }

    //Só é autorizado caso o usuário logado seja SUPER_ADMIN ou SECTOR_ADMIN
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @AuthenticationPrincipal UserDetails adminDetails,
            @Valid @RequestBody RegisterUserDTO dto
    ) {

        User admin = userService.getByEmail(adminDetails.getUsername());

        userService.registerUser(admin, dto);

        return ResponseEntity.ok("Usuário criado com sucesso. E-mail de ativação enviado.");
    }

    @GetMapping(value = "/{userId}/sectors")
    public ResponseEntity<List<SectorResponseDTO>> findSectorsByUser(
            @PathVariable(value = "userId") Long userId) {

        List<SectorResponseDTO> list = userService.findSectorsByUserId(userId);

        // Se a lista for vazia, ainda retornamos 200 OK com array vazio [],
        // pois não é um erro, o usuário só não tem setor ainda.
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    @PostMapping("/add-to-sector")
    public ResponseEntity<Void> addUserToSector(@RequestParam String email, @RequestParam String sectorCode) {
        userService.addToSector(sectorCode, email);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    @DeleteMapping("/remove-from-sector")
    public ResponseEntity<Void> removeUserFromSector(@RequestParam String email, @RequestParam String sectorCode) {
        userService.removeFromSector(sectorCode, email);
        return ResponseEntity.noContent().build();
    }

    //Reenvio de email de ativação
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    @PostMapping("/resend-activation")
    public ResponseEntity<String> resendActivation(@RequestParam String email) {

        userService.resendActivationEmail(email);
        return ResponseEntity.ok("E-mail de ativação reenviado com sucesso.");
    }

    //Usuarios que ainda nao se cadastraram
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> listPending() {
        return ResponseEntity.ok(userService.listPendingUsers());
    }

    //Usuarios ja cadastrados
    @GetMapping("/activated")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SECTOR_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> listActivated() {
        return ResponseEntity.ok(userService.listActivatedUsers());
    }


}
