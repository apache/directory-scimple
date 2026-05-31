# Security Policy

Apache Directory SCIMple follows the [Apache Software Foundation security
process](https://www.apache.org/security/). Please read this page before reporting a
vulnerability.

## Reporting a Vulnerability

**Do not report security vulnerabilities through public GitHub issues, pull requests,
or discussions.** Public disclosure before a fix is available puts users at risk.

Report security issues **privately** by email to **security@apache.org** — the ASF
Security Team, who will route the report to the Apache Directory PMC.

Please include, as much as you can:

- the affected module(s) and version (e.g. `scim-server 1.0.0-M1`),
- a description of the issue and its impact,
- steps to reproduce or a proof of concept,
- any known mitigations.

You will receive an acknowledgement of your report. The ASF Security Team and the
project maintainers coordinate the fix and disclosure timeline with you; please give
us reasonable time to address the issue before any public disclosure. See the ASF
guidance for [reporters](https://www.apache.org/security/) for what to expect.

## Security Updates

Security fixes are delivered as **patch releases against the latest major release
line**. Older major versions do not receive backported security fixes. This refers to
patches for the open-source project — there is no commercial support.

The project is currently pre-1.0 (most recent release `1.0.0-M1`; development on
`1.0.0-SNAPSHOT`), so the latest milestone is the line that receives fixes until
`1.0.0` ships.

## Scope

SCIMple is a set of **embeddable libraries**, not a turnkey identity server — the
integrating application owns authentication, authorization, transport security, input
limits, and persistence. What SCIMple does and does not defend against, and how a report
is triaged, is defined in [`THREAT_MODEL.md`](THREAT_MODEL.md) (§3 out-of-scope, §9
disclaimed properties, §13 triage dispositions).

When you are unsure whether something is in scope, **report it privately anyway** — do
not open a public issue. The maintainers will triage it against the threat model.
