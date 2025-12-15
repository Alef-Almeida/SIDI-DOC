package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.Sector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectorCreateDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,
        @NotBlank(message = "O código é obrigatório.")
        String code,
        @Size(max = 1000, message = "A descrição não pode ter mais de 1000 caracteres.")
        String description
) {
        public Sector toEntity() {
                Sector sector = new Sector();
                sector.setName(this.name);
                sector.setCode(this.code);
                sector.setDescription(this.description);
                return sector;
        }
}