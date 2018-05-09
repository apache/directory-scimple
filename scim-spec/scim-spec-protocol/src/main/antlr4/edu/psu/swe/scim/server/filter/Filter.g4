grammar Filter;

import Json, Urn;

filter: filterExpression;

patchPath: attributePath=partialAttributePath ('[' attributeExpression ']')? ('.' subAttributeName=AttributeName)?;


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