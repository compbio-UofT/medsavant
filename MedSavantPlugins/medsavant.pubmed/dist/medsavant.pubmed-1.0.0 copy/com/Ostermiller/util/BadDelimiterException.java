/*
 * Binary data exception.
 * Copyright (C) 2003 Stephen Ostermiller
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
 * Signals that binary data was encountered and continuing
 * with a text operation would likely corrupt the data.
 * <p>
 * This class exists to fix a spelling error in BadDelimeterException.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.02.20
 * @see BadDelimeterException
 */
public class BadDelimiterException extends BadDelimeterException {

	/**
	 * Constructs an IOException with null as its error detail message.
	 *
	 * @since ostermillerutils 1.02.20
	 */
	public BadDelimiterException(){
		super();
	}

	/**
	 * Constructs an exception with the specified detail message.
	 * The error message string s can later be retrieved by the
	 * Throwable.getMessage()  method of class java.lang.Throwable.
	 *
	 * @param s the detail message.
	 *
	 * @since ostermillerutils 1.02.20
	 */
	public BadDelimiterException(String s){
		super(s);
	}
}
