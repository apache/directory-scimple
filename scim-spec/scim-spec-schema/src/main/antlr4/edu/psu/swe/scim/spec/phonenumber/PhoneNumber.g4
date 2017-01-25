grammar PhoneNumber;

//import Core;

phoneNumber: 'tel:' (global=globalNumber | local=localNumber);

globalNumber: globalDigits=GlobalNumberDigits (extension | isdnSubaddress)? parameter*;
            
localNumber: localDigits=LocalNumberDigits (extension | isdnSubaddress)? phoneContext parameter*;

isdnSubaddress: ';isub=' isub=(Reserved | ALPHA | DIGIT | Mark | PctEncoded)+;

extension: ';ext=' ext=PhoneDigit+;

phoneContext: ';phone-context=' pContext=(DomainName | GlobalNumberDigits);

GlobalNumberDigits: '+' PhoneDigit* DIGIT PhoneDigit*;
                    
LocalNumberDigits: PhoneDigitHex* (HEX_DIGIT | '*' | '#') PhoneDigitHex*;         
          
DomainName: ( DomainLabel '.' )* TopLabel ( '.' )?;
          
fragment DomainLabel: (ALPHA | DIGIT ) 
                    | (ALPHA | DIGIT ) ( (ALPHA | DIGIT ) | '-' )* (ALPHA | DIGIT )
                    ;
           
fragment TopLabel: ALPHA 
                 | ALPHA ( (ALPHA | DIGIT ) | '-' )* (ALPHA | DIGIT )
                 ;
        
parameter: ';' pName=( ALPHA | DIGIT | '-' )+ ('=' pValue=(ParamUnreserved | ALPHA | DIGIT | Mark | PctEncoded)+ )?;
               
Mark: '-' 
    | '_' 
    | '.' 
    | '!' 
    | '~' 
    | '*' 
    | '\'' 
    | '(' 
    | ')'
    ;

PctEncoded: '%' HEX_DIGIT HEX_DIGIT;
           
ParamUnreserved: '[' 
                        | ']' 
                        | '/' 
                        | ':' 
                        | '&' 
                        | '+' 
                        | '$'
                        ;
               
PhoneDigit: DIGIT 
          | VisualSeparator
          ;

fragment PhoneDigitHex: HEX_DIGIT 
                      | '*' 
                      | '#' 
                      | VisualSeparator
                      ;
              
fragment VisualSeparator: '-' 
                          | '.' 
                          | '(' 
                          | ')'
                          ;

Reserved: ';' 
        | '/' 
        | '?' 
        | ':' 
        | '@' 
        | '&' 
        | '=' 
        | '+' 
        | '$' 
        | ','
        ;

    
ALPHA: [a-zA-Z];
DIGIT: [0-9];
fragment HEX_DIGIT: [A-F0-9];