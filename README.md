# vog-springboot-starter

A small full-stack starter that catalogs living things (mammals, fish, birds,
plants, humans…) and sorts them into categories. Built as a learning-friendly
Spring Boot + React example.

## Structure

| Folder | Stack | Description |
|--------|-------|-------------|
| [`vog-demo/`](vog-demo/) | Spring Boot 4.1.0, Java 17, Spring Data JPA, H2 | REST API backend |
| `vog-web/` | React + Vite + TypeScript | Frontend that consumes the API |

## Repository structure

High-level map of what each folder is for:

```
vog-springboot-starter/
├── README.md                     # this file — repo overview + quick start
├── .gitignore                    # ignores build output, node_modules, .claude, etc.
│
├── vog-demo/                     # ── BACKEND (Spring Boot REST API) ──
│   ├── pom.xml                   # Maven build: dependencies, Java/Boot version, plugins
│   ├── mvnw, mvnw.cmd, .mvn/     # Maven Wrapper — runs the correct Maven version
│   ├── .sdkmanrc                 # pins Java 17 for this project (SDKMAN)
│   ├── README.md                 # backend details: API, run/test, request-flow diagrams
│   │
│   ├── src/main/java/com/vog/example/vog_demo/
│   │   ├── VogDemoApplication.java   # app entry point (@SpringBootApplication)
│   │   ├── entity/               # JPA entities — DB tables as Java classes (Category, Organism)
│   │   ├── repository/           # Spring Data repositories — data access (auto-generated SQL)
│   │   ├── service/              # business logic and rules
│   │   ├── controller/           # REST endpoints (@RestController) — map HTTP to methods
│   │   ├── dto/                  # request/response shapes + validation, decoupled from entities
│   │   ├── exception/            # custom exceptions + one global error handler
│   │   └── config/               # startup data seeder + CORS configuration
│   │
│   ├── src/main/resources/
│   │   └── application.properties    # runtime config (datasource, JPA, Swagger, H2 console)
│   ├── src/test/java/...         # automated tests (repository, service, controller)
│   │
│   └── docs/                     # ── PROJECT DOCUMENTATION ──
│       ├── SPRING-BOOT-DEV-GUIDE.md   # beginner guide: create project in VS Code, add libs, baseline
│       ├── TUTORIAL.md               # step-by-step run-through of this app
│       ├── ENVIRONMENT.md            # environment setup + Java version management (SDKMAN)
│       ├── diagrams/                 # request-flow diagrams (Mermaid source + rendered SVG)
│       └── superpowers/              # artifacts from the design/planning workflow (see below)
│           ├── specs/                # the approved design spec (what to build + why)
│           └── plans/                # the implementation plan (ordered, testable build tasks)
│
└── vog-web/                      # ── FRONTEND (React + Vite + TypeScript) ──
    ├── package.json              # frontend dependencies and scripts
    ├── .env                      # VITE_API_BASE — where the backend API lives
    ├── index.html                # HTML entry point
    ├── public/                   # static assets served as-is
    └── src/
        ├── App.tsx               # main page / app shell
        ├── App.css               # styles
        ├── api/                  # typed client — functions that call the backend
        └── components/           # UI pieces: OrganismList, OrganismForm, CategoryManager
```

> **About `docs/superpowers/`:** these are planning artifacts, not application code.
> The **spec** (`specs/`) captures the agreed design — scope, data model, API, and
> the rationale — and the **plan** (`plans/`) breaks that design into an ordered,
> test-driven list of build tasks. They document *how the project was designed and
> built*, and are useful for understanding decisions or extending the app. They are
> safe to delete if you don't need that history.

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
