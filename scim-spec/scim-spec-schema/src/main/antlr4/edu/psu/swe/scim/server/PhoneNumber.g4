grammar PhoneNumber;

import Json, Urn;

phoneNumber: 'tel:'                # required prefix
           |  telephoneSubscriber  # main phone number body and params
           ;

telephoneSubscriber: globalNumber 
				   | localNumber
				   ;
				   
globalNumber: globalNumberDigits par?
            ;
            
localNumber: localNumberDigits par? phoneContext par?
           ;
           
par: parameter 
   | extension 
   | isdnSubaddress
   ;

isdnSubaddress: ";isub=" 1*uric
               ;

extension: ";ext=" 1*phoneDigit
         ;

phoneContext: ";phone-context=" descriptor
            ;
            
descriptor: domainname 
          | globalNumberDigits
          ;
          
globalNumberDigits: "+" *phoneDigit DIGIT *phoneDigit
                    ;
                    
localNumberDigits: *phoneDigitHex (HEXDIG / "*" / "#")*phoneDigitHex
                   ;         

domainName: *( domainLabel "." ) topLabel [ "." ]
          ;
          
domainLabel: alphanum | alphanum *( alphanum | "-" ) alphanum
           ;
           
topLabel: ALPHA / ALPHA *( alphanum / "-" ) alphanum
        ;
        
parameter: ";" pname ["=" pvalue ]
         ;
         
pname: 1*( alphanum / "-" )
     ;
     
pvalue: 1*paramChar
      ;
      
paramChar: paramUnreserved 
         | unreserved
         | pct-encoded
         ;
         
unreserved: alphanum 
          | mark
          ;
          
mark: "-" 
    | "_" 
    | "." 
    | "!" 
    | "~" 
    | "*" 
    | "'" 
    | "(" 
    | ")"
    ;
    
pctEncoded: "%" HEXDIG HEXDIG
          ;
           
paramUnreserved: "[" 
               | "]" 
               | "/" 
               | ":" 
               | "&" 
               | "+" 
               | "$"
               ;
               
phoneDigit: DIGIT | [ visualSeparator ]
          ;
          
phoneDigitHex: HEXDIG 
             | "*" 
             | "#" 
             | [ visualSeparator ]
             ;
              
visualSeparator: "-" 
               | "." 
               | "(" 
               | ")"
               ;
               
alphanum: ALPHA 
        | DIGIT
        ;
        
reserved: ";" 
        | "/" 
        | "?" 
        | ":" 
        | "@" 
        | "&" 
        | "=" 
        | "+" 
        | "$" 
        | ","
        ;
        
uric: reserved 
    | unreserved 
    | pctEncoded
    ;