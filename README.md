# Orbit X — Plataforma de Monitoramento Inteligente de Datacenters

> Plataforma enterprise de monitoramento de datacenters sustentáveis com Inteligência Artificial, rastreamento orbital de satélites e alertas em tempo real.

---

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Estrutura do Repositório](#estrutura-do-repositório)
- [Pré-requisitos](#pré-requisitos)
- [Configuração](#configuração)
- [Como Executar](#como-executar)
- [Endpoints da API](#endpoints-da-api)
- [Autenticação JWT](#autenticação-jwt)
- [Banco de Dados](#banco-de-dados)
- [Inteligência Artificial](#inteligência-artificial)
- [Mensageria (RabbitMQ)](#mensageria-rabbitmq)
- [Documentação Swagger](#documentação-swagger)

---

## Sobre o Projeto

O **Orbit X** é uma solução de monitoramento contínuo de datacenters globais, desenvolvida com foco em eficiência energética e sustentabilidade (ESG). A plataforma integra:

- **Monitoramento em tempo real** de KPIs como PUE, temperatura, consumo energético e emissões de carbono
- **Previsão de superaquecimento** por motor de IA com alertas automáticos
- **Rastreamento orbital** de satélites (LEO / MEO / GEO) para cobertura de telemetria global
- **Assistente de IA** com RAG + Tool Calling (Groq / LLaMA 3.3) para consultas em linguagem natural
- **Score ESG** com comparativo antes/depois da ativação do Orbit X
- **Mensageria assíncrona** via RabbitMQ com serviço de notificação por e-mail dedicado

**Dados da frota monitorada:**

| Recurso | Total |
|---|---|
| Datacenters | 6 regiões globais |
| Servidores | 32.800 |
| Satélites orbitais | 5 (LEO / MEO / GEO) |
| Score ESG | 87/100 — Nota A+ |
| Redução de PUE | 1,82 → 1,27 (−30,2%) |
| Carbono compensado | 142,3 t CO₂eq |

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTES                             │
│              (Browser / App / Swagger UI)                   │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS / JWT
                            ▼
┌─────────────────────────────────────────────────────────────┐
│               orbit-x-backend  :8080                        │
│                                                             │
│  ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌─────────────┐  │
│  │   Auth   │ │ Dashboard │ │  Infra   │ │  Assistant  │  │
│  │ /auth/*  │ │ /kpis     │ │ /dc      │ │ /chat       │  │
│  │          │ │ /alerts   │ │ /sats    │ │ RAG + Tools │  │
│  └──────────┘ └───────────┘ └──────────┘ └─────────────┘  │
│                                                             │
│  ┌──────────────────┐   ┌─────────────────────────────┐    │
│  │   ReportsService │   │    AlertEventPublisher      │    │
│  │  /sustainability │   │  → RabbitMQ Exchange        │    │
│  │  /export/pdf     │   └──────────────┬──────────────┘    │
│  └──────────────────┘                  │                    │
│                                        │ AMQP               │
│  ┌──────────────────┐                  │                    │
│  │  Oracle DB 19c   │                  │                    │
│  │  Flyway + JPA    │                  │                    │
│  └──────────────────┘                  │                    │
└────────────────────────────────────────┼────────────────────┘
                                         ▼
┌─────────────────────────────────────────────────────────────┐
│          orbit-x-notification-service  :8085                │
│                                                             │
│   AlertEventConsumer  →  NotificationService  →  SMTP      │
│   (alerts.queue)          (e-mail de alerta)               │
│   (thermal.queue)         (log crítico térmico)             │
└─────────────────────────────────────────────────────────────┘
```

**Padrões arquiteturais utilizados:**
- **DDD (Domain-Driven Design)** — domínios isolados: `auth`, `dashboard`, `infrastructure`, `reports`, `assistant`
- **Event-Driven Microservices** — publicação/consumo de eventos via RabbitMQ
- **HATEOAS** — links de navegação hypermedia nas respostas REST
- **Port & Adapter** — `AssistantPort` desacopla implementações de IA (Groq vs. fallback offline)
- **Circuit Breaker** — OpenFeign + Spring Cloud para o cliente de clima externo

---

## Tecnologias

### orbit-x-backend

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.2.5 | Framework base |
| Spring Security | 6.2.x | Autenticação / Autorização |
| Spring Data JPA | 3.2.x | Persistência |
| Spring HATEOAS | 2.2.x | Links hypermedia |
| Spring Cache + Caffeine | — | Cache em memória (TTL 15s / 30s / 5min) |
| Spring AMQP (RabbitMQ) | 3.1.x | Mensageria assíncrona |
| Spring Cloud OpenFeign | 2023.0.1 | Cliente HTTP declarativo |
| Spring AI | 1.0.0 | Integração com LLM (Groq) |
| JJWT | 0.12.5 | Geração / validação de JWT (HS512) |
| Flyway | 10.10.0 | Migrações do banco de dados |
| Oracle JDBC (ojdbc11) | — | Driver Oracle |
| SpringDoc OpenAPI | 2.5.0 | Documentação Swagger |
| Lombok | — | Redução de boilerplate |

### orbit-x-notification-service

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem principal |
| Spring Boot | 3.2.5 | Framework base |
| Spring AMQP (RabbitMQ) | 3.1.x | Consumidor de eventos |
| Spring Mail | — | Envio de e-mails de alerta |
| Lombok | — | Redução de boilerplate |

### Infraestrutura

| Ferramenta | Uso |
|---|---|
| Docker + Docker Compose | Orquestração dos serviços |
| RabbitMQ 3.13 (Management) | Message broker |
| Oracle Database 19c | Banco de dados principal |

---

## Estrutura do Repositório

```
global/
├── orbit-x-backend/                    # Serviço principal (API REST)
│   ├── src/main/java/com/orbitx/backend/
│   │   ├── config/                     # SecurityConfig, SwaggerConfig, CacheConfig,
│   │   │                               # RabbitMQConfig, FlywayConfig, ApplicationConfig
│   │   ├── domain/
│   │   │   ├── auth/                   # Registro, login, JWT, entidades User/Company
│   │   │   ├── dashboard/              # KPIs, alertas, telemetria, predição IA
│   │   │   ├── infrastructure/         # Datacenters, satélites, monitoramento orbital
│   │   │   ├── reports/                # Score ESG, exportação PDF
│   │   │   └── assistant/              # Chat IA (RAG + Tool Calling + fallback offline)
│   │   ├── integration/climate/        # Client Feign para API de clima (com fallback)
│   │   ├── messaging/                  # AlertEvent DTO + AlertEventPublisher
│   │   ├── security/                   # JwtService, JwtAuthenticationFilter
│   │   └── shared/                     # ApiResponse, ErrorResponse, exceções globais
│   ├── src/main/resources/
│   │   ├── db/migration/               # Flyway: V1 (schema), V2 (seed), V7 (frota)
│   │   └── application.yml
│   ├── Dockerfile
│   └── pom.xml
│
├── orbit-x-notification-service/       # Microsserviço de notificações
│   ├── src/main/java/com/orbitx/notification/
│   │   ├── config/                     # RabbitMQConfig
│   │   ├── messaging/consumer/         # AlertEventConsumer (alerts + thermal queues)
│   │   └── service/                    # NotificationService (e-mail + log crítico)
│   ├── Dockerfile
│   └── pom.xml
│
├── docker-compose.yml                  # Orquestração: RabbitMQ + backend + notification
├── .env.example                        # Template de variáveis de ambiente
└── README.md
```

---

## Pré-requisitos

- **Java 17+**
- **Maven 3.9+**
- **Docker** e **Docker Compose** (para execução em container)
- **Oracle Database 19c** (ou acesso à instância FIAP: `oracle.fiap.com.br:1521:ORCL`)
- **RabbitMQ** (ou via Docker Compose — já incluído)
- **Groq API Key** (opcional — sem ela o assistente funciona em modo offline)

---

## Configuração

Copie o arquivo de exemplo e preencha os valores:

```bash
cp orbit-x-backend/.env.example .env
```

### Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DATABASE_USERNAME` | Sim | Usuário do banco Oracle |
| `DATABASE_PASSWORD` | Sim | Senha do banco Oracle |
| `JWT_SECRET` | Sim | Segredo para assinar tokens JWT (mín. 64 chars hex) |
| `GROQ_API_KEY` | Não | Chave da API Groq (começa com `gsk_`). Sem ela, o assistente usa modo offline |
| `RABBITMQ_HOST` | Não | Host do RabbitMQ (padrão: `localhost`) |
| `RABBITMQ_PORT` | Não | Porta do RabbitMQ (padrão: `5672`) |
| `RABBITMQ_USER` | Não | Usuário RabbitMQ (padrão: `guest`) |
| `RABBITMQ_PASS` | Não | Senha RabbitMQ (padrão: `guest`) |
| `SMTP_HOST` | Notification | Host do servidor SMTP |
| `SMTP_USER` | Notification | Usuário SMTP |
| `SMTP_PASS` | Notification | Senha SMTP |
| `OPS_EMAIL` | Notification | E-mail destino dos alertas |

**Gerando um JWT_SECRET seguro:**
```bash
openssl rand -hex 32
```

---

## Como Executar

### Com Docker Compose (recomendado)

```bash
# Clone o repositório
git clone https://github.com/renatosgk/OrbitX.API.git
cd OrbitX.API

# Configure as variáveis de ambiente
cp orbit-x-backend/.env.example .env
# Edite o .env com seus valores reais

# Suba todos os serviços
docker compose up --build
```

Serviços disponíveis após o startup:

| Serviço | URL |
|---|---|
| API Backend | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Notification Service | http://localhost:8085 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |

---

### Execução local (desenvolvimento)

**1. Suba apenas o RabbitMQ:**
```bash
docker compose up rabbitmq -d
```

**2. Execute o backend:**
```bash
cd orbit-x-backend
./mvnw spring-boot:run
```

**3. Execute o serviço de notificação (opcional):**
```bash
cd orbit-x-notification-service
./mvnw spring-boot:run
```

---

## Endpoints da API

Todos os endpoints protegidos exigem header: `Authorization: Bearer <token>`

### Autenticação — `/api/v1/auth`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/auth/register` | Não | Registra empresa + admin e retorna JWT |
| `POST` | `/auth/login` | Não | Autentica e retorna JWT |
| `POST` | `/auth/forgot-password` | Não | Inicia fluxo de recuperação de senha |

### Dashboard — `/api/v1/dashboard`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `GET` | `/dashboard/kpis` | Sim | KPIs em tempo real (energia, temperatura, PUE, carbono, previsão IA) |
| `GET` | `/dashboard/alerts` | Sim | Alertas ativos não resolvidos |

### Infraestrutura — `/api/v1/infrastructure`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `GET` | `/infrastructure/datacenters` | Sim | Lista os 6 datacenters globais (cache 30s) |
| `GET` | `/infrastructure/satellites` | Sim | Lista os 5 satélites orbitais com posição calculada (cache 15s) |

### Relatórios ESG — `/api/v1/reports`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `GET` | `/reports/sustainability-score` | Sim | Score ESG (0-100), carbon offset, comparativo antes/depois (cache 5min) |
| `GET` | `/reports/export/pdf` | Sim | Download do relatório executivo em PDF |

### Assistente IA — `/api/v1/assistant`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/assistant/chat` | Sim | Chat com o Orbit X AI (RAG + Tool Calling ou modo offline) |

**Exemplo de payload — Chat:**
```json
{
  "message": "Qual é o status térmico dos datacenters agora?",
  "history": []
}
```

---

## Autenticação JWT

O sistema utiliza **JWT com algoritmo HS512**, válido por **24 horas**.

**1. Registrar conta:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Minha Empresa",
    "taxId": "12345678000199",
    "adminName": "João Silva",
    "email": "joao@empresa.com",
    "password": "Senha@1234"
  }'
```

**2. Usar o token retornado:**
```bash
# O token está em: data.accessToken
TOKEN="eyJhbGci..."

curl http://localhost:8080/api/v1/dashboard/kpis \
  -H "Authorization: Bearer $TOKEN"
```

**Claims incluídos no token:**

| Campo | Descrição |
|---|---|
| `sub` | E-mail do usuário |
| `role` | `ADMIN` / `OPERATOR` / `VIEWER` |
| `companyId` | ID da empresa |
| `companyName` | Nome da empresa |

---

## Banco de Dados

O schema é gerenciado automaticamente pelo **Flyway** na inicialização da aplicação.

### Tabelas principais

| Tabela | Descrição |
|---|---|
| `companies` | Empresas cadastradas (plano STARTER / PROFESSIONAL / ENTERPRISE) |
| `users` | Usuários com roles (ADMIN / OPERATOR / VIEWER) |
| `datacenters` | 6 datacenters globais com coordenadas geográficas |
| `satellites` | 5 satélites orbitais (LEO / MEO / GEO) |
| `alerts` | Histórico de alertas com severidade e componente de origem |

### Migrações Flyway

| Versão | Arquivo | Descrição |
|---|---|---|
| V1 | `V1__create_schema.sql` | Criação de todas as tabelas e índices |
| V2 | `V2__seed_initial_data.sql` | Dados iniciais de configuração |
| V7 | `V7__seed_fleet_data.sql` | Seed dos 6 datacenters + 5 satélites |

> **Nota Oracle:** O `FlywayConfig` executa `repair()` antes de `migrate()` para limpar entradas com falha, contornando a limitação de DDL não-transacional do Oracle.

---

## Inteligência Artificial

### Assistente com RAG + Tool Calling (modo online)

Quando `GROQ_API_KEY` começa com `gsk_`, o `SpringAiAssistantService` é ativado:

- **Modelo:** `llama-3.3-70b-versatile` via Groq (endpoint compatível com OpenAI)
- **RAG:** `KnowledgeBaseConfig` injeta contexto técnico da base de conhecimento Orbit X no system prompt
- **Tools disponíveis (Spring AI `@Tool`):**

| Tool | Descrição |
|---|---|
| `getLiveKpis()` | Busca temperatura, energia, PUE e risco de superaquecimento ao vivo |
| `getDatacenterStatus()` | Estado térmico de todos os datacenters |
| `getActiveAlerts()` | Alertas não resolvidos do motor de IA |
| `getSustainabilityMetrics()` | Score ESG, carbon offset e comparativo |

### Assistente por palavras-chave (modo offline)

Sem chave Groq, o `KeywordAssistantService` entra em ação — responde consultas sobre temperatura, energia, ESG, satélites e alertas usando dados da telemetria simulada, sem latência de rede.

### Motor de predição IA

O `AiPredictionService` calcula a probabilidade de superaquecimento e gera recomendações de resfriamento baseadas em temperatura e PUE:

| Temperatura | Probabilidade | Modo |
|---|---|---|
| > 30°C | ≥ 90% | `TURBO_COOLING` |
| 27–30°C | 50–90% | `ENHANCED_COOLING` |
| 24–27°C | 20–50% | `STANDARD_COOLING` |
| < 24°C | < 20% | `GREEN_MODE` |

---

## Mensageria (RabbitMQ)

### Topologia

```
orbit-x-backend
       │
       ├── publishAlert()       → orbitx.exchange  [alert.event]   → orbitx.alerts.queue
       └── publishThermalCritical() → orbitx.exchange [thermal.critical] → orbitx.thermal.queue
                                                                              │
                                                               orbit-x-notification-service
                                                                    │
                                                                    ├── consumeAlert()          → sendEmailAlert()
                                                                    └── consumeThermalCritical() → logThermalCritical()
                                                                                                   + sendEmailAlert()
```

### Regras de publicação

- **`alert.event`** — publicado quando temperatura ≥ 27°C ou PUE elevado (alertas HIGH / CRITICAL gerados dinamicamente)
- **`thermal.critical`** — publicado quando probabilidade de superaquecimento ≥ 70% (sempre `CRITICAL`)

### Configuração do consumidor

- **ACK manual** com `basicAck` / `basicNack`
- **Concorrência `thermal.queue`:** 2 threads paralelas
- **Dead-letter:** mensagens com `basicNack(false, false)` são descartadas (sem requeue)

---

## Documentação Swagger

A documentação interativa da API está disponível em:

```
http://localhost:8080/swagger-ui/index.html
```

Para testar endpoints protegidos:
1. Faça `POST /api/v1/auth/login` ou `POST /api/v1/auth/register`
2. Copie o valor de `data.accessToken`
3. Clique em **Authorize** (cadeado) no topo da página
4. Cole o token no campo **BearerAuth** e confirme

**Grupos de endpoints documentados:**

| Tag | Descrição |
|---|---|
| `Autenticação` | Registro, login e recuperação de senha |
| `Dashboard` | KPIs em tempo real e alertas da IA |
| `Infraestrutura` | Datacenters globais e frota de satélites |
| `Relatórios & ESG` | Score de sustentabilidade e exportação PDF |
| `AI Assistant` | Assistente contextual com RAG e Tool Calling |

---

## Integrantes do Grupo

| Nome                | RM |
|---------------------|---|
| Renato Kenji Sugaki | RM559810 |

---

*Orbit X — Monitoramento Inteligente de Datacenters Sustentáveis*
