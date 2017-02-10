package edu.psu.swe.scim.errai.client.models;

import org.jboss.errai.common.client.api.annotations.Portable;

import edu.psu.swe.scim.errai.client.business.common.Meta;
import edu.psu.swe.scim.errai.client.business.schema.Schema;
import lombok.Data;

/**
 * A DTO that represents the wire format of a SCIM ServiceProviderConfig
 * according to section 5 of the SCIM Schema Specification.  See:
 * 
 * https://tools.ietf.org/html/rfc7643#section-5
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class ServiceProviderConfig {
  
  Schema[] schemas;
  Meta meta;
  
  String documentationUri;
  PatchConfig patch;
  BulkConfig bulk;
  FilterConfig filter;
  ChangePasswordConfig changePassword;
  SortConfig sort;
  EtagConfig etag;
  AuthenticationSchemeConfig[] authenticationSchemes;

}
