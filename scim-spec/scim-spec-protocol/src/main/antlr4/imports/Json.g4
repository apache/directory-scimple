grammar Json;

// Json Number

NUMBER: (MINUS)? INT (FRAC)? (EXP)?;
DEC_PT: '.'; 
fragment DIGIT1_9 : [1-9] ;
fragment DIGIT : [0-9] ;
E: 'e' | 'E';
EXP: E (MINUS | PLUS)? DIGIT+;
FRAC: DEC_PT DIGIT+;
INT: ZERO | DIGIT1_9 (DIGIT+)?;
MINUS: '-';
PLUS: '+';
ZERO: '0';

// Json String

STRING: '"' (CHAR)* '"';
fragment CHAR: UNESCAPED | ESCAPED;
fragment UNESCAPED : (' ' .. '!') | ('#' .. '[') | (']' .. '~');
fragment ESCAPED: '\\' ('"' | '\\' | '/' | 'b' | 'f' | 'n' | 'r' | 't');