package edu.psu.swe.scim.api.schema;

import java.util.List;

public class ResourceType extends BaseResource {

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
