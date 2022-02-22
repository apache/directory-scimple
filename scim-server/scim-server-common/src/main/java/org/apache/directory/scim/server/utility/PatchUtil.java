package org.apache.directory.scim.server.utility;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.schema.Schema.Attribute;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import javax.ws.rs.core.Response;

import org.antlr.v4.runtime.misc.Pair;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.extern.slf4j.Slf4j;

/*
 * SCIM PATCH utility methods
 */
@Slf4j
public class PatchUtil {

  private static final Map<PatchOperation.Type, Set<String>> UNSUPPORTED =
    ImmutableMap.of(REMOVE, ImmutableSet.of("active"));

  private static final String PRIMARY_ATTR_NAME = "primary";
  private static final String SUB_ATTR_NAME = "type";

  /**
   * @param status      the HTTP {@link Response.Status}
   * @param messageType the {@link ErrorMessageType} type
   * @return Returns a populated {@link ScimException}
   */
  public static ScimException throwScimException(final Response.Status status, final ErrorMessageType messageType) {
    final ErrorResponse errorResponse = new ErrorResponse(status, messageType.getDetail());
    errorResponse.setScimType(messageType);

    return new ScimException(errorResponse, status);
  }

  /**
   * @param attribute the {@link Attribute}
   * @return Returns {@link Class} representing the generic
   * @throws ClassNotFoundException if the class isn't found
   */
  public static Class<?> genericClass(final Attribute attribute) throws ClassNotFoundException {
    try {
      Field field = attribute.getField();
      ParameterizedType listType = (ParameterizedType) field.getGenericType();
      Type actualTypeArgument = listType.getActualTypeArguments()[0];
      return Class.forName(actualTypeArgument.getTypeName());
    } catch (ClassNotFoundException e) {
      log.error("Class '{}' wasn't found", e.getMessage());
      log.debug("STACKTRACE::", e);
      throw e;
    }
  }

  /**
   * Check the mutability of an attribute.
   *
   * @param attribute Attribute.
   * @throws ScimException if the {@code Attribute} is {@link Attribute.Mutability#READ_ONLY} or {@link Attribute.Mutability#IMMUTABLE}
   */
  public static void checkMutability(Attribute attribute) throws ScimException {
    if (attribute.getMutability().equals(Attribute.Mutability.READ_ONLY) ||
      attribute.getMutability().equals(Attribute.Mutability.IMMUTABLE)) {
      log.error("Can not update a immutable attribute or a read-only attribute '{}'", attribute.getName());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.MUTABILITY);
    }
  }

  /**
   * Check if attribute is required.
   *
   * @param operation the {@link PatchOperation}.
   * @param schema    the {@link Schema}.
   * @throws ScimException if the {@code Attribute} is {@link Attribute.Mutability#READ_ONLY} or {@link Attribute.Mutability#IMMUTABLE}
   */
  public static void checkRequired(final PatchOperation operation, final Schema schema, final Registry registry) throws ScimException {
    final PathAttributePair pair = attributePair(operation, schema, registry);
    if (pair.getAttribute().getMutability().equals(Attribute.Mutability.READ_ONLY) || pair.getAttribute().isRequired()) {
      log.error("Can not {} a required or a read-only attribute, '{}'", operation.getOperation(), pair.getPath());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.MUTABILITY);
    }
  }

  /**
   * Check the path.
   *
   * @param operation the {@link PatchOperation}.
   * @param registry the {@link Registry}.
   * @return Returns the {@link Schema} the {@code operation} is found in
   * @throws ScimException if operation type isn't supported for the given attribute
   */
  public static Schema checkSchema(PatchOperation operation, Registry registry) throws ScimException {
    AttributeReference reference = attributeReference(operation);
    if (reference!=null) {
      for (final Schema schema : registry.getAllSchemas()) {
        if (schema.getAttribute(reference.getAttributeName())!=null) {
          return schema;
        }
      }
    }

    log.error("Invalid path specified for {} operation.", operation.getOperation());
    throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_PATH);
  }

  /**
   * Check the target path.
   *
   * @param patchOperation the {@link PatchOperation}.
   * @throws ScimException if {@link PatchOperation#getPath()} is null
   */
  public static void checkTarget(final PatchOperation patchOperation) throws ScimException {
    if (patchOperation.getOperation().equals(REMOVE) && patchOperation.getPath()==null) {
      log.error("No path value specified for {} operation.", patchOperation.getOperation());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.NO_TARGET);
    }
  }

  /**
   * Check the value.
   *
   * @param patchOperation the {@link PatchOperation}.
   * @param schema    the {@link Schema}.
   * @throws ScimException if {@link PatchOperation#getValue()} is null and the {@link PatchOperation#getOperation()} is not ADD or REPLACE
   */
  public static void checkValue(final PatchOperation patchOperation, final Schema schema) throws ScimException {
    if (!patchOperation.getOperation().equals(PatchOperation.Type.REMOVE) && patchOperation.getValue()==null) {
      log.error("The value is required to perform patch '{}' operation on '{}'.",
        patchOperation.getOperation(), patchOperation.getPath().toString());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
    }

    final AttributeReference attributeReference = attributeReference(patchOperation);
    if(attributeReference.getAttributeName() != null) {
      final Attribute attribute = schema.getAttribute(attributeReference.getAttributeName());
      if(attributeReference.getSubAttributeName() != null) {
        final Attribute subAttribute = attribute.getAttribute(attributeReference.getSubAttributeName());
        if(subAttribute != null) {
          if(validateValue(subAttribute.getType(), patchOperation.getValue())) {
            log.error("The value is not compatible with target type '{}', provided type '{}'.",
              subAttribute.getType(), patchOperation.getValue().getClass().getName());

            throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
          }
        }
      } else {
        if(validateValue(attribute.getType(), patchOperation.getValue())) {
          log.error("The value is not compatible with target type '{}', provided type '{}'.",
            attribute.getType(), patchOperation.getValue().getClass().getName());

          throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
        }
      }
    }
  }

  /**
   * Check the attribute is supported for given patch operation.
   *
   * @param operation the {@link PatchOperation}.
   * @param schema    the {@link Schema}.
   * @throws ScimException if operation type isn't supported for the given attribute
   */
  public static void checkSupported(final PatchOperation operation, final Schema schema, final Registry registry) throws ScimException {
    final PathAttributePair pair = attributePair(operation, schema, registry);
    if (UNSUPPORTED.getOrDefault(operation.getOperation(), ImmutableSet.of()).contains(pair.getPath())) {
      log.error("The operation type '{}' for attribute '{}' isn't supported", operation.getOperation(), pair.getPath());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_PATH);
    }
  }

  /**
   * @param oldValue the current value of the attribute being patched
   * @param newValue the new value of the attribute being patched
   * @return Returns {@code true} if and only if that values are the same.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public static boolean checkValueEquals(final Object oldValue, final Object newValue) {
    return Objects.equals(oldValue, newValue);
  }

  private static boolean validateValue(final Attribute.Type type, final Object value) {
    log.debug("Validate Value expected type: {} actual type: {}", type, value.getClass().getSimpleName());
    switch (type) {
      case BOOLEAN:
        return (!(value instanceof Boolean));
      case COMPLEX:
        return !(Collection.class.isAssignableFrom(value.getClass()) || value instanceof Map);
      case DATE_TIME:
        return (!(value instanceof LocalDateTime));
      case DECIMAL:
        return !((value instanceof Double));
      case INTEGER:
        return (!(value instanceof Integer));
      case REFERENCE:
        return (!(value instanceof URI));
      case BINARY:
      case STRING:
        return (!(value instanceof String));
    }

    return true;
  }

  /**
   * @param patchOperation a valid path in terms of PATCH operation for the case of value selection filter.
   * @return Returns {@link Pair} representing if the path has a filter expression and the selection filter
   */
  public static Pair<Boolean, String> validateFilterPath(PatchOperation patchOperation) {
    String selFilter = null;
    String path = patchOperation.getPath().toString();
    int lBracketIndex = path.indexOf("[");
    // Check if characters preceding bracket look like attribute name
    boolean isFilterExpression = (lBracketIndex > 0) &&
      path.substring(0, lBracketIndex).matches("[a-zA-Z]\\w*");

    if (isFilterExpression)
    {
      int rBracketIndex = path.lastIndexOf("]");

      int length = path.length() - 1;
      /*
       * It will be valid if character ']' is the last of string, or if it is followed by a dot and at least one
       * letter (thus specifying a sub-attribute name). Examples:
       *  - emails[type eq null]
       *  - addresses[value co "any[...]thing"]
       *  - ims[value eq "hi"].primary
       */
      if ((rBracketIndex > lBracketIndex) &&
        (rBracketIndex==length ||
          (length - rBracketIndex > 1 && path.charAt(rBracketIndex + 1)=='.' &&
            Character.isLetter(path.charAt(rBracketIndex + 2))))) {
        selFilter = path.substring(lBracketIndex + 1, rBracketIndex);
        log.debug("Selection filter '{}'", selFilter);
      }
    }

    return new Pair<>(isFilterExpression, selFilter);
  }

  /**
   * @param operation the {@link PatchOperation}.
   * @return Returns a {@link AttributeReference} representing the {@link PatchOperation#getPath()}
   */
  public static AttributeReference attributeReference(final PatchOperation operation) {
    return Objects.requireNonNull(
      Objects.requireNonNull(
        Objects.requireNonNull(operation, "Patch operation must not be null")
          .getPath(), "Patch Operation Path must not be null")
        .getValuePathExpression(), "Value Path Expression must not be null")
      .getAttributePath();
  }

  /**
   * @param operation the {@link PatchOperation}.
   * @param schema    the {@link Schema}.
   * @return Returns a populated {@link PathAttributePair} or {@code null} if attribute isn't found
   */
  public static PathAttributePair attributePair(PatchOperation operation, final Schema schema, final Registry registry) {
    AttributeReference reference = attributeReference(operation);
    Attribute attribute;

    if (reference.getUrn()==null) {
      final Schema useThisSchema = registry.getSchema(reference.getUrn());
      if (useThisSchema!=null) {
        attribute = useThisSchema.getAttribute(reference.getAttributeName());
      } else {
        attribute = schema.getAttribute(reference.getAttributeName());
      }

    } else {
      attribute = schema.getAttribute(reference.getAttributeName());
      if (attribute!=null) {
        if (reference.getSubAttributeName() != null) {
          attribute = attribute.getAttribute(reference.getSubAttributeName());
        }
      }
    }

    return new PathAttributePair(reference.getFullyQualifiedAttributeName(), attribute);
  }

  /**
   * For multivalued attributes, a PATCH operation that sets a value's "primary" sub-attribute to "true" SHALL cause
   * the server to automatically set "primary" to "false" for any other values in the array.
   *
   * @param scimResourceAsMap the {@link Map} representation of the {@link ScimResource}
   * @param patchOperation the {@link PatchOperation}
   * @param registry the {@link Registry}.
   */
  @SuppressWarnings("unchecked")
  public static void multiValuedPrimaryUniqueness(Map<String,Object> scimResourceAsMap, final PatchOperation patchOperation,
                                                  final Registry registry) {
    AttributeReference reference = attributeReference(patchOperation);
    if (reference == null) {
      return;
    }

    if(reference.getAttributeName() == null || reference.getSubAttributeName() == null) {
      return;
    }

    final Schema schema = registry.getAllSchemas()
      .stream()
      .filter( s -> s.getAttribute(reference.getAttributeName()) != null)
      .findFirst().orElse(null);

    if(schema == null) {
      return;
    }

    Attribute attribute = schema.getAttribute(reference.getAttributeName());

    if(attribute == null || (!scimResourceAsMap.containsKey(reference.getAttributeName()) &&
      (!attribute.isMultiValued() && !attribute.getType().equals(Attribute.Type.COMPLEX)))) {
      return;
    }

    if(attribute.getAttribute(reference.getSubAttributeName()) == null) {
      return;
    }

    try {
      Object multiValuedObject = scimResourceAsMap.get(reference.getAttributeName());
      if(multiValuedObject instanceof List) {
        PatchOperation searchPatchOps = new PatchOperation();
        searchPatchOps.setPath(new PatchOperationPath(String.format("%s[primary EQ true]", reference.getAttributeName())));

        Deque<Integer> matches = new LinkedList<>();
        List<Map<String, Object>> asListOfMaps = (List<Map<String, Object>>) multiValuedObject;
        int index = 0;
        for (final Map<String, Object> element : asListOfMaps) {
          if(!FilterMatchUtil.complexAttributeMatch(attribute, element, patchOperation) &&
            FilterMatchUtil.complexAttributeMatch(attribute, element, searchPatchOps)) {
            matches.push(index);
          }

          index++;
        }

        if(matches.size() >= 1) {
          log.info("Found {} multi-valued attribute {} with primary attribute set to 'true'", matches.size(), attribute.getName());
          matches.forEach(i -> {
            log.info("Setting 'primary = false' for element index {} whose associated attribute '{}' and sub-attribute '{}' is '{}'",
              i, reference.getAttributeName(), SUB_ATTR_NAME, asListOfMaps.get(i).get(SUB_ATTR_NAME));
            asListOfMaps.get(i).replace(PRIMARY_ATTR_NAME, false);
          });
        }
      }
    } catch (Exception e) {
      log.error("There was a problem determining if attribute '{}}' has a single unique element with 'primary' set to 'true'.",
        reference.getAttributeName(), e);
    }
  }

  public static String attributeLoggable(final Schema.Attribute attribute) throws ScimException {

    return Optional.ofNullable(attribute).map(sb -> String.format("Attribute: '%s' isMultiValued: '%b' Type: '%s'", sb.getName(), sb.isMultiValued(), sb.getType()))
      .orElseThrow(() -> throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_SYNTAX));
  }

  public static String subAttributeLoggable(final Schema.Attribute subAttribute) {
    return Optional.ofNullable(subAttribute).map(sb -> String.format("Sub-Attribute: '%s' isMultiValued: '%b' Type: '%s'", sb.getName(), sb.isMultiValued(), sb.getType()))
      .orElse("Sub-Name: 'omitted'");
  }

  @SuppressWarnings({"unchecked", "unused"})
  public static String valueLoggable(final Schema.Attribute attribute, final Schema.Attribute subAttribute,  final Object value) {
    if(value instanceof Map) {
      Map<String,Object> source = (Map<String,Object>) value;
      Object v = source.get(attribute.getName());
      if(subAttribute==null) {
        if(v instanceof Map) {
          return (String) Optional.of(v)
            .map(map -> ((Map<String, Object>) map).get(attribute.getName()))
            .orElse("unassigned");
        } else {
          log.error("Attribute: {} V::{}", attribute.getName(), v.getClass().getSimpleName());
          return (String) v;
        }
      } else {
        if(v instanceof Map) {
          return (String) Optional.of(v)
            .map(map -> ((Map<String, Object>) map).get(subAttribute.getName()))
            .orElse("unassigned");
        } else if(v instanceof List) {
          /*
           * we don't expect more than one element within the list.
           */
          List<Map<String,Object>> list = (List<Map<String,Object>>) v;
          Map<String,Object> map = list.get(0);
          if(!subAttribute.getType().equals(Schema.Attribute.Type.BOOLEAN)) {
            return (String) Optional.of(map)
              .map(element -> map.get(subAttribute.getName()))
              .orElse("unassigned");
          } else {
            return Optional.of(map)
              .map(element -> String.valueOf(map.get(subAttribute.getName())))
              .orElse("unassigned");
          }
        } else {
          return v == null ? "unassigned" : (String) v;
        }
      }
    }

    return (String) value;
  }
}
