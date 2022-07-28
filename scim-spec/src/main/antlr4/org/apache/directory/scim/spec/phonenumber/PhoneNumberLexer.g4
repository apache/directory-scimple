/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
 
* http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

lexer grammar PhoneNumberLexer;

PrefixTel: 'tel:';

Plus: '+' -> pushMode(GLOBAL_DIGITS);
                    
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

STAR: '*';
POUND: '#';

SPECIAL: '+'
       | '$'
       | '&'
       | '/'
       | ':';
       
SEMI: ';' -> pushMode(PARAM);
DASH: '-';
DOT: '.';
HEX_ALPHA: [A-F];
ALPHA: [a-zG-Z];
DIGIT: [0-9];

// handle characters which failed to match any other token
ErrorCharacter : . ;

mode PARAM;

PrefixExt: 'ext=' -> pushMode(EXTENSION);
PrefixIsub: 'isub=' -> pushMode(ISUB);
PrefixPhoneContext: 'phone-context=' -> pushMode(PHONE_CTX);
ParamName: ( HEX_ALPHA | ALPHA | DIGIT | DASH )+;
ParamWithValue: '=' -> pushMode(PARAM_VALUE);
ParamTerm: ';';

// handle characters which failed to match any other token
ParamErrorCharacter : . ;

mode PARAM_VALUE;

ParamValue: (ParamUnreserved | SPECIAL | HEX_ALPHA | ALPHA | DIGIT | Mark | STAR | VisualSeparator | DOT | DASH | PctEncoded)+ -> popMode;

// handle characters which failed to match any other token
ParamValueErrorCharacter : . ;

mode EXTENSION;

Ext:(DIGIT | VisualSeparator | DOT | DASH)+ -> popMode;


// handle characters which failed to match any other token
ExtErrorCharacter : . ;

mode ISUB;

Isub: (Reserved | SPECIAL | HEX_ALPHA | ALPHA | DIGIT | Mark | STAR | VisualSeparator | DOT | DASH | PctEncoded)+ -> popMode;

Reserved: '?' 
        | '@' 
        | '=' 
        | ','
        ;
        
// handle characters which failed to match any other token
IsubErrorCharacter : . ;

mode PHONE_CTX;

DomainName: ( ((HEX_ALPHA | ALPHA | DIGIT ) ((HEX_ALPHA | ALPHA | DIGIT | DASH )* (HEX_ALPHA | ALPHA | DIGIT ))?) DOT )* ((HEX_ALPHA | ALPHA) ((HEX_ALPHA | ALPHA | DIGIT | DASH )* (HEX_ALPHA | ALPHA | DIGIT ))?) ( DOT )? -> popMode;

CtxPlus: '+' -> popMode, pushMode(GLOBAL_DIGITS);

// handle characters which failed to match any other token
CtxErrorCharacter : . ;

mode GLOBAL_DIGITS;

GlobalNumberDigits: (DIGIT | VisualSeparator | DOT | DASH)* DIGIT (DIGIT | VisualSeparator | DOT | DASH)* -> popMode;

// handle characters which failed to match any other token
GlobalErrorCharacter : . ;

