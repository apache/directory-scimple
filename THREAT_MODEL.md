# Apache Directory SCIMple — Threat Model

## §1 Header

- **Project:** Apache Directory SCIMple — a Jakarta EE implementation of SCIM 2.0 (RFC 7642 / RFC 7643 / RFC 7644).
- **Applies to:** the SCIMple revision this file is committed with; each release's git tag binds its own copy (see *Version binding*).
- **Date:** 2026-05-30.
- **Authors of this model:** the Apache Directory PMC.
- **Status:** **Draft, prepared by the Apache Directory PMC (2026-05-30).** Not yet formally reviewed or ratified;
  submitted for PMC review and endorsement via lazy consensus on `dev@directory.apache.org`.

### Version binding

This threat model is versioned alongside the project. A vulnerability report filed against project version *N* is
triaged against this model **as it stood at *N***, not as it stands at `HEAD`. Because the file lives in the repository,
**the git tag for a release automatically binds that release's copy of the model** — no separate snapshot process is
needed; the `THREAT_MODEL.md` at a release's tag *is* the model for that release. The
publication venue is the repository (canonical); `SECURITY.md` links to it.

### Reporting cross-reference

- Findings that violate a property **claimed in §8** should be reported **privately**
  per [`SECURITY.md`](SECURITY.md) (the ASF security process) — never via a public GitHub issue.
- Findings that fall under **§3 (out of scope)** or **§9 (disclaimed properties)** will be closed citing this document;
  see the disposition table in §13.

### Evidence tags

Claims marked *(documented: …)* cite their source — code, a committed test, the README, or an RFC. All other statements
are assertions by the PMC for this draft.

### What this project is

Apache Directory SCIMple is a set of **embeddable Java libraries** that let an application speak SCIM 2.0 — the standard
REST protocol for provisioning users and groups across identity systems. It is not a turnkey identity server. The
integrator brings their own persistence by implementing a `Repository` interface and their own deployment container
(Spring Boot, Quarkus, Jersey, a servlet container, …); SCIMple supplies the SCIM wire protocol, the schema/extension
model, the filter and PATCH expression parsers, the REST endpoint implementations, and an outbound SCIM client. The
libraries are published to Maven Central under `org.apache.directory.scimple`.

---

## §2 Scope and intended use

**Primary intended use cases:**

- In-process embedding of SCIM 2.0 **server** endpoints into a host application that supplies its own `Repository`
  persistence and its own authentication layer.
- In-process embedding of a SCIM 2.0 **client** to provision against a remote SCIM service.
- Use of the **schema / spec** libraries (resource models, filter parser, PATCH path parser) independently of either.

**Deployment contexts:** an embedded library inside a JVM application — delivered through Spring Boot, Quarkus,
Jersey, or a Jakarta-EE servlet container. SCIMple does not run as a standalone daemon; the host owns the process,
the listening socket, TLS termination, and the authentication boundary.

**Caller roles.** The server side is exposed over HTTP, so "caller" is not a single trust level:

| Role                                                                                         | Trust         | Description                                                                                                                                                                                 |
|----------------------------------------------------------------------------------------------|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **SCIM client** — an external caller of your `scim-server` (*not* the `scim-client` library) | **untrusted** | Sends HTTP requests with attacker-controllable bodies, filter strings, PATCH paths, headers, and query params.                                                                              |
| **Integrator / operator**                                                                    | **trusted**   | Writes the `Repository` implementation, wires the deployment, configures auth/TLS, registers schemas. Authoring this code = already inside the trust boundary.                              |
| **Remote SCIM peer** (the server `scim-client` calls)                                        | **trusted**   | The server an integrator points `scim-client` at — chosen by the integrator, who holds credentials for it and trusts it. A malicious or compromised remote server is out of scope (§3, §7). |

### Component-family table

| Family                                                             | Representative entry point                                                                                                    | Touches outside the process?               | In model?                                                             |
|--------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|-----------------------------------------------------------------------|
| **scim-spec-schema** (parsing)                                     | SCIM JSON models, ANTLR **filter** grammar, **PATCH** path parser, URN/`@Urn` validator                                       | No (pure parsing/validation)               | **In**                                                                |
| **scim-spec-protocol**                                             | REST protocol models (`SearchRequest`, `BulkRequest`, `ErrorResponse`), JAX-RS interfaces                                     | No                                         | **In**                                                                |
| **scim-core**                                                      | `Repository<T>` SPI, `SchemaRegistry`, `RepositoryRegistry`, Jackson `ObjectMapper`/deserializer                              | No                                         | **In** (the SPI **boundary** is in model; integrator impls are out)   |
| **scim-server**                                                    | JAX-RS endpoint impls: `/Users`, `/Groups`, `/Bulk`, `/Me`, `/Schemas`, `/ResourceTypes`, `/ServiceProviderConfig`, `.search` | Inbound HTTP (via the host container)      | **In**                                                                |
| **scim-client**                                                    | `BaseScimClient` and friends — outbound SCIM REST calls over a caller-supplied JAX-RS `Client`                                | Outbound HTTP (via caller-supplied client) | **In** (integrator obligations only; the remote peer is trusted — §7) |
| **support/spring-boot**                                            | `scim-spring-boot-starter` auto-configuration                                                                                 | No (wiring only)                           | **In** (wiring)                                                       |
| **scim-server-examples/**, **reference-projects/scim-server-ldap** | demo apps (in-memory, Jersey, Quarkus, Spring Boot, LDAP)                                                                     | Yes (open sockets, persist data)           | **Out** — see §3                                                      |
| **scim-test, scim-tools, scim-compliance-tests, scim-coverage**    | test/CI tooling                                                                                                               | varies (test only)                         | **Out** — see §3                                                      |

---

## §3 Out of scope (explicit non-goals)

**Use cases the project does not aim to support:**

- A complete, secure identity server out of the box. SCIMple supplies the SCIM protocol layer only; **authentication,
  authorization, transport security, rate limiting, and persistence are the integrator's responsibility.**
- A query/persistence engine. SCIMple parses SCIM filters into structured
  `FilterExpression` trees and hands them to the integrator's `Repository`; it does not translate them to SQL/LDAP/etc.
  (`BaseFilterExpressionMapper` is a *helper* for integrators who choose to, not a SCIMple-owned query builder).

**Threats not defended against at this layer:**

- Network-level attacks (TLS interception, traffic analysis) — the host terminates TLS.
- Authentication/authorization bypass — there is no SCIMple-owned authN/authZ to bypass (§8/§9); this is the
  integrator's layer.
- **Abuse of legitimately-granted SCIM authority.** SCIM is a high-privilege surface — a client authorized to
  create/modify/delete users and groups (and set passwords) already wields broad power over identities. A caller using
  *granted* authority to harmful effect (creating a rogue admin, mass-deleting, resetting a password) is performing an
  authorized operation; SCIMple executing it faithfully is correct behavior, not a vulnerability. Constraining what each
  authenticated client may do — least privilege, per-operation authorization — is the integrator's responsibility
  (§9, §10). This is distinct from **subverting SCIMple's own machinery** (crash, secret leak, injection past the parsed
  boundary), which SCIMple *does* defend regardless of caller privilege (§8).
- Attacks requiring control of the embedding process, the `Repository`, the registered schemas, or the JAX-RS `Client` —
  that is trusted code (§7).
- A malicious or compromised **remote SCIM server** that `scim-client` connects to. The integrator chooses, trusts, and
  authenticates to that server; `scim-client` is a library under the integrator's control, not an adversary-facing
  surface. SCIMple does not harden `scim-client` against hostile responses from the server it calls.

**Code that ships in the repo but is out of this model:**

- `scim-server-examples/**` and `reference-projects/scim-server-ldap` — **example/demo applications, not published to
  Maven Central** *(enforced: the `scim-server-examples` parent pom and the `scim-server-ldap` module each set `maven.deploy.skip` / `skipNexusStagingDeployMojo`)*. They open sockets and store data (the
  `InMemoryUserService`
  examples hold passwords in plaintext) and are **not** production-hardened. Findings here are
  `OUT-OF-MODEL: unsupported-component` (§13). The example READMEs carry a demo-only warning.
- `scim-test`, `scim-tools`, `scim-compliance-tests`, `scim-coverage` — test/CI tooling.

---

## §4 Trust boundaries and data flow

**Where the boundary sits.** The trust boundary is the **HTTP/API surface of
`scim-server`** — untrusted bytes cross into the process there. `scim-client` calls *out* to a server the integrator
chose and trusts (§7), so its outbound side is **not**
an adversarial trust boundary; the integrator owns its transport configuration (§9/§10).

**Data flow (inbound request):**

1. Untrusted bytes arrive at a JAX-RS endpoint (`scim-server`). The body is deserialized by Jackson into strongly-typed
   SCIM resource objects; `filter`/PATCH `path` strings are parsed by the ANTLR grammar into `FilterExpression` /
   `PatchOperationPath` trees. **This is the parse step — the richest attack surface.**
2. The endpoint resolves the target `Repository` from `RepositoryRegistry` and invokes it with **already-parsed,
   structured objects** — never the raw request strings.
3. The integrator's `Repository` (trusted code) performs persistence and returns resources.
4. On the way out, `AttributeUtil` strips `returned = NEVER` attributes (e.g.
   `password`) and applies attribute selection before serialization.

The key transition: **untrusted text → structured object happens entirely inside SCIMple before any integrator code
runs.** This is what makes the `Repository` SPI a clean boundary.

### Reachability preconditions per component

- **scim-spec-schema (filter/PATCH/JSON parsing):** in-model only if reachable from **attacker-controlled input** — a
  `filter` query string, a PATCH `path`, or a request body.
- **scim-core (Repository SPI):** in-model only if the issue is in **SCIMple's marshaling/dispatch**, not in the
  integrator's `Repository`.
- **scim-server (endpoints):** in-model only if reachable by an **unauthenticated or authenticated-but-untrusted HTTP
  client** through the standard endpoints.
- **scim-client:** outside the adversary model — the remote server is trusted (§7) and the `Client` is the integrator's.
  Its obligations are integrator responsibilities (§9/§10), not adversarial-input handling.

---

## §5 Assumptions about the environment

- **Runtime:** a current, supported Java LTS — the exact floor is enforced by the build (17+ at time of writing).
  Memory-safety assumptions rest on running on a supported JVM.
- **Concurrency:** `SchemaRegistry` and `RepositoryRegistry` are populated at startup by trusted integrator code and
  read concurrently thereafter; their backing maps are not synchronized for concurrent *registration* (only
  `registerRepository` is
  `synchronized`). Assumption: registration completes before request handling begins. The shared Jackson `ObjectMapper`
  is a `static final` singleton, thread-safe once configured *(documented: Jackson)*.
- **Memory model:** standard JVM; no native code in the published libraries.

### What the project does *not* do to its host (negative side-effect inventory)

Verified by a source sweep across `scim-spec`, `scim-core`, `scim-server`, `scim-client`, and `support/spring-boot` main
sources *(verified 2026-05-30)*. The published libraries:

- do **not** open listening sockets (the host container does);
- do **not** spawn child processes;
- do **not** read environment variables or system properties for behavior;
- do **not** read or write files;
- do **not** install signal/shutdown handlers;
- **do** emit logging via SLF4J, and **do log resource objects and bulk dependency graphs at DEBUG level** in
  `scim-server` (`BulkResourceImpl`,
  `BaseResourceTypeResourceImpl`). With DEBUG enabled, non-secret SCIM attribute data can appear in logs — an operator
  responsibility (§10). **The `password` attribute is not exposed this way** (§8.1).
- hold a process-wide `static final` Jackson `ObjectMapper`; otherwise do not mutate global/process state.

---

## §5a Build-time and configuration variants

**Runtime configuration knobs (`ServerConfiguration`)** *(documented: source)*. These values are surfaced in the
`/ServiceProviderConfig` response, and the resource-limit knobs are **enforced in the request path** (§8.8):

| Knob                                                                                                             | Default | Intended contract                  | Enforced today?    |
|------------------------------------------------------------------------------------------------------------------|---------|------------------------------------|--------------------|
| `bulkMaxOperations`                                                                                              | 100     | **enforced** bound on bulk fan-out | **Yes** (§8.8) |
| `bulkMaxPayloadSize`                                                                                             | 1024    | **enforced** bound                 | **Yes** (§8.8) |
| `filterMaxResults`                                                                                               | 100     | **enforced** result bound          | **Yes** (§8.8) |
| `supportsBulk` / `supportsFilter` / `supportsPatch` / `supportsSort` / `supportsETag` / `supportsChangePassword` | varies  | capability advertisement           | advisory only      |
| `authenticationSchemas`                                                                                          | empty   | capability advertisement           | advisory only      |

The capability `supports*` flags remain advertisements — they describe what the deployment offers, not access controls.

**Build-time security tooling** *(documented: root `pom.xml`, `.github/`)* — changes what the *build* guarantees,
relevant to §11a/§12: OWASP Dependency-Check (build fails on any CVE), SpotBugs + find-sec-bugs (max effort), PMD,
CodeQL, Dependabot, and a CycloneDX SBOM on release. (Exact tool versions live in the build, not here.) No build flag
changes a runtime security property in the published libraries.

---

## §6 Assumptions about inputs

### Server endpoints (`scim-server`)

| Endpoint / message                  | Parameter                                       | Attacker-controllable? | Caller (integrator) must enforce                                     |
|-------------------------------------|-------------------------------------------------|------------------------|----------------------------------------------------------------------|
| `GET /Users`, `/Groups`             | `filter` (string)                               | **yes**                | authN before exposure                                                |
| `GET /Users`, `/Groups`             | `startIndex`, `count`                           | **yes**                | — (result size capped at `filterMaxResults`, §8.8)                   |
| `GET …`                             | `attributes`, `excludedAttributes`              | **yes**                | — (mutually-exclusive check exists)                                  |
| `POST/PUT /Users`, `/Groups`, `/Me` | JSON body                                       | **yes**                | body-size limits (container), schema trust                           |
| `PATCH /Users/{id}`, `/Me`          | `PatchRequest` body incl. `path`                | **yes**                | — (parsed; nesting depth capped, §8.7)                              |
| `POST /Bulk`                        | `BulkRequest` (operation array, `failOnErrors`) | **yes**                | — (operation count & payload size enforced, §8.8)                   |
| any mutating                        | `If-Match` header                               | **yes**                | — (parsed by `EtagParser`)                                           |
| `GET /Me`, `PUT/PATCH/DELETE /Me`   | caller principal                                | via container          | **authentication** (endpoint requires a `SecurityContext` principal) |
| `GET /Schemas`, `/ResourceTypes`    | `filter`                                        | **yes**                | — (SCIMple returns 403 if present, per RFC)                          |

### Client (`scim-client`)

| Message          | Parameter                                             | Attacker-controllable?                             | Caller must enforce                             |
|------------------|-------------------------------------------------------|----------------------------------------------------|-------------------------------------------------|
| outbound call    | `baseUrl`                                             | **no** — caller-supplied                           | SSRF-safe URL                                   |
| outbound call    | the JAX-RS `Client` (TLS, creds, timeouts, redirects) | **no** — caller-supplied                           | TLS verification, timeouts, credential handling |
| inbound response | remote SCIM peer's JSON body                          | **no** — the integrator trusts the server it calls | (out of model — §3, §7)                         |

**Size / shape / rate:** SCIMple relies on **Jackson's built-in
`StreamReadConstraints`** (max nesting depth, string/number length bounds) for JSON, and neither tightens nor loosens
them — these defaults satisfy the §8.7 "catastrophic" line for JSON depth/size. The ANTLR filter/PATCH grammar is
recursive; SCIMple caps nesting depth at `FilterParsers.MAX_NESTING_DEPTH` = 40 (§8.7). There is no input-length cap —
proportional cost is the integrator's/container's job (§9).

---

## §7 Adversary model

**Assumed attacker:**

- **Untrusted SCIM client** — can reach the server endpoints the integrator exposes and send arbitrary bodies, filter
  strings, PATCH paths, bulk operations, headers, and query params. May be unauthenticated (SCIMple enforces no authN
  itself) unless the integrator's layer blocks them first.

**Capabilities they have:** craft malformed/oversized/deeply-nested SCIM payloads; craft pathological filter/PATCH
expressions; submit large bulk batches. **Capabilities they do not have:** read/modify the embedding process's memory;
alter the
`Repository`, the registered schemas, or the `Client` configuration; observe local logs.

**Attacker goals in scope:** crash/hang the host via a **small** crafted input or super-linear cost (§8.7); read data
they shouldn't (e.g. `password` leakage); escape the parsed-object boundary or abuse deserialization; trigger
malformed-input failures. These goals are about **subverting SCIMple's own machinery**, and they hold *regardless of how
privileged the authenticated caller is*. They are **not** the consequences of operations the caller was authorized to
perform: SCIM is a high-privilege surface, and the blast radius of *granted* authority is governed by the integrator's
authorization (§3, §10), not by SCIMple.

**Actors explicitly NOT in the model:**

- Anyone with control of the embedding process or the integrator's `Repository`/
  `Client`/schema registration — trusted code; they have already won.
- The **remote SCIM server** that `scim-client` connects to — the integrator selects it, holds credentials for it, and
  trusts it; a malicious/compromised remote server is out of scope (§3).
- Local attackers reading DEBUG logs — a host operational concern (§9/§10).

This project is **not** a distributed/consensus system, so Byzantine-peer and honest-fraction concepts do not apply.

---

## §8 Security properties the project provides

> Each entry: **property + conditions / violation symptom / severity / evidence.**
> Severity is **SECURITY-CRITICAL** (warrants coordinated disclosure per `SECURITY.md`)
> or **CORRECTNESS-ONLY** (ordinary bug).

1. **All `Returned.NEVER` attributes (the only core one being `password`) are stripped from responses, and `password` is
   excluded from resource `toString()`.**
  - *Conditions:* response flows through `AttributeUtil.setAttributesForDisplay`;
    `password` is `returned = NEVER`.
  - *Violation symptom:* a `password` (or other NEVER attribute) appears in a SCIM response body or in logs via
    `toString()`. *Severity:* **SECURITY-CRITICAL**
    (sensitive-data disclosure). *Evidence:* *(documented)* — `ScimUser.password` is
    `returned = NEVER`; `AttributeUtil` strips NEVER attributes; `AttributeUtilTest`
    asserts `getPassword()` is null after display processing; `ScimUser.toString()`
    omits `password`. Regression-tested: `AttributeUtilTest` guards NEVER attributes out of responses (even when
    explicitly requested), and `ScimUserTest` guards every `Returned.NEVER` attribute out of `ScimUser.toString()`
    (schema-driven, so future NEVER fields are covered). Merged in `develop`.

2. **Untrusted filter / PATCH strings are parsed into structured objects before reaching the integrator's
   `Repository`.** *Severity:* **CORRECTNESS-ONLY** for SCIMple (any backend injection happens in integrator code; §9).
   *Evidence:* *(documented)* —
   `Repository.find(Filter, …)` / `patch(id, List<PatchOperation>, …)` take parsed types.

3. **No unsafe Jackson polymorphic deserialization.** *Violation symptom:* gadget-chain deserialization (RCE).
   *Severity:* **SECURITY-CRITICAL** if violated. *Evidence:*
   *(documented)* — no `enableDefaultTyping`/`activateDefaultTyping`; no
   `@JsonTypeInfo`/`@JsonSubTypes` on SCIM models (verified absent).

4. **Schema/extension registration loads no classes from untrusted input.** *Severity:*
   **SECURITY-CRITICAL** if violated. *Evidence:* *(documented)* — registration is an explicit integrator API; no
   `Class.forName` from request data.

5. **No untrusted XML is parsed (no XXE surface in the published libraries).**
   *Severity:* **SECURITY-CRITICAL** if violated. *Evidence:* *(documented)* — no
   `XMLInputFactory`/`SAXParser`/`DocumentBuilderFactory`/`Unmarshaller` in `scim-spec`/`scim-core` (verified); JAXB API is `provided`,
   annotation-only. See §11a.

6. **Filter / PATCH round-trip fidelity.** *Severity:* **CORRECTNESS-ONLY.**
   *Evidence:* *(documented)* — `FilterBuilderTest`, `PatchOperationPathTest`. *Caveat:* JSON string values are
   extracted by substring without unescaping (`ExpressionBuildingListener.parseJsonType`), so escaped sequences may not
   round-trip faithfully (correctness-only).

7. **Catastrophic-input resistance (resource bound).** SCIMple commits to a categorical line: a **small input that
   causes a crash** (e.g. `StackOverflowError` from a deeply nested filter/PATCH) or **super-linear / exponential cost
   in input size** is a **bug SCIMple will fix**; cost merely **proportional** to a large input is not (the integrator's
   job — §9).
  - *Violation symptom:* small/cheap request → `StackOverflowError`, hang, or exponential CPU/allocation. *Severity:*
    **SECURITY-CRITICAL** (DoS).
  - *Provided:* `FilterParsers.MAX_NESTING_DEPTH` (= 40) caps nesting by counting entries into the recursive
    `filterExpression` / `attributeExpression` grammar rules during parsing (a `DepthCountingListener` aborts before the
    recursive-descent parser can overflow the stack), for **both** the filter parser and the PATCH-path parser.
    Over-limit input is rejected as `FilterParseException` with a fixed message that does not echo the input. JSON
    depth/size is bounded by Jackson defaults (§6). *Evidence:* *(documented)* — `FilterParsers`
    (`MAX_NESTING_DEPTH = 40`, `DepthCountingListener`), merged in `develop`. There is **no** input-length cap;
    proportional cost is disclaimed in §9.

8. **Enforcement of advertised request limits.** SCIMple enforces the configured
   `bulkMaxOperations`, `bulkMaxPayloadSize`, and `filterMaxResults` in the request path.
  - *Violation symptom:* a request exceeding a configured limit is processed anyway. *Severity:* **SECURITY-CRITICAL**
    (DoS/fan-out) for the bulk bounds; **CORRECTNESS-ONLY** for `filterMaxResults`.
  - *Provided:* a `/Bulk` request exceeding `bulkMaxOperations` is rejected with HTTP 413 (`scimType` `tooMany`, per
    RFC 7644 §3.7.4) before any operation runs (`BulkResourceImpl`); `bulkMaxPayloadSize` is enforced by
    `BulkPayloadSizeFilter`; query results are capped at `filterMaxResults` (`BaseResourceTypeResourceImpl`).
    *Evidence:* *(documented)* — merged in `develop`.

---

## §9 Security properties the project does *not* provide

The most valuable section for an integrator. These are disclaimed **by design** unless a code citation notes otherwise:

- **No authentication.** SCIMple performs no authN of SCIM clients. Endpoints other than
  `/Me` are reachable by anyone who can reach the route; `/Me` merely requires that a
  `SecurityContext` principal already exists (the container/integrator must supply it). **Securing the endpoints is the
  integrator's job.** *(documented: no
  `ContainerRequestFilter`/`@RolesAllowed`; `SecurityContext` used only in
  `SelfResourceImpl`.)*
- **No authorization.** No role/permission checks anywhere.
- **No transport security.** `scim-client` does not configure TLS; it uses the caller-supplied JAX-RS `Client`.
  Certificate/hostname verification, timeouts, redirect policy, and credentials are entirely the caller's
  responsibility. *(documented: no
  `SSLContext`/`TrustManager`/`HostnameVerifier`/timeout code.)*
- **No resistance to proportional resource cost.** SCIMple bounds *catastrophic* input (§8.7) but makes **no guarantee
  about resources consumed in proportion to a large but well-formed input** (a huge body, a long flat filter, a large
  bulk batch within the intended limits). Bounding total input size/rate is the integrator's / container's job.
- **No defense against a malicious `Repository`, schema set, or `Client`.** Trusted code (§7).
- **No password hashing or at-rest protection.** SCIMple passes the cleartext `password`
  to `Repository.create/update`; hashing, storage, and never-logging are the integrator's responsibility. *(
  documented.)*
- **No protection against operator-enabled verbose logging.** With DEBUG enabled,
  `scim-server` logs non-secret resource contents (§5/§10). `password` is excluded (§8.1), but other attributes are not.

### False-friend properties (look like security, are not)

- **`returned = NEVER` password redaction.** It removes `password` from **responses**
  and from `toString()` (a real property, §8.1). It does **not** mean SCIMple protects the password elsewhere: the value
  is held in memory during request processing and passed in cleartext to the `Repository`. It is response/secret-in-log
  redaction, not secret management.
- **`/ServiceProviderConfig` `supports*` capability flags.** `supportsBulk=false`,
  `supportsFilter=false`, etc. *look like* enforcement switches; they are **advertisements only** — the endpoints behave
  identically regardless. (The numeric *limit* knobs are different — `bulkMaxOperations` / `bulkMaxPayloadSize` /
  `filterMaxResults` **are** enforced; see §5a/§8.8.)

### Well-known attack classes left to the caller

- **Oversized / high-rate input** (bodies, long filters, large bulk batches within limits) → proportional CPU/heap:
  bound size and rate upstream.
- **Filter → backend injection** (SQL/LDAP): SCIMple hands you a parsed
  `FilterExpression`; parameterize any backend query you build from it.
- **SSRF via `scim-client` `baseUrl`:** validate the target before pointing the client at it.
- **Mass-assignment / over-posting** on SCIM resources: validate which attributes a given caller may set.
- **ReDoS** on the `@Urn` validator regex against malformed URNs *(low-likelihood; see §11a; note §8.7 would classify a
  genuinely super-linear case as a bug)*.

---

## §10 Downstream responsibilities

For SCIMple, "downstream" = the **integrator/operator**. To make §5–§7 hold, they MUST:

1. **Authenticate and authorize** every SCIM request before it reaches SCIMple endpoints. SCIM is a high-privilege
   surface (it manages users, groups, and passwords), so apply **least privilege**: scope what each SCIM client may do
   rather than granting blanket create/modify/delete. A compromised or over-privileged client is "game over" at the
   identity layer no matter how robust SCIMple's parsing is — this authorization boundary is the primary control.
2. **Terminate and verify TLS** for inbound serving and the outbound `scim-client`
   `Client` (certificate + hostname verification, sane timeouts).
3. **Bound untrusted input** at the edge: request body size, bulk batch size, and request rate. (SCIMple bounds
   *catastrophic* parser cases per §8.7 but not proportional cost.)
4. **Implement the `Repository` securely:** hash passwords, never log secrets, parameterize any backend query built from
   a `FilterExpression`, and enforce per-caller authorization on every operation.
5. **Do not enable DEBUG logging in production**, or scrub it — `scim-server` logs non-secret resource contents at DEBUG
   (§5). (`password` is excluded.)
6. **If you point `scim-client` at a server you do not control or fully trust,** treat its responses as untrusted
   input — the default model assumes a trusted, integrator-chosen peer (§7).

---

## §11 Known misuse patterns

- **Exposing SCIMple endpoints directly to the internet with no auth in front.** The endpoints ship without authN/authZ;
  unguarded exposure yields unauthenticated CRUD over users/groups. *Instead:* gate every route behind the host's auth
  layer.
- **Assuming `/ServiceProviderConfig` `supports*` flags enforce behavior.** *Instead:*
  enforce capability decisions in the integrator/container; the flags only advertise.
- **Relying on SCIMple to bound all input cost.** Forwarding unbounded bodies/batches. *Instead:* cap size and rate at
  the edge (SCIMple covers catastrophic parser cases only, §8.7).
- **Building backend queries from `FilterExpression` via string concatenation.**
  Injection. *Instead:* parameterize / use `BaseFilterExpressionMapper` patterns.
- **Treating response `password` redaction as secret management.** *Instead:* hash and protect the secret in the
  `Repository`.
- **Logging at DEBUG in production.** Leaks non-secret resource contents. *Instead:* keep production at INFO+ or scrub.

---

## §11a Known non-findings (recurring false positives)

Feed this back to tooling as a suppression list.

**Reasoned non-findings (not in a suppression file, discharged by a §8 property):**

- **`jackson-databind` "unsafe deserialization."** Safe: no default typing and no
  `@JsonTypeInfo`/`@JsonSubTypes` in the published sources (verified); SCIM payloads bind to a fixed model (§8.3).
- **"XXE in JAXB."** Safe: no untrusted XML is parsed — no `XMLInputFactory`/`DocumentBuilderFactory`/`SAXParser`/
  `Unmarshaller` in `scim-spec`/`scim-core` (verified); JAXB API is annotation-only, `provided` scope (§8.5).

**Suppressed in `src/spotbugs/excludes.xml`:**

- **`EI_EXPOSE_REP` / `EI_EXPOSE_REP2`** (object exposes its internal representation) — suppressed for the
  `spec.filter.*` and `spec.phonenumber.*` model packages. Mutable-field exposure on value/model objects, not an
  attacker-input issue.
- **`REDOS` on `UrnValidator`** — the URN-validation regex is suppressed as a false positive (the maintainers judged it
  non-backtracking). Note: a URN can arrive in request data, so per §8.7 a *genuinely* super-linear case would be a
  `VALID` bug — re-evaluate if that regex changes.
- **`UNENCRYPTED_SERVER_SOCKET`** — only in `EmbeddedServerExtension` (the `scim-compliance-tests` integration-test
  harness); out of scope per §3.
- **`RCN_REDUNDANT_NULLCHECK_…`** (suppressed globally) and the `PhoneNumber` builder findings
  (`UWF_FIELD_NOT_INITIALIZED…`, `NP_NULL_ON_SOME_PATH…`), `ObjectMapperFactory` `MS_EXPOSE_REP`, and `BaseScimClient`
  `CT_CONSTRUCTOR_THROW` — code-quality false positives (largely Lombok-generated code), not security-relevant.

**Suppressed in `src/owasp/suppression.xml` (dependency-scan noise):**

- **Wrong CPE/GAV matches:** SCIMple JARs mis-identified as Apache HTTP Server; `junit-platform-engine` as the
  "fan_platform" Python project; `commons-codec` / `commons-logging` / `jcl-over-slf4j` mis-linked to `commons_net`.
- **Weld `probe.js` (old jQuery/Bootstrap) CVEs.** `weld-se-core` bundles `probe.js`; it is **test-scoped** in the
  published libraries (`scim-client`, `scim-server`) and a runtime dependency only of the Jersey demo examples. The JS
  asset is never in a published artifact's runtime surface and SCIMple never serves it.
- **SnakeYAML `CVE-2022-1471`** — transitive dependency; the unsafe `Constructor` path that triggers it is not used.

---

## §12 Conditions that would change this model

Revise this document when any of the following occurs:

- A new public endpoint, accepted input format, or protocol message is added.
- SCIMple begins parsing untrusted XML, or enables Jackson default typing (would void §8.3/§8.5), or overrides Jackson's
  `StreamReadConstraints`.
- A core attribute beyond `password` is marked `Returned.NEVER`, or `ScimUser.toString()`
  is regenerated (e.g. switched to Lombok `@Data`) — re-verify §8.1.
- `scim-client` gains its own transport/credential handling, or SCIMple adds authN/authZ (would move items from §9 to
  §8).
- A shipped-but-unsupported component (an example, the LDAP reference project) is promoted into a published artifact.
- **Evidence the model is incomplete:** a vulnerability report that cannot be cleanly routed to a §13 disposition is
  itself a trigger (`MODEL-GAP`) — add the missing property to §8 or §9 rather than making an ad-hoc call.

Ownership/cadence: the **Apache Directory PMC owns** this document; it is re-reviewed at each release and whenever a
trigger above fires. Each release's git tag binds that release's copy (§1).

---

## §13 Triage dispositions

| Disposition                            | Meaning                                                                                                                                                                            | Licensed by  |
|----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|
| `VALID`                                | Violates a property the project claims, via an in-scope adversary and input.                                                                                                       | §8, §6, §7   |
| `VALID-HARDENING`                      | No §8 property is violated, but the API makes a §11 misuse easy enough that the project elects to harden it. Reported privately; fixed at maintainer discretion; typically no CVE. | §11          |
| `OUT-OF-MODEL: trusted-input`          | Requires attacker control of a parameter the model marks trusted (e.g. `baseUrl`, the `Repository`, registered schemas).                                                           | §6           |
| `OUT-OF-MODEL: adversary-not-in-scope` | Requires a capability the model excludes (e.g. control of the embedding process).                                                                                                  | §7           |
| `OUT-OF-MODEL: unsupported-component`  | Lands in `scim-server-examples/**`, `reference-projects/**`, or test/CI tooling.                                                                                                   | §3           |
| `OUT-OF-MODEL: non-default-build`      | Only manifests under a discouraged or non-default §5a configuration.                                                                                                               | §5a          |
| `BY-DESIGN: property-disclaimed`       | Concerns a property the project explicitly does not provide (authN/authZ, transport, proportional resource cost, password hashing).                                                | §9           |
| `KNOWN-NON-FINDING`                    | Matches a documented recurring false positive.                                                                                                                                     | §11a         |
| `MODEL-GAP`                            | Cannot be cleanly routed to any of the above → revise the model.                                                                                                                   | triggers §12 |

---

## §14 Machine-readable companion

A sidecar [`threat-model.yaml`](threat-model.yaml) accompanies this prose for automated/AI triage, carrying only the
triage-relevant facts: entry points → per-parameter trust (§6), component families → in/out of scope (§2/§3), config
knobs → security-relevant?/default?/enforced? (§5a), claimed properties → severity + violation symptom (§8), disclaimed
properties + false friends (§9), known non-findings (§11a), and the disposition labels (§13).

The prose remains canonical; the YAML is a derived index. It pins no version — the release git tag binds both files
(§1). Regenerate it whenever this prose changes.
