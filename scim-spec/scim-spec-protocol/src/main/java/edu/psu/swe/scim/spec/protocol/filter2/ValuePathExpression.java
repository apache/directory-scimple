package edu.psu.swe.scim.spec.protocol.filter2;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

public class ValuePathExpression implements FilterExpression {
  AttributeReference attributePath;
  ValueFilterExpression valueFilter;
}
