/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.directory.scim.spec.protocol.data;

import org.apache.directory.scim.server.filter.FilterParser.PatchPathFullContext;
import org.apache.directory.scim.server.filter.FilterParser.PatchPathPartialContext;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.filter.ExpressionBuildingListener;
import org.apache.directory.scim.spec.protocol.filter.FilterExpression;
import org.apache.directory.scim.spec.protocol.filter.ValuePathExpression;
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
