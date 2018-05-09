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
  AttributeExpression attributeExpression;

  public ValuePathExpression(AttributeReference attributePath) {
    this.attributePath = attributePath;
  }

  public static ValuePathExpression fromAttributeExpression(String attrRef, AttributeExpression attributeExpression) throws FilterParseException {
    AttributeReference ref = new AttributeReference(attrRef);
    return fromAttributeExpression(ref, attributeExpression);
  }
  
  public static ValuePathExpression fromAttributeExpression(AttributeReference attrRef, AttributeExpression attributeExpression) throws FilterParseException {
    ValuePathExpression vpe = new ValuePathExpression();

    vpe.setAttributePath(attrRef);
    vpe.setAttributeExpression(attributeExpression);

    return null;
  }

  public static ValuePathExpression fromFilterExpression(String attribute, FilterExpression expression) throws FilterParseException {
    if (!(expression instanceof AttributeExpression)) {
      throw new FilterParseException(expression.getClass().getCanonicalName() + " is not an instance of " + AttributeExpression.class.getCanonicalName());
    }
    return fromAttributeExpression(attribute, (AttributeExpression) expression);
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
  
}
