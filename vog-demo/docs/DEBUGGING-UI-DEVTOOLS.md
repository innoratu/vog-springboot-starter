# Debugging UI ↔ API with Browser DevTools (Inspect)

When the React app (`vog-web`) doesn't behave, the fastest way to find out **why**
is your browser's built-in **DevTools** — the panel you get from the right-click
**"Inspect"** menu. This guide assumes you've never really used it.

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
```
