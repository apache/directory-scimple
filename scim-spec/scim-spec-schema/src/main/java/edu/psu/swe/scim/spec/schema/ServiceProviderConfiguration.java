package edu.psu.swe.scim.spec.schema;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import edu.psu.swe.scim.spec.resources.ScimResourceWithOptionalId;
import edu.psu.swe.scim.spec.schema.AuthenticationSchema.Type;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceProviderConfiguration extends ScimResourceWithOptionalId {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";

  @Data
  public class SupportedConfiguration {
    boolean supported;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public class BulkConfiguration extends SupportedConfiguration {
    int maxOperations;
    int maxPayloadSize;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public class FilterConfiguration extends SupportedConfiguration {
    int maxResults;
  }

  @XmlElement
  String documentationUrl;

  @XmlElement
  SupportedConfiguration patch;

  @XmlElement
  BulkConfiguration bulk;

  @XmlElement
  FilterConfiguration filter;

  @XmlElement
  SupportedConfiguration changePassword;

  @XmlElement
  SupportedConfiguration sort;

  @XmlElement
  SupportedConfiguration etag;
  
  @XmlElement
  List<AuthenticationSchema> authenticationSchemes;
  
  public ServiceProviderConfiguration() {
    super(SCHEMA_URI);
  }

}
