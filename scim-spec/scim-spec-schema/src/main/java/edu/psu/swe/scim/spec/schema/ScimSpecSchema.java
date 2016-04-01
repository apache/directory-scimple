package edu.psu.swe.scim.spec.schema;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.Getter;

@Data
public class ScimSpecSchema {
  
  @Getter
  static Set<String> schemaNames = new HashSet<>();
  
  static {
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:Group");
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:ResourceType");
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig");
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:User");
    schemaNames.add("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");
  }
}
