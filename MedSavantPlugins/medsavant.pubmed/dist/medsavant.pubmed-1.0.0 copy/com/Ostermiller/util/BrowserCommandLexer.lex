/* BrowserCommandLexer.java is a generated file.  You probably want to
 * edit BrowserCommandLexer to make changes.  Use JFlex to generate it.
 * JFlex may be obtained from
 * <a href="http://www.jflex.de">the JFlex website</a>.
 * Jflex 1.4 or later is required.
 * Run:<br>
 * jflex BrowserCommandLexer<br>
 * You will then have a file called BrowserCommandLexer.java
 */

/*
 * Tokenize a command line into application and arguments.
 * Copyright (C) 2001-2004 Stephen Ostermiller
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
 * Tokenize a command line into application and arguments.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
%%
%class BrowserCommandLexer
%apiprivate
%function getToken
%type String
%{
	/**
	 * Prints out tokens and line numbers from a file or System.in.
	 * If no arguments are given, System.in will be used for input.
	 * If more arguments are given, the first argument will be used as
	 * the name of the file to use as input
	 *
	 * @param args program arguments, of which the first is a filename
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static void main(String[] args) {
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
			BrowserCommandLexer shredder = new BrowserCommandLexer(in);
			String t;
			while ((t = shredder.getNextToken()) != null) {
				System.out.println(t);
			}
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}
    
    /**
     * Return the next token from the browser command.
     *
     * @return the next token
     * @throws IOException if an error occurs while reading the command.
     */
    public String getNextToken() throws IOException {
        return getToken();
    }

	private static String unescape(String s){
		StringBuffer sb = new StringBuffer(s.length());
		for (int i=0; i<s.length(); i++){
			if (s.charAt(i) == '\\' && i<s.length()){
				i++;
			}
			sb.append(s.charAt(i));
		}
		return sb.toString();
	}
%}

%unicode

AnyChar=([A]|[^A])
Escape=([\\]{AnyChar})
NonQuoted=((([^\t\f\r\n\\ ])|{Escape})*)
Quoted=([\"]([^\"]|{Escape})*[\"])

%%

<YYINITIAL> ({Quoted}) {
	return unescape(yytext().substring(1, yytext().length()-1));
}

<YYINITIAL> ({NonQuoted}) {
	return unescape(yytext());
}

<YYINITIAL> {AnyChar} {
}
