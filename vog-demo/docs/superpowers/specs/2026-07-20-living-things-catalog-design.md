# Design Spec — Living Things Catalog ("Vog")

**Date:** 2026-07-20
**Status:** Approved (design), pending implementation plan
**Backend module:** `vog-demo/` (Spring Boot 4.1.0, Java 17)
**Frontend module:** `vog-web/` (React + Vite + TypeScript) — new sibling folder

---

## 1. Purpose

A catalog application for recording living things and assigning each to a
category (Mammal, Fish, Bird, Plant, Human, Reptile, …). Users can create, read,
update, and delete both organisms and categories, and browse organisms filtered
by category.

Scope is deliberately small — a clean, well-structured CRUD app that is easy to
read and learn from. It is **not** an auto-classifier and categories are a **flat
list** (no biological tree/hierarchy).

## 2. Functionality

### Categories
- List all categories.
- Get one category by id.
- Create a category (unique `name`).
- Update a category.
- Delete a category (blocked with a clear error if organisms still reference it).

### Organisms
- List all organisms, with optional filters: `?categoryId=` and/or `?name=`.
- Get one organism by id.
- Create an organism (must reference an existing category).
- Update an organism.
- Delete an organism.

### Data seeding
On first startup the app seeds a starter set of categories
(Mammal, Fish, Bird, Plant, Human, Reptile, Amphibian, Insect) and a few example
organisms so the UI has something to show immediately.

### Validation & errors
- Required fields validated (e.g. organism `commonName`, category `name`).
- Invalid input → **400** with a JSON body listing field errors.
- Unknown id → **404** with a clear message.
- Deleting an in-use category → **409 Conflict**.
- All errors share one consistent JSON shape.

## 3. Data model

Two tables, a simple many-to-one relationship (many organisms → one category).

```
Category                         Organism
--------                         --------
id           Long (PK)           id             Long (PK)
name         String (unique)     commonName     String   (required)
description  String (nullable)   scientificName String   (nullable)
                                  habitat        String   (nullable)
                                  description    String   (nullable)
                                  category_id    Long (FK -> Category.id)
```

## 4. REST API

Base path: `/api`

| Method | Path | Purpose | Success |
|--------|------|---------|---------|
| GET | `/api/categories` | List categories | 200 |
| GET | `/api/categories/{id}` | Get category | 200 |
| POST | `/api/categories` | Create category | 201 |
| PUT | `/api/categories/{id}` | Update category | 200 |
| DELETE | `/api/categories/{id}` | Delete category | 204 |
| GET | `/api/organisms?categoryId=&name=` | List/filter organisms | 200 |
| GET | `/api/organisms/{id}` | Get organism | 200 |
| POST | `/api/organisms` | Create organism | 201 |
| PUT | `/api/organisms/{id}` | Update organism | 200 |
| DELETE | `/api/organisms/{id}` | Delete organism | 204 |

Interactive API docs served by Swagger UI at `/swagger-ui.html`.

Example — create an organism:
```bash
curl -X POST http://localhost:8080/api/organisms \
  -H 'Content-Type: application/json' \
  -d '{"commonName":"Blue Whale","scientificName":"Balaenoptera musculus","habitat":"Ocean","categoryId":1}'
```

## 5. Architecture & main components (for a Spring Boot newcomer)

Spring Boot apps are organized in layers. A request flows **top to bottom** and the
response flows back up:

```
HTTP request
   │
   ▼
[Controller]   @RestController — maps URLs/verbs to Java methods, parses JSON in,
   │                             returns JSON out. Knows HTTP, not business rules.
   ▼
[Service]      @Service — the business logic (validation, "can't delete in-use
   │                       category", filtering). Knows rules, not HTTP or SQL.
   ▼
[Repository]   interface extends JpaRepository — data access. Spring generates the
   │                       SQL for save/find/delete automatically. Knows the DB.
   ▼
[Entity]       @Entity — a plain Java class mapped to a database table.
   │
   ▼
 H2 database (in-memory)
```

**Key building blocks you'll see in the code:**

- **`@SpringBootApplication` main class** (`VogDemoApplication`) — the entry point.
  Its `main()` boots the whole app: it starts an embedded web server (Tomcat) and
  wires everything together automatically ("auto-configuration").

- **Entity** (`@Entity` classes: `Category`, `Organism`) — Java objects that JPA
  maps to database rows. `@Id`/`@GeneratedValue` mark the primary key;
  `@ManyToOne` links an organism to its category.

- **Repository** (`CategoryRepository`, `OrganismRepository`) — interfaces that
  extend `JpaRepository<Entity, Long>`. You write **no SQL**; you just declare
  method names like `findByCategoryId(...)` and Spring implements them.

- **Service** (`CategoryService`, `OrganismService`) — `@Service` classes holding
  the business rules. Controllers call services; services call repositories.

- **Controller** (`CategoryController`, `OrganismController`) — `@RestController`
  classes. Annotations like `@GetMapping`, `@PostMapping`, `@PathVariable`,
  `@RequestBody` connect HTTP to methods.

- **DTOs** (Data Transfer Objects) — request/response records (e.g.
  `OrganismRequest`, `OrganismResponse`) so the JSON shape of the API is decoupled
  from the database entities. Validation annotations (`@NotBlank`, `@NotNull`)
  live here.

- **Global exception handler** (`@RestControllerAdvice`) — one place that turns
  exceptions (not-found, validation, conflict) into consistent JSON error bodies.

- **Data seeder** (`CommandLineRunner` bean) — runs once at startup to insert the
  example categories and organisms.

- **Dependency Injection** — you never call `new CategoryService()`. You declare
  what a class needs in its constructor and Spring supplies ("injects") it. This
  is why the layers stay loosely coupled and easy to test.

- **`application.properties`** — configuration (datasource, JPA, logging). No code
  changes needed to point at a different database later.

## 6. Project structure

```
vog-demo/                         # Spring Boot backend
├── .sdkmanrc                     # pins Java 17 for this project
├── pom.xml                       # dependencies & build
├── docs/
│   ├── ENVIRONMENT.md            # env setup/validation (done)
│   └── superpowers/specs/        # this spec
└── src/
    ├── main/java/com/vog/example/vog_demo/
    │   ├── VogDemoApplication.java
    │   ├── entity/       Category.java, Organism.java
    │   ├── repository/   CategoryRepository.java, OrganismRepository.java
    │   ├── service/      CategoryService.java, OrganismService.java
    │   ├── controller/   CategoryController.java, OrganismController.java
    │   ├── dto/          request/response records
    │   ├── exception/    NotFoundException, InUseException, GlobalExceptionHandler
    │   └── config/       DataSeeder, CorsConfig
    ├── main/resources/   application.properties
    └── test/java/...     controller + service + repository tests

vog-web/                          # React frontend (separate module)
├── package.json
├── vite.config.ts
└── src/
    ├── api/              typed API client (fetch wrappers)
    ├── components/       OrganismList, OrganismForm, CategoryManager
    └── App.tsx
```

## 7. pom.xml changes

**Remove** (they conflict with the servlet + explicit-controller approach):
- `spring-boot-starter-webflux` (+ its test starter)
- `spring-boot-starter-data-rest` (+ its test starter)
- `spring-boot-starter-restclient` (+ its test starter) — not needed; the app is
  not calling outbound HTTP services.

**Add:**
- `spring-boot-starter-web` — REST controllers + embedded Tomcat.
- `spring-boot-starter-data-jpa` — repositories/ORM.
- `spring-boot-starter-validation` — `@NotBlank` etc.
- `com.h2database:h2` (runtime) — in-memory database.
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI (version confirmed
  compatible with Boot 4.1 during implementation).

**Keep:** `lombok`, `spring-boot-devtools`.

## 8. Configuration (`application.properties`)

```properties
spring.application.name=vog-demo

# H2 in-memory database
spring.datasource.url=jdbc:h2:mem:vogdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# H2 web console for inspecting data during dev
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

CORS is configured to allow the Vite dev server origin (`http://localhost:5173`)
so the React app can call the API.

## 9. How to run

**Backend:**
```bash
cd vog-demo
sdk env                 # activate Java 17 (or rely on auto-env)
./mvnw spring-boot:run  # starts on http://localhost:8080
```
- API base: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:vogdb`)

**Frontend:**
```bash
cd vog-web
npm install
npm run dev             # starts on http://localhost:5173
```

## 10. How to test

**Backend (JUnit + Spring Boot Test):**
```bash
cd vog-demo
./mvnw test
```
Coverage:
- **Repository tests** (`@DataJpaTest`) — custom finders like `findByCategoryId`.
- **Service tests** — business rules (e.g. deleting an in-use category fails).
- **Controller tests** (`@WebMvcTest` + MockMvc) — status codes, JSON shape,
  validation → 400, unknown id → 404.

**Manual testing:** use Swagger UI, the `curl` examples above, or the React app.

**Frontend:** kept light — optional component smoke tests via Vitest.

## 11. Out of scope (YAGNI)

- Authentication / user accounts.
- Hierarchical taxonomy (kingdom/phylum/class tree).
- Auto-classification from attributes.
- Image upload, pagination, search beyond simple name/category filter.
- Production database / deployment (H2 in-memory is enough for the demo; the
  config can later be pointed at Postgres with no code change).
