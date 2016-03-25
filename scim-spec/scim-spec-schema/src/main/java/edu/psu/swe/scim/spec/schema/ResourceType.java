package edu.psu.swe.scim.spec.schema;

import java.util.List;

import edu.psu.swe.scim.spec.resources.ScimResourceWithOptionalId;

public class ResourceType extends ScimResourceWithOptionalId {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

  public class SchemaExtentionConfiguration {
    Schema schema;
    boolean required;
  }
  
  String id;
  
  String name;
  
  String description;
  
  String endpoint;
  
  Schema schema;
  
  List<SchemaExtentionConfiguration> schemaExtensions;
}
