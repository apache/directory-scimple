package edu.psu.swe.scim.spec.protocol.attribute;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class AttributeReferenceListWrapper {

  @Setter(AccessLevel.NONE)
  private Set<AttributeReference> attributeReferences = new HashSet<>();
  
  public AttributeReferenceListWrapper(String attributeReferencesString) {

    String[] split = StringUtils.split(attributeReferencesString, ",");

    for (String af : split) {
      log.debug("--> Attribute -> " + af);
      AttributeReference attributeReference = new AttributeReference(af.trim());
      attributeReferences.add(attributeReference);
    }
  }
  
  public static AttributeReferenceListWrapper of(Set<AttributeReference> attributeReferences) {
    AttributeReferenceListWrapper wrapper = new AttributeReferenceListWrapper("");
    wrapper.attributeReferences = attributeReferences;
    return wrapper;
  }
}
