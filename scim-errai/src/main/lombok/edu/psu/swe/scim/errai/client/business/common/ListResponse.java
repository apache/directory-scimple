/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.common;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO that represents the result of a SCIM query on the wire.  See
 * setcion 3.4.2 of the SCIM Protocol Specification at:
 * 
 * https://tools.ietf.org/html/rfc7644#section-3.4.2
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class ListResponse<T> {
  
  int itemsPerPage = 0;
  List<T> Resources; 
  int startIndex = 0;
  int totalResults = 0;
  List<String> schemas;

}
