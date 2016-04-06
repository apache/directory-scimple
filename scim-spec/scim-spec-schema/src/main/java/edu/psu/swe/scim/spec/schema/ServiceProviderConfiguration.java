package edu.psu.swe.scim.spec.schema;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import lombok.EqualsAndHashCode;
import edu.psu.swe.scim.spec.resources.ScimResourceWithOptionalId;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceProviderConfiguration extends ScimResourceWithOptionalId {

  public static final String RESOURCE_NAME = "ServiceProviderConfig";
  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";

  @Data
  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  public static class AuthenticationSchema {

    public enum Type {
      @XmlEnumValue("oauth")
      OAUTH, @XmlEnumValue("oauth2")
      OAUTH2, @XmlEnumValue("oauthbearertoken")
      OAUTH_BEARER, @XmlEnumValue("httpbasic")
      HTTP_BASIC, @XmlEnumValue("httpdigest")
      HTTP_DIGEST;
    }

    @XmlElement
    Type type;

    @XmlElement
    String name;

    @XmlElement
    String description;

    @XmlElement
    String specUri;

    @XmlElement
    String documentationUri;

  }

  @Data
  public static class SupportedConfiguration {
    boolean supported;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class BulkConfiguration extends SupportedConfiguration {
    int maxOperations;
    int maxPayloadSize;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  public static class FilterConfiguration extends SupportedConfiguration {
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

  @Override
  public String getResourceType() {
    return RESOURCE_NAME;
  }

}
