/* CGILexer.java is a generated file.  You probably want to
 * edit CGILexer.lex to make changes.  Use JFlex to generate it.
 * JFlex may be obtained from
 * <a href="http://jflex.de">the JFlex website</a>.
 * JFlex 1.4 or later is required.
 * Run:<br>
 * jflex CGILexer.lex<br>
 * You will then have a file called CGILexer.java
 */

/*
 * Parse CGI query data.
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
 * Parses query string data from a CGI request into name value pairs.
 * <p>
 * This class has a <a href="http://ostermiller.org/utils/CGIParser.html">website</a>
 * where more information and examples are
 * available.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
%%
%class CGILexer
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
			CGILexer shredder = new CGILexer(in);
			String t;
			while ((t = shredder.nextToken()) != null) {
				System.out.println(t);
			}
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}

    
    /**
     * Return the next token from the cgi data.
     *
     * @return the next token
     * @throws IOException if an error occurs while reading the cgi data.
     */
    public String nextToken() throws IOException {
        return getToken();
    }

	private StringBuffer token = new StringBuffer();
%}

%unicode

NameValue=([^\&\=]*(\=[^&]*)?)

%%

<YYINITIAL> {NameValue} {
	return yytext();
}

<YYINITIAL> (\&) {
}
