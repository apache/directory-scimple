package edu.psu.swe.scim.server.provider.annotations;

import java.util.Set;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScimRequestContext {

  private Set<AttributeReference> attributeReferences;
  private Set<AttributeReference> excludedAttributeReferences;
  
}
