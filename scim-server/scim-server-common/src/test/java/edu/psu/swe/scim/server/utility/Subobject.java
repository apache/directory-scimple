package edu.psu.swe.scim.server.utility;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import lombok.Data;

@Data
public class Subobject implements Serializable {
  
  private static final long serialVersionUID = -8081556701833520316L;

  @ScimAttribute
  @XmlElement
  private String string1;
  
  @ScimAttribute
  @XmlElement
  private String string2;
  
  @ScimAttribute
  @XmlElement
  private Boolean boolean1;
  
  @ScimAttribute
  @XmlElement
  private Boolean boolean2;
  
  @ScimAttribute
  @XmlElement
  private List<String> list1;

  @ScimAttribute
  @XmlElement
  private List<String> list2;
}
