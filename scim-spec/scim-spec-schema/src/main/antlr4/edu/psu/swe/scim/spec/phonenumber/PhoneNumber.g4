grammar PhoneNumber;

//import Core;

phoneNumber: 'tel:' (global=globalNumber | local=localNumber);

globalNumber: globalDigits=GlobalNumberDigits (extension | isdnSubaddress)? parameter*;
            
localNumber: localDigits=LocalNumberDigits (extension | isdnSubaddress)? phoneContext parameter*;

isdnSubaddress: ';isub=' isub=(Reserved | SPECIAL | ALPHA | DIGIT | Mark | STAR | VisualSeparator | DASH | PctEncoded)+;

extension: ';ext=' ext=(DIGIT | VisualSeparator | DASH)+;

phoneContext: ';phone-context=' pContext=(DomainName | GlobalNumberDigits);

GlobalNumberDigits: '+' (DIGIT | VisualSeparator | DASH)* DIGIT (DIGIT | VisualSeparator | DASH)*;
                    
LocalNumberDigits: PhoneDigitHex* (HEX_DIGIT | STAR | POUND) PhoneDigitHex*;         
          
DomainName: ( DomainLabel '.' )* TopLabel ( '.' )?;
          
fragment DomainLabel: (ALPHA | DIGIT ) 
                    | (ALPHA | DIGIT ) ( (ALPHA | DIGIT ) | DASH )* (ALPHA | DIGIT )
                    ;
           
fragment TopLabel: ALPHA 
                 | ALPHA ( (ALPHA | DIGIT ) | DASH )* (ALPHA | DIGIT )
                 ;
        
parameter: ';' pName=( ALPHA | DIGIT | DASH )+ ('=' pValue=(ParamUnreserved | SPECIAL | ALPHA | DIGIT | Mark | STAR | VisualSeparator | DASH | PctEncoded)+ )?;

Mark: '_' 
    | '!' 
    | '~' 
    | '\'' 
    ;

PctEncoded: '%' HEX_DIGIT HEX_DIGIT;
           
ParamUnreserved: '[' 
               | ']' 
               ;

fragment PhoneDigitHex: HEX_DIGIT 
                      | STAR
                      | POUND
                      | VisualSeparator
                      | DASH
                      ;



VisualSeparator: '.' 
                        | '(' 
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
ALPHA: [a-zA-Z];
DIGIT: [0-9];
fragment HEX_DIGIT: [A-F0-9];