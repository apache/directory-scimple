package edu.psu.swe.scim.spec.protocol.data;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StatusAdapter extends XmlAdapter<String, Status> {

  @Override
  public Status unmarshal(String v) throws Exception {
    if (v == null) {
      return null;
    }

    for (Status status : Status.values()) {
      if (status.getStatusCode() == Integer.valueOf(v)) {
        return status;
      }
    }
    throw new EnumConstantNotPresentException(Status.class, v);
  }

  @Override
  public String marshal(Status v) throws Exception {
    if (v == null) {
      return null;
    }
    return Integer.toString(v.getStatusCode());
  }

}