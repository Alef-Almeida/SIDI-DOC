package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.PlanoClassificacao;

public record PlanoClassificacaoDTO(
        Long id,
        String codigo,
        String titulo,
        Integer prazoCorrenteAnos,
        Integer prazoIntermediarioAnos,
        String destinacaoFinal,
        String eventoGatilho
) {
    public static PlanoClassificacaoDTO fromEntity(PlanoClassificacao entity) {
        return new PlanoClassificacaoDTO(
                entity.getId(),
                entity.getCodigo(),
                entity.getTitulo(),
                entity.getPrazoCorrenteAnos(),
                entity.getPrazoIntermediarioAnos(),
                entity.getDestinacaoFinal() != null ? entity.getDestinacaoFinal().name() : null,
                entity.getEventoGatilho()
        );
    }
}
