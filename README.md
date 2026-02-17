# library_bff

Library microservice for books, members, and loans. All persistence goes through the sql_adapter service.

## Features

- Books, members, loans, reservations
- Borrowing rules enforced in service layer configuration
- SQL adapter integration for all reads and writes
- JWT authentication and role-based access control
- Audit log API (read-only)
- Loan history search
- Soft deletes for books and members
- Observability via Spring Boot Actuator
- OpenAPI documentation via springdoc

## Requirements

- Java 17
- Maven 3.9+
- Running sql_adapter service (with its own database)

## Configuration

Borrowing rules and adapter settings live in [src/main/resources/application.yml](src/main/resources/application.yml):

```yaml
app:
	borrowing:
		maxActiveLoansPerMember: 5
		maxLoanDays: 14
	sql-adapter:
		base-url: http://localhost:8081
		base-path: /api
	security:
		jwt:
			secret: change-me-change-me-change-me-change-me
			expiration: 2h
			issuer: library-bff
```

## Database

library_bff does not connect to a database directly. All reads and writes go through sql_adapter,
which is responsible for database connectivity and schema migrations.

Schema reference: [src/main/resources/db/migration/V1__init.sql](src/main/resources/db/migration/V1__init.sql),
[src/main/resources/db/migration/V2__add_roles.sql](src/main/resources/db/migration/V2__add_roles.sql),
[src/main/resources/db/migration/V3__reservations_soft_delete.sql](src/main/resources/db/migration/V3__reservations_soft_delete.sql)

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
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/books
```

```bash
curl -H "Authorization: Bearer <token>" -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","isbn":"9780132350884","totalCopies":3,"availableCopies":3}'
```

```bash
curl -H "Authorization: Bearer <token>" -X POST http://localhost:8080/api/loans/borrow \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"memberId":1}'
```

## Observability

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## Audit Log API

```bash
curl -H "Authorization: Bearer <token>" \
	"http://localhost:8080/api/audit-logs?user=admin&from=2026-02-01T00:00:00Z&to=2026-02-28T23:59:59Z"
```

## Loan History Search

```bash
curl -H "Authorization: Bearer <token>" \
	"http://localhost:8080/api/loans/search?memberId=1&status=OVERDUE"
```

## Reservations

When a book has no available copies, a reservation is created automatically on borrow. You can also create one:

```bash
curl -H "Authorization: Bearer <token>" -X POST http://localhost:8080/api/reservations \
	-H "Content-Type: application/json" \
	-d '{"bookId":1,"memberId":1}'
```