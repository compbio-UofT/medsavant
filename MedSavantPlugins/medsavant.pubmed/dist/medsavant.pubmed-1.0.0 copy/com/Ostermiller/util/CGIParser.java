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
import java.util.*;
import java.io.*;
import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * Parses query string data from a CGI request into name value pairs.
 * <p>
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/CGIParser.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class CGIParser {

	/**
	 * Hash of names to Array Lists of values.
	 */
	private HashMap nameValuePairHash = new HashMap();

	/**
	 * Array list of NameValuePair objects.
	 */
	private LinkedList nameValuePairList = new LinkedList();

	/**
	 * Extract the name, value pairs from the given input stream and
	 * make them available for retrieval.
	 * <p>
	 * The stream is read until the stream contains no more bytes.
	 * <p>
	 * Byte to character conversion on the stream is done according the platforms
	 * default character encoding.
	 *
	 * @param in Stream containing CGI Encoded name value pairs.
	 * @deprecated This method does not decode URLEncoded values properly.  Please use a method that specifies a character set.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public CGIParser(InputStream in) throws IOException {
		if (in == null) return;
		CGILexer lex = new CGILexer(in);
		parse(lex, "ISO-8859-1");
	}

	/**
	 * Extract the name, value pairs from the given input stream and
	 * make them available for retrieval.
	 * <p>
	 * The stream is read until the stream contains no more bytes.
	 * <p>
	 * The character set is used both when converting the byte stream to
	 * a character stream and when decoding URL decoded parameters.
	 *
	 * @param in Stream containing CGI Encoded name value pairs.
	 * @param charset Character encoding to use when converting bytes to characters
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public CGIParser(InputStream in, String charset) throws IOException, UnsupportedEncodingException {
		if (in == null) return;
		CGILexer lex = new CGILexer(new InputStreamReader(in, charset));
		parse(lex, charset);
	}

	/**
	 * Extract the name, value pairs from the given reader and
	 * make them available for retrieval.
	 * <p>
	 * The reader is read until the stream contains no more characters.
	 *
	 * @param in Reader containing CGI Encoded name value pairs.
	 * @deprecated This method does not decode URLEncoded values properly.  Please use a method that specifies a character set.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public CGIParser(Reader in) throws IOException {
		if (in == null) return;
		CGILexer lex = new CGILexer(in);
		parse(lex, "ISO-8859-1");
	}

	/**
	 * Extract the name, value pairs from the given reader and
	 * make them available for retrieval.
	 * <p>
	 * The reader is read until the stream contains no more characters.
	 *
	 * @param in Reader containing CGI Encoded name value pairs.
	 * @param charset Character encoding to use when converting bytes to characters
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public CGIParser(Reader in, String charset) throws IOException, UnsupportedEncodingException {
		if (in == null) return;
		CGILexer lex = new CGILexer(in);
		parse(lex, charset);
	}

	/**
	 * Extract the name, value pairs from the given string and
	 * make them available for retrieval.
	 *
	 * @param s CGI Encoded name value pairs.
	 * @deprecated This method does not decode URLEncoded values properly.  Please use a method that specifies a character set.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public CGIParser(String s){
		if (s == null) return;
		try {
			CGILexer lex = new CGILexer(new StringReader(s));
			parse(lex, "ISO-8859-1");
		} catch (IOException x){
			// This shouldn't be able to happen from a string.
			throw new RuntimeException(x);
		}
	}

	/**
	 * Extract the name, value pairs from the given string and
	 * make them available for retrieval.
	 *
	 * @param s CGI Encoded name value pairs.
	 * @param charset Character encoding to use when converting bytes to characters
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public CGIParser(String s, String charset) throws IOException, UnsupportedEncodingException {
		if (s == null) return;
		try {
			CGILexer lex = new CGILexer(new StringReader(s));
			parse(lex, charset);
		} catch (IOException x){
			// This shouldn't be able to happen from a string.
			throw new RuntimeException(x);
		}
	}

	/**
	 * Retrieve the name value pairs from the lexer and hash
	 * them in the cgi hashtable.
	 *
	 * @param lex Lexer that will return the tokens.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private void parse(CGILexer lex, String charset) throws IOException, UnsupportedEncodingException {
		String nameValue, name, value;
		while ((nameValue = lex.nextToken()) != null) {
			int equalInd = nameValue.indexOf("=");
			if (equalInd == -1){
				name = nameValue;
				value = "";
			} else {
				name = nameValue.substring(0, equalInd);
				value = nameValue.substring(equalInd+1, nameValue.length());
			}
			try {
				name = URLDecoder.decode(name, charset);
			} catch (IllegalArgumentException iax){
				// May be thrown for for illegal escape sequences such as %s
				name = "";
			}
			try {
				value = URLDecoder.decode(value, charset);
			} catch (IllegalArgumentException iax){
				// May be thrown for for illegal escape sequences such as %s
				value = "";
			}

			// Hash
			ArrayList values = (ArrayList)nameValuePairHash.get(name);
			if (values == null){
				values = new ArrayList();
			}
			values.add(value);
			nameValuePairHash.put(name, values);

			// List
			nameValuePairList.add(new NameValuePair(name, value));
		}
	}

	/**
	 * Returns an array of String objects containing all of the values the given request
	 * parameter has, or null if the parameter does not exist.
	 * <p>
	 * If the parameter has a single value, the array has a length of 1.
	 * @param name a String containing the name of the parameter whose value is requested
	 * @return an array of String objects containing the parameter's values
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String[] getParameterValues(String name){
		ArrayList values = (ArrayList)nameValuePairHash.get(name);
		if (values == null){
			return null;
		}
		String[] valArray = new String[values.size()];
		return (String[])(values.toArray(valArray));
	}

	/**
	 * Set a name value pair as used in a URL.
	 * This method will replace any previously defined values with the single
	 * value specified. If the value is null, the name is removed.
	 *
	 * @param name a String specifying the name of the parameter.
	 * @param value a String specifying the value of the single parameter, or null to remove.
	 *
	 * @since ostermillerutils 1.02.15
	 */
	public void setParameter(String name, String value){
		Iterator listIterator = nameValuePairList.iterator();
		while (listIterator.hasNext()){
			NameValuePair nameValuePair = (NameValuePair)listIterator.next();
			if (nameValuePair.getName().equals(name)){
				listIterator.remove();
			}
		}

		if (value == null){
			nameValuePairHash.remove(name);
			return;
		}
		ArrayList values = (ArrayList)nameValuePairHash.get(name);
		if (values == null){
			values = new ArrayList();
		}
		values.clear();
		values.add(value);
		nameValuePairHash.put(name, values);
		nameValuePairList.add(new NameValuePair(name, value));
	}

	/**
	 * Set a name value pair as used in a URL.
	 * This method will replace any previously defined values with the single
	 * value specified. If values is null or empty, the name is removed.
	 *
	 * @param name a String specifying the name of the parameter.
	 * @param values a String array specifying the values for the parameter, or null to remove.
	 * @throws NullPointerException if any of the values is null.
	 *
	 * @since ostermillerutils 1.02.15
	 */
	public void setParameter(String name, String[] values){
		Iterator listIterator = nameValuePairList.iterator();
		while (listIterator.hasNext()){
			NameValuePair nameValuePair = (NameValuePair)listIterator.next();
			if (nameValuePair.getName().equals(name)){
				listIterator.remove();
			}
		}

		if (values == null || values.length == 0){
			nameValuePairHash.remove(name);
			return;
		}
		for (int i=0; i<values.length; i++){
			if (values[i] == null) throw new NullPointerException();
		}
		ArrayList valuesVec = (ArrayList)nameValuePairHash.get(name);
		if (valuesVec == null){
			valuesVec = new ArrayList();
		}
		valuesVec.clear();
		for (int i=0; i<values.length; i++){
			valuesVec.add(values[i]);
			nameValuePairList.add(new NameValuePair(name, values[i]));
		}
		nameValuePairHash.put(name, valuesVec);
	}

	/**
	 * Set a name value pair as used in a URL.
	 * This method will add to any previously defined values the values
	 * specified. If value is null, this method has no effect.
	 *
	 * @param name a String specifying the name of the parameter.
	 * @param value a String specifying the value of the single parameter, or null to remove.
	 *
	 * @since ostermillerutils 1.02.15
	 */
	public void addParameter(String name, String value){
		if (value == null){
			return;
		}
		ArrayList values = (ArrayList)nameValuePairHash.get(name);
		if (values == null){
			values = new ArrayList();
		}
		values.add(value);
		nameValuePairHash.put(name, values);
		nameValuePairList.add(new NameValuePair(name, value));
	}

	/**
	 * Set a name value pair as used in a URL.
	 * This method will add to any previously defined values the values
	 * specified. If values is null, this method has no effect.
	 *
	 * @param name a String specifying the name of the parameter.
	 * @param values a String array specifying the values of the parameter, or null to remove.
	 * @throws NullPointerException if any of the values is null.
	 *
	 * @since ostermillerutils 1.02.15
	 */
	public void addParameter(String name, String[] values){
		if (values == null || values.length == 0){
			return;
		}
		for (int i=0; i<values.length; i++){
			if (values[i] == null) throw new NullPointerException();
		}
		ArrayList valuesVec = (ArrayList)nameValuePairHash.get(name);
		if (valuesVec == null){
			valuesVec = new ArrayList();
		}
		for (int i=0; i<values.length; i++){
			valuesVec.add(values[i]);
			nameValuePairList.add(new NameValuePair(name, values[i]));
		}
		nameValuePairHash.put(name, valuesVec);
	}

	/**
	 * Returns the value of a request parameter as a String, or null if the parameter does
	 * not exist. Request parameters are extra information sent with the request.
	 * <p>
	 * You should only use this method when you are sure the parameter has only one value.
	 * If the parameter might have more than one value, use getParameterValues(java.lang.String).
	 * <P>
	 * If you use this method with a multivalued parameter, the value returned is equal to
	 * the first value in the array returned by getParameterValues.
	 *
	 * @param name a String specifying the name of the parameter
	 * @return a String representing the single value of the parameter
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String getParameter(String name){
		ArrayList values = (ArrayList)nameValuePairHash.get(name);
		if (values == null){
			return null;
		}
		return (String)(values.get(0));
	}

	/**
	 * Returns an Enumeration of String  objects containing the names of the
	 * parameters contained in this request. If the request has no parameters,
	 * the method returns an empty Enumeration.
	 *
	 * @return an Enumeration of String objects, each String containing the name
	 *     of a request parameter; or an empty Enumeration if the request has
	 *     no parameters
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public Enumeration getParameterNames(){
		return new IteratorEnumeration(nameValuePairHash.keySet().iterator());
	}

	/**
	 * Returns the names of the
	 * parameters contained in this request. If the request has no parameters,
	 * the method returns an empty String array.  Each name will appear only
	 * once, even if it was contained in the request multiple times.  The order
	 * of the names may not match the order from the request.
	 *
	 * @return An array of Strings, each of which is the name
	 *     of a request parameter; or an array of length zero if the request has
	 *     no parameters
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String[] getParameterNameList(){
		return (String[])nameValuePairHash.keySet().toArray(new String[0]);
	}

	/**
	 * Get the all the parameters in the order in which they were added.
	 *
	 * @return array of all name value pairs.
	 */
	public NameValuePair[] getParameters(){
		return (NameValuePair[])nameValuePairList.toArray(new NameValuePair[0]);
	}

	/**
	 * Returns the name value pairs properly escaped and written in URL format.
	 *
	 * @param enc Character encoding to use when escaping characters.
	 * @return URLEncoded name value pairs.
	 * @throws  UnsupportedEncodingException If the named encoding is not supported.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String toString(String enc) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		boolean bFirst = true;
		Iterator listIterator = nameValuePairList.iterator();
		while (listIterator.hasNext()){
			NameValuePair nameValuePair = (NameValuePair)listIterator.next();
			if (!bFirst) sb.append('&');
			bFirst = false;
			sb.append(nameValuePair.toString(enc));
		}
		return sb.toString();
	}

	/**
	 * Returns the name value pairs properly escaped and written in URL format
	 * with UTF-8 URL encoding.
	 *
	 * @return URLEncoded name value pairs.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String toString() {
		try {
			return toString("UTF-8");
		} catch (UnsupportedEncodingException uex){
			// UTF-8 should be supported
			throw new RuntimeException(uex);
		}
	}
}
