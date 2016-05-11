package edu.psu.swe.scim.spec.protocol.filter2;

import lombok.Data;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Data
public class AttributeComparisonExpression<T> implements AttributeExpression, ValueFilterExpression {

  AttributeReference attributePath;
  CompareOperator operation;
  T compareValue;
}
