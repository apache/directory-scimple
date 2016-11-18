/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.common;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO the provides the wire format of the "meta" section sent with each
 * SCIM resource according to section 3.1 of the SCIM Schema Specification.
 * 
 * https://tools.ietf.org/html/rfc7643#section-3.1
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class Meta {
	
	String resourceType;
	String created; // DateTime
	String lastModified; // DateTime
	String location;
	String version;

}
