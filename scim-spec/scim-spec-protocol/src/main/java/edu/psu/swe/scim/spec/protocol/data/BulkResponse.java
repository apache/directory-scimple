package edu.psu.swe.scim.spec.protocol.data;

import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.psu.swe.scim.spec.resources.BaseResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BulkResponse extends BaseResource {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

  @XmlElement(name = "Operations")
  List<BulkOperation> operations;

  @XmlElement(name="status", nillable=true)
  @XmlJavaTypeAdapter(StatusAdapter.class)
  Status status;
  
  @XmlElement(name="response", nillable=true)
  ErrorResponse errorResponse;
  
  public BulkResponse() {
    super(SCHEMA_URI);
  }
}
