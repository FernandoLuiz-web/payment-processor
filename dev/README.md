# Payment Processor

Microserviço gRPC para processamento de pagamentos, construído com **Quarkus 3.29** e **Java 21**.

## Arquitetura

O projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**, separando claramente as responsabilidades em camadas.
```
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ gRPC Adapter │  │ Persistence  │  │   Mappers    │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌──────────────┐  ┌──────────────┐                         │
│  │  Use Cases   │  │     DTOs     │                         │
│  └──────────────┘  └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
                            ↓ ↑
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │    Models    │  │ Repositories │  │   Services   │       │
│  │  (Entities)  │  │ (Interfaces) │  │ (Bus. Logic) │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### Estrutura de Pacotes
```
org.brava/
├── domain/                    # Regras de negócio e modelos
│   ├── model/                 # Entidades de domínio
│   ├── repositories/          # Interfaces de persistência
│   └── service/               # Serviços de domínio
│
├── application/               # Casos de uso
│   ├── usecase/               # Lógica de aplicação
│   └── dto/                   # Objetos de transferência
│
├── infrastructure/            # Adaptadores externos
│   ├── grpc/                  # Servidor gRPC
│   └── persistence/           # JPA/Panache
│       ├── entity/            # Entidades JPA
│       ├── repositories/      # Implementações
│       └── mapper/            # Conversores
│
└── shared/                    # Utilitários e exceções
    ├── exception/
    └── util/
```

## Stack Tecnológica

| Categoria           | Tecnologia                  |
|---------------------|----------------------------|
| Linguagem           | Java 21                    |
| Framework           | Quarkus 3.29               |
| Protocol            | gRPC + Protocol Buffers    |
| Database            | PostgreSQL 16              |
| ORM                 | Hibernate Panache          |
| Migrations          | Flyway                     |
| Observability       | Micrometer + OpenTelemetry |
| Health Checks       | SmallRye Health            |
| Container Runtime   | Docker                     |

## Funcionalidades

- ✅ Processamento de pagamentos via gRPC
- ✅ Idempotência (previne duplicação)
- ✅ Persistência em PostgreSQL
- ✅ Migrations automáticas com Flyway
- ✅ Health checks (`/q/health`)
- ✅ Tracing distribuído (OpenTelemetry)

## Requisitos

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

## Configuração

### 1. Subir o PostgreSQL
```bash
docker-compose up -d
```

### 2. Executar o serviço

**Modo desenvolvimento (hot reload):**
```bash
./mvnw quarkus:dev
```

**Build nativo (GraalVM):**
```bash
./mvnw package -Pnative
./target/payment-processor-1.0.0-SNAPSHOT-runner
```

**Build JVM (Docker):**
```bash
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t payment-processor:latest .
docker run -p 9000:9000 payment-processor:latest
```

## Endpoints

| Tipo   | Endpoint       | Descrição              |
|--------|----------------|------------------------|
| gRPC   | `:9000`        | PaymentService         |
| HTTP   | `/q/health`    | Health checks          |
| HTTP   | `/q/metrics`   | Métricas Prometheus    |

## Contrato gRPC
```protobuf
service PaymentService {
  rpc ProcessPayment (PaymentRequest) returns (PaymentResponse);
}

message PaymentRequest {
  string idempotencyKey = 1;
  string payerId = 2;
  string payeeId = 3;
  double amount = 4;
  string currency = 5;
  string description = 6;
}

message PaymentResponse {
  string transactionId = 1;
  string status = 2;
  string message = 3;
}
```

## Testes
```bash
# Testes unitários
./mvnw test

# Testes de integração
./mvnw verify
```

## Observabilidade

### Health Check
```bash
curl http://localhost:8080/q/health
```

## Migrations

As migrations do Flyway estão em `src/main/resources/db/migration/`:
```
V1__create_payments_table.sql
V2__add_indexes.sql
```

Executadas automaticamente no startup da aplicação.

## Decisões de Design

1. **Separação Domain/Infrastructure**: Domínio independente de frameworks
2. **Repository Pattern**: Abstração da camada de dados
3. **Idempotência**: Chave de idempotência previne duplicações
4. **Flyway**: Controle versionado do schema
5. **Clean Architecture**: Facilita manutenibilidade e testes

## Variáveis de Ambiente
```properties
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/payment_db
QUARKUS_DATASOURCE_USERNAME=postgres
QUARKUS_DATASOURCE_PASSWORD=postgres
QUARKUS_GRPC_SERVER_PORT=9000
```

## Roadmap

- [ ] Circuit breaker pattern
- [ ] Retry policy configurável
- [ ] Autenticação mTLS
- [ ] Cache distribuído (Redis)
- [ ] Event sourcing