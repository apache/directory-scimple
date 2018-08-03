[<img src="https://directory.apache.org/fortress/gen-docs/1.0.1/apidocs/org/apache/directory/fortress/core/doc-files/apacheds-logo.jpeg" align="right" />](https://directory.apache.org/scimple/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Apache Directory SCIMple
========================

Apache's Java EE implmentation of the [Simple Cross-domain Identity
Management](http://www.simplecloud.info/) (SCIM) version 2.0 specfification
as defined by the following RFCs:

* [RFC7643 - SCIM: Core Schema](https://tools.ietf.org/html/rfc7643)

* [RFC7644 - SCIM: Protocol](https://tools.ietf.org/html/rfc7644)

* [RFC7642 - SCIM: Definitions, Overview, Concepts, and Requirements](https://tools.ietf.org/html/rfc7642)

## Project

Please fork this project and contribute Pull-Requests via this project's
Github page.  Issue tracking is being transitioned to the [Apache Foundation's
Jira server](http://issues.apache.org/jira/browse/SCIMPLE).

Please visit our project page at https://directory.apache.org/scimple.

## Features

* A full-featured Java SCIM client
* Declarative creation of new ResourceTypes and Extensions
* Dynamic generation of the Schema and ResourceType end-points.
* A suite of tools to allow the creation of SCIM resources that can then be
  verified against the appropriate schemas.

## Future features

* Example implementations of the server with various persistence paradigms.
* A set of verification tests to ensure the project meets the specification
  and to allow inter-operability testing.

## Example code
    
Examples of the project's declarative syntax are shown below.  The first example
shows how a SCIM resource is declared:

```java
@Data
@EqualsAndHashCode(callSuper = true)
@ScimResourceType(
    id = ScimUser.RESOURCE_NAME,
    name = ScimUser.RESOURCE_NAME,
    schema = ScimUser.SCHEMA_URI,
    description = "Top level ScimUser",
    endpoint = "/Users"
)
@XmlRootElement(name = ScimUser.RESOURCE_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
public class ScimUser extends ScimResource {

    public static final String RESOURCE_NAME = "User";
    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:User";

    @ScimAttribute(
    description="A Boolean value indicating the User's administrative status.",
    type=Type.BOOLEAN
    )
    
    @XmlElement(name = "active")
    Boolean active = true;

    @ScimAttribute(
    type = Type.COMPLEX,
    description="A physical mailing address for this User, as described in (address Element). Canonical Type Values of work, home, and other. The value attribute is a complex type with the following sub-attributes."
    )
    @XmlElement(name = "addresses")
    List<Address> addresses;
```

In the example above, annotations are used at the class level and member level
to declare a new SCIM resource and its attributes respectively.  The example
below shows the equivalent declaration for a resource Extension:

```java
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(
    required = false,
    name = "EnterpriseUser",
    id = EnterpriseExtension.URN,
    description = "Attributes commonly used in representing users that belong to, or act on behalf of, a business or enterprise."
)
@Data
public class EnterpriseExtension implements ScimExtension {

    public static final String URN = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

    @XmlType
    @XmlAccessorType(XmlAccessType.NONE)
    @Data
    public static class Manager {

        @ScimAttribute(
        description = "The \"id\" of the SCIM resource representing the user's manager.  RECOMMENDED."
        )
    
        @XmlElement
        private String value;

        @ScimAttribute(
        description = "The URI of the SCIM resource representing the User's manager.  RECOMMENDED."
        )
        
        @XmlElement
        private String $ref;
```

This example shows how an extension is declared at the class level, but also
provides an example of how complex SCIM types can be simply defined as
suitably annotated inner classes.

Implementations are fully customizable without altering the core server code.
The example below shows how the implementation for a provider is declared:

```java
public class InMemoryUserService implements Provider<ScimUser> {
```

Implementing the provider interface allows the customization of create,
retrieve, update and delete methods (as well as find).  Customization is
flexible - if your system implements soft deletes, create a delete method that
simply sets a flag and alter the find and retrieve methods to only return
"undeleted" resources.
