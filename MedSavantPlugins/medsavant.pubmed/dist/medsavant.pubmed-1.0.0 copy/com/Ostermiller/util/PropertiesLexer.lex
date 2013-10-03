/* PropertiesLexer.java is a generated file.  You probably want to
 * edit PropertiesLexer.lex to make changes.  Use JFlex to generate it.
 * To generate PropertiesLexer.java
 * JFlex may be obtained from
 * <a href="http://jflex.de">the JFlex website</a>.
 * JFlex 1.4 or later is required.
 * Run:<br>
 * jflex  PropertiesLexer.lex<br>
 * You will then have a file called PropertiesLexer.java
 */

/*
 * Goes with UberProperties
 * Copyright (C) 2002-2004 Stephen Ostermiller 
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

package com.Ostermiller.util;

import java.io.*;

/** 
 * PropertiesLexer is a Java Properties file lexer.  Created with JFlex.  An example of how it is used:
 *  <CODE>
 *  <PRE>
 *  PropertiesLexer shredder = new PropertiesLexer(System.in);
 *  PropertiesToken t;
 *  while ((t = shredder.getNextToken()) != null){
 *	  System.out.println(t);
 *  }
 *  </PRE>
 *  </CODE>
 *
 * @see PropertiesToken
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */ 

%%

%class PropertiesLexer
%apiprivate
%function getToken
%type PropertiesToken
%unicode

%{
	private int lastToken;
	private int nextState=YYINITIAL;
		
	/**
	 * Prints out tokens from a file or System.in.
	 * If no arguments are given, System.in will be used for input.
	 * If more arguments are given, the first argument will be used as
	 * the name of the file to use as input
	 *
	 * @param args program arguments, of which the first is a filename
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public static void main(String[] args) {
		InputStream in;
		try {
			if (args.length > 0){
				File f = new File(args[0]);
				if (f.exists()){
					if (f.canRead()){
						in = new FileInputStream(f);
					} else {
						throw new IOException("Could not open " + args[0]);
					}
				} else {
					throw new IOException("Could not find " + args[0]);
				}
			} else {
				in = System.in;
			}
			PropertiesLexer shredder = new PropertiesLexer(in);
			PropertiesToken t;
			while ((t = shredder.getNextToken()) != null) {
				if (true || t.getID() != PropertiesToken.WHITE_SPACE){
					System.out.println(t);
				}
			}
		} catch (IOException e){
			System.err.println(e.getMessage());
		}
	}  

    /**
     * Return the next token from the properties.
     *
     * @return the next token
     * @throws IOException if an error occurs while reading the properties.
     */
    public PropertiesToken getNextToken() throws IOException {
        return getToken();
    }

%}

%full

%state LINE_END
%state WHITE_SPACE
%state NAME
%state NAME_SPACE
%state MID_NAME
%state MID_NAME_NEW_LINE
%state SEPARATOR
%state VALUE
%state MID_VALUE
%state DONE

HexDigit=([0-9a-fA-F])
BLANK=([ ])
TAB=([\t])
FF=([\f])
EscChar=([\\])
CR=([\r])
LF=([\n])
EOL=({CR}|{LF}|{CR}{LF})
WhiteSpace=({BLANK}|{TAB}|{FF})
LineEndingWhiteSpace=({EOL})
fourHex=({HexDigit}{4})
UnicodeEscape=({EscChar}[u]({fourHex}*))
Escape=(({EscChar}[^\r\n])|{UnicodeEscape})
LineEscape=({EscChar}{EOL})

NameText=([^\=\:\t\f\r\n\\ ]|{Escape})
NameTextWSeparators=([^\t\f\r\n\\ ]|{Escape})
ValueText=([^\r\n\\]|{Escape})
Comment=({WhiteSpace}*[\!\#][^\r\n]*)
Name=({NameText}*)
Value=((({NameText}+){ValueText}*)?)
FullValue=((({NameTextWSeparators}+){ValueText}*)?)

%%

<YYINITIAL> {Comment} { 
	nextState = LINE_END;
	lastToken = PropertiesToken.COMMENT;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<YYINITIAL> {WhiteSpace}+ {
	nextState = WHITE_SPACE;	
	lastToken = PropertiesToken.WHITE_SPACE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<NAME, NAME_SPACE> {WhiteSpace}+ {
	nextState = NAME_SPACE;
	lastToken = PropertiesToken.WHITE_SPACE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<SEPARATOR, VALUE, MID_VALUE> {WhiteSpace}+ {
	lastToken = PropertiesToken.WHITE_SPACE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	return (t);
}
<YYINITIAL,WHITE_SPACE,LINE_END, NAME, NAME_SPACE, SEPARATOR, VALUE, NAME, MID_NAME, MID_NAME_NEW_LINE, MID_VALUE> {LineEndingWhiteSpace} {
	nextState = YYINITIAL;
	lastToken = PropertiesToken.END_LINE_WHITE_SPACE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<YYINITIAL,WHITE_SPACE,LINE_END, NAME, NAME_SPACE, SEPARATOR, VALUE, NAME, MID_NAME, MID_NAME_NEW_LINE, MID_VALUE> <<EOF>> {
	nextState = DONE;
	lastToken = PropertiesToken.END_LINE_WHITE_SPACE;
	PropertiesToken t = new PropertiesToken(lastToken,"");
	yybegin(nextState);
	return (t);
}
<DONE> ([^]*) {
	return null;
}
<YYINITIAL, WHITE_SPACE, MID_NAME, MID_NAME_NEW_LINE> {Name} {
	nextState = NAME;
	lastToken = PropertiesToken.NAME;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<YYINITIAL, WHITE_SPACE, NAME, NAME_SPACE, MID_NAME, MID_NAME_NEW_LINE> ":"|"=" {
	nextState = SEPARATOR;
	lastToken = PropertiesToken.SEPARATOR;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<NAME, NAME_SPACE> {Value} {
	nextState = VALUE;
	lastToken = PropertiesToken.VALUE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<NAME_SPACE> ({LineEscape}) {
	lastToken = PropertiesToken.CONTINUE_LINE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	return (t);
}
<NAME, MID_NAME, MID_NAME_NEW_LINE> ({LineEscape}) {
	nextState = MID_NAME_NEW_LINE;
	lastToken = PropertiesToken.CONTINUE_LINE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<MID_NAME_NEW_LINE> ({WhiteSpace}+) {
	nextState = MID_NAME;
	lastToken = PropertiesToken.WHITE_SPACE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<SEPARATOR, MID_VALUE> {FullValue} {
	nextState = VALUE;
	lastToken = PropertiesToken.VALUE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<SEPARATOR, VALUE, MID_VALUE> ({LineEscape}) {
	nextState = MID_VALUE;
	lastToken = PropertiesToken.CONTINUE_LINE;
	String text = yytext();
	PropertiesToken t = new PropertiesToken(lastToken,text);
	yybegin(nextState);
	return (t);
}
<YYINITIAL,LINE_END,WHITE_SPACE,NAME,NAME_SPACE,SEPARATOR,VALUE,MID_NAME,MID_NAME_NEW_LINE,MID_VALUE> {EscChar} {
	// Ignore escape characters at the end of the file.
}
<YYINITIAL,LINE_END,WHITE_SPACE,NAME,NAME_SPACE,SEPARATOR,VALUE,MID_NAME,MID_NAME_NEW_LINE,MID_VALUE> [^] {
	System.err.println("Unmatched input.");
	String state = "";	
	String text = yytext();
	switch (nextState){
		case YYINITIAL: state = "YYINITIAL"; break;
		case LINE_END: state = "LINE_END"; break;
		case WHITE_SPACE: state = "WHITE_SPACE"; break;
		case NAME: state = "NAME"; break;
		case SEPARATOR: state = "SEPARATOR"; break;
		case VALUE: state = "VALUE"; break;
		case MID_NAME: state = "MID_NAME"; break;
		case NAME_SPACE: state = "NAME_SPACE"; break;
	}
	System.err.println("State: '" + state + "'");
	System.err.println("Text: '" + text + "'");
	zzScanError(ZZ_NO_MATCH);
}
