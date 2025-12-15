package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.Sector;

public record SectorResponseDTO(
        Long id,
        String name,
        String code,
        String description,
        boolean active
) {
    public static SectorResponseDTO fromEntity(Sector sector) {
        return new SectorResponseDTO(
                sector.getId(),
                sector.getName(),
                sector.getCode(),
                sector.getDescription(),
                sector.isActive()
        );
    }
}
