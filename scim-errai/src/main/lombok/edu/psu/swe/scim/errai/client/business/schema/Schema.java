/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.schema;

import lombok.Data;

/**
 * @author smoyer1
 *
 */
@Data
public class Schema {
  
  String id;
  String name;
  String description;
  Attribute[] attributes;

}
