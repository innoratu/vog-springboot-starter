# Testing the TMF API with Postman

The `vog-tmf` backend has no web UI (the `vog-web` frontend only talks to
`vog-demo` on port 8080). To exercise `vog-tmf` you send HTTP requests directly —
Swagger UI works for quick clicks, and **Postman** is the tool to use when you
want a saved, reusable set of requests (a *collection*).

This doc explains the general process for getting **sample requests into Postman
for any API**, then walks through doing it for this backend specifically.

---

## The key idea: import the OpenAPI spec, don't hand-type requests

Any Spring Boot service using `springdoc` (this one does) publishes a machine-readable
**OpenAPI specification** — a JSON document that describes every endpoint: its URL,
HTTP method, path/query parameters, request body schema, and responses.

Postman can **import that spec and auto-generate a whole collection** — one ready-made
request per endpoint, with example bodies filled in from the schema. This is almost
always the right way to "get sample requests in Postman": you point Postman at one URL
and it builds the requests for you, instead of typing each one by hand.

For `vog-tmf`, the spec lives at:

```
http://localhost:8081/v3/api-docs
```

(The human-readable version of the same thing is Swagger UI at
`http://localhost:8081/swagger-ui.html`.)

---

## Step 1 — Start the backend

Postman calls the *running* service, so it must be up first:

```bash
cd vog-tmf
sdk env                    # Java 17
./mvnw spring-boot:run
```

> The `/legacyCategory` endpoints also need `vog-demo` running on port 8080. The
> other endpoints (`/category`, `/productSpecification`, `/productOffering`) work
> on their own.

Confirm the spec is reachable — open `http://localhost:8081/v3/api-docs` in a browser;
you should see a wall of JSON.

---

## Step 2 — Import the spec into Postman (auto-generate the collection)

1. Open Postman.
2. Click **Import** (top-left).
3. Choose the **Link** tab and paste:
   ```
   http://localhost:8081/v3/api-docs
   ```
   *(Alternatively, save that JSON to a file and use the **File** tab, or drag the
   file into the window — handy when the server isn't running.)*
4. Postman detects it as an **OpenAPI 3** spec — click **Import**.
5. Postman creates a collection named after the API, with folders/requests for every
   endpoint (`GET /category`, `POST /category`, `GET /category/{id}`,
   `POST /productSpecification`, `POST /productOffering`, etc.), each pre-populated
   with an example request body derived from the schema.

That's it — you now have sample requests without typing a single URL.

---

## Step 3 — Point the collection at your local server

Imported OpenAPI collections usually use a **variable** for the server address (often
`{{baseUrl}}`) rather than a hard-coded host. Set it so the requests hit your machine:

1. Open the collection's **Variables** tab (or create an **Environment**).
2. Set the base URL variable to:
   ```
   http://localhost:8081
   ```
3. Save.

The full path for each request then resolves to, e.g.,
`http://localhost:8081/tmf-api/productCatalogManagement/v4/category`.

> If Postman didn't create a variable, just check that a request's URL starts with
> `http://localhost:8081/...` and edit it if not.

---

## Step 4 — Send a request

**Read (no body):**
- Open **`GET /category`** → click **Send**. You get the seeded categories back as
  TMF620 JSON (each with `id`, `href`, `@type`, …).

**Create (with body):** open **`POST /category`**, go to the **Body** tab
(**raw** → **JSON**), and use a payload matching the `CategoryCreate` shape:

```json
{
  "name": "Wearables",
  "description": "Smart watches and trackers",
  "lifecycleStatus": "Active",
  "isRoot": true
}
```

Other create bodies for this backend:

```jsonc
// POST /productSpecification  (ProductSpecificationCreate)
{
  "name": "5G SIM-Only Spec",
  "description": "Template for SIM-only 5G plans",
  "brand": "vog",
  "version": "1.0",
  "lifecycleStatus": "Active"
}
```

```jsonc
// POST /productOffering  (ProductOfferingCreate)
// productSpecification.id and category[].id must reference existing ids
{
  "name": "Mobile 5G Unlimited",
  "description": "Unlimited 5G data",
  "isBundle": false,
  "isSellable": true,
  "lifecycleStatus": "Active",
  "productSpecification": { "id": "1" },
  "category": [ { "id": "1" } ]
}
```

Only `name` is strictly required on each of these (`@NotBlank`); the rest are optional.

---

## Alternative ways to get sample requests

You don't always need the import flow:

- **Copy from Swagger UI** — open `http://localhost:8081/swagger-ui.html`, expand an
  endpoint, click **Try it out** → **Execute**, then copy the generated **cURL** command.
  Paste that cURL into Postman's **Import** (Postman turns a cURL command into a request).
  Good for grabbing one endpoint at a time.
- **Save your own collection** — after tweaking imported requests, hit **Save** to keep
  them, and use **Export** to share the collection file with teammates.

---

## Why this is better than typing requests manually

- **Complete & accurate** — every endpoint, path param, and body field comes straight
  from the server's own contract, so nothing is missed or mistyped.
- **Stays in sync** — when the API changes, re-import the spec to refresh the collection.
- **Shareable** — export once, and anyone can import the same working requests.

---

## Quick reference

| Thing | Value |
|---|---|
| OpenAPI spec (import this) | `http://localhost:8081/v3/api-docs` |
| Swagger UI (browse / copy cURL) | `http://localhost:8081/swagger-ui.html` |
| API base path | `http://localhost:8081/tmf-api/productCatalogManagement/v4` |
| Resources | `/category`, `/productSpecification`, `/productOffering`, `/legacyCategory` |
| Suggested `{{baseUrl}}` | `http://localhost:8081` |
