package br.com.ifba.sididoc.entity;

import br.com.ifba.sididoc.enums.DestinacaoFinal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plano_classificacao")
@Getter
@Setter
public class PlanoClassificacao extends PersistenceEntity{
    @Column(unique = true, nullable = false, length = 20)
    private String codigo;

    // Referência hierárquica usando o Código (Natural Key)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pai_codigo", referencedColumnName = "codigo")
    private PlanoClassificacao pai;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "notas_escopo", columnDefinition = "TEXT")
    private String notasEscopo;

    @Column(name = "prazo_corrente_anos")
    private Integer prazoCorrenteAnos;

    @Column(name = "prazo_intermediario_anos")
    private Integer prazoIntermediarioAnos;

    @Enumerated(EnumType.STRING)
    @Column(name = "destinacao_final")
    private DestinacaoFinal destinacaoFinal;

    @Column(name = "evento_gatilho")
    private String eventoGatilho;

    @Column(nullable = false)
    private Boolean ativo = true;
}
