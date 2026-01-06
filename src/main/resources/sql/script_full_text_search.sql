-- 1. Cria a coluna para busca vetorial (caso não exista)
ALTER TABLE documents ADD COLUMN IF NOT EXISTS search_vector tsvector;

-- 2. Cria o índice para performance
CREATE INDEX IF NOT EXISTS idx_documents_search ON documents USING GIN(search_vector);

-- 3. Cria (ou atualiza) a função que extrai os termos
CREATE OR REPLACE FUNCTION documents_tsvector_trigger() RETURNS trigger AS $$
BEGIN
  -- Garante que o texto nulo vire string vazia para não quebrar
  new.search_vector := to_tsvector('portuguese', coalesce(new.extracted_text, ''));
return new;
END
$$ LANGUAGE plpgsql;

-- 4. Cria o gatilho (Remove antes para recriar e evitar duplicidade)
DROP TRIGGER IF EXISTS tsvectorupdate ON documents;

CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE ON documents FOR EACH ROW EXECUTE PROCEDURE documents_tsvector_trigger();

-- se possível, verifique se a coluna, o indice, a função e o trigger foram criados