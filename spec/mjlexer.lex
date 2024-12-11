package com.kosta.pp1;

import java_cup.runtime.Symbol; 

%%

%{
	private Symbol new_symbol(int type){
		return new Symbol(type,yyline+1,yycolumn);
	}
	
	private Symbol new_symbol(int type,Object value){
		return new Symbol(type,yyline+1,yycolumn,value);
	}
%}


%cup
%line
%column

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

[ \t\f]+ {}
[\r\n]+ {yyline++; }



"program" { return new_symbol(sym.PROGRAM,yytext()); }
"return" { return new_symbol(sym.RETURN,yytext()); }
"const" { return new_symbol(sym.CONST,yytext()); }
"class" { return new_symbol(sym.CLASS,yytext()); }
"interface" { return new_symbol(sym.INTERFACE,yytext()); }
"extends" { return new_symbol(sym.EXTENDS,yytext()); }
"map" { return new_symbol(sym.MAP,yytext()); }

"for" { return new_symbol(sym.FOR,yytext()); }
"if" { return new_symbol(sym.IF,yytext()); }
"else" { return new_symbol(sym.ELSE,yytext()); }
"continue" { return new_symbol(sym.CONTINUE,yytext()); }
"break" { return new_symbol(sym.BREAK,yytext()); }
"do" {  return new_symbol(sym.DO,yytext());  }
"while" {  return new_symbol(sym.WHILE,yytext());  }

"print" { return new_symbol(sym.PRINT,yytext()); }
"read" { return new_symbol(sym.READ,yytext()); }
"ord" { return new_symbol(sym.ORD,yytext()); }
"len" { return new_symbol(sym.LEN,yytext()); }
"chr" { return new_symbol(sym.CHR,yytext()); }
"add" { return new_symbol(sym.ADD,yytext()); }


"int" { return new_symbol(sym.INT,yytext()); }
"set" { return new_symbol(sym.SET,yytext()); }
"void" { return new_symbol(sym.VOID,yytext()); }
"char" { return new_symbol(sym.CHAR,yytext()); }
"bool" { return new_symbol(sym.BOOL,yytext()); }

"!=" { return new_symbol(sym.NEQ,yytext()); }
"!" { return new_symbol(sym.NOT,yytext()); }
"==" { return new_symbol(sym.EQ,yytext()); }
"<=" { return new_symbol(sym.LTE,yytext()); }
">=" { return new_symbol(sym.GTE,yytext()); }
"&&" { return new_symbol(sym.AND,yytext()); }
"||" { return new_symbol(sym.OR,yytext()); }
">" { return new_symbol(sym.GT,yytext()); }
"<" { return new_symbol(sym.LT,yytext()); }

"%" { return new_symbol(sym.MOD,yytext()); }
"++" { return new_symbol(sym.POSTINC,yytext()); }
"+" { return new_symbol(sym.PLUS,yytext()); }
"--" { return new_symbol(sym.POSTDEC,yytext()); }
"-" | "\u2010" { return new_symbol(sym.MINUS, yytext()); }
"*" { return new_symbol(sym.MUL,yytext()); }
"/" { return new_symbol(sym.DIV,yytext()); }
"=" { return new_symbol(sym.EQUAL,yytext()); }
"." { return new_symbol(sym.DOT,yytext()); }
"union" { return new_symbol(sym.UNION,yytext()); }

";" { return new_symbol(sym.SEMI,yytext()); }
":" { return new_symbol(sym.COLON,yytext()); }
"," { return new_symbol(sym.COMMA,yytext()); }
"(" { return new_symbol(sym.LPAREN,yytext()); } 
")" { return new_symbol(sym.RPAREN,yytext()); }
"{" { return new_symbol(sym.LBRACE,yytext()); }
"}" { return new_symbol(sym.RBRACE,yytext()); }
"[" { return new_symbol(sym.LSQBRACE,yytext()); }
"]" { return new_symbol(sym.RSQBRACE,yytext()); }

"//".* {}

"true" { return new_symbol(sym.BOOL_CONST,Boolean.TRUE); }
"false" { return new_symbol(sym.BOOL_CONST,Boolean.FALSE); }
[0-9]+ { return new_symbol(sym.NUMBER, Integer.valueOf(yytext())); }
[a-zA-Z][a-zA-Z0-9_]* { return new_symbol(sym.IDENT, yytext()); }
"'"[ -~]"'" { return new_symbol(sym.CHAR_CONST,Character.valueOf(yytext().charAt(1))); }
. { System.out.println("Lexing ERROR (" + yytext()+") at line "+(yyline+1)); }