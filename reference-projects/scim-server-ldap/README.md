<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
-->

# SCIMple LDAP Server

A SCIM 2.0 server backed by an LDAP directory, built on [Apache Directory SCIMple](https://directory.apache.org/scimple/) and the [Apache Directory LDAP API](https://directory.apache.org/api/).

This module implements `Repository<ScimUser>` and `Repository<ScimGroup>` against LDAP, translating SCIM operations into LDAP reads and writes. It is a reference implementation intended to be adapted for production deployments with real LDAP directories.

## Building

Build the full SCIMple project (the LDAP module depends on other SCIMple modules):

```shell
./mvnw package -DskipTests
```

This produces an executable uber JAR at `reference-projects/scim-server-ldap/target/scim-server-ldap-1.0.0-SNAPSHOT-exec.jar`.

> **Note:** The uber JAR is built with the [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/maven-plugin/) for its
> JAR repackaging capability only — the project has **no runtime dependency on Spring**. The JAR uses Spring Boot's `JarLauncher`
> as the entry point, which delegates to `LdapApplication.main()`. If you inspect the manifest, that is why you will see Spring
> Boot references.

## Quick Start

Run the server with an embedded [Apache DS](https://directory.apache.org/apacheds/) instance (no external LDAP required):

```shell
java -Dldap.embedded=true -jar reference-projects/scim-server-ldap/target/scim-server-ldap-1.0.0-SNAPSHOT-exec.jar
```

Or, during development, run directly via Maven without building the uber JAR first:

```shell
./mvnw exec:java -pl reference-projects/scim-server-ldap -Dldap.embedded=true
```

The embedded mode seeds the directory with sample users and a group. Test it:

```shell
curl http://localhost:8080/Users
curl http://localhost:8080/Groups
curl http://localhost:8080/ServiceProviderConfig
```

SCIM endpoints expect `Content-Type: application/scim+json` for write operations (POST, PUT, PATCH).

The server port defaults to 8080 and can be changed with `-Dserver.port=9090`.

To connect to an external LDAP server instead, configure `src/main/resources/scim-ldap.yml` and run without the embedded flag.

## Configuration

All configuration lives in a single file: **`scim-ldap.yml`** on the classpath.

```yaml
ldap:
  embedded: false  # set to true to start an in-memory ApacheDS (ignores host/port/bind settings)
  host: localhost
  port: 389
  bindDn: cn=admin,dc=example,dc=com
  bindPassword: secret
  useTls: false
  userBaseDn: ou=users,dc=example,dc=com
  groupBaseDn: ou=groups,dc=example,dc=com

user:
  objectClasses: [inetOrgPerson, organizationalPerson, person, top]
  rdnAttribute: uid
  attributes:
    userName: uid
    "name.givenName": givenName
    "name.familyName": sn
    displayName: displayName
    "emails.value": mail
    # ... full mapping in scim-ldap.yml

group:
  objectClasses: [groupOfNames, top]
  rdnAttribute: cn
  attributes:
    displayName: cn
    "members.value": member
```

### System Property Overrides

LDAP connection settings can be overridden at runtime via system properties, which is useful for containerized deployments where secrets come from the environment:

```shell
java -Dldap.host=ldap.corp.example.com \
     -Dldap.port=636 \
     -Dldap.bind.dn=cn=scim,ou=services,dc=corp \
     -Dldap.bind.password=$LDAP_PASSWORD \
     -Dldap.use.tls=true \
     -jar reference-projects/scim-server-ldap/target/scim-server-ldap-1.0.0-SNAPSHOT-exec.jar
```

| System Property       | YAML Key            | Default                       |
|-----------------------|---------------------|-------------------------------|
| `ldap.embedded`       | `ldap.embedded`     | `false`                       |
| `ldap.host`           | `ldap.host`         | `localhost`                   |
| `ldap.port`           | `ldap.port`         | `389`                         |
| `ldap.bind.dn`        | `ldap.bindDn`       | `cn=admin,dc=example,dc=com`  |
| `ldap.bind.password`  | `ldap.bindPassword` | `secret`                      |
| `ldap.use.tls`        | `ldap.useTls`       | `false`                       |
| `ldap.base.dn.users`  | `ldap.userBaseDn`   | `ou=users,dc=example,dc=com`  |
| `ldap.base.dn.groups` | `ldap.groupBaseDn`  | `ou=groups,dc=example,dc=com` |
| `server.port`         | -                   | `8080`                        |

## Architecture

```
SCIM Request
    |
    v
+-------------------+     +------------------+     +---------------------+
| LdapUserRepository|---->| AttributeMapper  |---->| LdapDao             |
| LdapGroupRepository     | (SCIM <-> LDAP)  |     | (CRUD operations)   |
+-------------------+     +------------------+     +---------------------+
         |                        |                          |
         |                +------------------+     +---------------------+
         +--------------->| FilterTranslator |     | LdapConnectionManager
                          | (SCIM -> LDAP)   |     | (connection pool)   |
                          +------------------+     +---------------------+
```

### Repository Layer (`service/`)

`LdapUserRepository` and `LdapGroupRepository` implement SCIMple's `Repository<T>` SPI. They handle:

- **CRUD operations** — create, get, update, patch, delete
- **Search with filtering** — delegates to `FilterTranslator` then executes against LDAP
- **Identity via entryUUID** — SCIM resource IDs are the LDAP server's `entryUUID` operational attribute (immutable, server-assigned)
- **Group membership resolution** — bidirectional mapping between member DNs and SCIM entryUUIDs

### Filter Translation (`mapping/FilterTranslator`)

SCIM filter expressions are translated into LDAP filter strings. All SCIM comparison operators are supported:

| SCIM | LDAP | Example |
|---|---|---|
| `eq` | `(attr=value)` | `userName eq "jdoe"` -> `(uid=jdoe)` |
| `co` | `(attr=*value*)` | `displayName co "test"` -> `(displayName=*test*)` |
| `sw` | `(attr=value*)` | `userName sw "j"` -> `(uid=j*)` |
| `ew` | `(attr=*value)` | `emails.value ew "@example.com"` -> `(mail=*@example.com)` |
| `pr` | `(attr=*)` | `title pr` -> `(title=*)` |
| `gt`, `ge`, `lt`, `le` | Ordering filters | Mapped to LDAP `>=`/`<=` combinations |
| `and`, `or`, `not` | `(&...)`, `(\|...)`, `(!...)` | Logical composition |

Attribute names are resolved through the configurable SCIM-to-LDAP mapping (e.g., `userName` -> `uid`). Filter values are escaped per RFC 4515.

### Attribute Mapping (`mapping/AttributeMapper`)

Converts between SCIM resources and LDAP entries using the `user` and `group` sections of `scim-ldap.yml`. The mapping is a simple key-value structure:

```yaml
# SCIM attribute path -> LDAP attribute name
userName: uid
"name.givenName": givenName
"emails.value": mail
```

To adapt for a different LDAP schema (e.g., Active Directory), change the attribute mapping and objectClasses in `scim-ldap.yml` — no code changes required.

## Testing

The module uses the SCIMple compliance test suite (`scim-compliance-tests`) with an embedded [Apache DS](https://directory.apache.org/apacheds/) server. Run the tests with:

```shell
./mvnw verify -pl reference-projects/scim-server-ldap
```

The test configuration in `src/test/resources/scim-ldap.yml` adds `extensibleObject` to the user objectClasses so that custom attributes (`scimActive`, `scimPhoneTypes`) are accepted by the Apache DS schema.
