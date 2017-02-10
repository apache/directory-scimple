package edu.psu.swe.scim.errai.client.models;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO that describes the AuthenticationScheme sub-object of the SCIM
 * ServiceProviderConfig according to section 5 of the SCIM Schema
 * Specification.  See:
 * 
 * https://tools.ietf.org/html/rfc7643#section-5
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class AuthenticationSchemeConfig {
  
  public enum Type {
    
  }
  
  Type type;
  String name;
  String description;
  String specUri;
  String documentationUri;

}
