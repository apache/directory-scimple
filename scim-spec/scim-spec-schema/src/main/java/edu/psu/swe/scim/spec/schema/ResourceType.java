package edu.psu.swe.scim.spec.schema;

import java.util.List;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.psu.swe.scim.spec.resources.ScimResourceWithOptionalId;
import edu.psu.swe.scim.spec.validator.Urn;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SCIM ResourceType
 * 
 * @see <a href="https://tools.ietf.org/html/rfc7643#section-6">ResourceType Schema</a>
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceType extends ScimResourceWithOptionalId {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ResourceType";

  @Data
  public class SchemaExtentionConfiguration {

    @XmlElement(name = "schema")
    @Urn
    @Size(min = 1)
    String schemaUrn;

    @XmlElement
    boolean required;
  }

  @XmlElement
  String name;

  @XmlElement
  String description;

  @XmlElement
  String endpoint;

  @XmlElement(name = "schema")
  @Urn
  @Size(min = 1)
  String schemaUrn;

  List<SchemaExtentionConfiguration> schemaExtensions;
}
