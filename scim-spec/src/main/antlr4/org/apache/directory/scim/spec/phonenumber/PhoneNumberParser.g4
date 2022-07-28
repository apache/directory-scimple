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

parser grammar PhoneNumberParser;

options {tokenVocab = PhoneNumberLexer;}

phoneNumber: PrefixTel (global=globalNumber | local=localNumber);

globalNumber: globalDigits=Plus GlobalNumberDigits (SEMI (PrefixExt Ext | PrefixIsub Isub))? parameter*;
            
localNumber: localDigits=localNumberDigits (SEMI (PrefixExt Ext | PrefixIsub Isub))? (SEMI|ParamTerm) phoneContext parameter*;

phoneContext: PrefixPhoneContext (dn=DomainName | (CtxPlus dig=GlobalNumberDigits));

localNumberDigits: (DIGIT | HEX_ALPHA | STAR | POUND | VisualSeparator | DOT | DASH)* (DIGIT | HEX_ALPHA | STAR | POUND) (DIGIT | HEX_ALPHA | STAR | POUND | VisualSeparator | DOT | DASH)*;         
          
parameter: (SEMI|ParamTerm) (ParamName (ParamWithValue ParamValue)? );
