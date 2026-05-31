Apache Directory SCIMple Jersey 4 Example
==========================================

> ⚠️ **Demo only — not production-hardened.** This example exists to show how to
> integrate Apache Directory SCIMple. It is **not** published to Maven Central and is
> **not** suitable for production: it stores data (including any `password`) in memory in
> cleartext, and it provides **no authentication, authorization, or transport security** —
> those are the integrator's responsibility. Example code is out of scope of the project
> threat model; see [`THREAT_MODEL.md`](../../THREAT_MODEL.md) (§3, §9, §10).

This example project demo's how to:

* Add a custom SCIM Extension
* Manage Users and Groups (in memory)

Use this as a starter point on how to integrate Apache Directory SCIMple into your own project.

Run: `mvn package exec:run` and then access one of the endpoints:

```bash
# httpie
http :8080/Users

# curl
curl localhost:8080/Users
```
n
