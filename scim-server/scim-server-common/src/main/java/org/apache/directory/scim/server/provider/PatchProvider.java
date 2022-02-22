package org.apache.directory.scim.server.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.server.utility.FilterMatchUtil;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.protocol.filter.AttributeComparisonExpression;
import org.apache.directory.scim.spec.protocol.filter.FilterExpression;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.protocol.filter.ValuePathExpression;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.apache.directory.scim.server.utility.PatchUtil.*;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.schema.Schema.Attribute;

/**
 * This implementation attempts to comply with [SCIM Modifying with PATCH]([https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2)
 * section of [RFC7644](https://datatracker.ietf.org/doc/html/rfc7644) spec.
 * <p>
 * Based on the Section 3.12 of the Spec, @see https://datatracker.ietf.org/doc/html/rfc7644#section-3.12, for the
 * error types are associated with PATCH operations.
 *
 * @since 2.23
 */
@Stateless
@Slf4j
public class PatchProvider {
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
  };

  private static final String NO_CHANGE = "Current value and requested target value are the same, no changes made for {}: '{}'";
  private static final String APPLYING_PATCH_OPERATION_FOR_ATTRIBUTE = "Applying Patch Operation '{}' for attribute '{}'";
  public static final String SCHEMAS = "schemas";

  private final ObjectMapper objectMapper;

  @Inject
  Registry registry;

  @Inject
  public PatchProvider(Registry registry) {
    this.registry = registry;
    this.objectMapper = new ObjectMapperFactory(this.registry).createObjectMapper();
  }

  /**
   * Apply the supplied list of patch operation to the given SCIM resource.
   *
   * @param source          the SCIM resource to apply patches
   * @param patchOperations the list of patch operations to be applied
   * @return Returns the patched SCIM resource
   * @throws ScimException if any of the patch operations can't be applied
   */

  @SuppressWarnings("unchecked")
  public <T extends ScimResource>T apply(final T source, final List<PatchOperation> patchOperations) throws ScimException {
    if (source==null) {
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.NO_TARGET);
    }

    if (patchOperations==null) {
      /*
       * The SCIM Spec doesn't call out how an empty or non-existing patch operation list should be handled.
       *
       * Use invalid syntax, since it make the more sense, IMHO.
       */
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_SYNTAX);
    }

    final Instant start = Instant.now();

    T scimResource;
    try {
      scimResource = SerializationUtils.clone(source);

      for (PatchOperation it : patchOperations) {
        if (it.getPath()==null && it.getValue() instanceof Map) {
          Map<String, Object> properties = (Map<String, Object>) it.getValue();

          for (Map.Entry<String, Object> entry : properties.entrySet()) {
            // convert SCIM patch to RFC-6902 patch
            PatchOperation newPatchOperation = new PatchOperation();
            newPatchOperation.setOperation(it.getOperation());
            newPatchOperation.setPath(patchOperationPath(entry.getKey()));
            newPatchOperation.setValue(entry.getValue());

            scimResource = apply(scimResource, newPatchOperation);
          }
        } else if (it.getPath()==null && !(it.getValue() instanceof Map)) {
          checkTarget(it);
          throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
        } else {
          scimResource = apply(scimResource, it);
        }
      }
    } finally {
      final Instant finish = Instant.now();
      log.info("Processed {} Patch operation(s) in {} ms.", patchOperations.size(), Duration.between(start, finish).toMillis());
    }

    return scimResource;
  }

  /**
   * Apply the supplied patch operation to the given SCIM resource.
   *
   * @param source         the SCIM resource to apply patches
   * @param patchOperation the patch operations to be applied
   * @return Returns the patched SCIM resource
   * @throws ScimException if the patch operation can't be applied
   */
  private <T extends ScimResource> T apply(final T source, final PatchOperation patchOperation) throws ScimException {

    if (source==null) {
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.NO_TARGET);
    }

    if (patchOperation==null) {
      /*
       * The SCIM Spec doesn't call out how an empty or non-existing patch operation list should be handled.
       *
       * Use invalid syntax, since iy make the more sense, IMHO.
       */
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_SYNTAX);
    }

    final Instant start = Instant.now();
    T target;

    try {
      String path = patchOperation.getPath() != null ? patchOperation.getPath().toString():"";

      // determine if patch operation includes a filter, if so handle here
      if (StringUtils.isNotEmpty(path)) {
        Pair<Boolean, String> pair = validateFilterPath(patchOperation);
        if (pair.a) {
          String valSelFilter = pair.b;
          if (StringUtils.isBlank(valSelFilter)) {
            throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
          } else {
            int i = path.indexOf("[");
            String attribute = path.substring(0, i);

            i = path.lastIndexOf("].");
            String subAttribute = i==-1
              ? ""
              :path.substring(i + 2);

            return applyWithValueFilter(source, patchOperation, valSelFilter,
              attribute, subAttribute);
          }
        }
      }

      switch (patchOperation.getOperation()) {
        case ADD:
          target = patchAdd(patchOperation, source);
          break;
        case REMOVE:
          target = patchRemove(patchOperation, source);
          break;
        case REPLACE:
          target = patchReplace(patchOperation, source);
          break;
        default:
          log.info("Unsupported Patch Operation {}", patchOperation.getOperation());
          throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VERS);
      }
    } finally {
      final Instant finish = Instant.now();
      log.info("Processed {} Patch operation for attribute {} took {} ms to process", patchOperation.getOperation(), patchOperation.getPath(),
        Duration.between(start, finish).toMillis());
    }

    return target;
  }

  /**
   * @param resource     the source SCIM resource
   * @param operation    the {@link PatchOperation}
   * @param valSelFilter a {@link String} representing the value filter
   * @param attribute    the value attribute
   * @param subAttribute the sub-attribute
   * @param <T>          the parameter
   * @return Returns the patched {@link ScimResource}
   * @throws ScimException if any errors occur while trying to patch the supplied resource
   */
  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T applyWithValueFilter(T resource,
                                                          final PatchOperation operation,
                                                          final String valSelFilter,
                                                          final String attribute,
                                                          final String subAttribute) throws ScimException {
    log.info("Applying Patch Operation '{}' with value filter '{}' attribute '{}'",
      operation.getOperation(), valSelFilter, attribute);

    Schema schema = this.registry.getSchema(resource.getBaseUrn());
    Map<String, Object> fromMap = scimResourceAsMap(resource);
    List<Map<String, Object>> targetAttributes = null;

    final Attribute parentAttribute = schema.getAttribute(attribute);
    Map<String, Object> resourceAsMap = scimResourceAsMap(resource);
    if (parentAttribute != null) {
      if (parentAttribute.isMultiValued() && Attribute.Type.COMPLEX.equals(parentAttribute.getType())) {
        Object object = fromMap.getOrDefault(attribute, null);
        targetAttributes = (object==null)
          ? null
          : new ArrayList<>((Collection<Map<String, Object>>) object);
      } else {
        throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
      }
    }

    if (!ADD.equals(operation.getOperation()) && (targetAttributes == null || targetAttributes.isEmpty())) {
      // We can't replace or remove what isn't there. - see section 3.5.2.3/4 of RFC7644
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.NO_TARGET);
    }
    if (targetAttributes == null) {
      targetAttributes = new ArrayList<>();
    }

    Deque<Integer> matchingIndexes = new LinkedList<>();
    for (int i = 0; i < targetAttributes.size(); i++) {
      if (FilterMatchUtil.complexAttributeMatch(parentAttribute, targetAttributes.get(i), operation)) {
        matchingIndexes.push(i);
      }
    }

    log.info("There are {} existing entries matching the filter '{}'", matchingIndexes.size(), operation.getPath());

    if (matchingIndexes.isEmpty()) {
      if(ADD.equals(operation.getOperation()) && attribute != null && subAttribute != null) {
        /*
         * Based on the Spec complex filters aren't supported. So we should only care about AttributeComparisonExpression
         */
        PatchOperationPath patchOperationPath = operation.getPath();
        ValuePathExpression vpe = patchOperationPath.getValuePathExpression();
        FilterExpression fe = vpe.getAttributeExpression();
        if(fe instanceof AttributeComparisonExpression) {
          AttributeComparisonExpression ace = (AttributeComparisonExpression)fe;
          targetAttributes.add(createFromAddOperation(ace));
          matchingIndexes.push(targetAttributes.size() - 1);
          log.info("Entry added based on filter '{}'", operation.getPath());
        }
      } else {
        /*
         * see section 3.5.2.3/4 of RFC7644
         *
         * If the target location is a multi-valued attribute for which a value selection filter ("valuePath") has been
         * supplied and no record match was made, the service provider SHALL indicate failure by returning HTTP status
         * code 400 and a "scimType" error code of "noTarget".
         */
        throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.NO_TARGET);
      }
    }

    for (Integer index : matchingIndexes) {
      if (REMOVE.equals(operation.getOperation())) {
        if (subAttribute == null || subAttribute.isEmpty()) {
          // Remove the whole item
          targetAttributes.remove(index.intValue());  // If intValue is not used, the remove(Object) method is
        } else {
          // remove sub-attribute only
          targetAttributes.get(index).remove(subAttribute);
        }
      } else {
        applyPartialUpdate(parentAttribute,
          parentAttribute != null
            ? parentAttribute.getAttribute(subAttribute)
           : null,
          targetAttributes, index, operation.getValue());
      }
    }

    resourceAsMap.put(attribute, targetAttributes.size() == 0 ? null : targetAttributes);

    multiValuedPrimaryUniqueness(resourceAsMap, operation, this.registry);

    return (T) mapAsScimResource(resourceAsMap, resource.getClass());
  }

  private Map<String, Object> createFromAddOperation(AttributeComparisonExpression filter) {
    final HashMap<String, Object> newAttribute = new HashMap<>();
    newAttribute.put(filter.getAttributePath().getSubAttributeName(), filter.getCompareValue());
    return newAttribute;
  }

  /**
   * @param attribute    the attribute name
   * @param subAttribute the sub-attribute
   * @param list         the {@link List} of {@link Map} representing the attribute/value pairs
   * @param index        the complex attribute index
   * @param value        the replacement value
   * @throws ScimException if any errors occur while trying to patch the supplied resource
   */
  @SuppressWarnings("unchecked")
  private void applyPartialUpdate(final Attribute attribute,
                                  final Attribute subAttribute,
                                  List<Map<String, Object>> list,
                                  final int index,
                                  final Object value)
    throws ScimException {

    if (subAttribute==null) {
      /*
       * Updates the whole item in the list after passing mutability check, see section 3.5.2 RFC 7644:
       *
       * Each operation against an attribute MUST be compatible with the attribute's mutability and schema ...
       */
      Map<String, Object> map;
      if(value instanceof List) {
        try {
          Class<?> clazz = genericClass(attribute);
          if(clazz.isAssignableFrom(Map.class)) {
            List<Map<String, Object>> valueList = (List<Map<String, Object>>) value;
            if(valueList.size()!=1) {
              throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.UNIQUENESS);
            }
            map = valueList.get(0);
          } else {
            map = objectMapper.convertValue(((List<?>) value).get(0), MAP_TYPE);
          }
        } catch (ClassNotFoundException e) {
          log.error("Failed to determine the generic class list of elements, {}.", e.getMessage());
          log.debug("STACKTRACE::", e);

          throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
        }
      } else {
        map = (Map<String, Object>) value;
      }

      if(map == null) {
        throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
      }

      for (String subAttr : map.keySet()) {
        log.debug("Attribute: {} SubAttribute: {}", attribute.getName(), subAttr);
        checkMutability(attribute);
      }

      list.set(index, map);
    } else {
      log.debug("Full Attribute {}.{}", attribute.getName(), subAttribute.getName());
      checkMutability(attribute);
      if(list.get(index).containsKey(subAttribute.getName())) {
        if (!checkValueEquals(list.get(index).get(subAttribute.getName()), value)) {
          list.get(index).replace(subAttribute.getName(), value);
        } else {
          log.info(NO_CHANGE, index, attribute.getName());
        }
      } else {
        if (!checkValueEquals(list.get(index).get(subAttribute.getName()), value)) {
          list.get(index).put(subAttribute.getName(), value);
        } else {
          log.info(NO_CHANGE, index, attribute.getName());
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T patchAdd(final PatchOperation patchOperation, T source) throws ScimException {
    log.info(APPLYING_PATCH_OPERATION_FOR_ATTRIBUTE,
      Objects.requireNonNull(patchOperation, "patchOperation must not be null.").getOperation(),
      Objects.requireNonNull(patchOperation.getPath(), "patchOperation PATH must not be null."));

    final Schema schema = checkSchema(patchOperation, this.registry);
    checkValue(patchOperation, schema);

    Map<String, Object> sourceAsMap = scimResourceAsMap(source);

    processPatchOperation(source.getBaseUrn(), sourceAsMap, patchOperation);

    return (T) mapAsScimResource(sourceAsMap, source.getClass());
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T patchReplace(final PatchOperation patchOperation, T source) throws ScimException {
    log.info(APPLYING_PATCH_OPERATION_FOR_ATTRIBUTE,
      Objects.requireNonNull(patchOperation, "patchOperation must not be null.").getOperation(),
      Objects.requireNonNull(patchOperation.getPath(), "patchOperation PATH must not be null."));

    final Schema schema = checkSchema(patchOperation, this.registry);
    checkValue(patchOperation, schema);

    Map<String, Object> sourceAsMap = scimResourceAsMap(source);

    processPatchOperation(source.getBaseUrn(), sourceAsMap, patchOperation);

    return (T) mapAsScimResource(sourceAsMap, source.getClass());
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T patchRemove(final PatchOperation patchOperation, T source) throws ScimException {
    log.info(APPLYING_PATCH_OPERATION_FOR_ATTRIBUTE,
      Objects.requireNonNull(patchOperation, "patchOperation must not be null.").getOperation(),
      Objects.requireNonNull(patchOperation.getPath(), "patchOperation PATH must not be null."));

    final Schema schema = checkSchema(patchOperation, this.registry);

    checkTarget(patchOperation);
    checkRequired(patchOperation, schema, this.registry);
    checkSupported(patchOperation, schema, this.registry);

    Map<String, Object> sourceAsMap = scimResourceAsMap(source);

    processPatchOperation(source.getBaseUrn(), sourceAsMap, patchOperation);

    return (T) mapAsScimResource(sourceAsMap, source.getClass());
  }

  /**
   * @param scimResourceAsMap the {@link Map} representing the SCIM resource
   * @param clazz             the {@link ScimResource} class
   * @return Returns a {@link ScimResource} representing the {@code scimResourceAsMap}
   */
  private <T extends ScimResource> T mapAsScimResource(final Map<String, Object> scimResourceAsMap, final Class<T> clazz) {
    return objectMapper.convertValue(scimResourceAsMap, clazz);
  }

  /**
   * @param object the {@link Object} to represent as a {@link Map}
   * @return Returns the {@link Map<>} representing {@code object}
   */
  private Map<String, Object> objectAsMap(final Object object) {
    return objectMapper.convertValue(object, MAP_TYPE);
  }

  /**
   * @param scimResource the {@link ScimResource} representing the SCIM resource
   * @return Returns a {@link Map} representing the {@link ScimResource}
   */
  private <T extends ScimResource> Map<String, Object> scimResourceAsMap(final T scimResource) {
    return objectAsMap(scimResource);
  }

  /**
   * @param resourceUrn    the {@link String} representing the SCIM resource's URN
   * @param source         the {@link Map} representing the SCIM resource
   * @param patchOperation the {@link PatchOperation}.
   */
  @SuppressWarnings("unchecked")
  private void processPatchOperation(final String resourceUrn,
                                     Map<String, Object> source,
                                     final PatchOperation patchOperation) throws ScimException {
    final AttributeReference attributeReference = attributeReference(patchOperation);

    Schema baseSchema;
    Schema subSchema;

    // patch operation is for a SCIM Extension
    if(attributeReference.getUrn() != null) {
      baseSchema = this.registry.getSchema(attributeReference.getUrn());
      Map<String, Object> extensionMap;
      final Attribute attribute = baseSchema.getAttribute(attributeReference.getAttributeName());
      Attribute subAttribute = Optional.ofNullable(attributeReference.getSubAttributeName())
        .map(attribute::getAttribute).orElse(null);

      if(source.containsKey(attributeReference.getUrn())) {
        extensionMap = (Map<String, Object>) source.get(attributeReference.getUrn());
      } else {
        if(patchOperation.getValue() instanceof Map) {
          extensionMap = (Map<String, Object>) patchOperation.getValue();
        } else {
          extensionMap = new HashMap<>();
        }
      }

      if(isSingularAttribute(attribute)) {
        singularValuedAttribute(attribute, extensionMap, patchOperation);
      } else if(isComplexValuedAttribute(attribute)) {
        complexValuedAttribute(attribute, subAttribute, extensionMap,patchOperation);
      } else { /* isMultiValuedComplexAttribute(attribute)) */
        multiValuedComplexAttribute(attribute, subAttribute, extensionMap,patchOperation);
      }

      Object schemaAttributeValue = source.get(SCHEMAS);
      if(schemaAttributeValue instanceof List) {
        final List<String> schemaUrns = (List<String>) schemaAttributeValue;
        if(!schemaUrns.contains(attributeReference.getUrn())) {
          schemaUrns.add(attributeReference.getUrn());
          source.replace(SCHEMAS, schemaUrns);
        }
      }

      source.put(attributeReference.getUrn(), extensionMap);
    } else {
      // patch operation is for a SCIM Resource
      baseSchema = this.registry.getSchema(resourceUrn);
      subSchema = attributeReference.getUrn()!=null
        ? this.registry.getSchema(attributeReference.getUrn())
        :null;

      Attribute attribute;
      Attribute subAttribute = null;
      if (subSchema==null) {
        attribute = baseSchema.getAttribute(attributeReference.getAttributeName());
      } else { // looks to be an extension
        attribute = subSchema.getAttribute(attributeReference.getAttributeName());
      }

      if (attributeReference.getSubAttributeName()!=null) {
        subAttribute = attribute.getAttribute(
          attributeReference.getSubAttributeName());
      }

      if (isMultiValuedComplexAttribute(attribute)) {
        multiValuedComplexAttribute(attribute, subAttribute, source,
          patchOperation);
      } else if (isComplexValuedAttribute(attribute)) {
        complexValuedAttribute(attribute, subAttribute, source,
          patchOperation);
      } else if (isSingularAttribute(attribute)) {
        singularValuedAttribute(attribute, source, patchOperation);
      }

      multiValuedPrimaryUniqueness(source, patchOperation, this.registry);
    }
  }

  /**
   * @param path the patch operation path
   * @return Returns a {@link PatchOperationPath} wrapper {@code path}
   * @throws ScimException if the {@code path} cannot be wrapper
   */
  private PatchOperationPath patchOperationPath(final String path) throws ScimException {
    try {
      return new PatchOperationPath(path);
    } catch (FilterParseException e) {
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
    }
  }

  /**
   * @param attribute the attribute
   * @return Returns {@code true} if and only if the attribute represents a multi-valued complex attribute
   */
  private boolean isMultiValuedComplexAttribute(final Attribute attribute) {
    return attribute!=null && attribute.isMultiValued() && attribute.getType().equals(Attribute.Type.COMPLEX);
  }

  /**
   * @param attribute the attribute
   * @return Returns {@code true} if and only if the attribute represents a singular value complex attribute
   */
  private boolean isComplexValuedAttribute(final Attribute attribute) {
    return attribute!=null && !attribute.isMultiValued() && attribute.getType().equals(Attribute.Type.COMPLEX);
  }

  /**
   * @param attribute the attribute
   * @return Returns {@code true} if and only if the attribute represents a singular value attribute
   */
  private boolean isSingularAttribute(final Attribute attribute) {
    return attribute!=null && !attribute.isMultiValued() && !attribute.getType().equals(Attribute.Type.COMPLEX);
  }

  @SuppressWarnings("unchecked")
  private void multiValuedComplexAttribute(final Attribute attribute, final Attribute subAttribute,
                                           Map<String, Object> source, final PatchOperation patchOperation) throws ScimException {

    log.info("Multi-Valued Complex attribute - Operation: {} {} {}",
      patchOperation.getOperation(), attributeLoggable(attribute), subAttributeLoggable(subAttribute));

    switch (patchOperation.getOperation()) {
      case ADD:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */
      case REPLACE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */
        if(!source.containsKey(attribute.getName())) {
          source.put(attribute.getName(), new ArrayList<Map<String,Object>>());
          ((List<Map<String,Object>>) source.get(attribute.getName())).add(new HashMap<>());
        }

        List<Map<String,Object>> list = (List<Map<String,Object>>) source.get(attribute.getName());
        if(!list.isEmpty()) {
          if (list.size() > 1) {
            /*
             * 3.5.2.1.  Add Operation
             *  o If the target location exists, the value is replaced.
             *
             * 3.5.2.3.  Replace Operation
             *  o If the target location is a multi-valued attribute and no filter is specified, the attribute and
             *    all values are replaced.
             */
            if(patchOperation.getValue() instanceof List) {
              list = (List<Map<String,Object>>) patchOperation.getValue();
              source.replace(attribute.getName(), list);
              return;
            }
          }

          // replace the sub-attributes
          if(subAttribute != null) {
            Map<String, Object> element = list.get(0);
            complexValuedAttribute(subAttribute, null, element, patchOperation);
            list.add(element);
            // replace all sub-attributes
          } else {
            if(patchOperation.getValue() instanceof Map) {
              list.remove(0);
              list.add((Map<String, Object>) patchOperation.getValue());
            } else if(patchOperation.getValue() instanceof List) {
              list = (List<Map<String, Object>>) patchOperation.getValue();
            }

            source.replace(attribute.getName(), list);
          }
        }
        break;
      case REMOVE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.2
         */
        if(subAttribute == null) {
          source.remove(attribute.getName());
        } else {
          if(source.get(attribute.getName()) instanceof List) {
            List<Map<String,Object>> removeList = (List<Map<String,Object>>) source.get(attribute.getName());
            if(!removeList.isEmpty()) {
              if (removeList.size() > 1) {
                log.error("There are {} existing entries for {}, use a filter to narrow the results.",
                  removeList.size(), patchOperation.getPath());
                throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.TOO_MANY);
              }
              Map<String, Object> map = removeList.get(0);
              map.remove(subAttribute.getName());
            }
          }
        }
        break;
    }
  }

  /**
   * @param attribute the attribute
   * @param subAttribute the sub-attribute
   * @param source the {@link Map} representing the SCIM resource
   * @param patchOperation the {@link PatchOperation}.
   */
  @SuppressWarnings("unchecked")
  private void complexValuedAttribute(final Attribute attribute, final Attribute subAttribute,
                                      Map<String, Object> source, final PatchOperation patchOperation) throws ScimException {
    log.info("Complex Valued attribute - Operation: {} {} {}",
      patchOperation.getOperation(), attributeLoggable(attribute), subAttributeLoggable(subAttribute));

    switch (patchOperation.getOperation()) {
      case ADD:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */
      case REPLACE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */

        /*
         * If omitted, the target location is assumed to be the resource itself.  The "value" parameter contains a set
         * of attributes to be added to the resource.
         */
        if(subAttribute == null) {
          singularValuedAttribute(attribute, source, patchOperation);
        } else {
          Object complexSource = source.get(attribute.getName());
          if(complexSource == null) {
            complexSource = new HashMap<>();
          }

          if(complexSource instanceof Map) {
            Map<String,Object> complexSourceMap = (Map<String,Object>) complexSource;
            singularValuedAttribute(subAttribute, complexSourceMap, patchOperation);

            if(source.containsKey(attribute.getName())) {
              source.replace(attribute.getName(), complexSourceMap);
            } else {
              source.put(attribute.getName(), complexSourceMap);
            }
          }
        }

        break;
      case REMOVE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.2
         */
        if(subAttribute == null) {
          source.remove(attribute.getName());
        } else {
          if(source.get(attribute.getName()) instanceof Map) {
            ((Map<String,Object>)source.get(attribute.getName())).remove(subAttribute.getName());
          }
        }
        break;
    }
  }

  /**
   * @param attribute      the attribute
   * @param singularAttributeSource   the {@link Map} representing the SCIM resource
   * @param patchOperation the {@link PatchOperation}.
   */
  private void singularValuedAttribute(final Attribute attribute, Map<String, Object> singularAttributeSource,
                                       final PatchOperation patchOperation) throws ScimException {
    log.info("Singular Valued attribute - Operation: {} {}", patchOperation.getOperation(), attributeLoggable(attribute));

    final Object oldValue = singularAttributeSource.get(attribute.getName());

    switch (patchOperation.getOperation()) {
      /*
       * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
       */
      case ADD:
      case REPLACE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.3
         */
        if (!singularAttributeSource.containsKey(attribute.getName())) {
          singularAttributeSource.put(attribute.getName(), patchOperation.getValue());
        } else { // otherwise, it does contain the attribute
          if (!checkValueEquals(oldValue, patchOperation.getValue())) {
            singularAttributeSource.replace(attribute.getName(), patchOperation.getValue());
          } else {
            log.info(NO_CHANGE, patchOperation.getOperation(), attribute.getName());
          }
        }
        break;
      /*
       * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.2
       */
      case REMOVE:
        singularAttributeSource.remove(attribute.getName());
        break;
    }
  }
}
