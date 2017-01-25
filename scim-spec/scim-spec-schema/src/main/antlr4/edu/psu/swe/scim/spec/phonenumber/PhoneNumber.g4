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
         
ParameterName: ( ALPHA_NUM | '-' )+;
     
ParameterValue: ParamChar+;
      
ParamChar: ParamUnreserved 
         | Unreserved
         | PctEncoded
         ;
         
Unreserved: ALPHA_NUM 
          | Mark
          ;
          
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
               
PhoneDigit: DIGIT | VisualSeparator
          ;
          
PhoneDigitHex: HEX_DIGIT 
             | '*' 
             | '#' 
             | VisualSeparator
             ;
              
VisualSeparator: '-' 
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
        
Uric: Reserved 
    | Unreserved 
    | PctEncoded
    ;