package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.User;
import br.com.ifba.sididoc.enums.Role;

public record UserResponseDTO(
        String name,
        Role role
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getName(),
                user.getRole()
        );
    }
}