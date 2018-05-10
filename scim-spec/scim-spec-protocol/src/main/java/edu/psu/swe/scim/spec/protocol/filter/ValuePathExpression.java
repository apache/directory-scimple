package edu.psu.swe.scim.spec.protocol.filter;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValuePathExpression implements FilterExpression {
  // urn:parentAttribute[attributeExpression].subAttribute

  AttributeReference attributePath;
  FilterExpression attributeExpression;

  public ValuePathExpression(AttributeReference attributePath) {
    this.attributePath = attributePath;
  }

  public static ValuePathExpression fromFilterExpression(AttributeReference attrRef, FilterExpression attributeExpression) throws FilterParseException {
    ValuePathExpression vpe = new ValuePathExpression(attrRef, attributeExpression);

    return vpe;
  }

  public static ValuePathExpression fromFilterExpression(String attribute, FilterExpression expression) throws FilterParseException {
    AttributeReference attributeReference = new AttributeReference(attribute);

    return fromFilterExpression(attributeReference,  expression);
  }

  @Override
  public String toFilter() {
    String filter;

    if (this.attributeExpression != null) {
      String parentAttribute = this.attributePath.getParent();
      String attributeExpressionFilter = this.attributeExpression.toUnqualifiedFilter();

      if (parentAttribute != null) {
        String base = this.attributePath.getAttributeBase();
        String subAttribute = this.attributePath.getAttributeName();
        filter = base + "[" + attributeExpressionFilter + "]." + subAttribute;
      } else {
        String attribute = this.attributePath.getFullyQualifiedAttributeName();
        filter = attribute + "[" + attributeExpressionFilter + "]";
      }
    } else {
      filter = this.attributePath.getFullyQualifiedAttributeName();
    }
    return filter;
  }

  @Override
  public void setAttributePath(String urn, String parentAttributeName) {
    this.attributePath.setUrn(urn);
    this.attributePath.setParent(parentAttributeName);
    this.attributeExpression.setAttributePath(urn, parentAttributeName);
  }

  @Override
  public String toUnqualifiedFilter() {
    String filter;

    if (this.attributeExpression != null) {
      String parentAttribute = this.attributePath.getParent();
      String attributeExpressionFilter = this.attributeExpression.toUnqualifiedFilter();

      if (parentAttribute != null) {
        String subAttribute = this.attributePath.getAttributeName();
        filter = parentAttribute + "[" + attributeExpressionFilter + "]." + subAttribute;
      } else {
        String attribute = this.attributePath.getAttributeName();
        filter = attribute + "[" + attributeExpressionFilter + "]";
      }
    } else {
      String parent = this.attributePath.getParent();
      filter = (parent != null ? parent + "." : "") + this.attributePath.getAttributeName();
    }
    return filter;
  }
}
