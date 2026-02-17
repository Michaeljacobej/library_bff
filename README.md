# library_bff

Self-contained Java microservice for managing a library catalog, members, and loans.

## Features

- Books, members, and loans REST APIs
- Borrowing rules enforced in service layer configuration
- Persistence delegated to sql_adapter service
- Basic authentication with in-memory users
- Observability via Spring Boot Actuator
- OpenAPI documentation via springdoc

## Requirements

- Java 17
- Maven 3.9+
- Running sql_adapter service (with its own database)

## Configuration

Borrowing rules live in [src/main/resources/application.yml](src/main/resources/application.yml):

```yaml
app:
	borrowing:
		maxActiveLoansPerMember: 5
		maxLoanDays: 14
	sql-adapter:
		base-url: http://localhost:8081
```

## Database

library_bff does not connect to a database directly. All reads and writes go through sql_adapter,
which is responsible for database connectivity and schema migrations.

Schema reference: [src/main/resources/db/migration/V1__init.sql](src/main/resources/db/migration/V1__init.sql)

## Run

Start sql_adapter separately (default: http://localhost:8081), then run:

```bash
mvn spring-boot:run
```

## Tests

```bash
mvn test
```

## Authentication

Obtain a JWT:

```bash
curl -X POST http://localhost:8080/auth/login \
	-H "Content-Type: application/json" \
	-d '{"username":"admin","password":"admin-pass"}'
```

Use the token:

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/books
```

## API Documentation

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Example Requests

```bash
curl -u admin:admin-pass http://localhost:8080/api/books
```

```bash
curl -u librarian:librarian-pass -X POST http://localhost:8080/api/books \
	-H "Content-Type: application/json" \
	-d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"9780132350884","totalCopies":3,"availableCopies":3}'
```

```bash
curl -u member:member-pass -X POST http://localhost:8080/api/loans/borrow \
	-H "Content-Type: application/json" \
	-d '{"bookId":1,"memberId":1}'
```

## Observability

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`