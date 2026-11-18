# MediSUS API

Backend REST para o MediSUS, uma plataforma que ajuda a localizar medicamentos disponíveis na rede pública. Este repositório contém somente a API; o aplicativo React Native é mantido em outro projeto.

## Requisitos

- Java 17+
- Maven 3.9+
- Docker com Docker Compose (para PostgreSQL e RabbitMQ)

## Executar localmente

1. Copie `.env.example` para `.env` e ajuste `JWT_SECRET` para um segredo aleatório com pelo menos 32 caracteres. Para habilitar a leitura de receitas, configure `OPENAI_API_KEY`, `MEDISUS_AI_ENABLED=true` e `MEDISUS_AI_PROVIDER=openai`.
2. Inicie API, banco e broker: `docker compose up --build` (ou `docker compose up --build -d` para deixá-los em segundo plano).
3. Swagger UI: `http://localhost:8080/swagger-ui.html`; OpenAPI: `http://localhost:8080/v3/api-docs`.

Para desenvolvimento com Maven instalado na máquina, inicie dependências com `docker compose up -d postgres rabbitmq` e a API com `powershell -ExecutionPolicy Bypass -File .\scripts\run-local.ps1`. O script carrega `.env` no processo Java sem imprimir valores.

Sem OpenAI configurado, a API inicia normalmente e o endpoint de identificação de receita responde HTTP `503` com o envelope de erro padronizado. A API processa a imagem em memória, sem persistir ou registrar seu conteúdo em logs; quando a IA está habilitada, a imagem é enviada ao provedor OpenAI configurado para análise, sujeita aos termos e controles de privacidade dessa conta. A IA apenas sugere nomes de medicamentos; o cidadão deve revisar os nomes e consultar o estoque real separadamente.

### Usuários e acesso

Não há senhas de demonstração embutidas. Para criar um gestor de teste, configure `BOOTSTRAP_MANAGER_EMAIL` e `BOOTSTRAP_MANAGER_PASSWORD` (12–72 caracteres) no `.env` antes da primeira inicialização; por exemplo, use o e-mail `gestor@example.com` e uma senha criada por você. A conta é criada uma única vez. Crie um cidadão em `POST /api/auth/register` (sempre com papel `CITIZEN`), autentique em `POST /api/auth/login` e promova usuários para `PHARMACIST` ou `MANAGER` com `PUT /api/users/{id}/role` usando o token do gestor.

Papéis: `CITIZEN` pode consultar UBS/medicamentos/estoques, criar alertas e ver notificações; `PHARMACIST` pode criar e manter UBS, medicamentos e estoques; `MANAGER` tem essas permissões e também administra usuários e papéis.

## API para o aplicativo Mobile

Configure a URL base por ambiente (por exemplo, emulador Android: `http://10.0.2.2:8080`; simulador iOS: `http://localhost:8080`). Para dispositivos físicos, use o endereço IP acessível da máquina ou a URL pública implantada. Todas as rotas `/api/**`, exceto cadastro e login, exigem `Authorization: Bearer <token>`.

### Autenticação

`POST /api/auth/register` — corpo: `{"name":"Ana","email":"ana@example.com","password":"senha-com-8-ou-mais"}`. Cadastro público cria apenas `CITIZEN`.

`POST /api/auth/login` — corpo: `{"email":"ana@example.com","password":"senha-com-8-ou-mais"}`. Retorno: `{"token":"...","tokenType":"Bearer","expiresIn":3600,"role":"CITIZEN","name":"Ana"}`.

### Recursos e exemplos

- `GET /api/ubs?page=0&size=20&sort=name,asc` e `GET /api/ubs/{id}`
- `POST /api/ubs` (PHARMACIST/MANAGER), corpo: `{"name":"UBS Central","address":"Rua A, 10","city":"São Paulo","state":"SP","latitude":-23.55,"longitude":-46.63}`
- `GET /api/medications?page=0&size=20`, `GET /api/medications/{id}`; `POST /api/medications` (PHARMACIST/MANAGER), corpo: `{"name":"Paracetamol 500 mg","activeIngredient":"paracetamol","description":"Comprimido"}`
- `GET /api/stocks?medicationId=1&ubsId=1&page=0&size=20` — resultados incluem somente registros com quantidade positiva. `POST /api/stocks` (PHARMACIST/MANAGER), corpo: `{"ubsId":1,"medicationId":1,"quantity":30}`.
- `POST /api/alerts` — corpo: `{"medicationId":1,"ubsId":1}` (UBS opcional); alertas pertencem ao usuário autenticado. `GET /api/alerts` lista os alertas do usuário.
- `GET /api/notifications` lista notificações persistentes do usuário autenticado.
- `GET /api/users?page=0&size=20` e `PUT /api/users/{id}/role` (MANAGER), corpo: `{"role":"PHARMACIST"}`.
- `POST /api/prescriptions/identify` aceita multipart `image` e devolve candidatos reconhecidos; não pesquisa estoque automaticamente. Após revisão do cidadão, consulte `GET /api/stocks?medicationName=Paracetamol`.

Listas usam paginação Spring (`content`, `totalElements`, `totalPages`, `size`, `number`). Campos inválidos retornam HTTP `400` com `fieldErrors`. Erros comuns: `401` token ausente/inválido, `403` papel insuficiente, `404` recurso não encontrado, `409` conflito (por exemplo e-mail já cadastrado), `503` IA indisponível.

O DTO de disponibilidade de estoque inclui `farmaciaPopularEligibility: "UNKNOWN"`. A aplicação não tem uma fonte oficial configurada e, portanto, não afirma elegibilidade à Farmácia Popular. Só integre um catálogo oficial/verificado antes de informar `"ELIGIBLE"` ou `"NOT_ELIGIBLE"`.

## Opção distribuída: RabbitMQ

Quando o estoque de um medicamento aumenta, a API publica `StockReplenishedEvent` no RabbitMQ. O consumidor procura alertas ativos para aquele medicamento e UBS (ou alertas sem UBS específica), persiste notificações para os respectivos cidadãos e não expõe os dados pessoais no evento.

## Configuração e implantação

Veja `.env.example` e `docker-compose.yml`. PostgreSQL e RabbitMQ são serviços locais; para produção, configure instâncias gerenciadas, HTTPS, segredo JWT próprio e credenciais OpenAI via variáveis de ambiente/secret manager. Não há deployment cloud incluído: a conta de hospedagem, banco, broker, domínio e secrets devem ser fornecidos pelo responsável pela implantação.

## Arquitetura e transparência

Camadas simples de API (controllers/DTOs/validação), serviço (regras e eventos), persistência (Spring Data JPA) e infraestrutura (JWT, RabbitMQ e Spring AI). PostgreSQL armazena usuários, UBS, medicamentos, estoques, alertas e notificações; RabbitMQ desacopla o aviso de reposição; Spring AI multimodal extrai candidatos de uma foto de receita. A IA não inventa disponibilidade, não diagnostica, não recomenda tratamento e não decide elegibilidade à Farmácia Popular. O paciente confirma os candidatos antes de consultar o estoque persistido.

### Declaração de uso de IA

Este projeto contou com apoio de ferramentas de IA generativa durante o desenvolvimento, conforme permitido pelo docente. Quando configurada e habilitada, a funcionalidade do produto usa Spring AI com OpenAI para analisar a foto da receita e sugerir nomes candidatos de medicamentos; o cidadão deve revisar as sugestões, e a disponibilidade é consultada separadamente no estoque persistido.

## Executar testes

`mvn test`
