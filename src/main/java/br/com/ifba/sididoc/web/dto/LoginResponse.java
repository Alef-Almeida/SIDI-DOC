package br.com.ifba.sididoc.web.dto;

import java.util.List;

//Usando record para melhoria do código
public record LoginResponse(
        String token,
        String role,
        String name,
        List<SectorResponseDTO> sectors
) {}
