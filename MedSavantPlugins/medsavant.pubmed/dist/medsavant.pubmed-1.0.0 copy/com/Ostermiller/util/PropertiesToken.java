/*
 * This file is part of UberProperties
 * Copyright (C) 2002 Stephen Ostermiller
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

/**
 * A PropertiesToken is a token that is returned by a lexer that is lexing a Java
 * Properties file.  It has several attributes describing the token:
 * The type of token, the text of the token, the line number on which it
 * occurred, the number of characters into the input at which it started, and
 * similarly, the number of characters into the input at which it ended. <br>
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
class PropertiesToken {

	public final static int COMMENT = 0x0;
	public final static int END_LINE_WHITE_SPACE = 0x1;
	public final static int WHITE_SPACE = 0x2;
	public final static int SEPARATOR = 0x3;
	public final static int CONTINUE_LINE = 0x4;
	public final static int NAME = 0x5;
	public final static int VALUE = 0x6;

	private int ID;
	private String contents;

	/**
	 * Create a new token.
	 * The constructor is typically called by the lexer
	 *
	 * @param ID the id number of the token
	 * @param contents A string representing the text of the token
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public PropertiesToken(int ID, String contents){
		this.ID = ID;
		this.contents = contents;
	}

	/**
	 * get the ID number of this token
	 *
	 * @return the id number of the token
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int getID(){
		return ID;
	}

	/**
	 * get the contents of this token
	 *
	 * @return A string representing the text of the token
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String getContents(){
		return (contents);
	}

	public String toString(){
			String idString = "";
			switch (ID){
				case COMMENT: idString = "COMMENT"; break;
				case END_LINE_WHITE_SPACE: idString = "END_LINE_WHITE_SPACE"; break;
				case WHITE_SPACE: idString = "WHITE_SPACE"; break;
				case SEPARATOR: idString = "SEPARATOR"; break;
				case CONTINUE_LINE: idString = "CONTINUE_LINE"; break;
				case NAME: idString = "NAME"; break;
				case VALUE: idString = "VALUE"; break;
			}
			idString = StringHelper.postpad(idString, 21);
		return idString + '"' + contents + '"';
	}
}
