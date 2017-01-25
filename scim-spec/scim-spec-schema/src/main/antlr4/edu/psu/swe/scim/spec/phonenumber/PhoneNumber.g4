grammar PhoneNumber;

import Core;

phoneNumber: 'tel:' (global=globalNumber | local=localNumber);

globalNumber: globalDigits=GlobalNumberDigits (extension | isdnSubaddress)? parameter*;
            
localNumber: localDigits=LocalNumberDigits (extension | isdnSubaddress)? phoneContext parameter*;

isdnSubaddress: ';isub=' isub=Uric+;

extension: ';ext=' ext=PhoneDigit+;

phoneContext: ';phone-context=' pContext=Descriptor;
            
fragment Descriptor: DomainName 
                   | GlobalNumberDigits
                   ;
          
GlobalNumberDigits: '+' PhoneDigit* DIGIT PhoneDigit*;
                    
LocalNumberDigits: PhoneDigitHex* (HEX_DIGIT | '*' | '#') PhoneDigitHex*;         

fragment DomainName: ( DomainLabel '.' )* TopLabel ( '.' )?;
          
fragment DomainLabel: ALPHA_NUM 
                    | ALPHA_NUM ( ALPHA_NUM | '-' )* ALPHA_NUM
                    ;
           
fragment TopLabel: ALPHA 
                 | ALPHA ( ALPHA_NUM | '-' )* ALPHA_NUM
                 ;
        
parameter: ';' pName=ParameterName ('=' pValue=ParameterValue )?;
         
fragment ParameterName: ( ALPHA_NUM | '-' )+;
     
fragment ParameterValue: ParamChar+;
      
fragment ParamChar: ParamUnreserved 
         | Unreserved
         | PctEncoded
         ;
         
fragment Unreserved: ALPHA_NUM 
          | Mark
          ;
          
fragment Mark: '-' 
    | '_' 
    | '.' 
    | '!' 
    | '~' 
    | '*' 
    | '\'' 
    | '(' 
    | ')'
    ;

fragment PctEncoded: '%' HEX_DIGIT HEX_DIGIT;
           
fragment ParamUnreserved: '[' 
                        | ']' 
                        | '/' 
                        | ':' 
                        | '&' 
                        | '+' 
                        | '$'
                        ;
               
fragment PhoneDigit: DIGIT 
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
        
fragment Uric: Reserved 
    | Unreserved 
    | PctEncoded
    ;