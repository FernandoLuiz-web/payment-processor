# Quarkus Payment Gateway

## Visão Geral

O **Quarkus Payment Gateway** é uma demonstração prática de arquitetura de **microserviços em Java 22**, desenvolvida com **Quarkus 3.x**, integrando comunicação **REST e gRPC**, persistência com **PostgreSQL** e instrumentação com **Micrometer/OpenTelemetry**.

O objetivo é simular um fluxo realista de processamento de pagamentos em um ecossistema de microsserviços, com um serviço público de API REST e um serviço interno especializado que executa o processamento via gRPC.

---

## Arquitetura

```
+---------------------+        gRPC       +-----------------------+
|   payment-api       | --------------->  |   payment-processor   |
| REST (HTTP JSON)    |                   |   gRPC (Protobuf)     |
|                     | <---------------  |   Status: APPROVED    |
| Observability, etc. |                   |                       |
+---------------------+                   +-----------------------+
```

* **payment-api**: API REST pública, orquestra o fluxo de pagamento e comunica-se com o processador via gRPC.
* **payment-processor**: serviço gRPC responsável por processar o pagamento e retornar o status.

Essa separação ilustra boas práticas de *loose coupling*, *bounded contexts* e comunicação síncrona entre microsserviços.

---

## Objetivos Técnicos

| Área                             | Demonstração                                                  |
| -------------------------------- | ------------------------------------------------------------- |
| Framework moderno                | Uso do Quarkus com CDI, Panache ORM, RESTEasy Reactive e gRPC |
| Comunicação entre microsserviços | REST → gRPC                                                   |
| Persistência                     | PostgreSQL via Panache                                        |
| Observabilidade                  | Health, Metrics, Tracing                                      |
| Resiliência                      | Retry e Timeout configuráveis                                 |
| Deployability                    | Dockerfile + Docker Compose                                   |
| Testabilidade                    | Testes integrados com QuarkusTest e REST Assured              |

---

## Estrutura do Projeto

```
quarkus-payment-gateway/
├── payment-api/
│   ├── src/main/java/com/example/paymentapi/
│   │   ├── controller/PaymentResource.java
│   │   ├── service/PaymentService.java
│   │   ├── dto/PaymentRequest.java
│   │   ├── dto/PaymentResponse.java
│   │   └── grpc/PaymentProcessorClient.java
│   └── src/main/resources/application.properties
│
├── payment-processor/
│   ├── src/main/proto/payment.proto
│   ├── src/main/java/com/example/paymentprocessor/
│   │   ├── service/PaymentProcessorService.java
│   │   └── model/PaymentStatus.java
│   └── src/main/resources/application.properties
│
└── docker-compose.yml
```

---

## Fluxo de Execução

### 1. Endpoint REST

**POST /api/payments**

Exemplo de requisição:

```json
{
  "transactionId": "123",
  "amount": 100.0,
  "method": "CREDIT_CARD"
}
```

Fluxo:

1. O serviço **payment-api** recebe a requisição REST.
2. Valida e envia via gRPC para o **payment-processor**.
3. O **payment-processor** simula o processamento e retorna um status (`APPROVED` ou `DECLINED`).
4. O **payment-api** persiste o resultado no PostgreSQL e retorna a resposta JSON.

Exemplo de resposta:

```json
{
  "transactionId": "123",
  "status": "APPROVED"
}
```

---

## Tecnologias

| Categoria           | Tecnologia                                 |
| ------------------- | ------------------------------------------ |
| Linguagem           | Java 21                                    |
| Framework           | Quarkus 3.x                                |
| Comunicação interna | gRPC                                       |
| API externa         | RESTEasy Reactive                          |
| Banco de dados      | PostgreSQL                                 |
| ORM                 | Panache                                    |
| Observabilidade     | Micrometer, SmallRye Health, OpenTelemetry |
| Testes              | JUnit 5, QuarkusTest, REST Assured         |
| Containerização     | Dockerfile + Docker Compose                |

---

## Execução Local

### 1. Clonar o repositório

```bash
git clone https://github.com/seu-usuario/quarkus-payment-gateway.git
cd quarkus-payment-gateway
```

### 2. Subir ambiente

```bash
docker-compose up -d
```

Serviços:

* PostgreSQL
* `payment-processor` (porta 9000)
* `payment-api` (porta 8080)

### 3. Testar endpoint

```bash
curl -X POST http://localhost:8080/api/payments \
     -H "Content-Type: application/json" \
     -d '{"transactionId":"123", "amount":100.0, "method":"CREDIT_CARD"}'
```

---

## Arquivo Proto

`payment.proto`

```proto
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.example.paymentprocessor.grpc";
option java_outer_classname = "PaymentProto";

service PaymentProcessor {
  rpc ProcessPayment (PaymentRequest) returns (PaymentResponse);
}

message PaymentRequest {
  string transactionId = 1;
  double amount = 2;
  string method = 3;
}

message PaymentResponse {
  string transactionId = 1;
  string status = 2;
}
```

---

## Roadmap Sugerido (8h de Implementação)

| Etapa                      | Tarefas                              | Tempo estimado |
| -------------------------- | ------------------------------------ | -------------- |
| Setup inicial              | Estruturar módulos, configs e Docker | 1h             |
| gRPC contract + service    | Criar proto e implementar servidor   | 1.5h           |
| REST API + client gRPC     | Endpoint REST e integração           | 1.5h           |
| Persistência               | Configurar Panache + PostgreSQL      | 1h             |
| Observabilidade            | Health + Metrics                     | 0.5h           |
| Testes                     | REST Assured + gRPC integration test | 1.5h           |
| Documentação e refinamento | README + scripts                     | 1h             |

---

## Explicação Técnica

Este projeto demonstra uma arquitetura de microsserviços orientada a contratos, utilizando Quarkus.
O módulo `payment-api` atua como gateway REST e orquestrador, enquanto o módulo `payment-processor` é um serviço interno otimizado para performance via gRPC.
A integração entre os dois exemplifica práticas modernas de comunicação interserviços, separação de responsabilidades, rastreabilidade e observabilidade — pilares essenciais em sistemas distribuídos.
