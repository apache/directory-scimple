grammar PhoneNumber;

//import Core;

phoneNumber: 'tel:' (global=globalNumber | local=localNumber);

globalNumber: globalDigits=GlobalNumberDigits (extension | isdnSubaddress)? parameter*;
            
localNumber: localDigits=localNumberDigits (extension | isdnSubaddress)? phoneContext parameter*;

isdnSubaddress: ';isub=' isub=(Reserved | SPECIAL | HEX_ALPHA | ALPHA | DIGIT | Mark | STAR | VisualSeparator | DOT | DASH | PctEncoded)+;

extension: ';ext=' ext=(DIGIT | VisualSeparator | DOT | DASH)+;

phoneContext: ';phone-context=' (dn=domainName | dig=GlobalNumberDigits);

GlobalNumberDigits: '+' (DIGIT | VisualSeparator | DOT | DASH)* DIGIT (DIGIT | VisualSeparator | DOT | DASH)*;
                    
localNumberDigits: (DIGIT | HEX_ALPHA | STAR | POUND | VisualSeparator | DOT | DASH)* (DIGIT | HEX_ALPHA | STAR | POUND) (DIGIT | HEX_ALPHA | STAR | POUND | VisualSeparator | DOT | DASH)*;         
          
domainName: ( ((HEX_ALPHA | ALPHA | DIGIT ) ((HEX_ALPHA | ALPHA | DIGIT | DASH )* (HEX_ALPHA | ALPHA | DIGIT ))?) DOT )* ((HEX_ALPHA | ALPHA) ((HEX_ALPHA | ALPHA | DIGIT | DASH )* (HEX_ALPHA | ALPHA | DIGIT ))?) ( DOT )?;

parameter: ';' pName=( HEX_ALPHA | ALPHA | DIGIT | DASH )+ ('=' pValue=(ParamUnreserved | SPECIAL | HEX_ALPHA | ALPHA | DIGIT | Mark | STAR | VisualSeparator | DOT | DASH | PctEncoded)+ )?;

Mark: '_' 
    | '!' 
    | '~' 
    | '\'' 
    ;

PctEncoded: '%' [A-F0-9] [A-F0-9];
           
ParamUnreserved: '[' 
               | ']' 
               ;

VisualSeparator: '(' 
               | ')'
               ;

Reserved: ';' 
        | '?' 
        | '@' 
        | '=' 
        | ','
        ;

STAR: '*';
POUND: '#';

SPECIAL: '+'
       | '$'
       | '&'
       | '/'
       | ':';
DASH: '-';
DOT: '.';
HEX_ALPHA: [A-F];
ALPHA: [a-zG-Z];
DIGIT: [0-9];

