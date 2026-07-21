# Debugging Guide — UI, API, and Observability

> **This is a living document.** It starts with the browser **DevTools** for the
> UI↔API layer and lists complementary tools (API clients, SOAP tools, observability
> like Dynatrace). Add new tools/sections as the app grows and gains real deployments.

When the React app (`vog-web`) doesn't behave, the fastest first step is your
browser's built-in **DevTools** — the panel you get from the right-click
**"Inspect"** menu. This section (1–7) assumes you've never really used it; §8 covers
alternatives and when to reach for each.

The big idea: the UI and the backend talk over HTTP. DevTools lets you **watch every
one of those requests** — the URL, what the UI sent, what the server replied, and the
status code. That tells you immediately whether a bug is in the **frontend**, the
**network/CORS**, or the **backend**.

> Setup for everything below: backend running on `http://localhost:8080`, frontend on
> `http://localhost:5173` (see [`TUTORIAL.md`](TUTORIAL.md) Part 6–7). Open the app at
> http://localhost:5173.

---

## 1. Open DevTools

Any of these (Chrome/Edge):
- Right-click anywhere on the page → **Inspect**, or
- Press **F12**, or
- **Ctrl+Shift+I** (Windows/Linux).

A panel opens with tabs along the top. The two you'll use:
- **Network** — every request/response (your main tool here).
- **Console** — JavaScript errors and messages.

---

## 2. The Network tab — watch the traffic

1. Click the **Network** tab.
2. Tick **Preserve log** (so entries survive page reloads) and **Disable cache**
   (while DevTools is open, so you always hit the server).
3. Click the **Fetch/XHR** filter — this hides images/CSS/fonts and shows only the
   **API calls** (which is what we care about).
4. **Reload the page** (Ctrl+R). You should now see the requests the app makes on
   startup, e.g.:
   - `categories` → `GET http://localhost:8080/api/categories`
   - `organisms` → `GET http://localhost:8080/api/organisms`

Each row shows **Name**, **Status**, **Method**, **Type**, and **Time**.

---

## 3. Inspect a single request (click a row)

Click one request (e.g. `categories`). A detail pane opens with sub-tabs:

| Sub-tab | What it tells you |
|---------|-------------------|
| **Headers** | The full URL, the HTTP **method** (GET/POST/…), the **Status Code**, and request/response headers (including CORS headers like `Access-Control-Allow-Origin`). |
| **Payload** | What the UI **sent** — query params, and for POST/PUT the **request JSON body**. |
| **Response** | The **raw** body the server returned (the JSON text). |
| **Preview** | The same body, formatted/expandable — nicer for reading JSON. |
| **Timing** | How long it took, broken into phases. |

**Reading the status code** (maps directly to this app's API):

| Status | Meaning here |
|--------|--------------|
| **200 OK** | GET/PUT succeeded |
| **201 Created** | POST created a record |
| **204 No Content** | DELETE succeeded (empty body) |
| **400 Bad Request** | Validation failed — check **Response** for `details` (e.g. "commonName is required") |
| **404 Not Found** | ID/category doesn't exist |
| **409 Conflict** | Business rule blocked it (e.g. deleting an in-use category) |
| **(failed)/CORS error** | Request never reached/was blocked — see §5 |

---

## 4. Watch a real interaction — creating an organism

1. With the Network tab open, use the app's **"Add organism"** form and submit.
2. A new request appears: `organisms` → **POST**.
3. Click it:
   - **Payload** shows exactly what the form sent, e.g.
     `{"commonName":"Clownfish","categoryId":2,...}` — great for confirming the UI is
     sending what you expect.
   - **Response** shows the created organism (with its new `id`) and status **201**.
4. Delete a row → you'll see a **DELETE** request returning **204**.

This is how you confirm the UI and API agree: compare what **Payload** sent against
what **Response** came back.

---

## 5. Diagnosing the common failures

DevTools turns vague "it doesn't work" into a precise cause:

**a) "Failed to load data" / red request, status `(failed)` or `CORS error`**
- Click the request → **Console** usually shows a CORS message like
  *"No 'Access-Control-Allow-Origin' header"*.
- Causes: the backend isn't running, or the origin isn't allowed. This app allows
  `http://localhost:5173` (see `CorsConfig`). Confirm the backend is up (§2 reload
  should show a 200 on `categories`).

**b) `net::ERR_CONNECTION_REFUSED`**
- The backend isn't running at all. Start it (`./mvnw spring-boot:run`) and retry.

**c) 400 Bad Request on a form submit**
- Not a crash — validation rejected the input. Open the failing request →
  **Response**; you'll see the exact field errors, e.g.
  `{"status":400,"message":"Validation failed","details":["commonName: commonName is required"]}`.
  Fix the input (or the form), not the server.

**d) 404 / 409**
- **404** — the URL references an id that doesn't exist (check the **Headers** URL).
- **409** — a business rule blocked it (e.g. deleting a category that still has
  organisms). The **Response** message explains it.

---

## 6. Handy tricks

- **Copy as cURL:** right-click a request → *Copy* → *Copy as cURL*. Paste in a
  terminal to replay the exact call (great for sharing a bug or testing without the
  UI).
- **Filter by text:** type in the Network filter box (e.g. `organisms`) to narrow the
  list.
- **Console tab:** shows JavaScript errors from the React app and the error messages
  thrown by the API client (`src/api/client.ts`), which surface the backend's
  message. If the UI shows an error banner, the Console often has the detail.
- **Throttling:** the Network tab's throttling dropdown (e.g. "Slow 3G") lets you see
  how the UI behaves on slow connections.

---

## 7. Which layer is the bug in? (quick decision guide)

- **No request appears** when you click a button → the bug is in the **frontend**
  (the click handler/fetch never fired).
- **Request appears but `(failed)`/CORS** → **network/CORS or backend down**.
- **Request returns 4xx/5xx** → the request reached the **backend**; read the
  **Response** body for the reason (validation, not-found, conflict, server error).
- **Request returns 2xx but the UI looks wrong** → the API is fine; the bug is in how
  the **frontend renders** the response (compare **Response** to what's on screen).

That single question — *did a request appear, and what status did it return?* —
usually points you straight at the right layer.

---

## 8. Other tools (alternatives and complements)

DevTools is best for watching what the **UI** actually does in the browser. These
tools are better for other jobs — testing the API without a UI, hitting SOAP
backends, or debugging **deployed** environments where DevTools can't reach.

### API clients — Postman / Insomnia / Bruno
Desktop apps for crafting and saving HTTP requests. Best when you want to test the
**backend directly**, without going through the React app.
- **Import the API instantly:** point them at this app's OpenAPI spec
  (`http://localhost:8080/v3/api-docs`) to auto-generate a collection of every
  endpoint. (See "Reading the API docs offline" in [`../README.md`](../README.md).)
- **Save requests & environments:** keep a collection of common calls; switch a
  base-URL variable between local / test / prod.
- **Test scripts & automation:** Postman/Newman and Bruno can run request suites in
  CI as lightweight API tests.
- **Bruno** stores collections as plain files in your repo (nice for git); **Insomnia**
  is a lean alternative; **Postman** is the most feature-rich.
- Pick this when: reproducing a bug without the UI, exploring the API, or sharing a
  request with a teammate.

### Command line — curl / HTTPie
Quickest for one-off checks and scripts (you've seen `curl` throughout the tutorial).
**HTTPie** (`http`) is a friendlier, colorized alternative:
```bash
http GET :8080/api/organisms name==whale
http POST :8080/api/organisms commonName=Clownfish categoryId:=2
```
Pick this when: scripting, CI, or a fast sanity check in a terminal.

### SOAP services — SoapUI
`vog-demo` is REST, so you won't need this here. But many enterprise backends (e.g.
the SOAP `ProductOfferingService` behind [`REAL-LIFE-EXAMPLE.md`](REAL-LIFE-EXAMPLE.md))
are **SOAP/WSDL**. **SoapUI** (or ReadyAPI) loads a **WSDL** and generates sample
requests, handles XML namespaces/envelopes, WS-Security, and mocking — things REST
clients don't do well. Postman can do basic SOAP too, but SoapUI is purpose-built.
Pick this when: the service you're calling is SOAP, not REST.

### Built-in to this app — Swagger UI & Actuator
- **Swagger UI** (`/swagger-ui.html`) — try endpoints interactively with zero setup
  (see [`TUTORIAL.md`](TUTORIAL.md) §6.5). Great first stop for the backend.
- **Spring Boot Actuator** — add `spring-boot-starter-actuator` to expose operational
  endpoints like `/actuator/health`, `/actuator/metrics`, `/actuator/httpexchanges`,
  and `/actuator/loggers` (change log levels at runtime). This is the bridge between
  local debugging and production monitoring.

### Server-side logs
Don't forget the backend's own output. `vog-demo` runs with `spring.jpa.show-sql=true`
so you can see the SQL Hibernate issues. In production, logs are typically **structured
JSON** shipped to a log platform. When a request returns 500, the **stack trace in the
logs** is usually the real answer — DevTools only shows you the response, not the cause.

### Deployed environments — Dynatrace (observability / APM)
DevTools, Postman, and curl all assume you can reach the service from your machine.
This app is expected to be **deployed to Google Cloud (GKE)** at some point; once it
runs there you debug with an **observability platform** rather than local tools. At
TELUS that's **Dynatrace**, which gives you what local tools can't:
- **Distributed traces** — follow one request across services (e.g. UI → API → SOAP →
  DB) and see exactly which hop was slow or failed.
- **Service-level metrics** — response times, throughput, error rates (RED metrics),
  and automatic problem/anomaly detection.
- **Logs correlated to traces** — jump from a slow trace straight to its log lines.
- **Real-user / synthetic monitoring** — how the UI performs for actual users.

The [`REAL-LIFE-EXAMPLE.md`](REAL-LIFE-EXAMPLE.md) service is already wired for this
(Sleuth/Micrometer tracing + structured JSON logging feeding the platform). `vog-demo`
isn't instrumented — that would be a natural future addition as it grows toward a
deployable service.

> If you use Claude Code here, there are TELUS **Dynatrace skills** (e.g. `dtctl`,
> `dt-obs-services`, `dt-obs-logs`, `dt-obs-tracing`) that query a Dynatrace tenant
> directly — handy once the app is deployed and instrumented.

---

## 9. Choosing the right tool (cheat sheet)

| Situation | Reach for |
|-----------|-----------|
| The **UI** misbehaves in the browser | Browser **DevTools** → Network/Console (§1–7) |
| Test the **backend** without the UI | **Postman / Insomnia / Bruno**, or **Swagger UI** |
| Quick check or a script | **curl / HTTPie** |
| The backend you call is **SOAP/WSDL** | **SoapUI** |
| Runtime health / metrics of a local app | **Actuator** endpoints |
| Understand a **500** or a crash | **Server-side logs** (stack trace) |
| Debug a **deployed** service (slow/errors) | **Dynatrace** (traces, metrics, logs) |

---

## 10. How this guide will evolve

As the app grows toward its planned **GKE deployment**, extend this doc with the tools
that become relevant, for example:
- **Actuator + Micrometer/Prometheus** once you add metrics (and Kubernetes
  liveness/readiness probes hitting `/actuator/health`).
- **Dynatrace instrumentation** on GKE — OneAgent (cluster-level) or OpenTelemetry —
  once there's a deployment, plus the `kubectl logs` / GCP Cloud Logging basics.
- **Auth debugging** (inspecting JWTs, OAuth2 flows) if security is added.
- **Frontend tooling** (React DevTools, source maps) as the UI grows.

Keep each addition in the same shape: *what the tool is, when to reach for it, and a
concrete example against this app.*
