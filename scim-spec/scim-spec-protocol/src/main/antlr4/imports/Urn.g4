grammar Urn;

URN: 'urn:' NID ':' NSS;
fragment NID: LETNUM LETNUMHYP+; // TODO limit to {1,31}
fragment LETNUM: [a-zA-Z0-9];
fragment LETNUMHYP: LETNUM | '-';
fragment NSS: URNCHARS+;
fragment URNCHARS: TRANS | '%' HEX HEX;
fragment TRANS: LETNUM | OTHER;
fragment HEX: [a-fA-F0-9];
fragment OTHER: '(' | ')' | '+' | ',' | '-' | '.' | ':' | '=' | '@' | ';' | '$' | '_' | '!' | '*' | '\'';
