grammar PhoneNumber;

import Core;

phoneNumber: 'tel:' (global=globalNumber | local=localNumber);

globalNumber: globalDigits=GlobalNumberDigits (extension | isdnSubaddress)? parameter*;
            
localNumber: localDigits=LocalNumberDigits (extension | isdnSubaddress)? phoneContext parameter*;

isdnSubaddress: ';isub=' isub=Uric+;

extension: ';ext=' ext=PhoneDigit+;

phoneContext: ';phone-context=' pContext=(DomainName | GlobalNumberDigits);

GlobalNumberDigits: '+' PhoneDigit* DIGIT PhoneDigit*;
                    
LocalNumberDigits: PhoneDigitHex* (HEX_DIGIT | '*' | '#') PhoneDigitHex*;         
          
DomainName: ( DomainLabel '.' )* TopLabel ( '.' )?;
          
fragment DomainLabel: ALPHA_NUM 
                    | ALPHA_NUM ( ALPHA_NUM | '-' )* ALPHA_NUM
                    ;
           
fragment TopLabel: ALPHA 
                 | ALPHA ( ALPHA_NUM | '-' )* ALPHA_NUM
                 ;
        
parameter: ';' pName=( ALPHA_NUM | '-' )+ ('=' pValue=(ParamUnreserved | ALPHA_NUM | Mark | PctEncoded)+ )?;
               
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

fragment Reserved: ';' 
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
        
Uric: Reserved 
    | ALPHA_NUM 
    | Mark 
    | PctEncoded
    ;