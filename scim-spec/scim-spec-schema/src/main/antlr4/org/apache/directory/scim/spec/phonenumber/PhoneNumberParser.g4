parser grammar PhoneNumberParser;

options {tokenVocab = PhoneNumberLexer;}

phoneNumber: PrefixTel (global=globalNumber | local=localNumber);

globalNumber: globalDigits=Plus GlobalNumberDigits (SEMI (PrefixExt Ext | PrefixIsub Isub))? parameter*;
            
localNumber: localDigits=localNumberDigits (SEMI (PrefixExt Ext | PrefixIsub Isub))? (SEMI|ParamTerm) phoneContext parameter*;

phoneContext: PrefixPhoneContext (dn=DomainName | (CtxPlus dig=GlobalNumberDigits));

localNumberDigits: (DIGIT | HEX_ALPHA | STAR | POUND | VisualSeparator | DOT | DASH)* (DIGIT | HEX_ALPHA | STAR | POUND) (DIGIT | HEX_ALPHA | STAR | POUND | VisualSeparator | DOT | DASH)*;         
          
parameter: (SEMI|ParamTerm) (ParamName (ParamWithValue ParamValue)? );
