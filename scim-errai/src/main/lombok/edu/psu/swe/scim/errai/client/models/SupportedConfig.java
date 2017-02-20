package edu.psu.swe.scim.errai.client.models;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * An abstract super-class for configuration objects that include
 * the supports sub-attribute within a sub-object of the SCIM
 * ServiceProviderConfig according to section 5 of the SCIM Schema
 * Specification.  See:
 * 
 * https://tools.ietf.org/html/rfc7643#section-5
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public abstract class SupportedConfig {
  
  boolean supported;

}
