package edu.psu.swe.scim.errai.client.business.resourcetype;

import org.jboss.errai.common.client.api.annotations.Portable;

import lombok.Data;

/**
 * A DTO that represents the wire format of a SCIM ResourceType's SchemaExtension
 * according to section 6 of the SCIM Schema Specification.  See:
 * 
 * https://tools.ietf.org/html/rfc7643#section-6
 * 
 * @author Steve Moyer &lt;smoyer@psu.edu&gt;
 */
@Data
@Portable
public class SchemaExtension {
	
	String schema;
	boolean required;

}
