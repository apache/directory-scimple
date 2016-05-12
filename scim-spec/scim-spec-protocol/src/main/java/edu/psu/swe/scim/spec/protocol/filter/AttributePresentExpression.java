package edu.psu.swe.scim.spec.protocol.filter;

import lombok.Data;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;

@Data
public class AttributePresentExpression implements AttributeExpression, ValueFilterExpression {
  AttributeReference attributePath;
}
