grammar PhoneNumber;

import Core;

phoneNumber: 'tel:' (globalNumber=GlobalNumber | localNumber=LocalNumber);

GlobalNumber: GlobalNumberDigits Par? Parameter?
            ;
            
LocalNumber: LocalNumberDigits Par? PhoneContext Parameter?
           ;
           
Par: Extension 
   | IsdnSubaddress
   ;

IsdnSubaddress: ';isub=' Uric+
               ;

Extension: ';ext=' PhoneDigit+
         ;

PhoneContext: ';phone-context=' Descriptor
            ;
            
Descriptor: DomainName 
          | GlobalNumberDigits
          ;
          
GlobalNumberDigits: '+' PhoneDigit* DIGIT PhoneDigit*
                  ;
                    
LocalNumberDigits: PhoneDigitHex* (HEX_DIGIT | '*' | '#') PhoneDigitHex*
                 ;         

DomainName: ( DomainLabel '.' )* TopLabel ( '.' )?
          ;
          
DomainLabel: ALPHA_NUM | ALPHA_NUM ( ALPHA_NUM | '-' )* ALPHA_NUM
           ;
           
TopLabel: ALPHA | ALPHA ( ALPHA_NUM | '-' )* ALPHA_NUM
        ;
        
Parameter: ';' ParameterName ('=' ParameterValue )?
         ;
         
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