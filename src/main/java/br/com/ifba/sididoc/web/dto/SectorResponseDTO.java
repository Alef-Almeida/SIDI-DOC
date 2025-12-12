package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.Sector;

public record SectorResponseDTO(
        String name,
        String code,
        String description
) {
    public static SectorResponseDTO fromEntity(Sector sector) {
        return new SectorResponseDTO(
                sector.getName(),
                sector.getCode(),
                sector.getDescription()
        );
    }
}
