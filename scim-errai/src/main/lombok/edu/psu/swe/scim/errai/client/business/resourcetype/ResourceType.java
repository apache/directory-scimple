/**
 * 
 */
package edu.psu.swe.scim.errai.client.business.resourcetype;

import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

import edu.psu.swe.scim.errai.client.business.common.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A DTO that represents the wire format of a SCIM ResourceType according
 * to section 6 of the SCIM Schema Specification.  See:
 * 
 * https://tools.ietf.org/html/rfc7643#section-6
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Portable
public class ResourceType extends Resource {
	
	String name;
	String description;
	String endpoint;
	String schema;
	List<SchemaExtension> schemaExtensions;

}
