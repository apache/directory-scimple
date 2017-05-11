package edu.psu.swe.scim.spec.protocol.data;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.psu.swe.scim.spec.protocol.ErrorMessageType;
import edu.psu.swe.scim.spec.resources.BaseResource;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ErrorResponse extends BaseResource {

  private static final long serialVersionUID = 9045421198080348116L;

  public static String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:Error";

  @XmlElement(nillable = true)
  private String detail;

  @XmlElement
  @XmlJavaTypeAdapter(StatusAdapter.class)
  private Status status;

  @XmlElement
  private ErrorMessageType scimType;

  private List<String> errorMessageList;
  
  protected ErrorResponse() {
    super(SCHEMA_URI);
  }

  public ErrorResponse(Status status, String detail) {
    super(SCHEMA_URI);
    this.status = status;
    this.detail = detail;
  }
  
  public void addErrorMessage(String message) {
    if (errorMessageList == null) {
      errorMessageList = new ArrayList<>();
    }
    
    errorMessageList.add(message);
  }
  
  public Response toResponse() {
    if (errorMessageList != null) {
      StringBuilder sb = new StringBuilder();
      for (String s : errorMessageList) {
        sb.append("\n").append(s);
      }
      detail += sb.toString();
    }
    
    return Response.status(status).entity(this).build();
  }

}
