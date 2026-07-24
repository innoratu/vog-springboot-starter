# TMF620 Product Catalog tutorial step — design

**Date:** 2026-07-24
**Status:** Approved by user (this revision incorporates their follow-up requirements)
**Audience for the deliverable:** developers who completed `vog-demo/docs/TUTORIAL.md`; familiar with TM Forum only at a very high level.

## Goal

Take the SPRING starter repo "one step further": a new runnable Spring Boot module implementing a
recognizably-conformant slice of **TMF620 Product Catalog Management**, plus a tutorial document
that (a) gently introduces TM Forum Open APIs, (b) teaches how to build a TMF API from scratch,
and (c) gives a concise, proven best-practice method for retrofitting/migrating an existing
(including legacy) application to a TMF-compliant spec.

## Decisions made during brainstorming

| Decision | Choice |
|---|---|
| TMF API | TMF620 Product Catalog Management (maps naturally onto vog-demo's catalog domain) |
| Code location | New sibling module `vog-tmf`; `vog-demo` untouched except a "Where to next" pointer |
| Code production | Hand-written for teachability; contract-first codegen explained (not executed) as the production approach |
| Scope | `category`, `productSpecification`, `productOffering` CRUD + TMF630 REST-guideline basics; hub/events and import/export docs-only |
| Retrofit track | Worked guide + small coded read-only adapter exposing vog-demo Category data as TMF `category` |
| Docs | One new `vog-tmf/docs/TMF-TUTORIAL.md` |

User's follow-up requirements (all incorporated below):

1. **Cross-reference old material:** whenever a previously-introduced term reappears (Lombok, JPA,
   Spring Boot, DTO, MockMvc, H2, springdoc…), give a one-line refresher **and a relative link** to
   the existing doc/anchor where it was taught (`../../vog-demo/docs/TUTORIAL.md`,
   `SPRING-BOOT-DEV-GUIDE.md`). New TMF terms get the same gentle build-up treatment as the
   original tutorial.
2. **Legacy migration best practice:** a concise, proven playbook for migrating a legacy app to a
   TMF-compliant spec — not just "modify an existing Spring app".
3. **Deployment reality:** the audience deploys to **Google Cloud GKE**; legacy estate includes
   **WebLogic-hosted Java/Maven applications**. The retrofit/migration chapter must speak to that:
   TMF adoption typically rides along with WebLogic → containers/GKE modernization, and the
   adapter/facade pattern is exactly how you put a TMF face on a WebLogic app you can't rewrite yet.

## Architecture

### Module: `vog-tmf`

Sibling of `vog-demo`. Same stack so all prior knowledge transfers: Java 17, Spring Boot 4
(web, data-jpa, validation), H2 in-memory, springdoc OpenAPI UI, Lombok, Maven wrapper,
`.sdkmanrc` pinning Java 17. Server port **8081** (`vog-demo` keeps 8080).

Package layout mirrors `vog-demo` (`entity`, `repository`, `service`, `controller`, `dto`,
`exception`, `config`) plus one new package `adapter` for the retrofit demo. Base path constant:
`/tmf-api/productCatalogManagement/v4`.

### Resources and relationships

- **`category`** — id, href, name, description, lifecycleStatus, validFor, lastUpdate, isRoot,
  parent category (self-reference), `@type: "Category"`.
- **`productSpecification`** — id, href, name, description, brand, version, lifecycleStatus,
  validFor, lastUpdate, `@type: "ProductSpecification"`.
- **`productOffering`** — id, href, name, description, isBundle, isSellable, lifecycleStatus,
  validFor, lastUpdate, one `productSpecification` ref, many `category` refs,
  `@type: "ProductOffering"`.

JPA entities store the data; DTOs carry the TMF envelope (`@JsonProperty("@type")`, `href` built
from the request context). Relationship references use the TMF `*Ref` shape (`id`, `href`, `name`,
`@referredType`).

### TMF630 patterns implemented in code

- TMF **error model** (`code`, `reason`, `message`, `status`) via a global `@RestControllerAdvice`,
  contrasted in the doc with vog-demo's `ApiError`.
- **Partial response** `?fields=` (comma-separated top-level fields; `id`/`href` always included).
- **Query filtering** on simple attributes (e.g. `?lifecycleStatus=Active`, `?name=`).
- **Pagination** `?offset=&limit=` with `X-Total-Count` and `X-Result-Count` headers; 206 when a
  page is returned, 200 when complete.
- **PATCH = JSON Merge Patch** (`application/merge-patch+json`), not PUT.
- Status codes: 201 + `Location` on create, 204 on delete, 404/400/409 mapped to TMF errors.
- **Docs-only** (explicitly flagged out of code scope): hub/listener notifications, importJob/exportJob,
  advanced JSON-Path filtering, polymorphism via `@schemaLocation`.

### Retrofit adapter (track 2 proof)

`adapter/LegacyCategoryAdapterController` + `LegacyCatalogClient`: read-only
(`GET /legacyCategory`, `GET /legacyCategory/{id}`) endpoints that call vog-demo's existing
`/api/categories` with Spring `RestClient` and map `CategoryResponse` → TMF `category`
representation. Configurable base URL (`vog.legacy.base-url`, default `http://localhost:8080`).
If vog-demo is down: TMF error with `status: 503` and a clear `reason`. The doc presents this as
the **facade/adapter strategy** — the same pattern used to put a TMF face on a WebLogic app.

### Seed data

`DataSeeder` loads a small telco-flavoured catalog: 2–3 categories (e.g. Mobile, Internet), 2
product specifications, 3–4 product offerings (e.g. "Mobile 5G Unlimited") so every curl/Swagger
example in the doc returns real data.

### Testing

Mirrors vog-demo patterns: MockMvc controller tests (Boot 4 per-module test-slice packages),
service unit tests, repository test. Adapter tested with a mocked `LegacyCatalogClient`.
`./mvnw test` green is the acceptance gate, plus the doc's curl walkthrough verified manually.

## Tutorial document: `vog-tmf/docs/TMF-TUTORIAL.md`

Same voice and conventions as `TUTORIAL.md` (numbered parts, "what you'll see" after each command,
diagrams as fenced ASCII/mermaid consistent with existing docs, glossary at the end). Every
previously-taught term links back on first use; every new TMF term is introduced before it is used.

- **Part 0 — What is TM Forum and why Open APIs exist.** Telco context, the interoperability
  problem, ODA in one paragraph, what "Open API conformance" means, where the specs live
  (tmforum.org, Open API table on GitHub). No code.
- **Part 1 — The TMF "grammar" (TMF630 essentials).** Resource envelope (`id`, `href`, `@type`,
  `lifecycleStatus`, `validFor`), error model, verb semantics (PATCH-not-PUT), `fields`,
  filtering, pagination. Each contrasted with what the reader already built in vog-demo.
- **Part 2 — TMF620 specifically.** Catalog → category → productOffering → productSpecification,
  relationship diagram, which slice we implement and why hub/import-export are out of scope.
- **Part 3 — Build it from scratch.** Layer-by-layer walk of `vog-tmf` ("you already know
  `@RestController` — link — here's what TMF adds"), including the envelope mapping, merge-patch
  handling, and the filtering/pagination utilities.
- **Part 4 — Run and verify.** `./mvnw spring-boot:run` on 8081, curl walkthrough for every
  pattern (create → 201/Location, fields, filtering, pagination headers, merge patch, TMF error),
  Swagger UI, running vog-demo + vog-tmf together to see the adapter live.
- **Part 5 — Production best practice: contract-first.** Official TMF620 OAS file,
  `openapi-generator-maven-plugin` config shown and explained (not wired into the build), pros/cons
  vs hand-written, TMF Conformance Test Kit (CTK), API versioning (v4 in the path).
- **Part 6 — Migrating existing and legacy apps to TMF (the playbook).** The concise proven
  method, told for the reader's real estate (GKE target, WebLogic/Java/Maven legacy):
  1. Pick the TMF API + resources that match the app's domain (mapping table technique, shown
     concretely for vog-demo → TMF620).
  2. Gap analysis against the spec: data model, verbs, error shape, lifecycle semantics.
  3. Choose a strategy by system freedom:
     - **Facade/adapter** (new TMF service in front; legacy untouched) — right first move for
       WebLogic apps; the adapter runs as a container on GKE while WebLogic stays put.
     - **In-place retrofit** (add TMF controllers inside the app) — when the app is already
       Spring/containers and actively developed.
     - **Strangler migration** (facade first, then move capabilities out until legacy retires) —
       the proven end-state path, aligned with WebLogic → GKE modernization.
  4. Conformance check (CTK), then cut consumers over behind the API gateway.
  Worked example = the coded adapter from Part 3/4. Explicit note on what changes when the
  "legacy" side is WebLogic (SOAP/EJB-era interfaces → adapter also does protocol translation).
- **Part 7 — Glossary and where to next.** TMF terms table; pointers to other TMF APIs (TMF632,
  TMF641), ODA, and the repo's other docs.

### Other doc touches

- `vog-demo/docs/TUTORIAL.md`: short "Where to next → TMF tutorial" section at the end.
- Root `README.md`: add `vog-tmf` to structure + quick start (port 8081).
- `vog-tmf/README.md`: brief module readme linking to the tutorial.

## Error handling

All error paths return the TMF error body. Validation failures → 400 with field detail in
`message`; unknown id → 404; adapter downstream failure → 503; malformed merge patch → 400.

## Out of scope

- No changes to `vog-web` (frontend) — TMF APIs consumed via curl/Swagger/Postman in this step.
- No hub/notifications, import/export jobs, or generated-code module.
- No deployment manifests (GKE is discussed as context in Part 6, not implemented).

## Acceptance criteria

1. `cd vog-tmf && ./mvnw test` passes; app starts on 8081 with seeded data.
2. Every curl command in TMF-TUTORIAL.md Part 4 works as documented against a fresh start.
3. With both apps running, `GET /tmf-api/productCatalogManagement/v4/legacyCategory` returns
   vog-demo's categories in TMF shape; with vog-demo stopped it returns a TMF 503 error body.
4. Every previously-taught term in the new doc links to the existing docs on first use; every new
   TMF term is defined before use.
5. Part 6 playbook is self-contained: a reader with a WebLogic/Maven legacy app knows which
   strategy to pick and what the first three concrete steps are.
