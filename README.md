# Wallet & Settlement Microservice

A production-ready Spring Boot microservice for wallet management and transaction reconciliation with PostgreSQL, RabbitMQ integration, and comprehensive testing.

## 🚀 Features

### Core Functionality
- **Wallet Management**: Create, topup, consume balance operations
- **Transaction Ledger**: Complete audit trail of all transactions
- **Reconciliation Engine**: Daily reconciliation with external payment providers
- **Concurrency Safety**: Optimistic locking prevents race conditions
- **Idempotency**: Duplicate transaction prevention using unique transaction IDs
- **Async Processing**: RabbitMQ integration for transaction queuing

### Technical Features
- **Spring Boot 3.x** with modern Java patterns
- **PostgreSQL** with Flyway migrations
- **RabbitMQ** with dead letter queues
- **Docker** containerization with Docker Compose
- **Comprehensive Testing** with Testcontainers
- **REST API** with proper error handling and validation
- **Production Monitoring** with Spring Actuator

## 📋 API Endpoints

### Wallet Operations

#### Get Balance
```http
GET /api/v1/wallets/{customerId}/balance
```
**Response:**
```json
{
    "customerId": "customer-123",
    "balance": 150.00,
    "timestamp": "2023-12-01T10:30:00"
}
```

#### Top Up Wallet
```http
POST /api/v1/wallets/{customerId}/topup
Content-Type: application/json

{
    "transactionId": "tx-12345",
    "amount": 100.00,
    "reference": "Bank transfer"
}
```
**Response:**
```json
{
    "transactionId": "tx-12345",
    "customerId": "customer-123",
    "type": "TOPUP",
    "amount": 100.00,
    "balanceBefore": 50.00,
    "balanceAfter": 150.00,
    "status": "COMPLETED",
    "reference": "Bank transfer",
    "timestamp": "2023-12-01T10:30:00"
}
```

#### Consume from Wallet
```http
POST /api/v1/wallets/{customerId}/consume
Content-Type: application/json

{
    "transactionId": "tx-12346",
    "amount": 25.00,
    "reference": "Purchase payment"
}
```
**Response:** Same format as topup with `"type": "CONSUME"`

### Reconciliation Operations

#### Get Reconciliation Report
```http
GET /api/v1/reconciliation/report?date=2023-12-01
```
**Response:**
```json
{
    "reconciliationDate": "2023-12-01",
    "summary": {
        "totalRecords": 150,
        "matchedRecords": 145,
        "missingInternalRecords": 2,
        "missingExternalRecords": 3,
        "amountMismatchRecords": 0,
        "totalInternalAmount": 15000.00,
        "totalExternalAmount": 14950.00,
        "discrepancyAmount": 50.00
    },
    "details": [...]
}
```

#### Export Reconciliation Report
```http
GET /api/v1/reconciliation/report/export?date=2023-12-01
```
Downloads CSV file with reconciliation details.

#### Upload External Data
```http
POST /api/v1/reconciliation/upload-csv
Content-Type: multipart/form-data

file: external_transactions.csv
```
```http
POST /api/v1/reconciliation/upload-json
Content-Type: multipart/form-data

file: external_transactions.json
```

## 🛠️ Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### 1. Clone and Build
```bash
git clone <repository-url>
cd wallet-settlement-system
mvn clean install
```

### 2. Start Infrastructure
```bash
docker-compose up -d postgres rabbitmq
```

### 3. Run Application
```bash
mvn spring-boot:run
```

### 4. Run with Docker
```bash
# Build the application
mvn clean package

# Start everything
docker-compose up --build
```

The application will be available at:
- **API**: http://localhost:8080/api/v1
- **Health Check**: http://localhost:8080/api/v1/actuator/health
- **RabbitMQ Management**: http://localhost:15672 (admin/admin)

## 🗄️ Database Schema

### Wallets Table
```sql
CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL UNIQUE,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,  -- Optimistic locking
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    type transaction_type NOT NULL,  -- TOPUP, CONSUME
    amount DECIMAL(19,2) NOT NULL,
    balance_before DECIMAL(19,2) NOT NULL,
    balance_after DECIMAL(19,2) NOT NULL,
    status transaction_status NOT NULL DEFAULT 'PENDING',
    reference VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Reconciliation Records Table
```sql
CREATE TABLE reconciliation_records (
    id BIGSERIAL PRIMARY KEY,
    reconciliation_date DATE NOT NULL,
    internal_transaction_id VARCHAR(100),
    external_transaction_id VARCHAR(100),
    internal_amount DECIMAL(19,2),
    external_amount DECIMAL(19,2),
    status reconciliation_status NOT NULL,  -- MATCHED, MISSING_*, AMOUNT_MISMATCH
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 📨 RabbitMQ Configuration

### Exchanges and Queues
- **Exchange**: `wallet.exchange` (topic)
- **Transaction Queue**: `wallet.transaction.processing`
- **Reconciliation Queue**: `wallet.reconciliation.processing`
- **Dead Letter Queues**: Automatic retry handling

### Message Flow
1. **Transaction Events**: Sent to transaction queue after wallet operations
2. **Reconciliation Events**: Sent during reconciliation processing
3. **Dead Letter Handling**: Failed messages routed to DLQ for investigation

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn test -Dtest="*IntegrationTest"
```

### Test Coverage
- **Unit Tests**: Service layer logic with Mockito
- **Integration Tests**: Full API testing with Testcontainers
- **Concurrency Tests**: Multi-threaded transaction safety
- **Database Tests**: Repository layer with embedded PostgreSQL

### Test Data Setup
Tests use Testcontainers to spin up real PostgreSQL and RabbitMQ instances:
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

@Container
static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management-alpine");
```

## 🔧 Configuration

### Application Properties
```yaml
# Database
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wallet_db
    username: wallet_user
    password: wallet_pass

# RabbitMQ
  rabbitmq:
    host: localhost
    port: 5672
    username: wallet_user
    password: wallet_pass
    virtual-host: wallet_vhost

# Custom Configuration
wallet:
  transaction:
    retry:
      max-attempts: 3
      delay: 1000
  reconciliation:
    batch-size: 1000
    schedule:
      cron: "0 0 2 * * ?"  # Daily at 2 AM
```

### Environment-Specific Profiles
- **local**: Default development profile
- **docker**: Docker container profile
- **test**: Testing profile with H2 database

## 🔐 Security & Production Considerations

### Implemented Security Features
- **Input Validation**: JSR-303 annotations on all DTOs
- **SQL Injection Prevention**: JPA prepared statements
- **Optimistic Locking**: Prevents race conditions
- **Idempotency**: Duplicate transaction prevention
- **Error Handling**: No sensitive data in error responses

### Production Readiness Checklist
- ✅ Database connection pooling
- ✅ Health checks and monitoring
- ✅ Structured logging with correlation IDs
- ✅ Graceful shutdown handling
- ✅ Circuit breaker patterns for external calls
- ✅ Comprehensive error handling
- ✅ Docker containerization

### Monitoring Endpoints
```bash
# Health Check
curl http://localhost:8080/api/v1/actuator/health

# Metrics
curl http://localhost:8080/api/v1/actuator/metrics

# Info
curl http://localhost:8080/api/v1/actuator/info
```

## 📁 Project Structure
```
src/
├── main/java/com/wallet/
│   ├── controller/           # REST controllers
│   ├── service/              # Business logic
│   ├── repository/           # Data access layer
│   ├── entity/               # JPA entities
│   ├── dto/                  # Request/Response objects
│   ├── messaging/            # RabbitMQ producers/consumers
│   ├── exception/            # Custom exceptions & handlers
│   └── config/               # Configuration classes
├── main/resources/
│   ├── db/migration/         # Flyway SQL scripts
│   └── application.yml       # Application configuration
└── test/java/com/wallet/
    ├── service/              # Unit tests
    ├── integration/          # Integration tests
    └── AbstractIntegrationTest.java  # Test base class
```

## 🚀 Deployment

### Docker Deployment
```bash
# Build application
mvn clean package -DskipTests

# Deploy with Docker Compose
docker-compose up -d

# Check status
docker-compose ps
docker-compose logs wallet-app
```

### Kubernetes Deployment
Example deployment files available in `/k8s` directory:
- Database deployment and service
- RabbitMQ deployment and service
- Application deployment with health checks
- ConfigMap and Secret configurations

## 📊 Performance Considerations

### Optimizations Implemented
- **Connection Pooling**: HikariCP for database connections
- **Batch Processing**: Reconciliation processes in configurable batches
- **Async Processing**: RabbitMQ for non-blocking operations
- **Optimistic Locking**: Better performance than pessimistic locking
- **Indexed Queries**: Database indexes on frequently queried columns

### Scalability Features
- **Stateless Design**: Can be horizontally scaled
- **Database Connection Pooling**: Efficient resource usage
- **Message Queue**: Decoupled async processing
- **Optimized Queries**: Pagination support for large datasets

## 🐛 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check PostgreSQL container
docker-compose logs postgres

# Verify connection
docker-compose exec postgres psql -U wallet_user -d wallet_db -c "\dt"
```

#### RabbitMQ Issues
```bash
# Check RabbitMQ status
docker-compose logs rabbitmq

# Access management UI
open http://localhost:15672
# Username: wallet_user, Password: wallet_pass
```

#### Application Issues
```bash
# Check application logs
docker-compose logs wallet-app

# Check health endpoint
curl http://localhost:8080/api/v1/actuator/health
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Write tests for new functionality
4. Ensure all tests pass: `mvn test`
5. Commit changes: `git commit -m 'Add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Submit pull request

Created with ❤️ by Pucci.
