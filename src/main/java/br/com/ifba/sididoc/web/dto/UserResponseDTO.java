package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.User;
import br.com.ifba.sididoc.enums.Role;

import java.util.List;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        Role role,
        List<SectorResponseDTO> sectors
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getSectors().stream()
                        .map(SectorResponseDTO::fromEntity)
                        .toList()
        );
    }
}
