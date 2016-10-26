/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.common;

import java.util.List;

import lombok.Data;

/**
 * @author smoyer1
 *
 */
@Data
public class ListResponse<T> {
  
  int itemsPerPage = 0;
  List<T> Resources; 
  int startIndex = 0;
  int totalResults = 0;

}
