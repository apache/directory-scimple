package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Value;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Value
public class AttributeComparisonExpression implements AttributeExpression, ValueFilterExpression {
  AttributeReference attributePath;
  CompareOperator operation;
  Object compareValue;
}
