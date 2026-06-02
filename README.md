# Orbit X — Plataforma de Monitoramento Inteligente de Datacenters

> Plataforma enterprise de monitoramento de datacenters sustentáveis com Inteligência Artificial, rastreamento orbital de satélites e alertas em tempo real.

---

## Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Produção](#produção)
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
- [Integrantes](#integrantes-do-grupo)

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

## Produção

API deployada no Render:

```
https://orbitx-api-ve63.onrender.com
```

Documentação Swagger (produção):

```
https://orbitx-api-ve63.onrender.com/swagger-ui/index.html
```

> O plano free do Render dorme após 15 min sem uso. Acesse o Swagger antes de demonstrar para acordar o servidor (~30s na primeira requisição).

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
| Render | Deploy em produção (plano free) |

---

## Estrutura do Repositório

```
global/
├── orbit-x-backend/
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
│   │   ├── db/migration/               # V1 (schema), V2 (seed), V3 (frota)
│   │   └── application.yml
│   ├── Dockerfile
│   ├── render.yaml
│   └── pom.xml
│
├── orbit-x-notification-service/
│   ├── src/main/java/com/orbitx/notification/
│   │   ├── config/                     # RabbitMQConfig
│   │   ├── messaging/consumer/         # AlertEventConsumer
│   │   └── service/                    # NotificationService
│   ├── Dockerfile
│   └── pom.xml
│
└── docker-compose.yml
```

---

## Pré-requisitos

- **Java 17+**
- **Maven 3.9+**
- **Docker** e **Docker Compose**
- **Oracle Database 19c** (ou acesso à instância FIAP: `oracle.fiap.com.br:1521:ORCL`)
- **RabbitMQ** (via Docker Compose — já incluído)
- **Groq API Key** (opcional — sem ela o assistente opera em modo offline)

---

## Configuração

```bash
cp orbit-x-backend/.env.example .env
```

### Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DATABASE_USERNAME` | Sim | Usuário Oracle |
| `DATABASE_PASSWORD` | Sim | Senha Oracle |
| `DATABASE_SCHEMA` | Sim | Schema Oracle (ex: `RM560416`) |
| `JWT_SECRET` | Sim | Secret JWT (mín. 64 chars hex) |
| `GROQ_API_KEY` | Não | Chave Groq (`gsk_...`). Sem ela, modo offline é ativado |
| `GROQ_AI_ENABLED` | Não | `true` para ativar Spring AI + llama-3.3-70b |
| `RABBITMQ_ENABLED` | Não | `false` para desabilitar RabbitMQ (necessário no Render) |
| `RESEND_API_KEY` | Não | Chave Resend para envio de e-mails de recuperação de senha |
| `RABBITMQ_HOST` | Não | Host RabbitMQ (padrão: `localhost`) |
| `RABBITMQ_PORT` | Não | Porta RabbitMQ (padrão: `5672`) |
| `RABBITMQ_USER` | Não | Usuário RabbitMQ (padrão: `guest`) |
| `RABBITMQ_PASS` | Não | Senha RabbitMQ (padrão: `guest`) |

**Gerando um JWT_SECRET seguro:**
```bash
openssl rand -hex 32
```

---

## Como Executar

### Com Docker Compose (recomendado)

```bash
git clone https://github.com/renatosgk/OrbitX.API.git
cd OrbitX.API
cp orbit-x-backend/.env.example .env
docker compose up --build
```

| Serviço | URL |
|---|---|
| API Backend | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| Notification Service | http://localhost:8085 |
| RabbitMQ Management | http://localhost:15672 (guest/guest) |

### Execução local

```bash
# RabbitMQ via Docker
docker compose up rabbitmq -d

# Backend
cd orbit-x-backend
./mvnw spring-boot:run

# Notification Service (opcional)
cd orbit-x-notification-service
./mvnw spring-boot:run
```

---

## Endpoints da API

Endpoints protegidos exigem: `Authorization: Bearer <token>`

### Autenticação — `/api/v1/auth`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/auth/register` | ❌ | Registra empresa + admin, retorna JWT |
| `POST` | `/auth/login` | ❌ | Autentica e retorna JWT |
| `POST` | `/auth/forgot-password` | ❌ | Envia senha temporária por e-mail (Resend) |

### Dashboard — `/api/v1/dashboard`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `GET` | `/dashboard/kpis` | ✅ | KPIs em tempo real (energia, temperatura, PUE, carbono, previsão IA) |
| `GET` | `/dashboard/alerts` | ✅ | Alertas ativos não resolvidos |

### Infraestrutura — `/api/v1/infrastructure`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `GET` | `/infrastructure/datacenters` | ✅ | 6 datacenters globais (cache 30s) |
| `GET` | `/infrastructure/satellites` | ✅ | 5 satélites com posição orbital calculada (cache 15s) |

### Relatórios ESG — `/api/v1/reports`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `GET` | `/reports/sustainability-score` | ✅ | Score ESG, carbon offset, comparativo (cache 5min) |
| `GET` | `/reports/export/pdf` | ✅ | Download do relatório executivo em PDF |

### Assistente IA — `/api/v1/assistant`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| `POST` | `/assistant/chat` | ✅ | Chat com RAG + Tool Calling (Groq) ou modo offline |

**Payload de exemplo:**
```json
{
  "message": "Qual é o status térmico dos datacenters agora?",
  "history": []
}
```

---

## Autenticação JWT

Algoritmo **HS512**, validade de **24 horas**.

```bash
curl -X POST https://orbitx-api-ve63.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"seu@email.com","password":"suaSenha"}'
```

**Claims no token:**

| Campo | Descrição |
|---|---|
| `sub` | E-mail do usuário |
| `role` | `ADMIN` / `OPERATOR` / `VIEWER` |
| `companyId` | ID da empresa |
| `companyName` | Nome da empresa |

---

## Banco de Dados

Schema gerenciado pelo **Flyway** na inicialização.

| Tabela | Descrição |
|---|---|
| `companies` | Empresas (plano STARTER / PROFESSIONAL / ENTERPRISE) |
| `users` | Usuários com roles |
| `datacenters` | 6 datacenters com coordenadas geográficas |
| `satellites` | 5 satélites (LEO / MEO / GEO) |
| `alerts` | Histórico de alertas com severidade |

| Versão | Arquivo | Descrição |
|---|---|---|
| V1 | `V1__create_schema.sql` | Criação de tabelas e índices |
| V2 | `V2__seed_initial_data.sql` | Dados iniciais |
| V3 | `V3__seed_fleet_data.sql` | 6 datacenters + 5 satélites |

---

## Inteligência Artificial

### Modo online (Groq + Spring AI)

Ativado quando `GROQ_API_KEY=gsk_...` e `GROQ_AI_ENABLED=true`.

- **Modelo:** `llama-3.3-70b-versatile`
- **RAG:** `KnowledgeBaseConfig` injeta contexto técnico no system prompt
- **Tools disponíveis:**

| Tool | Descrição |
|---|---|
| `getLiveKpis()` | Temperatura, energia, PUE e risco ao vivo |
| `getDatacenterStatus()` | Estado térmico de todos os datacenters |
| `getActiveAlerts()` | Alertas não resolvidos |
| `getSustainabilityMetrics()` | Score ESG e carbon offset |

### Modo offline (KeywordAssistantService)

Sem chave Groq, responde sobre temperatura, energia, ESG, satélites e alertas usando dados de telemetria simulada, sem latência de rede.

### Motor de predição

| Temperatura | Probabilidade | Modo |
|---|---|---|
| > 30°C | ≥ 90% | `TURBO_COOLING` |
| 27–30°C | 50–90% | `ENHANCED_COOLING` |
| 24–27°C | 20–50% | `STANDARD_COOLING` |
| < 24°C | < 20% | `GREEN_MODE` |

---

## Mensageria (RabbitMQ)

```
orbit-x-backend
  ├── publishAlert()            → alert.event     → orbitx.alerts.queue
  └── publishThermalCritical()  → thermal.critical → orbitx.thermal.queue
                                                          │
                                          orbit-x-notification-service
                                            ├── consumeAlert()          → e-mail de alerta
                                            └── consumeThermalCritical() → log crítico + e-mail
```


---

## Documentação Swagger

```
https://orbitx-api-ve63.onrender.com/swagger-ui/index.html
```

Para testar endpoints protegidos:
1. `POST /api/v1/auth/login` → copie `data.accessToken`
2. Clique em **Authorize** → cole o token em **BearerAuth**

---

## Integrantes do Grupo

| Nome | RM |
|---|---|
| Fabio Eduardo | RM 560416 |
| Gabriel Wu Castro | RM 560210 |
| Lucas Chicote | RM 559366 |
| Renato Kenji Sugaki | RM 559810 |

---

*Orbit X — FIAP Global Solution 2026 — 2TDS Agosto*
