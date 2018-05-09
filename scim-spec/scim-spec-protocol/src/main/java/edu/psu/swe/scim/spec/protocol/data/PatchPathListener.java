package edu.psu.swe.scim.spec.protocol.data;

import edu.psu.swe.scim.server.filter.FilterParser.PatchPathContext;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.filter.AttributeExpression;
import edu.psu.swe.scim.spec.protocol.filter.ExpressionBuildingListener;
import edu.psu.swe.scim.spec.protocol.filter.ValuePathExpression;
import lombok.Getter;

@Getter
public class PatchPathListener extends ExpressionBuildingListener {

  private ValuePathExpression valuePathExpression;
  
  @Override
  public void exitPatchPath(PatchPathContext ctx) {
    super.exitPatchPath(ctx);

    String attributePathText = ctx.attributePath.getText();
    AttributeReference attributePath = new AttributeReference(attributePathText);

    if (this.getFilterExpression() instanceof AttributeExpression) {
      AttributeExpression attributeExpression = (AttributeExpression) expressionStack.pop();
      this.valuePathExpression = new ValuePathExpression(attributePath, attributeExpression);
    } else {
      this.valuePathExpression = new ValuePathExpression(attributePath);
    }
  }
}
