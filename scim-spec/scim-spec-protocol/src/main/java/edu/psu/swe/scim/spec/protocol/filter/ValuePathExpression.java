package edu.psu.swe.scim.spec.protocol.filter;

import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValuePathExpression implements FilterExpression {
  AttributeReference attributePath;
  ValueFilterExpression valueFilter;
  
  public static ValuePathExpression fromFilterExpression(String attrRef, FilterExpression filterExpression) throws FilterParseException {
    AttributeReference ref = new AttributeReference(attrRef);
    return fromFilterExpression(ref, filterExpression);
  }
  
  public static ValuePathExpression fromFilterExpression(AttributeReference attrRef, FilterExpression filterExpression) throws FilterParseException {
        
    ValuePathExpression vpe = new ValuePathExpression();
    vpe.setAttributePath(attrRef);
    
    if (filterExpression instanceof LogicalExpression) {
      LogicalExpression le = (LogicalExpression) filterExpression;
      vpe.setValueFilter(le); 
      return vpe;
    } else if (filterExpression instanceof GroupExpression) {
      GroupExpression ge = (GroupExpression) filterExpression;
      vpe.setValueFilter(ge);
      return vpe;
    } else if (filterExpression instanceof AttributePresentExpression) {
      AttributePresentExpression ape = (AttributePresentExpression) filterExpression;
      vpe.setValueFilter(ape);
      return vpe;
    } else if (filterExpression instanceof AttributeComparisonExpression) {
      AttributeComparisonExpression ace = (AttributeComparisonExpression) filterExpression;
      vpe.setValueFilter(ace);
      return vpe;
    } else if (filterExpression instanceof ValuePathExpression) {
      throw new FilterParseException("Value path expressions can not own other value path expressions");
    }
      
    return null;
  }
  
  
  @Override
  public String toFilter() {
    return attributePath.getFullyQualifiedAttributeName() + "[" + valueFilter.toFilter() + "]";
  }
  
}
