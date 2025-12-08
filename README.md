# SIDI-DOC
SISTEMA DE DIGITALIZAÇÃO INTELIGENTE DE DOCUMENTOS

🛠️ Pré-requisitos
Para executar este projeto, certifique-se de ter instalado em sua máquina:

- Java 21 (JDK 21)
- Docker e Docker Compose

🚀 Como Rodar o Projeto
Siga os passos abaixo para configurar o ambiente e iniciar a aplicação.

### 1. Configuração de Ambiente (.env)

Crie um arquivo chamado .env na raiz do projeto e copie o conteúdo do .env que está no discord.

### 2. Subir o Banco de Dados (Docker)

Com o Docker aberto, execute o comando abaixo em um terminal na raiz do projeto para baixar a imagem do PostgreSQL e iniciar o container:

```bash
docker-compose up -d
```

### 3. Verificar o Perfil de Execução (Importante!) ⚠️

O projeto está configurado com dois perfis de execução:

dev (Padrão): Conecta no PostgreSQL do Docker. Ideal para desenvolvimento e testes locais.

prod: Conecta no Banco e Storage do Supabase. Use com cuidado, pq as suas alterações alteram o banco de todos.

Certifique-se de estar rodando em DEV: Abra o arquivo src/main/resources/application.yaml e verifique se a linha active está como dev:

```yaml
spring:
  profiles:
    active: dev  # <--- Mantenha 'dev' para usar o Docker ou 'homolog' pra usar o banco do supabase
```

### 4. Executar a Aplicação

Agora execute a aplicação.

### ☁️ Banco Compartilhado (Supabase)⚠️

O perfil homolog aponta para um projeto no Supabase que serve como nosso Banco Compartilhado de Desenvolvimento.

Regra para novas Features: Sempre que você desenvolver uma funcionalidade que altere o banco de dados (criar tabelas, adicionar colunas, mudar tipos) no seu Docker local:

Teste localmente e garanta que funcionou.
 
Após isso troque o perfil do application para usar o banco compartilhado.

E salve alguns dados para servir de base para relacionamentos (Foreign Keys). Se sua entidade for pré-requisito para outras, deixe registros prontos para que os colegas possam testar suas funcionalidades dependentes sem retrabalho.

Mantenha o ambiente compartilhado sincronizado!