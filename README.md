# SIDI-DOC
SISTEMA DE DIGITALIZAÇÃO INTELIGENTE DE DOCUMENTOS

🛠️ Pré-requisitos
Para executar este projeto, certifique-se de ter instalado em sua máquina:

- Java 21 (JDK 21)
- Docker e Docker Compose

🚀 Como Rodar o Projeto
Siga os passos abaixo para configurar o ambiente e iniciar a aplicação.

### 1. Configuração de Ambiente (.env)

Crie um arquivo chamado .env na raiz do projeto

Crie o arquivo .env e cole o seguinte conteúdo:

```properties
DB_HOST=localhost
DB_PORT=5433
DB_NAME=sididoc
DB_USER=postgres
DB_PASSWORD=postgres
```

### 2. Subir o Banco de Dados (Docker)

Com o Docker aberto, execute o comando abaixo em um terminal na raiz do projeto para baixar a imagem do PostgreSQL e iniciar o container:

```bash
docker-compose up -d
```

### 3. Executar a Aplicação (Java)

Agora que o banco está rodando, inicie a aplicação Spring Boot.