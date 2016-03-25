package edu.psu.swe.scim.spec.schema;

import java.util.List;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import edu.psu.swe.scim.spec.resources.ScimResourceWithOptionalId;
import edu.psu.swe.scim.spec.validator.Urn;
import lombok.Data;

@Data
public class ResourceType extends ScimResourceWithOptionalId {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

  @Data
  public class SchemaExtentionConfiguration {
    
    @XmlElement(name = "schema")
    @Urn
    @Size(min = 1)
    String schemaUrn;
    
    @XmlElement(name = "required")
    boolean required;
  }
  
  String id;
  
  String name;
  
  String description;
  
  String endpoint;
  
  @XmlElement(name = "schema")
  @Urn
  @Size(min = 1)
  String schemaUrn;
  
  List<SchemaExtentionConfiguration> schemaExtensions;
}
