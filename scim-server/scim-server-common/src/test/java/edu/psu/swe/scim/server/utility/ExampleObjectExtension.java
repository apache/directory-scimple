package edu.psu.swe.scim.server.utility;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;
import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.extension.EnterpriseExtension;
import edu.psu.swe.scim.spec.extension.EnterpriseExtension.Manager;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Mutability;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Returned;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(required = false, name = "ExampleObject", id = ExampleObjectExtension.URN, description = "Example Object Extensions.")
@Data
public class ExampleObjectExtension implements ScimExtension {

  private static final long serialVersionUID = -5398090056271556423L;

  public static final String URN = "urn:ietf:params:scim:schemas:extension:example:2.0:Object";

  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  @Data
  public static class ComplexObject implements Serializable {

    private static final long serialVersionUID = 2822581434679824690L;

    @ScimAttribute(description = "The \"id\" of the complex object.")
    @XmlElement
    private String value;

    @ScimAttribute(mutability = Mutability.READ_ONLY, description = "displayName of the object.")
    @XmlElement
    private String displayName;
  }

  @ScimAttribute(returned = Returned.ALWAYS)
  @XmlElement
  private String valueAlways;

  @ScimAttribute(returned = Returned.DEFAULT)
  @XmlElement
  private String valueDefault;

  @ScimAttribute(returned = Returned.NEVER)
  @XmlElement
  private String valueNever;

  @ScimAttribute(returned = Returned.REQUEST)
  @XmlElement
  private String valueRequest;
  
  @ScimAttribute(returned = Returned.REQUEST)
  @XmlElement
  private ComplexObject valueComplex;
  
  @ScimAttribute
  @XmlElement
  private List<String> list;


  @Override
  public String getUrn() {
    return URN;
  }
}
