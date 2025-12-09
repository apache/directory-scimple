# Migration Guide

## Migrating from `1.0.0-M1` to `1.0.0`

This guide covers the breaking changes to the `org.apache.directory.scim.core.repository.Repository` interface and how to update your implementations.

> [!CAUTION]
> The `Repository` interface has **breaking changes** that require updates to all implementations.

---

## Summary of Changes

The `Repository` interface has been refactored to use `ScimRequestContext` instead of passing individual parameters. This consolidates request metadata (included/excluded attributes, pagination, sorting, and ETags) into a single context object.

### Key Changes

1. **Package Change**: `ScimRequestContext` moved from `org.apache.directory.scim.spec` to `org.apache.directory.scim.core.repository`
2. **ETag Handling**: ETag information is now accessed via `ScimRequestContext.getETag()` instead of being a separate method parameter
3. **Simplified Signatures**: All methods now use `ScimRequestContext` to consolidate request metadata

### Method Signature Changes

| Method | Old Signature (1.0.0-M1) | New Signature (1.0.0) |
|--------|--------------------------|----------------------|
| `create` | `create(T)` | `create(T, ScimRequestContext)` |
| `get` | `get(String)` | `get(String, ScimRequestContext)` |
| `find` | `find(Filter, PageRequest, SortRequest)` | `find(Filter, ScimRequestContext)` |
| `update` | `update(String, String, T, Set<AttributeReference>, Set<AttributeReference>)` | `update(String, T, ScimRequestContext)` |
| `patch` | `patch(String, String, List<PatchOperation>, Set<AttributeReference>, Set<AttributeReference>)` | `patch(String, List<PatchOperation>, ScimRequestContext)` |

---

## New Classes

### `org.apache.directory.scim.core.repository.ETag`

A new `ETag` class has been added to represent HTTP ETags for optimistic concurrency control:

```java
import org.apache.directory.scim.core.repository.ETag;

// ETag is used for versioning and optimistic concurrency
// The constructor takes a value and a boolean indicating if it's a weak ETag
ETag etag = new ETag("abc123", false);  // Strong ETag
ETag weakEtag = new ETag("abc123", true); // Weak ETag

// Useful methods
etag.getValue();   // Returns the ETag value
etag.isWeak();     // Returns true if this is a weak ETag
```

---

## Migration Steps

### Step 1: Update Imports

Update the import for `ScimRequestContext` - the package has changed:

**Before (1.0.0-M1):**
```java
import org.apache.directory.scim.spec.ScimRequestContext;
```

**After (1.0.0):**
```java
import org.apache.directory.scim.core.repository.ScimRequestContext;
```

Also, the ETag import is no longer needed as a separate parameter (ETags are accessed via `ScimRequestContext`):

```java
// No longer needed as a method parameter, but still available via ScimRequestContext
import org.apache.directory.scim.core.repository.ETag;
```

### Step 2: Update Method Signatures

#### `create` Method

**Before (1.0.0-M1):**
```java
@Override
public ScimUser create(ScimUser resource) throws ResourceException {
    // implementation
}
```

**After (1.0.0):**
```java
@Override
public ScimUser create(ScimUser resource, ScimRequestContext requestContext) throws ResourceException {
    // implementation - requestContext available if needed for attribute filtering
}
```

#### `get` Method

**Before (1.0.0-M1):**
```java
@Override
public ScimUser get(String id) throws ResourceException {
  // implementation
}
```

**After (1.0.0):**
```java
@Override
public ScimUser get(String id, ScimRequestContext requestContext) throws ResourceException {
  // implementation - requestContext available if needed for attribute filtering
}
```

#### `find` Method

**Before (1.0.0-M1):**
```java
@Override
public FilterResponse<ScimUser> find(Filter filter, PageRequest pageRequest, SortRequest sortRequest) throws ResourceException {
    long count = pageRequest != null ? pageRequest.getCount() : users.size();
    long startIndex = pageRequest != null ? pageRequest.getStartIndex() - 1 : 0;
    // ...
}
```

**After (1.0.0):**
```java
@Override
public FilterResponse<ScimUser> find(Filter filter, ScimRequestContext requestContext) throws ResourceException {
    long count = requestContext.getPageRequest().map(PageRequest::getCount).orElse(users.size());
    long startIndex = requestContext.getPageRequest().map(PageRequest::getStartIndex).map(it -> it - 1).orElse(0);
    // ...
}
```

#### `update` Method

**Before (1.0.0-M1):**
```java
@Override
public ScimUser update(String id, String version, ScimUser resource, 
                       Set<AttributeReference> includedAttributes, 
                       Set<AttributeReference> excludedAttributes) throws ResourceException {
    // version was a String for ETag
    // implementation
}
```

**After (1.0.0):**
```java
@Override
public ScimUser update(String id, ScimUser resource, 
                       ScimRequestContext requestContext) throws ResourceException {
    // ETag is now accessed via requestContext.getETag()
    // includedAttributes/excludedAttributes are also in requestContext
    // implementation
}
```

#### `patch` Method

**Before (1.0.0-M1):**
```java
@Override
public ScimUser patch(String id, String version, List<PatchOperation> patchOperations,
                      Set<AttributeReference> includedAttributes,
                      Set<AttributeReference> excludedAttributes) throws ResourceException {
    // version was a String for ETag
    // implementation
}
```

**After (1.0.0):**
```java
@Override
public ScimUser patch(String id, List<PatchOperation> patchOperations,
                      ScimRequestContext requestContext) throws ResourceException {
    // ETag is now accessed via requestContext.getETag()
    // includedAttributes/excludedAttributes are also in requestContext
    // implementation
}
```

---

## Additional Notes

### Java Version Requirement

The minimum JDK version has changed from **JDK 11** to **JDK 17**.

### Using ScimRequestContext

The `ScimRequestContext` provides access to all request metadata:

| Method | Description |
|--------|-------------|
| `getIncludedAttributes()` | Attributes that should be included in the response |
| `getExcludedAttributes()` | Attributes that should be excluded from the response |
| `getPageRequest()` | Pagination information (returns `Optional<PageRequest>`) |
| `getSortRequest()` | Sorting information |
| `getETag()` | ETag(s) from the `If-Match` header for optimistic concurrency |

### ETag Handling

ETags are now accessed via `ScimRequestContext.getETag()` instead of being a separate method parameter. This returns a `Set<ETag>` which aligns with the HTTP `If-Match` header semantics (which can contain multiple ETags).

If you need to implement optimistic locking with ETags:

```java
@Override
public ScimUser update(String id, ScimUser resource, 
                       ScimRequestContext requestContext) throws ResourceException {
    ScimUser existing = store.get(id);
    if (existing == null) {
        throw new ResourceNotFoundException(id);
    }
    
    // Optional: Check ETag for optimistic concurrency
    Set<ETag> etags = requestContext.getETag();
    if (etags != null && !etags.isEmpty()) {
        String currentVersion = existing.getMeta().getVersion();
        boolean matches = etags.stream()
            .anyMatch(etag -> etag.getValue().equals(currentVersion));
        if (!matches) {
            throw new PreconditionFailedException("ETag mismatch");
        }
    }
    
    store.save(resource);
    return resource;
}
```

---

## Need Help?

If you encounter issues during migration, please:
1. Check the [Apache Directory SCIMple documentation](https://directory.apache.org/scimple/)
2. Open an issue on the [GitHub repository](https://github.com/apache/directory-scimple)
