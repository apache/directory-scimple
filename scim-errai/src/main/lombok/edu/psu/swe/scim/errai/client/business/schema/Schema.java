/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.schema;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO representing the format of a SCIM Schema object on the wire.  See
 * section 7 of the SCIM Schema Specification at:
 * 
 * https://tools.ietf.org/html/rfc7643#section-7
 * 
 * Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class Schema {
  
  String id;
  String name;
  String description;
  Attribute[] attributes;

}
