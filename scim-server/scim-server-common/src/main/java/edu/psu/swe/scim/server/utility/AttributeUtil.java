package edu.psu.swe.scim.server.utility;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import edu.psu.swe.scim.server.exception.AttributeDoesNotExistException;
import edu.psu.swe.scim.server.schema.Registry;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimResource;
import edu.psu.swe.scim.spec.resources.ScimUser;
import edu.psu.swe.scim.spec.schema.AttributeContainer;
import edu.psu.swe.scim.spec.schema.Schema;
import edu.psu.swe.scim.spec.schema.Schema.Attribute;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Type;

@Stateless
public class AttributeUtil {

  @Inject
  Registry registry;

  public <T extends ScimResource> T setAttributesForDisplay(T resource, String attributes) {
    List<AttributeReference> attributesReferences = parseAttributeString(attributes);

    if (StringUtils.isEmpty(attributes)) {
      // TODO return always and default, exclude never
    }

    // TODO return always and specified attributes, exclude never

    return resource;
  }

  public <T extends ScimResource> T setExcludedAttributesForDisplay(T resource, String excludedAttributes) {
    List<AttributeReference> attributesReferences = parseAttributeString(excludedAttributes);

    if (StringUtils.isEmpty(excludedAttributes)) {
      // TODO return always and default, exclude never
    }

    // TODO return always and default, exclude never and specified attributes

    return resource;
  }

  private static List<AttributeReference> parseAttributeString(String s) {
    List<AttributeReference> list = new ArrayList<>();
    String[] split = StringUtils.split(s, ",");

    for (String af : split) {
      list.add(new AttributeReference(af));
    }
    return list;
  }

  private List<Attribute> getAttributesOfType(AttributeContainer attributeContainer, Returned returned) {
    List<Attribute> attributesOfType = new ArrayList<>();
    for (Attribute attribute : attributeContainer.getAttributes()) {
      if (returned == attribute.getReturned()) {
        attributesOfType.add(attribute);
      }
      if (attribute.getType() == Type.COMPLEX) {
        attributesOfType.addAll(getAttributesOfType(attribute, returned));
      }
    }
    return attributesOfType;
  }
  
  private List<Attribute> getAttributes(List<AttributeReference> attributeReferences) throws AttributeDoesNotExistException {
    List<Attribute> attributes = new ArrayList<>();

    for (AttributeReference attributeReference : attributeReferences) {
      attributes.add(findAttribute(attributeReference));
    }
    return attributes;
  }
  
  private Attribute findAttribute(AttributeReference attributeReference) throws AttributeDoesNotExistException {
    String schemaUrn = attributeReference.getUrn();
    String[] attributeNames = attributeReference.getAttributeName();
    Schema schema = null;
    
    if (!StringUtils.isEmpty(schemaUrn)) {
      schema = registry.getSchema(schemaUrn);

      Attribute attribute = findAttributeInSchema(schema, attributeNames);
      if (attribute == null) {
        throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
      }
      return attribute;
    }

    // Handle unqualified attributes, look in the core schemas
    schema = registry.getSchema(ScimUser.SCHEMA_URI);
    Attribute attribute = findAttributeInSchema(schema, attributeNames);
    if (attribute != null) {
      return attribute;
    }
    
    schema = registry.getSchema(ScimGroup.SCHEMA_URI);
    attribute = findAttributeInSchema(schema, attributeNames);
    if (attribute != null) {
      return attribute;
    }

    throw new AttributeDoesNotExistException(attributeReference.getFullyQualifiedAttributeName());
  }

  private Attribute findAttributeInSchema(Schema schema, String[] attributeNames) throws AttributeDoesNotExistException {
    AttributeContainer attributeContainer = schema;

    for (String attributeName : attributeNames) {
      attributeContainer = attributeContainer.getAttribute(attributeName);
      if (attributeContainer == null) {
        break;
      }
    }
    return (Attribute) attributeContainer;
  }

}
