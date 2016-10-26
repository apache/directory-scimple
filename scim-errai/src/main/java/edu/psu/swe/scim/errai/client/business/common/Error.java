/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.common;

import lombok.Data;

/**
 * @author smoyer1
 *
 */
@Data
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
  
  int status = 200;
  Type scimType;
  String detail;

}
