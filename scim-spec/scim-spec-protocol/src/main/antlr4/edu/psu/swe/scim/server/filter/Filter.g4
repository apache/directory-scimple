grammar Filter;

import Json, Urn;

filter: attrExp                                     # filterAttrExp
      | left=filter SP op=LogicOp SP right=filter   # filterLogicExp
      | valuePath                                   # filterValuePath
      | not='not'? '(' f=filter ')'                 # filterGroupExp
      ;

valuePath: attrPath=AttrPath '[' valueFilter=valFilter ']';
valFilter: attrExp                                          # valFilterAttrExp
         | left=valFilter SP op=LogicOp SP right=valFilter  # valFilterLogicExp
         | not='not'? /'(' valueFilter=valFilter ')'        # valFilterGroupExp
         ;

attrExp: attrPath=AttrPath SP op=PresentOp                          # attrExpPresent
       | attrPath=AttrPath SP op=CompareOp SP compValue=CompValue   # attrExpCompareOp
       ;

LogicOp: 'and' | 'or';

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
         
PresentOp: 'pr';

AttrPath: (URN ':')? AttrName (SubAttr)?;
AttrName: ALPHA NAMECHAR*;
NAMECHAR: ('-' | '_' | DIGIT | ALPHA);
fragment ALPHA : [a-zA-Z];
SubAttr: '.' AttrName;

SP: ' ';