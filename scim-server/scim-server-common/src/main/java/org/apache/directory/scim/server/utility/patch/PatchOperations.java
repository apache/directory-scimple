package org.apache.directory.scim.server.utility.patch;

import static java.util.Objects.requireNonNull;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.schema.Schema.Attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.antlr.v4.runtime.misc.Pair;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.schema.Schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.extern.slf4j.Slf4j;

@Stateless
@Slf4j
public class PatchOperations {
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
  };

  private static final Map<PatchOperation.Type, Set<String>> UNSUPPORTED =
    ImmutableMap.of(REMOVE, ImmutableSet.of("active"));

  private final ObjectMapper objectMapper;

  @Inject
  Registry registry;

  @Inject
  public PatchOperations(Registry registry) {
    this.registry = registry;
    this.objectMapper = new ObjectMapperFactory(this.registry).createObjectMapper();
  }

  /**
   * @param object the {@link Object} to {@link Map}
   * @return Returns the {@link Map} representation of {@code object}, or a {@link ClassCastException} is thrown if
   * the {@code object} isn't representable as a {@link Map}
   */
  @SuppressWarnings("unchecked")
  static Map<String, Object> castToMap(final Object object) {
    requireNonNull(object, "object must not be null.");

    if (object instanceof Map) {
      return (Map<String, Object>) object;
    }

    throw new ClassCastException(String.format("Parameter \"value\" isn't a Map, its \"%s\"",
      object.getClass().getName()));
  }

  @SuppressWarnings( "unchecked" )
  static List<Map<String,Object>> castToList( final Object object) {
    requireNonNull(object, "object must not be null.");

    if (object instanceof List) {
      return (List<Map<String,Object>>) object;
    }

    throw new ClassCastException(String.format("Parameter \"value\" isn't a List, its \"%s\"",
            object.getClass().getName()));
  }

  static boolean isCollection(Class<?> clazz) {
    return Collection.class.isAssignableFrom(clazz);
  }

  /**
   * @param status      the HTTP {@link Response.Status}
   * @param messageType the {@link ErrorMessageType} type
   * @return Returns a populated {@link ScimException}
   */
  static ScimException throwScimException(final Response.Status status, final ErrorMessageType messageType) {
    final ErrorResponse errorResponse = new ErrorResponse(status, messageType.getDetail());
    errorResponse.setScimType(messageType);

    return new ScimException(errorResponse, status);
  }

  /**
   * @param source          the resource to be patched
   * @param patchOperations the {@link List} of {@link PatchOperation}s to be applied to the given SCIM resource
   * @return Returns the patched resource
   * @throws ScimException if any errors occur while trying to patch the supplied resource
   */
  @SuppressWarnings("unchecked")
  public <T extends ScimResource> T apply(T source, final List<PatchOperation> patchOperations) throws ScimException {
    T scimResource = SerializationUtils.clone(source);

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
        throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
      } else {
        scimResource = apply(scimResource, it);
      }
    }

    return scimResource;
  }

  /**
   * @param source         the resource to be patched
   * @param patchOperation the {@link PatchOperation} to be applied to the given SCIM resource
   * @return Returns the patched resource
   * @throws ScimException if any errors occur while trying to patch the supplied resource
   */
  private <T extends ScimResource> T apply(T source, final PatchOperation patchOperation) throws ScimException {
    T target;

    String path = Objects.nonNull(patchOperation.getPath()) ? patchOperation.getPath().toString():"";

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

          return applyWithValueFilter(source, patchOperation, valSelFilter, attribute, subAttribute);
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

    return target;
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
   * @param scimResourceAsMap the {@link Map} representing the SCIM resource
   * @param clazz             the {@link ScimResource} class
   * @return Returns a {@link ScimResource} representing the {@code scimResourceAsMap}
   */
  private <T extends ScimResource> T mapAsResource(final Map<String, Object> scimResourceAsMap, final Class<T> clazz) {
    return objectMapper.convertValue(scimResourceAsMap, clazz);
  }

  /**
   * @param scimRExtensionAsMap the {@link Map} representing the SCIM extension
   * @param clazz               the {@link ScimResource} class
   * @return Returns a {@link ScimExtension} representing the {@code scimRExtensionAsMap}
   */
  private <T extends ScimExtension> T mapAsExtension(final Map<String, Object> scimRExtensionAsMap, final Class<T> clazz) {
    return objectMapper.convertValue(scimRExtensionAsMap, clazz);
  }

  /**
   * @param scimResource the {@link ScimResource} representing the SCIM resource
   * @return Returns a {@link Map} representing the {@link ScimResource}
   */
  private <T extends ScimResource> Map<String, Object> resourceAsMap(final T scimResource) {
    return objectMapper.convertValue(scimResource, MAP_TYPE);
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
    Map<String, Object> fromMap = resourceAsMap(resource);
    List<Map<String, Object>> list = null;

    final Attribute parentAttribute = schema.getAttribute(attribute);
    Map<String, Object> resourceAsMap = resourceAsMap(resource);
    if (Objects.nonNull(parentAttribute)) {
      if (parentAttribute.isMultiValued() && parentAttribute.getType().equals(Attribute.Type.COMPLEX)) {
        Object object = fromMap.getOrDefault(attribute, null);
        list = (object==null)
          ? null
          : new ArrayList<>((Collection<Map<String, Object>>) object);
      } else {
        throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
      }
    }

    if (Objects.isNull(list) || list.isEmpty()) {
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
    } else {
      List<Integer> matchingIndexes = new ArrayList<>();
      for (int i = 0; i < list.size(); i++) {
        if (FilterMatchUtil.complexAttributeMatch(parentAttribute, list.get(i), operation)) {
          matchingIndexes.add(0, i);
        }
      }

      /*
       * see section 3.5.2.3/4 of RFC7644
       *
       * If the target location is a multi-valued attribute for which a value selection filter ("valuePath") has been
       * supplied and no record match was made, the service provider SHALL indicate failure by returning HTTP status
       * code 400 and a "scimType" error code of "noTarget".
       */
      if (matchingIndexes.isEmpty()) {
        throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.NO_TARGET);
      }

      log.info("There are {} entries matching the filter '{}'", matchingIndexes.size(), operation.getPath());

      for (Integer index : matchingIndexes) {
        if (operation.getOperation().equals(REMOVE)) {
          if (Objects.isNull(subAttribute) || subAttribute.isEmpty()) {
            // Remove the whole item
            list.remove(index.intValue());  // If intValue is not used, the remove(Object) method is
          } else {    // remove sub-attribute only
            list.get(index).remove(subAttribute);
          }
        } else {
          applyPartialUpdate(parentAttribute, parentAttribute.getAttribute(subAttribute),
            list, index, operation.getValue());
        }
      }

      resourceAsMap.put(attribute, list.size()==0 ? null:list);
    }

    return (T) mapAsResource(resourceAsMap, resource.getClass());
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

    if (Objects.isNull(subAttribute)) {
      /*
       * Updates the whole item in the list after passing mutability check, see section 3.5.2 RFC 7644:
       *
       * Each operation against an attribute MUST be compatible with the attribute's mutability and schema ...
       */
      Map<String, Object> map = (Map<String, Object>) value;

      for (String subAttr : map.keySet()) {
        log.debug("Attribute: {} SubAttribute: {}", attribute.getName(), subAttr);
        checkMutability(attribute);
      }

      list.set(index, map);
    } else {
      log.debug("Full Attribute {}.{}", attribute.getName(), subAttribute.getName());
      checkMutability(attribute);
      list.get(index).put(subAttribute.getName(), value);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T patchAdd(final PatchOperation patchOperation, T source) throws ScimException {

    log.info("Applying Patch Operation '{}' for attribute '{}'",
      patchOperation.getOperation(), patchOperation.getPath().toString());

    checkSchema(patchOperation, this.registry.getAllSchemas());
    checkValue(patchOperation);

    Map<String, Object> sourceAsMap = resourceAsMap(source);

    addOrReplace(source, sourceAsMap, patchOperation);

    return (T) mapAsResource(sourceAsMap, source.getClass());
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T patchReplace(final PatchOperation patchOperation, T source) throws ScimException {
    log.info("Applying Patch Operation '{}' for attribute '{}'",
      patchOperation.getOperation(), patchOperation.getPath().toString());

    checkSchema(patchOperation, this.registry.getAllSchemas());

    Map<String, Object> sourceAsMap = resourceAsMap(source);

    addOrReplace(source, sourceAsMap, patchOperation);

    return (T) mapAsResource(sourceAsMap, source.getClass());
  }

  @SuppressWarnings("unchecked")
  private <T extends ScimResource> T patchRemove(final PatchOperation patchOperation,
                                                 T source) throws ScimException {
    final AttributeReference attributeReference = attributeReference(patchOperation);

    log.info("Applying Patch Operation '{}' for attribute '{}'",
      patchOperation.getOperation(),
      patchOperation.getPath().toString());

    final Schema schema = checkSchema(patchOperation, this.registry.getAllSchemas());

    checkTarget(patchOperation);
    checkRequired(patchOperation, schema);
    checkSupported(patchOperation, schema);

    return (T) mapAsResource(remove(source, attributeReference), source.getClass());
  }

  private <T extends ScimResource> Map<String, Object> remove(T resource, final AttributeReference reference) {
    Map<String, Object> sourceAsMap = resourceAsMap(resource);

    // are we dealing with Scim Extension?
    if (Objects.nonNull(reference.getUrn())) {
      final List<ScimExtension> scimExtensions = getExtensions(resource);
      removeExtension(sourceAsMap, reference, scimExtensions);
    } else {
      remove(sourceAsMap, reference);
    }

    return sourceAsMap;
  }

  @SuppressWarnings("unchecked")
  private void remove(Map<String, Object> sourceAsMap, final AttributeReference reference) {
    String path = reference.getFullAttributeName();

    int i = path.indexOf(".");
    Object value = null;

    if (i==-1) {
      sourceAsMap.remove(path);
    } else {
      String key = path.substring(0, i);
      value = sourceAsMap.get(key);
      path = path.substring(i + 1);
    }

    if (value!=null) {
      try {
        // If it's a map we must recurse
        remove(castToMap(value), new AttributeReference(path));
      } catch (Exception e) {
        if (isCollection(value.getClass())) {
          Collection<Object> col = (Collection<Object>) value;
          for (Object item : col) {
            if (item instanceof Map) {
              remove(castToMap(item), new AttributeReference(path));
            }
          }
        }
      }
    }
  }

  private void removeExtension(Map<String, Object> source,
                               final AttributeReference reference,
                               final List<ScimExtension> extensions) {
    String path = reference.getFullyQualifiedAttributeName();

    extensions.stream()
      .map(ScimExtension::getUrn)
      .filter(path::startsWith)
      .forEach(urn -> {
        Object extensionObject = source.get(urn);
        Map<String, Object> processedExtension = new HashMap<>();

        if (extensionObject instanceof Map) {
          Map<String, Object> extensionMap = castToMap(extensionObject);
          processedExtension.putAll(extensionMap);

          if (Objects.isNull(reference.getSubAttributeName())) {
            processedExtension.remove(reference.getAttributeName());
          } else /* reference.getSubAttributeName() != null */ {
            Object subExtensionObject = extensionMap.get(reference.getAttributeName());
            if (subExtensionObject instanceof Map) {
              Map<String, Object> subExtensionMap =
                new HashMap<>(castToMap(subExtensionObject));
              subExtensionMap.remove(reference.getSubAttributeName());

              processedExtension.replace(reference.getAttributeName(),
                subExtensionObject,
                subExtensionMap);
            }
          }
        }

        source.replace(urn, extensionObject, processedExtension);
      });
  }

  @SuppressWarnings( "unchecked" )
  private <T extends ScimResource> void addOrReplace( final T resource, Map<String, Object> source, final PatchOperation patchOperation) {
    final AttributeReference attributeReference = attributeReference(patchOperation);

    // are we dealing with Scim Extension?
    if (Objects.nonNull(attributeReference.getUrn())) {
      final List<ScimExtension> scimExtensions = getExtensions(resource);
      addOrReplaceExtension(resource.getClass(), source, attributeReference,
        scimExtensions, patchOperation.getValue());
    } else {
      Object oldValue = source.getOrDefault(attributeReference.getAttributeName(), null);
      Object newValue = patchOperation.getValue();

      if (Objects.nonNull(attributeReference.getSubAttributeName())) {
        if (Objects.nonNull(oldValue) && oldValue instanceof Map) {
          Map<String, Object> subSourceMap = castToMap(oldValue);
          addOrReplace(subSourceMap, attributeReference, newValue);
          if (!source.containsKey(attributeReference.getAttributeName())) {
            source.put(attributeReference.getAttributeName(), subSourceMap);
          } else {
            source.replace(attributeReference.getAttributeName(), subSourceMap);
          }
        } else if (newValue instanceof Map) {
          Map<String, Object> subSourceMap = castToMap(newValue);
          if (!source.containsKey(attributeReference.getAttributeName())) {
            source.put(attributeReference.getAttributeName(), subSourceMap);
          } else {
            source.replace(attributeReference.getAttributeName(), subSourceMap);
          }
        } else {
          Map<String, Object> subSourceMap = new HashMap<>();
          subSourceMap.put(attributeReference.getSubAttributeName(), newValue);
          source.put(attributeReference.getAttributeName(), subSourceMap);
        }
      } else {
        if (!Objects.equals(oldValue, newValue)) {
          if (newValue instanceof Map) {
            if (!source.containsKey(attributeReference.getAttributeName())) {
              source.put(attributeReference.getAttributeName(), castToMap(newValue));
            } else {
              source.replace(attributeReference.getAttributeName(), castToMap(newValue));
            }
          } else {
            if (!source.containsKey(attributeReference.getAttributeName())) {
              source.put(attributeReference.getAttributeName(), newValue);
            } else {
              Object existing = source.get(attributeReference.getAttributeName());
              if(existing instanceof List) {
                List<Map<String,Object>> asList = (List<Map<String,Object>>) existing;
                if(newValue instanceof List) {
                  asList.addAll(castToList( newValue));
                } else { // expecting a Map
                  asList.add( castToMap( newValue ) );
                }
                source.replace( attributeReference.getAttributeName(), asList);
              } else {
                source.replace( attributeReference.getAttributeName(), newValue );
              }
            }
          }
        }
      }
    }
  }

  private void addOrReplace(Map<String, Object> subSource, final AttributeReference attributeReference, Object newValue) {
    if (attributeReference.getSubAttributeName()!=null) {
      Object oldValue = subSource.getOrDefault(attributeReference.getSubAttributeName(), null);
      if (Objects.isNull(oldValue) || !Objects.equals(oldValue, newValue)) {
        subSource.put(attributeReference.getSubAttributeName(), newValue);
      }
    } else {
      Object oldValue = subSource.getOrDefault(attributeReference.getAttributeName(), null);
      if (Objects.isNull(oldValue) || !Objects.equals(oldValue, newValue)) {
        subSource.put(attributeReference.getAttributeName(), newValue);
      }
    }
  }

  private void addOrReplaceExtension(Class<? extends ScimResource> resourceClass,
                                     Map<String, Object> source, final AttributeReference reference,
                                     List<ScimExtension> extensions, final Object newValue) {
    String path = reference.getFullyQualifiedAttributeName();

    if (extensions.isEmpty() && Objects.nonNull(reference.getUrn())) {
      /*
       * no extension exists, so dummy one up to populate with add/replace values
       */
      Class<? extends ScimExtension> clazz =
        ScimExtensionRegistry.getInstance().getExtensionClass(resourceClass, reference.getUrn());
      Map<String, Object> extensionAsMap = new HashMap<>();
      if (Objects.nonNull(reference.getSubAttributeName()) && Objects.nonNull(reference.getAttributeName())) {
        // add sub-complex extension
        extensionAsMap.put(reference.getAttributeName(), new HashMap<>());
      }

      source.put(reference.getUrn(), extensionAsMap);
      extensions.add(mapAsExtension(extensionAsMap, clazz));
    }

    extensions.stream()
      .map(ScimExtension::getUrn)
      .filter(path::startsWith)
      .forEach(urn -> {
        Object extensionObject = source.get(urn);
        Map<String, Object> processedExtension = new HashMap<>();

        if (extensionObject instanceof Map) {
          Map<String, Object> extensionMap = castToMap(extensionObject);
          processedExtension.putAll(extensionMap);

          if (Objects.isNull(reference.getSubAttributeName())) {
            processedExtension.replace(reference.getAttributeName(), newValue);
          } else /* reference.getSubAttributeName() != null */ {
            Object subExtensionObject = extensionMap.get(reference.getAttributeName());

            if (subExtensionObject instanceof Map) {
              Map<String, Object> subExtensionMap = castToMap(subExtensionObject);
              if (subExtensionMap.containsKey(reference.getSubAttributeName())) {
                subExtensionMap.replace(reference.getSubAttributeName(), newValue);
              } else {
                subExtensionMap.put(reference.getSubAttributeName(), newValue);
              }

              processedExtension.replace(reference.getAttributeName(),
                subExtensionObject,
                subExtensionMap);
            }
          }
        }

        source.replace(urn, extensionObject, processedExtension);
      });
  }

  private AttributeReference attributeReference(final PatchOperation operation) {
    return Objects.requireNonNull(
      Objects.requireNonNull(
        Objects.requireNonNull(operation, "Patch operation must not be null")
          .getPath(), "Patch Operation Path must not be null")
        .getValuePathExpression(), "Value Path Expression must not be null")
      .getAttributePath();
  }

  /**
   * @param patchOperation a valid path in terms of PATCH operation for the case of value selection filter.
   * @return Returns {@link Pair} representing if the path has a filter expression and the selection filter
   */
  private Pair<Boolean, String> validateFilterPath(PatchOperation patchOperation) {
    String selFilter = null;
    String path = patchOperation.getPath().toString();
    int lBracketIndex = path.indexOf("[");
    // Check if characters preceding bracket look like attribute name
    boolean isFilterExpression = (lBracketIndex > 0) &&
      path.substring(0, lBracketIndex).matches("[a-zA-Z]\\w*");

    if (isFilterExpression) {
      int rBracketIndex = path.lastIndexOf("]");

      int length = path.length() - 1;
      /*
       * It will be valid if character ] is the last of string, or if it is followed by a dot and at least one
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
   * Check the mutability of an attribute.
   *
   * @param attribute Attribute.
   * @throws ScimException if the {@code Attribute} is {@link Attribute.Mutability#READ_ONLY} or {@link Attribute.Mutability#IMMUTABLE}
   */
  private void checkMutability(Attribute attribute) throws ScimException {
    if (attribute.getMutability().equals(Attribute.Mutability.READ_ONLY) ||
      attribute.getMutability().equals(Attribute.Mutability.IMMUTABLE)) {
      log.error("Can not update a immutable attribute or a read-only attribute '{}'", attribute.getName());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_FILTER);
    }
  }

  /**
   * Check if attribute is required.
   *
   * @param operation the {@link PatchOperation}.
   * @param schema    the {@link Schema}.
   * @throws ScimException if the {@code Attribute} is {@link Attribute.Mutability#READ_ONLY} or {@link Attribute.Mutability#IMMUTABLE}
   */
  private void checkRequired(final PatchOperation operation, final Schema schema) throws ScimException {
    final PathAttributePair pair = attributePair(operation, schema);
    if (pair.getAttribute().getMutability().equals(Attribute.Mutability.READ_ONLY) || pair.getAttribute().isRequired()) {
      log.error("Can not remove a required attribute or a read-only attribute '{}'.", pair.getPath());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.MUTABILITY);
    }
  }

  /**
   * Check the value.
   *
   * @param patchOperation the {@link PatchOperation}.
   * @throws ScimException if {@link PatchOperation#getValue()} is null
   */
  private void checkValue(PatchOperation patchOperation) throws ScimException {
    if (patchOperation.getOperation().equals(PatchOperation.Type.ADD) &&
      Objects.isNull(patchOperation.getValue())) {
      log.error("The value is required to perform patch '{}' operation on '{}'.",
        patchOperation.getOperation(), patchOperation.getPath().toString());

      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
    }
  }

  /**
   * Check the path.
   *
   * @param operation the {@link PatchOperation}.
   * @param schemas   a {@link Collection} of {@link Schema}s.
   *
   * @return Returns the {@link Schema} the {@code operation} is found in
   * @throws ScimException if operation type isn't supported for the given attribute
   */
  private Schema checkSchema(PatchOperation operation, final Collection<Schema> schemas) throws ScimException {
    AttributeReference reference = attributeReference(operation);
    if (reference!=null) {
      for (final Schema schema : schemas) {
        if (schema.getAttribute(reference.getAttributeName()) != null) {
          return schema;
        }
      }
    }

    log.error("Invalid attribute specified for {} operation.", operation.getOperation());
    throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_PATH);
  }

  /**
   * Check the target path.
   *
   * @param patchOperation the {@link PatchOperation}.
   * @throws ScimException if {@link PatchOperation#getPath()} is null
   */
  private void checkTarget(PatchOperation patchOperation) throws ScimException {
    if (patchOperation.getOperation().equals(REMOVE) &&
      Objects.isNull(patchOperation.getPath())) {
      log.error("No path value specified for {} operation.", patchOperation.getOperation());
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.NO_TARGET);
    }
  }

  /**
   * Check the attribute is supported for given patch operation.
   *
   * @param operation the {@link PatchOperation}.
   * @param schema    the {@link Schema}.
   * @throws ScimException if operation type isn't supported for the given attribute
   */
  private void checkSupported(PatchOperation operation, Schema schema) throws ScimException {
    final PathAttributePair pair = attributePair(operation, schema);
    if (UNSUPPORTED.getOrDefault(operation.getOperation(), ImmutableSet.of()).contains(pair.getPath())) {
      log.error("The operation type '{}' for attribute '{}' isn't supported", operation.getOperation(), pair.getPath());
      throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_SYNTAX);
    }
  }

  /**
   * @param operation the {@link PatchOperation}.
   * @param schema    the {@link Schema}.
   * @return Returns a populated {@link PathAttributePair} or {@code null} if attribute isn't found
   */
  private PathAttributePair attributePair(PatchOperation operation, final Schema schema) {
    AttributeReference reference = attributeReference(operation);
    Attribute attribute;

    if (Objects.nonNull(reference.getUrn())) {
      final Schema useThisSchema = this.registry.getSchema(reference.getUrn());
      if (Objects.nonNull(useThisSchema)) {
        attribute = useThisSchema.getAttribute(reference.getAttributeName());
      } else {
        attribute = schema.getAttribute(reference.getAttributeName());
      }

    } else {
      attribute = schema.getAttribute(reference.getAttributeName());
      if (Objects.nonNull(attribute)) {
        if (Objects.nonNull(reference.getSubAttributeName())) {
          attribute = attribute.getAttribute(reference.getSubAttributeName());
        }
      }
    }

    return new PathAttributePair(reference.getFullyQualifiedAttributeName(), attribute);
  }

  private <T extends ScimResource> List<ScimExtension> getExtensions(final T scimResource) {
    return new ArrayList<>(scimResource.getExtensions().values());
  }
}

