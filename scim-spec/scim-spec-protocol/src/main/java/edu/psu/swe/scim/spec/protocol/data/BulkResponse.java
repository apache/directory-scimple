package edu.psu.swe.scim.spec.protocol.data;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@Data
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class BulkResponse {

  public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:BulkResponse";

  @XmlElement(name = "Operations")
  List<BulkOperation> operations;

  @XmlElement(name="status", nillable=true)
  String status;
  
  @XmlElement(name="response", nillable=true)
  ErrorResponse errorResponse;
}
