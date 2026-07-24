# vog-tmf — TM Forum Open API (TMF620 Product Catalog)

A second Spring Boot backend that seeds its own small telco catalog and exposes
it shaped according to a **TM Forum Open API** instead of a home-grown one. It
also fronts `vog-demo`'s live catalog through a `/legacyCategory` adapter, as an
example of modernizing an existing app. Continues from the first tutorial — you
should be comfortable with Spring Boot concepts before starting this one.

- **Framework:** Spring Boot 4.1.0, Java 17, Spring Data JPA, H2.
- **API Standard:** TM Forum Open APIs — specifically **TMF620 (Product Catalog Management)**.
- **Port:** 8081 (separate from vog-demo's 8080, so both can run side by side).

**Docs:**
- [`docs/TMF-TUTORIAL.md`](docs/TMF-TUTORIAL.md) — step-by-step tutorial: what TM Forum is, the shared REST grammar (TMF630), TMF620's resources, building the API, and the real-world playbook for migrating legacy systems.
- **Design & Implementation:** [`vog-demo/docs/superpowers/specs/2026-07-24-tmf620-tutorial-design.md`](../vog-demo/docs/superpowers/specs/2026-07-24-tmf620-tutorial-design.md) (approved design spec) and [`vog-demo/docs/superpowers/plans/2026-07-24-tmf620-tutorial.md`](../vog-demo/docs/superpowers/plans/2026-07-24-tmf620-tutorial.md) (ordered build plan).

---

## What it does

Seeds and exposes its own **categories**, **product specifications**, and
**product offerings** — a small telco catalog (Mobile/Internet/Business
categories, 5G/Fibre specs and offerings) — following the **TMF620 Product
Catalog Management API** contract:

- RESTful endpoints that match TM Forum's resource shapes, URLs, and behavior.
- A **facade/adapter** (`/legacyCategory`) that reads `vog-demo`'s live REST API (standing in for a legacy system) and reshapes it to TMF620 — the real-world playbook for modernizing existing apps.
- Seeds example data on startup.
- Interactive **Swagger UI** so you can explore and call the API without external tools.

---

## How to run

### Backend (port 8081)

```bash
cd vog-tmf
sdk env                    # activate Java 17 (see ../vog-demo/docs/ENVIRONMENT.md)
./mvnw spring-boot:run
```

Once started:
- API base: `http://localhost:8081/tmf-api/productCatalogManagement/v4`
- **Swagger UI: `http://localhost:8081/swagger-ui.html`**
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`
- Data is stored in an in-memory H2 database (`tmfdb`) that resets on restart; browse data via the API instead of a console.

> **Prerequisite:** `vog-demo` should also be running on port 8080, since `vog-tmf` calls its
> `/api/categories` endpoints to fetch the legacy data. If `vog-demo` is down, `vog-tmf` returns
> a clean TMF error (503 Service Unavailable) rather than a stack trace.

---

## How to test

```bash
cd vog-tmf
./mvnw test
```

Runs automated tests against the repository, service, and controller layers.

---

## How this maps to TMF620

| TMF620 Resource | What it is | Where it's built | Tutorial part |
|---|---|---|---|
| **Category** | Top-level folder for organizing products, e.g. Mobile, Internet, Business. Own seeded entity, plus a `/legacyCategory` adapter that reshapes `vog-demo`'s live `Category` data into the same TMF620 shape. | `controller/`, `service/`, `entity/`, `adapter/LegacyCategoryAdapterController.java` | Part 3 (built); Part 7 (`legacyCategory` migration playbook) |
| **ProductSpecification** | The template for a product: what fields it has, what values are valid, etc., e.g. "5G SIM-Only Spec", "Fibre 1G Spec". | `controller/`, `service/`, `entity/` | Part 3 |
| **ProductOffering** | A product ready to sell — a concrete version of a ProductSpecification with a lifecycle status, e.g. "Mobile 5G Unlimited", "Business Fibre 1G". | `controller/`, `service/`, `entity/` | Part 3 |

The **grammar** — the resource envelope (`id`, `href`, `@type`), error shapes,
HTTP verbs, filtering, and partial responses — is **TMF630**, the Design Guidelines
that every TM Forum Open API shares. Part 1 of the tutorial walks you through it.

---

## Next: reading the tutorial

Start with [`docs/TMF-TUTORIAL.md`](docs/TMF-TUTORIAL.md) if you haven't already.
It's structured in 8 parts, building up from "what is TM Forum" to the playbook for
updating legacy systems. You'll understand what an Open API is, what conformance
means, and how this codebase implements the contract.
