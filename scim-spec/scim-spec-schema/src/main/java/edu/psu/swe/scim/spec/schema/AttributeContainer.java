package edu.psu.swe.scim.spec.schema;

import java.util.List;

import edu.psu.swe.scim.spec.schema.Schema.Attribute;

public interface AttributeContainer {
  List<Attribute> getAttributes();

  Attribute getAttribute(String attributeName);
}
