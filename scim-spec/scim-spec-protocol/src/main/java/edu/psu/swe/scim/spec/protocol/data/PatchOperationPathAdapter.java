package edu.psu.swe.scim.spec.protocol.data;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PatchOperationPathAdapter extends XmlAdapter<String, PatchOperationPath>{

  @Override
  public PatchOperationPath unmarshal(String v) throws Exception {
    if (v == null) {
      return null;
    }
    return new PatchOperationPath();
  }

  @Override
  public String marshal(PatchOperationPath v) throws Exception {
    if (v == null) {
      return null;
    }
    return v.toString();  
  }

}
