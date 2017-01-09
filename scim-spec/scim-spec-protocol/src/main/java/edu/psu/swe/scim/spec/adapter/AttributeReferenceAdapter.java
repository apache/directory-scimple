package edu.psu.swe.scim.spec.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

public class AttributeReferenceAdapter extends XmlAdapter<String, AttributeReference> {

  @Override
  public AttributeReference unmarshal(String string) throws Exception {
    if (string == null) {
      return null;
    }
    return new AttributeReference(string);
  }

  @Override
  public String marshal(AttributeReference attributeReference) throws Exception {
    if (attributeReference == null) {
      return null;
    }
    return attributeReference.getFullyQualifiedAttributeName();
  }


}
