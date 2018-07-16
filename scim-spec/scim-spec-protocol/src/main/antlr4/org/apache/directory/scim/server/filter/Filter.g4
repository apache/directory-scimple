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

grammar Filter;

import Json, Urn;

filter: filterExpression;

patchPath: attributePath=partialAttributePath '[' attributeExpression ']' ('.' subAttributeName=AttributeName)? # PatchPathFull
         | attributePath=partialPatchPath                                                                       # PatchPathPartial
         ;


filterExpression: filterExpression SP op=LogicOp SP filterExpression                        # FilterLogicExpression
                | not=NotOp? SP? '(' filterExpression ')'                                   # FilterGroupExpression
                | attributePath=fullAttributePath SP PresentOp                              # FilterAttributePresentExpression
                | attributePath=fullAttributePath SP op=CompareOp SP compareValue=CompValue # FilterAttributeCompareExpression
                | attributePath=partialAttributePath '[' attributeExpression ']'            # FilterValuePathExpression
                | attributeExpression                                                       # FilterAttributeExpression
                ;

attributeExpression: attributeExpression SP op=LogicOp SP attributeExpression              # AttributeLogicExpression
                   | not=NotOp? SP? '(' attributeExpression ')'                            # AttributeGroupExpression
                   | attributeName=AttributeName SP PresentOp                              # AttributePresentExpression
                   | attributeName=AttributeName SP op=CompareOp SP compareValue=CompValue # AttributeCompareExpression
                   ;

fullAttributePath: FullAttributePath | ParentChildAttributePath | UrnAndNameAttributePath;

partialAttributePath: UrnAndNameAttributePath | AttributeName;

partialPatchPath: FullAttributePath | UrnAndNameAttributePath | ParentChildAttributePath | AttributeName;


LogicOp: [aA][nN][dD]
       | [oO][rR]
       ;

CompValue: 'false'
         | 'null'
         | 'true'
         | NUMBER
         | STRING
         ;

CompareOp: [eE][qQ]
         | [nN][eE]
         | [cC][oO]
         | [sS][wW]
         | [eE][wW]
         | [gG][tT]
         | [lL][tT]
         | [gG][eE]
         | [lL][eE]
         ;

PresentOp: [pP][rR];

NotOp: [nN][oO][tT];

FullAttributePath: URN ':' AttributeName '.' AttributeName;
UrnAndNameAttributePath: URN ':' AttributeName;
ParentChildAttributePath: AttributeName '.' AttributeName;
AttributeName: ALPHA NAMECHAR*;
fragment NAMECHAR: '-' | '_' | DIGIT | ALPHA;
fragment ALPHA : [a-zA-Z];

SP: ' '+;