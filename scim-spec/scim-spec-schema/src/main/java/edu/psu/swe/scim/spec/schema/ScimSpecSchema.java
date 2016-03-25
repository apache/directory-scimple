package edu.psu.swe.scim.spec.schema;

import java.util.HashSet;
import java.util.Set;

public class ScimSpecSchema {
  
  static Set<String> schemaNames = new HashSet<>();
  
  static {
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:Group");
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:ResourceType");
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig");
    schemaNames.add("urn:ietf:params:scim:schemas:core:2.0:User");
    schemaNames.add("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User");
  }
  
  private ScimSpecSchema(){
  }
  
  public static Set<String> getSchemaNameSet(){
    return schemaNames;
  }
}
