/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.resourcetype;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO that represents the wire format of a SCIM ResourceType according
 * to section 6 of the SCIM Protocol Specification.  See:
 * 
 * https://tools.ietf.org/html/rfc7643#section-6
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class ResourceType {
	
	String id;
	String name;
	String description;
	String endpoint;
	String schema;
	List<SchemaExtension> schemaExtensions;

}
