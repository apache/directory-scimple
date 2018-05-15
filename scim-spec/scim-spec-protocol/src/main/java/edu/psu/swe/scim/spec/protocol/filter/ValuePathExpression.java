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
      String subAttributeName = this.attributePath.getSubAttributeName();
      String attributeExpressionFilter = this.attributeExpression.toUnqualifiedFilter();

      if (subAttributeName != null) {
        String base = this.attributePath.getAttributeBase();
        filter = base + "[" + attributeExpressionFilter + "]." + subAttributeName;
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
    String subAttributeName = this.attributePath.getAttributeName();
    this.attributePath.setAttributeName(parentAttributeName);
    this.attributePath.setSubAttributeName(subAttributeName);
    this.attributeExpression.setAttributePath(urn, parentAttributeName);
  }

  @Override
  public String toUnqualifiedFilter() {
    String filter;

    if (this.attributeExpression != null) {
      String attributeName = this.attributePath.getAttributeName();
      String subAttributeName = this.attributePath.getSubAttributeName();
      String attributeExpressionFilter = this.attributeExpression.toUnqualifiedFilter();

      if (subAttributeName != null) {
        filter = attributeName + "[" + attributeExpressionFilter + "]." + subAttributeName;
      } else {
        filter = attributeName + "[" + attributeExpressionFilter + "]";
      }
    } else {
      String subAttributeName = this.attributePath.getSubAttributeName();
      filter = this.attributePath.getAttributeName() + (subAttributeName != null ? "." + subAttributeName : "");
    }
    return filter;
  }
}
