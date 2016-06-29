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
  
  private static class ValuePathValidator {
    public ValuePathValidator(FilterExpression expression) throws FilterParseException {
      
      if (!(expression instanceof ValuePathExpression)) {
        validateFilterExpressionAsValuePath(expression);
      }
    }
    
    private void validateFilterExpressionAsValuePath(FilterExpression expression) throws FilterParseException {
      if (expression instanceof LogicalExpression) {
        LogicalExpression le = (LogicalExpression) expression;
        validateFilterExpression(le.getLeft());
        validateFilterExpression(le.getRight());
      }
    }
    
    private void validateFilterExpression(FilterExpression expression) throws FilterParseException {
      if (expression instanceof ValueFilterExpression) {
        throw new FilterParseException("Value filter expressions can not own other value filter expressions");
      } else if (expression instanceof LogicalExpression) {
        LogicalExpression le = (LogicalExpression) expression;
        validateFilterExpression(le.getLeft());
        validateFilterExpression(le.getRight());
      }
    }
  }
  
  public static ValuePathExpression fromFilterExpression(String attrRef, FilterExpression filterExpression) throws FilterParseException {
    AttributeReference ref = new AttributeReference(attrRef);
    return fromFilterExpression(ref, filterExpression);
  }
  
  public static ValuePathExpression fromFilterExpression(AttributeReference attrRef, FilterExpression filterExpression) throws FilterParseException {
    
    ValuePathValidator vpv = new ValuePathValidator(filterExpression);
    
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
    }
      
    return null;
  }
  
  
  @Override
  public String toFilter() {
    return attributePath.getFullyQualifiedAttributeName() + "[" + valueFilter.toFilter() + "]";
  }
  
}
