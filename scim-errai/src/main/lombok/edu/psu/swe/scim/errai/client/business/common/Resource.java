package edu.psu.swe.scim.errai.client.business.common;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * Provides the common attributes for SCIM resources as defined in section
 * 3.1 of the SCIM Schema specification.
 * 
 * https://tools.ietf.org/html/rfc7643#section-3.1
 * 
 * @author  Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public abstract class Resource {

  String id;
  String externalId;
  Meta meta;
  
}
