package edu.psu.swe.scim.spec.protocol.attribute;

import java.io.Serializable;

import edu.psu.swe.scim.spec.validator.Urn;
import lombok.Data;

@Data
public class AttributeReference implements Serializable {

  private static final long serialVersionUID = -3559538009692681470L;

  @Urn
  String urn;

  String attributeName;

  String subAttributeName;

  public AttributeReference(String name) {
    int endOfUrn = name.lastIndexOf(':');
    String[] attributes = name.substring(endOfUrn + 1).split("\\.");
    this.attributeName = attributes[0];

    if (endOfUrn > -1) {
      this.urn = name.substring(0, endOfUrn);
    }
    if (attributes.length > 1) {
      this.subAttributeName = attributes[1];
    }
  }

  public AttributeReference(String urn, String name) {
    this.urn = urn;

    if (name != null) {
      String[] attributes = name.split("\\.");
      this.attributeName = attributes[0];

      if (attributes.length > 1) {
        this.subAttributeName = attributes[1];
      }
    }
  }

  public AttributeReference(String urn, String attributeName, String subAttributeName) {
    this.urn = urn;
    this.attributeName = attributeName;
    this.subAttributeName = subAttributeName;
  }

  public String getFullAttributeName() {
    return this.attributeName + (this.subAttributeName != null ? "." + this.subAttributeName : "");
  }

  public String getFullyQualifiedAttributeName() {
    String fullyQualifiedAttributeName;
    StringBuilder sb = new StringBuilder();

    if (this.urn != null) {
      sb.append(this.urn);

      if (this.attributeName != null) {
        sb.append(":");
      }
    }
    if (this.attributeName != null) {
      sb.append(this.attributeName);
    }
    if (this.subAttributeName != null) {
      sb.append(".");
      sb.append(subAttributeName);
    }
    fullyQualifiedAttributeName = sb.toString();

    return fullyQualifiedAttributeName;
  }

  public String getAttributeBase() {
    String attributeBase;
    StringBuilder sb = new StringBuilder();

    if (this.urn != null) {
      sb.append(this.urn);

      if (this.subAttributeName != null) {
        sb.append(":");
        sb.append(this.attributeName);
      }
    } else if (this.subAttributeName != null) {
      sb.append(this.attributeName);
    }
    attributeBase = sb.toString();

    return attributeBase;
  }

}
