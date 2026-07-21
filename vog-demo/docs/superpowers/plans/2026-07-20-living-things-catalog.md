# Living Things Catalog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot REST backend + React frontend to catalog living things and assign each to a flat category.

**Architecture:** Layered Spring Boot (Controller → Service → Repository → JPA Entity) over in-memory H2, exposing `/api` JSON endpoints; a separate Vite/React/TypeScript app consumes the API over CORS.

**Tech Stack:** Java 17, Spring Boot 4.1.0, Spring Data JPA, H2, Bean Validation, springdoc OpenAPI, Lombok; React 18 + Vite + TypeScript.

## Global Constraints
- Java 17 (pinned via `.sdkmanrc` = `17.0.19-tem`); build via `./mvnw`.
- Package root: `com.vog.example.vog_demo`.
- API base path: `/api`; error responses share one JSON shape.
- Categories are a flat list; organism → category is many-to-one.
- Frontend dev origin `http://localhost:5173` must be allowed via CORS.

---

### Task 1: Fix pom.xml dependencies
**Files:** Modify `vog-demo/pom.xml`
- [ ] Remove `starter-webflux`, `starter-data-rest`, `starter-restclient` (+ their test starters).
- [ ] Add `starter-web`, `starter-data-jpa`, `starter-validation`, `h2` (runtime), `springdoc-openapi-starter-webmvc-ui`.
- [ ] Run `./mvnw dependency:resolve` — expect BUILD SUCCESS.
- [ ] Commit.

### Task 2: Configuration
**Files:** Modify `vog-demo/src/main/resources/application.properties`
- [ ] Add H2 datasource, JPA (`ddl-auto=update`, `show-sql=true`), H2 console per spec §8.
- [ ] Run `./mvnw spring-boot:run`, confirm it starts on 8080, stop it. Commit.

### Task 3: Entities
**Files:** Create `entity/Category.java`, `entity/Organism.java`
**Produces:** `Category(id,name,description)`, `Organism(id,commonName,scientificName,habitat,description,category)`.
- [ ] Write `@DataJpaTest` saving a Category+Organism and reading them back.
- [ ] Run test → fails (no classes). Create entities with JPA + Lombok annotations. Run → passes. Commit.

### Task 4: Repositories
**Files:** Create `repository/CategoryRepository.java`, `repository/OrganismRepository.java`; Test `OrganismRepositoryTest`
**Produces:** `CategoryRepository extends JpaRepository<Category,Long>` with `findByName`; `OrganismRepository` with `findByCategoryId(Long)`, `findByCommonNameContainingIgnoreCase(String)`.
- [ ] Write `@DataJpaTest` for `findByCategoryId`. Run → fails. Add repos. Run → passes. Commit.

### Task 5: DTOs + error types
**Files:** Create `dto/CategoryRequest`, `dto/CategoryResponse`, `dto/OrganismRequest`, `dto/OrganismResponse`, `dto/ApiError`; `exception/NotFoundException`, `exception/InUseException`
**Produces:** request records with validation (`@NotBlank name`; `@NotBlank commonName`, `@NotNull categoryId`); response records; runtime exceptions.
- [ ] Create records + exceptions (no test yet — exercised via services/controllers). Compile. Commit.

### Task 6: CategoryService
**Files:** Create `service/CategoryService.java`; Test `CategoryServiceTest`
**Produces:** `list()`, `get(id)`, `create(req)`, `update(id,req)`, `delete(id)` → returns/accepts DTOs; `delete` throws `InUseException` if organisms reference it; missing id throws `NotFoundException`.
- [ ] Write test: delete in-use category throws `InUseException`; get unknown throws `NotFoundException`. Run → fails. Implement. Run → passes. Commit.

### Task 7: OrganismService
**Files:** Create `service/OrganismService.java`; Test `OrganismServiceTest`
**Produces:** `list(categoryId,name)`, `get(id)`, `create(req)`, `update(id,req)`, `delete(id)`; create/update validates category exists (`NotFoundException`).
- [ ] Write test: create with unknown categoryId throws `NotFoundException`; list filters by categoryId. Run → fails. Implement. Run → passes. Commit.

### Task 8: Global exception handler
**Files:** Create `exception/GlobalExceptionHandler.java` (`@RestControllerAdvice`)
**Produces:** maps `NotFoundException`→404, `InUseException`→409, `MethodArgumentNotValidException`→400, all as `ApiError` JSON.
- [ ] Implement handlers. Compile. Commit (tested via controller tests next).

### Task 9: Controllers
**Files:** Create `controller/CategoryController.java`, `controller/OrganismController.java`; Test `CategoryControllerTest`, `OrganismControllerTest` (`@WebMvcTest` + MockMvc, service mocked)
**Produces:** REST endpoints per spec §4.
- [ ] Write MockMvc tests: POST valid→201; POST invalid→400; GET unknown→404. Run → fails. Implement controllers. Run → passes. Commit.

### Task 10: Data seeder + CORS
**Files:** Create `config/DataSeeder.java` (`CommandLineRunner`), `config/CorsConfig.java`
- [ ] Seed categories (Mammal, Fish, Bird, Plant, Human, Reptile, Amphibian, Insect) + a few organisms if DB empty.
- [ ] CORS allow `http://localhost:5173` for `/api/**`.
- [ ] `./mvnw test` all green; run app, `curl /api/categories` shows seed data. Commit.

### Task 11: React frontend
**Files:** Create `vog-web/` (Vite react-ts), `src/api/client.ts`, `src/components/OrganismList.tsx`, `OrganismForm.tsx`, `CategoryManager.tsx`, `src/App.tsx`, `.env` with `VITE_API_BASE=http://localhost:8080/api`
- [ ] `npm create vite@latest vog-web -- --template react-ts`; `npm install`.
- [ ] Typed API client (fetch) for categories + organisms.
- [ ] OrganismList with category filter; OrganismForm create; CategoryManager list/add.
- [ ] `npm run build` succeeds; `npm run dev` + backend running shows seeded organisms. Commit.

### Task 12: README
**Files:** Create/update `vog-demo/README.md`
- [ ] Document run/test for backend + frontend, Swagger URL, H2 console (link ENVIRONMENT.md). Commit.

## Self-Review
- Spec §2–4 functionality → Tasks 6,7,9. Data model §3 → Task 3. API §4 → Task 9. Errors → Tasks 5,8. Seeding → Task 10. pom §7 → Task 1. Config §8 → Task 2. Run/test §9,10 → Tasks 10,12. React §5/§6 → Task 11. No gaps.
- Type names consistent across tasks (`findByCategoryId`, DTO record names).
- No placeholders.
