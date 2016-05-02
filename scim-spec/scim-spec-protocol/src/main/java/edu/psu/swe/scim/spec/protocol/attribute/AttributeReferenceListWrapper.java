package edu.psu.swe.scim.spec.protocol.attribute;

import java.util.HashSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

@Data
public class AttributeReferenceListWrapper {

  @Setter(AccessLevel.NONE)
  private Set<AttributeReference> attributeReferences = new HashSet<>();
  
  public AttributeReferenceListWrapper(String attributeReferencesString) {

    String[] split = StringUtils.split(attributeReferencesString, ",");

    for (String af : split) {
      AttributeReference attributeReference = new AttributeReference(af.trim());
      attributeReferences.add(attributeReference);
    }
    
  }

}
