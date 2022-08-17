Apache Directory SCIMple In Memory Example
==========================================

This example project demo's how to:

* Add a custom SCIM Extension
* Manage Users and Groups (in memory)

Use this as a starter point on how to integrate Apache Directory SCIMple into your own project.

Run: `mvn spring-boot:run` and then access one of the endpoints:

```bash
# httpie
http :8080/v2/Users

# curl
curl localhost:8080/v2/Users
```
n
