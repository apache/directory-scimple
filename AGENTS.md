# AGENTS.md

Guidance for AI coding agents (and humans) working in the Apache Directory
SCIMple codebase. Treat this like a `CLAUDE.md`: read it before making changes
and follow the conventions below.

## Project overview

Apache Directory SCIMple is a Jakarta EE implementation of the
[SCIM 2.0](http://www.simplecloud.info/) specification (RFC 7642, RFC 7643,
RFC 7644). It provides:

- A declarative model for defining SCIM `ResourceType`s and `Extension`s using
  annotations (`@ScimResourceType`, `@ScimExtensionType`, `@ScimAttribute`).
- Dynamic generation of the `/Schema`, `/ServiceProvider`, and `/ResourceTypes`
  endpoints.
- A full-featured Java SCIM REST client.
- A `Repository<T>` SPI: implementers customize CRUD + find + patch and the
  server handles marshaling SCIM requests/responses to/from those methods.

This is an Apache Software Foundation (ASF) project. **Issues are tracked using
GitHub Issues** on the project's GitHub repository; contributions come in as
GitHub pull requests.

## Module layout

| Module | Purpose |
|--------|---------|
| `scim-spec/scim-spec-schema` | SCIM resource models (RFC 7643) and filter models (RFC 7644) |
| `scim-spec/scim-spec-protocol` | REST protocol models (RFC 7644) |
| `scim-core` | Repository SPI and core CRUD plumbing |
| `scim-server` | REST endpoint implementations |
| `scim-client` | SCIM REST client |
| `scim-tools` | Utilities for building/verifying SCIM resources against schemas |
| `support/spring-boot` | Spring Boot starter |
| `scim-server-examples/*` | Reference servers (in-memory, Jersey, Quarkus, Spring Boot) |
| `scim-compliance-tests` | Integration tests for verifying a SCIM server |
| `scim-test` | Shared test fixtures/utilities |
| `scim-coverage` | Aggregates JaCoCo coverage across modules (`report-aggregate`) |

## Build & verification

- **Java 17.** The build requires JDK 17+ and compiles to the Java 17 bytecode
  level (enforced via the compiler `release` and the enforcer plugin). Do not
  introduce APIs or language features newer than Java 17.
- **Use the Maven wrapper** (`./mvnw`, or `mvnw.cmd` on Windows) so everyone
  builds with the pinned Maven version.
- Tests run in **parallel** by default (JUnit Jupiter
  `parallel.mode.default = concurrent`). Keep tests independent and free of
  shared mutable static state.

Common examples:

```bash
# Build and run unit tests
./mvnw verify


# Run a single module's tests
./mvnw test -pl scim-core

# Run one test class
./mvnw test -pl scim-server -Dtest=SomeServiceTest
```

### The `ci` profile (coverage, PMD, SpotBugs)

The `ci` profile activates automatically when the `CI` environment variable is
set (as it is in GitHub Actions), and can be enabled locally with `-Pci`. It
adds the static-analysis quality gates that run in CI:

- **SpotBugs** (`spotbugs:check`) at the `verify` phase
- **PMD** (`pmd:check`) at the `verify` phase

The canonical CI build is:

```bash
./mvnw verify -Pci
```

Run this locally before opening a PR to catch SpotBugs/PMD findings that the
default build skips. SpotBugs exclusions live in `src/spotbugs/excludes.xml` —
prefer fixing a finding over excluding it; only add an exclusion with a clear
justification.

JaCoCo coverage is collected during the test run; the aggregated coverage
report is produced by the `scim-coverage` module (`report-aggregate`), so a
full reactor build (`install`/`verify`) is needed to get cross-module numbers.

## Code style

- A `.editorconfig` governs formatting: **2-space indentation**, UTF-8, LF line
  endings, and a trailing newline on every file. Match it.
- **Lombok** is used throughout (`@Data`, `@EqualsAndHashCode(callSuper = true)`,
  `@Accessors`). Prefer Lombok over hand-written boilerplate to stay consistent
  with existing code.
- **JAXB annotations** (`@XmlElement`, `@XmlRootElement`, `@XmlAccessorType`)
  accompany the SCIM model classes — keep them in sync when adding fields.
- Add SCIM resources/extensions declaratively via annotations rather than
  editing server code; the framework discovers them.

### License headers (required)

Every source file must carry the Apache license header. This is enforced by
**Apache RAT** (`apache-rat:check`), which also runs as a `pre-push` git hook.
A missing or malformed header fails the build. Copy the header from an existing
file in the same language when creating new files.

## Testing conventions

- **JUnit 5 (Jupiter)** is the test framework.
- **AssertJ** is the preferred assertion library — prefer `assertThat(...)`
  fluent assertions over raw JUnit `assert*`.
- **Mockito** (with `mockito-junit-jupiter`) for mocking; don't mock what you
  can construct and test directly.
- Aim for meaningful coverage of new code (~80% as a guideline, but prioritize
  behavior and edge cases over the number). Include error-path and boundary
  tests, not just the happy path.
- Integration/compliance tests live in `scim-compliance-tests`; some
  example-server tests use Testcontainers.

## Contribution conventions

- Open a **pull request** against the default branch; do not push directly.
- This is an ASF project: PRs must come from a human contributor.
- Keep commits focused and write clear, descriptive commit messages.
- Before pushing, make sure `./mvnw clean verify -Pci` passes (build, tests,
  RAT headers, SpotBugs, PMD).

## Reference docs in this repo

- `README.md` — feature overview, module table, and declarative-syntax examples
- `MIGRATION_GUIDE.md` — upgrade/migration notes
- `RELEASE_GUIDE.md` — release process
- `SECURITY.md` / `THREAT_MODEL.md` — security policy and threat model
