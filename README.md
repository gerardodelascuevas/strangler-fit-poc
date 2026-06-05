# Strangler Fig Pattern — Proof of Concept

## What is this?

A demonstration of how to **modernize a legacy application** without rewriting it from scratch, using the [Strangler Fig Pattern](https://martinfowler.com/bliki/StranglerFigApplication.html).

This project contains:

| Module | Description | Tech Stack |
|--------|-------------|------------|
| `legacy-catalog` | A terrible SOAP service full of anti-patterns and code smells | Java 17, Apache Axis2, raw JDBC, Jetty |
| `modern-catalog` | A clean REST API implementing new functionality | Java 17, Spring Boot 3.2, JPA, HATEOAS, OpenAPI |
| `router` | An API gateway that routes traffic to the appropriate service | Nginx |

Both services share the **same database**, simulating a real-world scenario where the modern service reads/writes to the same data store as the legacy one.

## Architecture

```
                    ┌─────────────┐
                    │   Nginx     │
                    │   Router    │
                    │  (port 80)  │
                    └──────┬──────┘
                           │
              ┌────────────┴────────────┐
              │                         │
    /services/*                  /api/*
              │                         │
    ┌─────────▼─────────┐    ┌─────────▼─────────┐
    │  Legacy Catalog   │    │  Modern Catalog   │
    │  (SOAP - Axis2)   │    │  (REST - Spring)  │
    │  port 8080        │    │  port 8081        │
    └─────────┬─────────┘    └─────────┬─────────┘
              │                         │
              └────────────┬────────────┘
                           │
                  ┌────────▼────────┐
                  │   Shared H2     │
                  │   Database      │
                  │   (TCP mode)    │
                  └─────────────────┘
```

## The Strangler Fig Strategy

1. **Identify** a module to extract (in this case: reporting/analytics)
2. **Build** the new functionality with modern practices alongside the legacy system
3. **Route** traffic — legacy CRUD stays on the old service, new reporting goes to the modern one
4. **Retire** the legacy equivalent once the modern service is proven stable

## Anti-patterns in the Legacy Code (intentional!)

The `legacy-catalog` module is deliberately written with common anti-patterns to illustrate what "bad legacy code" looks like:

| Anti-pattern | Where |
|---|---|
| **SQL Injection** | `ProductDatabase.java` — string concatenation in all queries |
| **Resource leaks** | Connections, Statements, ResultSets never properly closed |
| **No encapsulation** | `Product.java` — all fields are public |
| **Primitive obsession** | Everything is `String` (price, stock, IDs) |
| **Static methods everywhere** | Untestable, no dependency injection |
| **Singleton connection** | `DatabaseManager` — single shared Connection, no pool |
| **System.out as logging** | No logging framework, just println |
| **Mixed responsibilities** | Service does validation, formatting, business logic, and reporting |
| **Mutable shared state** | `callCounter` — not thread-safe |
| **Magic numbers** | Hardcoded discount logic (price > 1000 → 10% off) |
| **Error handling via strings** | Returns "ERROR:..." instead of exceptions |

## Modern Code Practices (modern-catalog)

| Practice | Implementation |
|---|---|
| Layered architecture | Controller → Service → Repository |
| Immutable DTOs | Records/final fields with proper types |
| Bean Validation | `@NotBlank`, `@Min`, `@DecimalMin` |
| Global exception handling | `@RestControllerAdvice` |
| Structured logging | SLF4J + Logback |
| Unit tests | JUnit 5 + Mockito + AssertJ |
| Pagination | Spring Data Pageable |
| HATEOAS | Hypermedia links in responses |
| API documentation | OpenAPI 3 / Swagger UI |
| Transactional reads | `@Transactional(readOnly = true)` |

## Running the Project

### With Docker Compose (recommended)

```bash
docker-compose up --build
```

This starts all three services:
- **Legacy SOAP**: http://localhost/services/ProductCatalogService?wsdl
- **Modern REST**: http://localhost/api/products/reports/inventory
- **Swagger UI**: http://localhost/swagger-ui.html
- **H2 Console**: http://localhost:9092 (JDBC URL: `jdbc:h2:tcp://h2-db:9092/catalog`)

### Without Docker

1. Start the H2 database in TCP mode:
   ```bash
   java -cp h2-2.2.224.jar org.h2.tools.Server -tcp -tcpAllowOthers -ifNotExists
   ```

2. Start the legacy service:
   ```bash
   cd legacy-catalog
   mvn jetty:run
   ```

3. Start the modern service:
   ```bash
   cd modern-catalog
   mvn spring-boot:run
   ```

## API Examples

### Legacy SOAP (the ugly way)
```xml
POST http://localhost:8080/services/ProductCatalogService
Content-Type: text/xml

<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.catalog.legacy.com">
   <soapenv:Body>
      <ser:getAllProducts/>
   </soapenv:Body>
</soapenv:Envelope>
```

### Modern REST (the clean way)
```bash
# Inventory report
curl http://localhost:8081/api/products/reports/inventory?lowStockThreshold=10

# Search with pagination
curl "http://localhost:8081/api/products/reports/search?q=laptop&page=0&size=5"

# Single product report
curl http://localhost:8081/api/products/reports/products/1

# Category summaries
curl http://localhost:8081/api/products/reports/categories
```

## Key Takeaway

> You don't need to rewrite the world. You can grow new, clean code around the old system — like a fig tree growing around an aging host — until the legacy is fully replaced.

## License

MIT
