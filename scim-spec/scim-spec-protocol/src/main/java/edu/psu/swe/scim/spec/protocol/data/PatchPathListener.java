package edu.psu.swe.scim.spec.protocol.data;

import java.util.List;

import org.antlr.v4.runtime.Token;

import edu.psu.swe.scim.server.filter.FilterParser.PatchPathContext;
import edu.psu.swe.scim.server.filter.FilterParser.ValuePathContext;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.filter.ExpressionBuildingListener;
import edu.psu.swe.scim.spec.protocol.filter.ValueFilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.ValuePathExpression;
import lombok.Getter;

@Getter
public class PatchPathListener extends ExpressionBuildingListener {

  private AttributeReference attributeReference;
  private ValueFilterExpression valueFilter;
  private String[] subAttributes;
  
  @Override
  public void exitPatchPath(PatchPathContext ctx) {
    super.exitPatchPath(ctx);
    Token attributeReferenceToken = ctx.attrPath;
    if (attributeReferenceToken != null) {
      String attrPath = attributeReferenceToken.getText();
      attributeReference = new AttributeReference(attrPath);
    }
    
    List<Token> subAttributeTokens = ctx.subAttr;
    if (subAttributeTokens != null) {
      subAttributes = new String[subAttributeTokens.size()];
      int i = 0;
      for (Token subAttributeToken : subAttributeTokens) {
        String subAttr = subAttributeToken.getText();
        subAttributes[i] = subAttr.substring(1);
        i++;
      }
    }
  }

  @Override
  public void exitValuePath(ValuePathContext ctx) {
    super.exitValuePath(ctx);
    ValuePathExpression valuePathExpression = (ValuePathExpression) getFilterExpression();
    attributeReference = valuePathExpression.getAttributePath();
    valueFilter = valuePathExpression.getValueFilter();
  }

}
