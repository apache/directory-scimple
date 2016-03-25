package edu.psu.swe.scim.spec.schema;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.psu.swe.scim.spec.resources.ScimResourceWithOptionalId;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ServiceProviderConfiguration extends ScimResourceWithOptionalId {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig";
  
  public class SupportedConfiguration {
    boolean supported;
  }

  public class BulkConfiguration extends SupportedConfiguration {
    int maxOperations;
    int maxPayloadSize;
  }

  public class FilterConfiguration extends SupportedConfiguration {
    int maxResults;
  }

  String documentationUrl;

  SupportedConfiguration patch;

  BulkConfiguration bulk;

  FilterConfiguration filter;

  SupportedConfiguration changePassword;

  SupportedConfiguration sort;

  SupportedConfiguration etag;
  
  List<AuthenticationSchema> authenticationSchemes;

}
