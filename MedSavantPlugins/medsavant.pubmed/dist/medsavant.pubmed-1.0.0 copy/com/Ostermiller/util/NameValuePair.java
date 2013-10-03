/*
 * Name Value Pair
 * Copyright (C) 2004 Stephen Ostermiller
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Represents a name value pair as would be used as a CGI parameter.
 * <p>
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.03.00
 */
public class NameValuePair {

	/**
	 * Name of the pair.
	 */
	private String name;

	/**
	 * Value of the pair.
	 */
	private String value;

	/**
	 * Construct a name value pair.
	 *
	 * @param name name of the pair.
	 * @param value value of the pair.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public NameValuePair (String name, String value){
		if (name == null) name = "";
		if (value == null) value = "";
		this.name = name;
		this.value = value;
	}

	/**
	 * Get the name of the pair.
	 *
	 * @return the name of the pair.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String getName(){
		return name;
	}

	/**
	 * Get the value of the pair.
	 *
	 * @return the value of the pair.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String getValue(){
		return value;
	}

	/**
	 * Get the name and value as CGI parameters, URL Encoded to UTF-8.
	 *
	 * @return CGI appropriate representation of the pair.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String toString(){
		try {
			return toString("UTF-8");
		} catch (UnsupportedEncodingException uex){
			// UTF-8 Should be acceptable
			throw new RuntimeException(uex);
		}
	}

	/**
	 * Get the name and value as CGI parameters, URL Encoded to the given encoding.
	 *
	 * @param charset Character set to use when URL Encoding.
	 * @return CGI appropriate representation of the pair.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String toString(String charset) throws UnsupportedEncodingException {
		return URLEncoder.encode(name, charset) + "=" + URLEncoder.encode(value, charset);
	}
}
