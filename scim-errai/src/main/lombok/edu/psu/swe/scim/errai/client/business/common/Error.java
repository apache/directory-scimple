/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.common;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO that represents the SCIM error while on the wire.  See
 * section 3.12 of the SCIM Protocol Specification at:
 * 
 * https://tools.ietf.org/html/rfc7644#section-3.12
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class Error {
  
  public enum Type {
    
    invalidFilter,
    tooMany,
    uniqueness,
    mutability,
    invalidSyntax,
    invalidPath,
    noTarget,
    invalidValue,
    invalidVers,
    sensitive;
    
  }
  
  int status = 400;
  Type scimType;
  String detail;

}
