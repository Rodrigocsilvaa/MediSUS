# MediSUS API

API REST em Java 17 com Spring Boot para apoiar a busca de medicamentos em UBS da rede pública. O backend foi desenvolvido para a disciplina **Java Advanced** e expõe contratos HTTP consumíveis pelo aplicativo React Native da disciplina Mobile. Esta pasta contém somente o backend; o app Mobile não está implementado neste projeto.

## Equipe

- Natália Cristina
- Nickolas Davi
- Rodrigo Silva
- Samara Vilela
- Otávio Ferreira

Turma: **2TDSR**

Os RMs estão reservados para o arquivo `integrantes e links.txt`, que será colocado somente no ZIP de entrega e não deve ser enviado como informação pessoal no README público.

## Entrega e validação

- Vídeo-pitch: [assistir no YouTube](https://www.youtube.com/watch?v=kMr_TDTlXBU).
- Build Java 17 e `mvn test`/`mvn package` passaram anteriormente com **7 testes**.
- Docker Compose foi validado. Um teste ponta a ponta isolado com PostgreSQL, RabbitMQ e API confirmou cadastro/autenticação, UBS, estoque, alerta, reposição, consumo do evento e notificação.
- O fluxo também foi verificado no ambiente local: o estoque passou de 1 para 2 e o cidadão recebeu a notificação.
- API publicada no Render: [https://medisus.onrender.com](https://medisus.onrender.com) | [Swagger](https://medisus.onrender.com/swagger-ui/index.html).
- O Render não tem RabbitMQ configurado; o fluxo assíncrono foi validado localmente com Docker Compose, não no deploy público.
- A aplicação Mobile não está neste repositório. O README documenta os endpoints e a autenticação para integração React Native, mas não afirma que o app foi executado ponta a ponta com esta API.
- Uma chamada real ao provedor OpenAI depende de uma chave válida e não foi validada nesta entrega.

## Tecnologias

- Java 17
- Spring Boot 3.4.4
- Spring Web
- Spring Data JPA
- Spring Security
- JWT com JJWT
- Bean Validation
- Spring AI com OpenAI
- RabbitMQ AMQP
- PostgreSQL
- Springdoc OpenAPI/Swagger
- Maven
- Docker Compose
- H2 para testes

## Como o backend foi implementado

1. **Camada API:** controllers REST recebem payloads JSON ou multipart, aplicam validação DTO e delegam regras aos services.
2. **Domínio JPA:** entidades são persistidas com Spring Data JPA no PostgreSQL.
3. **Regras de negócio:** services criam, atualizam, consultam e removem recursos e controlam reposição de estoque.
4. **Segurança:** cadastro/login são públicos; todas as demais rotas `/api/**` exigem JWT. Filtros carregam o usuário e aplicam papéis.
5. **Erros:** `@RestControllerAdvice` padroniza respostas com timestamp, status HTTP, error, mensagem, path e `fieldErrors` quando aplicável.
6. **IA:** o endpoint multipart envia uma foto ao Spring AI/OpenAI e recebe somente nomes candidatos legíveis. Os candidatos não são gravados automaticamente como estoque.
7. **Arquitetura distribuída:** ao aumentar a quantidade de estoque, a API publica um evento no RabbitMQ; um consumidor encontra alertas correspondentes e cria notificações persistidas.
8. **Documentação:** Springdoc expõe Swagger e OpenAPI.

## Modelo JPA

O domínio possui seis entidades relacionadas:

### `Ubs`

Tabela `ubs`.

- `id`: `Long`, chave primária gerada.
- `name`: `String`, obrigatório, máximo 120 caracteres.
- `address`: `String`, obrigatório, máximo 240 caracteres.
- `city`: `String`, obrigatório, máximo 100 caracteres.
- `state`: `String`, obrigatório, máximo 2 caracteres.
- `latitude`: `Double`, opcional.
- `longitude`: `Double`, opcional.

### `Medication`

Tabela `medications`.

- `id`: `Long`, chave primária gerada.
- `name`: `String`, obrigatório, máximo 160 caracteres.
- `activeIngredient`: `String`, obrigatório, máximo 160 caracteres.
- `description`: `String`, opcional, máximo 500 caracteres.

### `Stock`

Tabela `stocks`, com restrição única em `(ubs_id, medication_id)`.

- `id`: `Long`, chave primária gerada.
- `ubs`: `@ManyToOne`, obrigatório, `LAZY`, relacionamento com `Ubs`.
- `medication`: `@ManyToOne`, obrigatório, `LAZY`, relacionamento com `Medication`.
- `quantity`: `int`, obrigatório.
- `updatedAt`: `Instant`, obrigatório e atualizado em persistência/atualização.

`Stock` relaciona uma UBS a um medicamento e guarda a quantidade disponível. A listagem retorna somente registros cuja quantidade é maior que zero.

### `AppUser`

Tabela `app_users`, com e-mail único.

- `id`: `Long`, chave primária gerada.
- `name`: `String`, obrigatório, máximo 120 caracteres.
- `email`: `String`, obrigatório e único, máximo 180 caracteres; armazenado em minúsculas.
- `password`: `String`, obrigatório, armazenado com BCrypt.
- `role`: enum `Role`, obrigatório e armazenado como texto.

Enum `Role`: `CITIZEN`, `PHARMACIST`, `MANAGER`.

### `StockAlert`

Tabela `stock_alerts`.

- `id`: `Long`, chave primária gerada.
- `user`: `@ManyToOne`, obrigatório, `LAZY`, relacionamento com `AppUser`.
- `medication`: `@ManyToOne`, obrigatório, `LAZY`, relacionamento com `Medication`.
- `ubs`: `@ManyToOne`, opcional, `LAZY`, relacionamento com `Ubs`; `null` significa qualquer UBS.
- `active`: `boolean`, obrigatório, iniciado como `true`.
- `createdAt`: `Instant`, obrigatório.

### `AppNotification`

Tabela `app_notifications`.

- `id`: `Long`, chave primária gerada.
- `user`: `@ManyToOne`, obrigatório, `LAZY`, relacionamento com `AppUser`.
- `message`: `String`, obrigatório, máximo 500 caracteres.
- `read`: `boolean`, obrigatório.
- `createdAt`: `Instant`, obrigatório.

Os relacionamentos principais são: `Stock -> Ubs` e `Stock -> Medication`; `StockAlert -> AppUser`, `StockAlert -> Medication` e `StockAlert -> Ubs`; `AppNotification -> AppUser`.

## Papéis e segurança

- `CITIZEN`: consulta UBS, medicamentos e estoques; cria e remove seus alertas; consulta notificações; usa o endpoint de identificação da receita.
- `PHARMACIST`: pode manter UBS, medicamentos e estoques.
- `MANAGER`: possui as permissões de farmacêutico e administra usuários e papéis.

`POST /api/auth/register` cria somente `CITIZEN`. `POST /api/auth/login` gera um JWT válido por 3600 segundos. Todas as rotas `/api/**`, exceto cadastro e login, exigem:

```http
Authorization: Bearer <token>
```

Não existe refresh token. Quando o token expirar, o app deve executar login novamente.

## Executar o backend

### Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker com Docker Compose para PostgreSQL e RabbitMQ
- Chave OpenAI opcional para a análise de receita

### Configuração

1. Copie `.env.example` para `.env`.
2. Defina um `JWT_SECRET` próprio com pelo menos 32 caracteres.
3. Para testar RabbitMQ, mantenha o PostgreSQL e o RabbitMQ disponíveis por meio do Compose.
4. Para habilitar a IA real, configure `OPENAI_API_KEY`, `MEDISUS_AI_ENABLED=true` e `MEDISUS_AI_PROVIDER=openai`.
5. Para criar um gestor no primeiro início, configure `BOOTSTRAP_MANAGER_EMAIL` e `BOOTSTRAP_MANAGER_PASSWORD` com 12–72 caracteres.

Nunca coloque o segredo JWT ou a chave OpenAI no Git. Use `.env` local ou um secret manager.

### Docker

```powershell
docker compose up --build
```

Swagger: `http://localhost:8081/swagger-ui.html`

OpenAPI: `http://localhost:8081/v3/api-docs`

No arquivo Docker Compose, a API fica disponível na porta 8081 do host e continua usando a porta 8080 dentro do container. PostgreSQL usa a porta 55432 do host (5432 dentro do container) e RabbitMQ usa 5672, além da interface 15672.

### Maven local

Com PostgreSQL e RabbitMQ ativos:

```powershell
docker compose up -d postgres rabbitmq
powershell -ExecutionPolicy Bypass -File .\scripts\run-local.ps1
```

O script carrega variáveis do `.env` sem imprimir os valores.

Sem configuração OpenAI, a API inicia normalmente e `POST /api/prescriptions/identify` retorna HTTP `503`. Os modelos OpenAI que não fazem parte do fluxo de chat também ficam desabilitados para que o starter não exija uma chave de áudio/imagem/embedding apenas para iniciar a aplicação. Isso é o comportamento seguro, não um sucesso falso.

## API para React Native

### URLs base

- Android Emulator: `http://10.0.2.2:8081`
- iOS Simulator: `http://localhost:8081`
- Aparelho físico: `http://<IP-LAN-da-maquina>:8081`, com PC e celular na mesma rede
- Produção: use uma URL HTTPS pública

No dispositivo físico, o firewall do PC deve permitir a porta 8081. O app Mobile deve permitir HTTP apenas em ambiente local explícito; em produção, use HTTPS.

### Cadastro

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Ana",
  "email": "ana@example.com",
  "password": "senha-com-8-ou-mais"
}
```

O cadastro público cria sempre `CITIZEN`.

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "ana@example.com",
  "password": "senha-com-8-ou-mais"
}
```

Resposta:

```json
{
  "token": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "role": "CITIZEN",
  "name": "Ana"
}
```

No app, guarde o token em armazenamento seguro adequado à plataforma, envie-o em todas as chamadas protegidas e trate `401` como sessão inválida e `403` como acesso negado ao papel.

### Endpoints principais

#### Consulta

- `GET /api/ubs?page=0&size=20&sort=name,asc`
- `GET /api/ubs/{id}`
- `GET /api/medications?page=0&size=20&sort=name,asc`
- `GET /api/medications/{id}`
- `GET /api/stocks?page=0&size=20`
- `GET /api/stocks?medicationId=1&ubsId=1&page=0&size=20`
- `GET /api/stocks?medicationName=Paracetamol&page=0&size=20`
- `GET /api/stocks/{id}`

A resposta paginada segue o formato Spring:

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0
}
```

Somente estoques com quantidade positiva são listados.

#### Criar/manter UBS

`POST /api/ubs` e `PUT /api/ubs/{id}` exigem `PHARMACIST` ou `MANAGER`.

```json
{
  "name": "UBS Central",
  "address": "Rua A, 10",
  "city": "São Paulo",
  "state": "SP",
  "latitude": -23.55,
  "longitude": -46.63
}
```

#### Criar/manter medicamentos

`POST /api/medications` e `PUT /api/medications/{id}` exigem `PHARMACIST` ou `MANAGER`.

```json
{
  "name": "Paracetamol 500 mg",
  "activeIngredient": "paracetamol",
  "description": "Comprimido"
}
```

#### Criar/manter estoque

`POST /api/stocks` e `PUT /api/stocks/{id}` exigem `PHARMACIST` ou `MANAGER`.

```json
{
  "ubsId": 1,
  "medicationId": 1,
  "quantity": 30
}
```

Ao aumentar uma quantidade, o backend publica `StockReplenishedEvent`. Quando o consumidor encontra alertas compatíveis, cria notificações persistidas.

#### Alertas e notificações

Estes endpoints exigem `CITIZEN`:

- `POST /api/alerts`
- `GET /api/alerts?page=0&size=20`
- `DELETE /api/alerts/{id}`
- `GET /api/notifications?page=0&size=20`

Criar alerta:

```json
{
  "medicationId": 1,
  "ubsId": 1
}
```

`ubsId` é opcional. Quando omitido, o alerta representa o medicamento em qualquer UBS.

#### Administração

Exigem `MANAGER`:

- `GET /api/users?page=0&size=20`
- `PUT /api/users/{id}/role`

```json
{
  "role": "PHARMACIST"
}
```

Papéis válidos: `CITIZEN`, `PHARMACIST`, `MANAGER`.

### Identificação da receita no Mobile

Endpoint:

```http
POST /api/prescriptions/identify
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

O campo multipart deve se chamar `image`.

- Permitidos: JPEG, PNG e WebP.
- Tamanho máximo: 5 MB.
- Requer papel `CITIZEN`.
- A API valida o tipo informado no multipart.
- A imagem é processada em memória e não é persistida pelo backend.
- Quando OpenAI está configurado, a imagem é enviada ao provedor para análise.
- O resultado é uma lista de nomes candidatos; o cidadão deve revisar a sugestão.
- A resposta não consulta estoque nem determina tratamento.

Exemplo React Native/Fetch:

```jsx
const form = new FormData();

form.append('image', {
  uri: photo.uri,
  name: photo.fileName || 'receita.jpg',
  type: photo.type || 'image/jpeg',
});

const response = await fetch(`${BASE_URL}/api/prescriptions/identify`, {
  method: 'POST',
  headers: {
    Authorization: `Bearer ${token}`,
  },
  body: form,
});

const result = await response.json();
```

Não defina manualmente `Content-Type: multipart/form-data` no React Native: o próprio `fetch` precisa criar o boundary. Depois que o usuário revisa os candidatos, o app deve consultar separadamente `/api/stocks?medicationName=<nome>` e não assumir que o texto da receita representa disponibilidade real.

## Respostas de erro

Erros são retornados em um envelope JSON padrão:

```json
{
  "timestamp": "2026-...",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "fieldErrors": {
    "email": "must be a well-formed email address"
  }
}
```

Códigos relevantes:

- `400`: validação, imagem vazia, tipo ou conteúdo incompleto.
- `401`: token ausente ou inválido.
- `403`: usuário sem papel necessário.
- `404`: recurso não encontrado.
- `409`: conflito, por exemplo e-mail já cadastrado.
- `503`: Spring AI não configurado ou indisponível.

## Arquitetura distribuída com RabbitMQ

O projeto usa RabbitMQ como opção distribuída, atendendo ao requisito Java Advanced:

- fila durável: `medisus.stock.replenished`
- publicação JSON em aumento de quantidade de estoque
- evento contendo identificadores e nomes do estoque, medicamento e UBS
- consumidor assíncrono procura alertas ativos por medicamento e UBS
- notificações são persistidas no PostgreSQL
- dados pessoais não são colocados no conteúdo do evento

A publicação e o consumo foram verificados ponta a ponta com RabbitMQ real em Docker Compose: ao aumentar o estoque, o consumidor processou o evento e uma notificação foi persistida e retornada ao cidadão. Essa configuração está disponível localmente; o serviço do Render ainda não tem broker configurado.

## Spring AI

A funcionalidade usa Spring AI com OpenAI para ler uma imagem de receita e gerar somente nomes candidatos. Ela não diagnostica, não recomenda tratamento, não confirma disponibilidade e não decide elegibilidade à Farmácia Popular.

Quando configurada, o modelo recebe texto de instrução com temperatura zero e a imagem como mídia. A resposta é interpretada como JSON array e filtrada para textos únicos com até 160 caracteres.

O campo `farmaciaPopularEligibility` é sempre `"UNKNOWN"`, pois o projeto não possui uma fonte oficial ou catálogo verificado integrado. Não afirmar elegibilidade sem essa integração.

## Swagger/OpenAPI

Com a API ativa:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI: `http://localhost:8081/v3/api-docs`

Os endpoints, DTOs e respostas podem ser consultados pelo app, pelo Swagger e pelos testes.

## Testes

Execute:

```powershell
mvn test
```

Os testes existentes cobrem:

- modelo de IA mockado e IA desabilitada;
- validação e bloqueio de cidadão em operação de UBS;
- filtro de estoque disponível;
- publicação de evento ao aumentar estoque;
- consumidor que seleciona os alertas compatíveis.

Os testes usam H2/mock onde necessário. Eles não iniciam um broker RabbitMQ real, PostgreSQL real nem uma conta OpenAI real.

## Limitações conhecidas

- Não há integração ponta a ponta validada com o app Mobile nesta pasta.
- O deploy público no Render não está conectado a um RabbitMQ; notificações geradas por eventos estão validadas localmente.
- Não há refresh token.
- Não há configuração CORS específica. Mobile nativo não depende de CORS, mas Expo Web pode exigir configuração explícita no backend.
- Não há catalogação oficial da Farmácia Popular; a disponibilidade dessa elegibilidade fica como `UNKNOWN`.
- A API valida o MIME informado pelo multipart, mas não verifica magic bytes da imagem.
- Não há credenciais de demonstração embutidas no repositório.

## Declaração de uso de IA

Ferramentas de IA generativa foram permitidas pelo docente e usadas como apoio ao desenvolvimento, à documentação e à revisão da implementação. Na funcionalidade do produto, Spring AI com OpenAI recebe uma foto de receita e sugere nomes candidatos. O backend não substitui revisão humana, diagnóstico ou consulta oficial de disponibilidade.
