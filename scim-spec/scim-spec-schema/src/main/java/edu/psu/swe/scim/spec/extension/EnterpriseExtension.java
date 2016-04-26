package edu.psu.swe.scim.spec.extension;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import edu.psu.swe.scim.spec.annotation.ScimExtensionType;
import edu.psu.swe.scim.spec.resources.ScimExtension;
import edu.psu.swe.scim.spec.schema.Schema.Attribute.Mutability;
import lombok.Data;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@ScimExtensionType(required = false, name = "EnterpriseUser", id = EnterpriseExtension.URN, description = "Attributes commonly used in representing users that belong to, or act on behalf of, a business or enterprise.")
@Data
public class EnterpriseExtension implements ScimExtension {

  public static final String URN = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";

  @XmlType
  @XmlAccessorType(XmlAccessType.NONE)
  @Data
  public static class Manager {

    @ScimAttribute(description = "The \"id\" of the SCIM resource representing the user's manager.  RECOMMENDED.")
    @XmlElement
    private String value;

    @ScimAttribute(description = "The URI of the SCIM resource representing the User's manager.  RECOMMENDED.")
    @XmlElement
    private String $ref;

    @ScimAttribute(mutability = Mutability.READ_ONLY, description = "he displayName of the user's manager.  This attribute is OPTIONAL.")
    @XmlElement
    private String displayName;
  }

  @ScimAttribute(description = "A string identifier, typically numeric or alphanumeric, assigned to a person, typically based on order of hire or association with an organization.")
  @XmlElement(nillable = true)
  private String employeeNumber;

  @ScimAttribute(description = "Identifies the name of a cost center.")
  @XmlElement
  private String costCenter;

  @ScimAttribute(description = "Identifies the name of an organization.")
  @XmlElement
  private String organization;

  @ScimAttribute(description = "Identifies the name of a division.")
  @XmlElement
  private String division;

  @ScimAttribute(description = "Identifies the name of a department.")
  @XmlElement
  private String department;

  @ScimAttribute(description = "The user's manager.  A complex type that optionally allows service providers to represent organizational hierarchy by referencing the \"id\" attribute of another User.")
  @XmlElement
  private Manager manager;

  @Override
  public String getUrn() {
    return URN;
  }
}
