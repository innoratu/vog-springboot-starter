# vog-springboot-starter

A small full-stack starter that catalogs living things (mammals, fish, birds,
plants, humans…) and sorts them into categories. Built as a learning-friendly
Spring Boot + React example.

## Structure

| Folder | Stack | Description |
|--------|-------|-------------|
| [`vog-demo/`](vog-demo/) | Spring Boot 4.1.0, Java 17, Spring Data JPA, H2 | REST API backend |
| `vog-web/` | React + Vite + TypeScript | Frontend that consumes the API |

## Quick start

Run both in separate terminals:

```bash
# Backend (port 8080)
cd vog-demo && sdk env && ./mvnw spring-boot:run

# Frontend (port 5173)
cd vog-web && npm install && npm run dev
```

- App UI: http://localhost:5173
- Swagger UI: http://localhost:8080/swagger-ui.html

## Documentation

All docs live under [`vog-demo/docs/`](vog-demo/docs/):

- **[SPRING-BOOT-DEV-GUIDE.md](vog-demo/docs/SPRING-BOOT-DEV-GUIDE.md)** — beginner Spring Boot guide: creating a project in VS Code, where Initializr settings live, adding libraries, changing the baseline.
- **[TUTORIAL.md](vog-demo/docs/TUTORIAL.md)** — step-by-step run-through of this app.
- **[ENVIRONMENT.md](vog-demo/docs/ENVIRONMENT.md)** — environment setup & Java version management.
- See also [`vog-demo/README.md`](vog-demo/README.md) for backend details, API, and request-flow diagrams.
