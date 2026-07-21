# Real-life example: a production Spring Boot service

`vog-demo` is a deliberately small learning starter. To see the *same* Spring Boot
fundamentals scaled up for production, look at a real TELUS service:

**`wls-product-offering-svc`** — <https://github.com/telus/wls-product-offering-svc>

## What it is

The **Wireless Product Catalog Management API** — a **System API** that serves
wireless catalog data (price plans and add-ons, internally "SOCs"). It:

- implements the **TM Forum Open API** standards (TMF620/TMF630), the telecom-industry
  standard for catalog management, under `tmf-api/productOffer`;
- is built on **reactive Spring WebFlux** (Netty) — controller methods return
  `Mono<…>` — with **Spring Boot 2.7 / Java 11**;
- pulls data from an Oracle database ("KNowbility") plus an upstream **SOAP** web
  service reached through a TELUS API gateway.

**Data flow in one sentence:** REST (WebFlux) request → `ProductOfferingController` →
service layer → either the SOAP client (`dao/jaxws`) or Oracle JDBC repositories
(`dao/repo`) → results mapped to OpenAPI-generated models, cached (EhCache + Redis),
returned reactively as JSON — with Resilience4j circuit breakers around the SOAP calls
and a scheduled job refreshing caches.

## The fundamentals from the dev guide are all there

- The same **controller → service → dao/repository** layering and dependency injection.
- A `@SpringBootApplication` main class with a `CommandLineRunner` bean (used there to
  log every bean name at TRACE — the "inspect what Spring wired up" debug trick).
- The `@ComponentScan("com.telus.cis.common.*")` lines are exactly the component-scan
  idea from the dev guide: they adopt beans from **shared `capi-common-*` libraries**
  whose packages sit outside the app's own package, so Spring's default scan wouldn't
  find them.

## What it adds beyond a starter

The concerns a real service needs:

| Concern | Starter (`vog-demo`) | Production service |
|---------|----------------------|--------------------|
| API contract | code-first | **contract-first**: OpenAPI + WSDL generate code at build |
| Config | one `application.properties` | **Spring profiles** per environment |
| Secrets | none (H2) | **GCP Secret Manager** (`sm://…`) |
| Resilience | none | **Resilience4j** circuit breakers |
| Observability | basic logs | **Sleuth** tracing + structured JSON logging |
| Caching | none | **EhCache + Redis** |
| Security | none | **OAuth2 JWT** resource server |
| Cross-cutting code | self-contained | shared `capi-common-*` libs via component scan |

## Two honest caveats when reading it

- Its `README.md` is a leftover cookiecutter template (says "sample Redis
  Application") — the **OpenAPI spec** and the `pom.xml` `<description>` are the real
  source of truth for what the service does.
- The bean-listing `CommandLineRunner` is a **debug leftover** you'd normally remove
  in production.

**Takeaway:** the same Spring Boot skeleton you learn in this starter, plus the
enterprise layers — contract-first APIs, security, resilience, caching, cloud config —
that a live system needs.
