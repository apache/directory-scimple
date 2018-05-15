package edu.psu.swe.scim.spec.protocol.data;

import edu.psu.swe.scim.server.filter.FilterParser.PatchPathFullContext;
import edu.psu.swe.scim.server.filter.FilterParser.PatchPathPartialContext;
import edu.psu.swe.scim.spec.protocol.attribute.AttributeReference;
import edu.psu.swe.scim.spec.protocol.filter.ExpressionBuildingListener;
import edu.psu.swe.scim.spec.protocol.filter.FilterExpression;
import edu.psu.swe.scim.spec.protocol.filter.ValuePathExpression;
import lombok.Getter;

@Getter
public class PatchPathListener extends ExpressionBuildingListener {

  private ValuePathExpression valuePathExpression;
  
  @Override
  public void exitPatchPathFull(PatchPathFullContext ctx) {
    super.exitPatchPathFull(ctx);

    String attributePathText = ctx.attributePath.getText();
    String subAttributeName = ctx.subAttributeName != null ? ctx.subAttributeName.getText() : null;
    FilterExpression attributeExpression = expressionStack.pop();
    AttributeReference attributePath = new AttributeReference(attributePathText);
    String urn = attributePath.getUrn();
    String parentAttributeName = attributePath.getAttributeName();

    attributeExpression.setAttributePath(urn, parentAttributeName);

    if (subAttributeName != null) {
      attributePath.setAttributeName(parentAttributeName);
      attributePath.setSubAttributeName(subAttributeName);
    }
    this.valuePathExpression = new ValuePathExpression(attributePath, attributeExpression);
  }

  @Override
  public void exitPatchPathPartial(PatchPathPartialContext ctx) {
    super.exitPatchPathPartial(ctx);

    String attributePathText = ctx.attributePath.getText();
    AttributeReference attributePath = new AttributeReference(attributePathText);

    this.valuePathExpression = new ValuePathExpression(attributePath);
  }
}
