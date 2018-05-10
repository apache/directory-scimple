package edu.psu.swe.scim.spec.protocol.attribute;

import java.io.Serializable;

import edu.psu.swe.scim.spec.validator.Urn;
import lombok.Data;

@Data
public class AttributeReference implements Serializable {

  private static final long serialVersionUID = -3559538009692681470L;

  @Urn
  String urn;

  String parent;

  String attributeName;

  public AttributeReference(String name) {
    int endOfUrn = name.lastIndexOf(':');
    String[] attributes = name.substring(endOfUrn + 1).split("\\.");

    if (endOfUrn > -1) {
      this.urn = name.substring(0, endOfUrn);
    }
    if (attributes.length > 1) {
      this.parent = attributes[0];
      this.attributeName = attributes[1];
    } else if (attributes.length > 0) {
      this.attributeName = attributes[0];
    }
  }

  public AttributeReference(String urn, String name) {
    this.urn = urn;

    if (name != null) {
      String[] attributes = name.split("\\.");

      if (attributes.length > 1) {
        this.parent = attributes[0];
        this.attributeName = attributes[1];
      } else {
        this.attributeName = attributes[0];
      }
    }
  }

  public AttributeReference(String urn, String parent, String name) {
    this.urn = urn;
    this.parent = parent;
    this.attributeName = name;
  }

  public String getFullAttributeName() {
    return (parent != null ? parent + "." : "") + this.attributeName;
  }

  public String getFullyQualifiedAttributeName() {
    String fullyQualifiedAttributeName;
    StringBuilder sb = new StringBuilder();

    if (this.urn != null) {
      sb.append(this.urn);

      if (this.parent != null || this.attributeName != null) {
        sb.append(":");
      }
    }
    if (this.parent != null) {
      sb.append(this.parent);

      if (this.attributeName != null) {
        sb.append(".");
      }
    }
    if (this.attributeName != null) {
      sb.append(attributeName);
    }
    fullyQualifiedAttributeName = sb.toString();

    return fullyQualifiedAttributeName;
  }

  public String getAttributeBase() {
    String attributeBase;
    StringBuilder sb = new StringBuilder();

    if (this.urn != null) {
      sb.append(this.urn);

      if (this.parent != null) {
        sb.append(":");
      }
    }
    if (this.parent != null) {
      sb.append(this.parent);
    }
    attributeBase = sb.toString();

    return attributeBase;
  }

}
