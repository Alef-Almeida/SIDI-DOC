package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.User;

import java.util.List;

public record RegisterUserDTO(
        String name,
        String email,
        User.Role role,
        List<Long> sectorIds
) {
}
