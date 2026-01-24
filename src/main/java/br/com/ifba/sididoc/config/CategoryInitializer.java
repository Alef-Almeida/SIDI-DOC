package br.com.ifba.sididoc.config;

import br.com.ifba.sididoc.entity.DocumentCategory;
import br.com.ifba.sididoc.repository.DocumentCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryInitializer implements CommandLineRunner {

    private final DocumentCategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        log.info("Verificando e inicializando categorias de documentos...");

        LocalDateTime now = LocalDateTime.now();

        // DECRETOS
// Palavras-chave: Chefe do Executivo, Regulamentação, Crédito Suplementar, Calamidade.
        createCategoryIfMissing("Decretos",
                "Ato administrativo normativo de competência exclusiva do Chefe do Executivo (Prefeito). Utilizado para regulamentar leis, abrir créditos suplementares, declarar utilidade pública, desapropriações, decretar estado de calamidade ou emergência e dispor sobre a organização da administração municipal.", now);

// PORTARIAS
// Palavras-chave: Servidores, Nomeação, Exoneração, Férias, Sindicância.
        createCategoryIfMissing("Portarias",
                "Atos administrativos ordinatórios expedidos por autoridades (Prefeito, Secretários) para disciplinar o funcionamento interno. Abrange nomeação e exoneração de servidores, concessão de férias, licenças, gratificações, diárias, designação de fiscais de contrato e instauração de sindicâncias ou processos disciplinares.", now);

// LEIS MUNICIPAIS
// Palavras-chave: Câmara, Sanção, Promulgação, Artigo, Complementar.
        createCategoryIfMissing("Leis Municipais",
                "Normas jurídicas supremas no âmbito municipal, aprovadas pela Câmara de Vereadores e sancionadas ou promulgadas pelo Prefeito. Inclui Lei Orgânica, Leis Ordinárias, Leis Complementares, Plano Diretor, Código Tributário e as leis orçamentárias (PPA, LDO, LOA).", now);

// EDITAIS DE LICITAÇÃO
// Palavras-chave: Pregão, Concorrência, Chamamento, Termo de Referência, 14.133, 8.666.
        createCategoryIfMissing("Editais de Licitação",
                "Instrumento convocatório que estabelece as regras para contratação de bens, obras e serviços. Contém o objeto, prazos, critérios de julgamento, termo de referência e minuta contratual. Vinculado a modalidades como Pregão (Eletrônico/Presencial), Concorrência, Tomada de Preços, Leilão e Chamamento Público.", now);

// CONTRATOS ADMINISTRATIVOS
// Palavras-chave: Cláusula, Vigência, Contratante, Contratada, Aditivo, Valor Global.
        createCategoryIfMissing("Contratos Administrativos",
                "Instrumento jurídico que formaliza o acordo de vontades entre a Administração Pública (Contratante) e particulares (Contratada). Define obrigações, objeto, vigência, valor global, dotação orçamentária e penalidades. Inclui Termos de Contrato originais, Termos Aditivos (prazo/valor), Apostilamentos e Rescisões.", now);

// NOTAS FISCAIS
// Palavras-chave: NF-e, NFS-e, DANFE, Fornecedor, CNPJ, Liquidação.
        createCategoryIfMissing("Notas Fiscais",
                "Documentos fiscais obrigatórios para a comprovação da despesa pública, aquisição de materiais ou prestação de serviços. Inclui Nota Fiscal Eletrônica (NF-e, NFS-e), DANFE, faturas e recibos emitidos por fornecedores, essenciais para a fase de liquidação da despesa.", now);

// OFÍCIOS
// Palavras-chave: Solicitação, Encaminhamento, Resposta, Gabinete, Comunicação Externa.
        createCategoryIfMissing("Ofícios",
                "Modalidade de comunicação oficial predominantemente externa, utilizada para troca de informações entre órgãos públicos diferentes ou entre a Prefeitura e entidades particulares. Utilizado para solicitações formais, convites, notificações, pedidos de providências, encaminhamento de documentos e respostas institucionais.", now);

// MEMORANDOS
// Palavras-chave: Comunicação Interna, CI, Departamentos, Setores.
        createCategoryIfMissing("Memorandos",
                "Documento de comunicação interna (CI) tramitado entre unidades administrativas, departamentos ou secretarias de um mesmo órgão. Caracteriza-se pela agilidade, objetividade e ausência de formalidades excessivas. Utilizado para avisos, solicitações de compras internas e tramitação de processos.", now);

// FOLHAS DE PAGAMENTO
// Palavras-chave: Holerite, Vencimentos, Salário, Proventos, Descontos, INSS, IRRF, Líquido.
        createCategoryIfMissing("Folhas de Pagamento",
                "Relatórios financeiros analíticos ou sintéticos contendo a remuneração mensal dos servidores públicos. Discrimina vencimentos base, salários, proventos, vantagens (anuênios, gratificações), subsídios e descontos (INSS, IRRF, consignados). Inclui holerites, contracheques e fichas financeiras.", now);

// PARECERES JURÍDICOS
// Palavras-chave: Procuradoria, PGM, Opina-se, Legalidade, Fundamentação.
        createCategoryIfMissing("Pareceres Jurídicos",
                "Manifestações técnicas emitidas pela Procuradoria Geral do Município (PGM) ou assessoria jurídica. Analisa a legalidade e constitucionalidade de atos administrativos, editais de licitação, minutas de contratos e projetos de lei. Contém relatório, fundamentação legal e conclusão opinativa.", now);
        log.info("Verificação de categorias concluída.");
    }

    private void createCategoryIfMissing(String name, String description, LocalDateTime now) {
        if (!categoryRepository.existsByName(name)) {
            DocumentCategory cat = new DocumentCategory();
            cat.setName(name);
            cat.setDescription(description);

            cat.setActive(true);
            cat.setCreatedBy("SystemInitializer");
            cat.setCreatedAt(now);
            cat.setLastModifiedBy("SystemInitializer");
            cat.setLastModifiedAt(now);

            categoryRepository.save(cat);
            log.info("Categoria criada: {}", name);
        } else {
            log.debug("Categoria já existente: {}", name);
        }
    }
}