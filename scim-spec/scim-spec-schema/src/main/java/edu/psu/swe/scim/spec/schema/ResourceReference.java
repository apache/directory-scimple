package edu.psu.swe.scim.spec.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import edu.psu.swe.scim.spec.annotation.ScimAttribute;
import lombok.Data;

@Data
@XmlType(propOrder = {"value","ref","display"})
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceReference {

  @XmlEnum
  public enum ReferenceType {
    @XmlEnumValue("direct") DIRECT,
    @XmlEnumValue("indirect") INDIRECT
  }
  
  @ScimAttribute(description="Reference Element Identifier")
  @XmlElement
  String value;

  @ScimAttribute(description="The URI of the corresponding resource ", referenceTypes={"User", "Group"})
  @XmlElement(name = "$ref")
  String ref;

  @ScimAttribute(description="A human readable name, primarily used for display purposes. READ-ONLY.")
  @XmlElement
  String display;

  @ScimAttribute(description="A label indicating the attribute's function; e.g., 'direct' or 'indirect'.", canonicalValues={"direct", "indirect"})
  @XmlElement
  ReferenceType type;
}
