/*
 * A souped up version of the Java Properties format which can
 * handle multiple properties with the same name.
 * Copyright (C) 2002-2003 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * Copyright (C) 2003 Carlo Magnaghi <software at tecnosoft dot net>
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

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * The Properties class represents a persistent set of properties. The
 * Properties can be saved to a stream or loaded from a stream. Each key and
 * its corresponding value in the property list is a string.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/UberProperties.html">ostermiller.org</a>.
 * <p>
 * A property list can contain another property list as its "defaults"; this
 * second property list is searched if the property key is not found in the
 * original property list.
 * <p>
 * When saving properties to a stream or loading them from a stream, the ISO
 * 8859-1 character encoding is used. For characters that cannot be directly
 * represented in this encoding, Unicode escapes are used; however, only a
 * single 'u' character is allowed in an escape sequence. The native2ascii tool
 * can be used to convert property files to and from other character encodings.
 * <p>
 * Unlike the java.util.Properties, UberProperties does not inherit from
 * java.util.Hashtable, so Objects other than strings cannot be stored in it.
 * Also, comments from a files are preserved, and there can be several
 * properties for a given name.
 * <p>
 * This class is not synchronized, so it should not be used in a
 * multi-threaded environment without external synchronization.
 * <p>
 * The file format that UberProperties uses is as follows:
 * <blockquote>
 * The file is assumed to be using the ISO 8859-1 character encoding. All of the
 * comment lines (starting with a '#' or '!') at the beginning of the file before the
 * first line that is not a comment, are the comment associated with the file.
 * After that, each comment will be associated with the next property.  If there
 * is more than one property with the same name, the first comment will be the
 * only one that is loaded.
 * <p>
 * Every property occupies one line of the input stream. Each line is terminated
 * by a line terminator (\n or \r or \r\n).
 * <p>
 * A line that contains only whitespace or whose first non-whitespace character
 * is an ASCII # or ! is ignored (thus, # or ! indicate comment lines).
 * <p>
 * Every line other than a blank line or a comment line describes one property
 * to be added to the table (except that if a line ends with \, then the
 * following line, if it exists, is treated as a continuation line,
 * as described below). The key consists of all the characters in the line
 * starting with the first non-whitespace character and up to, but not
 * including, the first ASCII =, :, or whitespace character. All of the key
 * termination characters may be included in the key by preceding them with a \.
 * Any whitespace after the key is skipped; if the first non-whitespace
 * character after the key is = or :, then it is ignored and any whitespace
 * characters after it are also skipped. All remaining characters on the line
 * become part of the associated element string. Within the element string, the
 * ASCII escape sequences \t, \n, \r, \\, \", \', \ (a backslash and a space),
 * and \\uxxxx are recognized and converted to single characters. Moreover, if
 * the last character on the line is \, then the next line is treated as a
 * continuation of the current line; the \ and line terminator are simply
 * discarded, and any leading whitespace characters on the continuation line are
 * also discarded and are not part of the element string.
 * <p>
 * As an example, each of the following four lines specifies the key "Truth"
 * and the associated element value "Beauty":<br>
 * <pre>Truth = Beauty
 * 	   Truth:Beauty
 *   Truth			:Beauty</pre>
 * <p>
 * As another example, the following three lines specify a single property:<br>
 * <pre>fruits				apple, banana, pear, \
 *                                cantaloupe, watermelon, \
 *                                kiwi, mango</pre>
 * <p>
 * The key is "fruits" and the associated element is:<br>
 * "apple,&nbsp;banana,&nbsp;pear,&nbsp;cantaloupe,&nbsp;watermelon,&nbsp;kiwi,&nbsp;mango"<br>
 * Note that a space appears before each \ so that a space will appear after
 * each comma in the final result; the \, line terminator, and leading
 * whitespace on the continuation line are merely discarded and are not replaced
 * by one or more other characters.
 * <p>
 * As a third example, the line:<br>
 * cheeses<br>
 * specifies that the key is "cheeses" and the associated element is the empty
 * string.
 * </blockquote>
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class UberProperties {

	/**
	 * A hash map that contains all the properties.
	 * This should never be null, but may be empty.
	 * This should hold objects of type Property.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private HashMap properties = new HashMap();

	/**
	 * Comment for this set of properties.
	 * This may be either null or empty.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private String comment = null;

	/**
	 * The object type that goes in the HashMap.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private class Property {

		/**
		 * List of values for this property.
		 * This should never be null or empty.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		private ArrayList list;

		/**
		 * Comment for this set of properties.
		 * This may be either null or empty.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		private String comment = null;

		/**
		 * Set the comment associated with this property.
		 *
		 * @param comment the comment for this property, or null to clear.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public void setComment(String comment){
			this.comment = comment;
		}

		/**
		 * Get the comment associated with this property.
		 *
		 * @return comment for this property, or null if none is set.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public String getComment(){
			return this.comment;
		}

		/**
		 * Construct a new property with the given value.
		 *
		 * @param value initial value for this property.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public Property(String value){
			list = new ArrayList(1);
			add(value);
		}

		/**
		 * Construct a new property with the given values.
		 *
		 * @param values initial values for this property.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public Property(String[] values){
			list = new ArrayList(values.length);
			add(values);
		}

		/**
		 * Set this property to have this single value.
		 *
		 * @param value lone value for this property.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public void set(String value){
			list.clear();
			add(value);
		}

		/**
		 * Set this property to have only these values.
		 *
		 * @param values lone values for this property.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public void set(String[] values){
			list.clear();
			add(values);
		}

		/**
		 * Add this value to the list of values for this property.
		 *
		 * @param value another value for this property.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public void add(String value){
			list.add(value);
		}

		/**
		 * Add these values to the list of values for this property.
		 *
		 * @param values other values for this property.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public void add(String[] values){
			list.ensureCapacity(list.size() + values.length);
			for (int i=0; i<values.length; i++){
				add(values[i]);
			}
		}

		/**
		 * Get the last value for this property.
		 *
		 * @return the last value.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public String getValue(){
			return (String)list.get(list.size() - 1);
		}

		/**
		 * Get all the values for this property.
		 *
		 * @return a list of all the values.
		 *
		 * @since ostermillerutils 1.00.00
		 */
		public String[] getValues(){
			return (String[])list.toArray(new String[list.size()]);
		}
	}

	/**
	 * Creates an empty property list with no default values.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public UberProperties(){
	}

	/**
	 * Creates an empty property list with the specified defaults.
	 *
	 * @param defaults the defaults.
	 * @throws NullPointerException if defaults is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public UberProperties(UberProperties defaults){
		merge(defaults);
	}

	/**
	 * Put all the properties from the defaults in this.
	 * Calling this from a constructor will clone (deep)
	 * the default properties.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private void merge(UberProperties defaults){
		setComment(defaults.getComment());
		String[] names = defaults.propertyNames();
		for (int i=0; i<names.length; i++){
			setProperties(names[i], defaults.getProperties(names[i]));
			setComment(names[i], defaults.getComment(names[i]));
		}
	}

	/**
	 * Test to see if a property with the given name exists.
	 *
	 * @param name the name of the property.
	 * @return true if the property existed and was removed, false if it did not exist.
	 * @throws NullPointerException if name is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public boolean contains(String name){
		if (name == null) throw new NullPointerException();
		return properties.containsKey(name);
	}

	/**
	 * Remove any property with the given name.
	 *
	 * @param name the name of the property.
	 * @return true if the property existed and was removed, false if it did not exist.
	 * @throws NullPointerException if name is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public boolean remove(String name){
		if (!contains(name)) return false;
		properties.remove(name);
		return true;
	}

	/**
	 * Replaces all properties of the given name with
	 * a single property with the given value.
	 *
	 * @param name the name of the property.
	 * @param value the value of the property, or null to remove it.
	 * @throws NullPointerException if name is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setProperty(String name, String value){
		if (name == null) throw new NullPointerException();
		if (value == null){
			properties.remove(name);
		} else {
			Property property;
			if (properties.containsKey(name)){
				property = (Property)properties.get(name);
				property.set(value);
			} else {
				property = new Property(value);
				properties.put(name, property);
			}
		}
	}

	/**
	 * Replaces all properties of the given name with
	 * properties with the given values.
	 *
	 * @param name the name of the property.
	 * @param values for the property.
	 * @throws NullPointerException if name is null.
	 * @throws NullPointerException if values is null.
	 * @throws IllegalArgumentException if values is empty.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setProperties(String name, String[] values){
		if (name == null) throw new NullPointerException();
		if (values.length == 0) throw new IllegalArgumentException();
		Property property;
		if (properties.containsKey(name)){
			property = (Property)properties.get(name);
			property.set(values);
		} else {
			property = new Property(values);
			properties.put(name, property);
		}
	}

	/**
	 * Replaces all properties of the given name with
	 * a single property with the given value.
	 *
	 * @param name the name of the property.
	 * @param value the value of the property or null to remove it.
	 * @param comment the comment for the property, or null to remove it.
	 * @throws NullPointerException if name is null.
	 * @throws NullPointerException if comment is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setProperty(String name, String value, String comment){
		if (name == null) throw new NullPointerException();
		if (value == null){
			properties.remove(name);
		} else {
			setProperty(name, value);
			setComment(name, comment);
		}
	}

	/**
	 * Replaces all properties of the given name with
	 * properties with the given values.
	 *
	 * @param name the name of the property.
	 * @param values value of the property.
	 * @param comment the comment for the property, or null to remove it.
	 * @throws NullPointerException if name is null.
	 * @throws NullPointerException if values is null.
	 * @throws IllegalArgumentException if values is empty.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setProperties(String name, String[] values, String comment){
		if (name == null) throw new NullPointerException();
		if (values.length == 0) throw new IllegalArgumentException();
		setProperties(name, values);
		setComment(name, comment);
	}

	/**
	 * Set the comment on the property of the given name.
	 * The property must exist before this method is called.
	 *
	 * @param name the name of the property.
	 * @param comment the comment for the property.
	 * @param comment the comment for the property, or null to remove it.
	 * @throws NullPointerException if name is null.
	 * @throws IllegalArgumentException if name is not a known key.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private void setComment(String name, String comment){
		if (name == null) throw new NullPointerException();
		if (!properties.containsKey(name)) throw new IllegalArgumentException();
		((Property)properties.get(name)).setComment(comment);
	}

	/**
	 * Adds a value to the list of properties with the
	 * given name.
	 *
	 * @param name the name of the property.
	 * @param value the values for the property, or null to remove.
	 * @param comment the comment for the property, or null to remove it.
	 * @throws NullPointerException if name is null.
	 * @throws NullPointerException if value is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void addProperty(String name, String value, String comment){
		if (name == null) throw new NullPointerException();
		if (value == null) throw new NullPointerException();
		addProperty(name, value);
		setComment(name, comment);
	}

	/**
	 * Adds the values to the list of properties with the
	 * given name.
	 *
	 * @param name the name of the property.
	 * @param values the values for the property.
	 * @param comment the comment for the property, or null to remove it.
	 * @throws NullPointerException if name is null.
	 * @throws NullPointerException if values is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void addProperties(String name, String[] values, String comment){
		if (name == null) throw new NullPointerException();
		if (values == null) throw new NullPointerException();
		addProperties(name, values);
		setComment(name, comment);
	}

	/**
	 * Adds a value to the list of properties with the
	 * given name.
	 *
	 * @param name the name of the property.
	 * @param value the values for the property.
	 * @throws NullPointerException if name is null.
	 * @throws NullPointerException if value is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void addProperty(String name, String value){
		if (name == null) throw new NullPointerException();
		if (value == null) throw new NullPointerException();
		Property property;
		if (properties.containsKey(name)){
			property = (Property)properties.get(name);
			property.add(value);
		} else {
			property = new Property(value);
			properties.put(name, property);
		}
	}

	/**
	 * Adds the values to the list of properties with the
	 * given name.
	 *
	 * @param name the name of the property.
	 * @param values the values for the property.
	 * @throws NullPointerException if name is null.
	 * @throws NullPointerException if values is null.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void addProperties(String name, String[] values){
		if (name == null) throw new NullPointerException();
		if (values == null) throw new NullPointerException();
		Property property;
		if (properties.containsKey(name)){
			property = (Property)properties.get(name);
			property.add(values);
		} else {
			property = new Property(values);
			properties.put(name, property);
		}
	}

	private static int hexDigitValue(char c){
		switch (c){
			case '0': return 0;
			case '1': return 1;
			case '2': return 2;
			case '3': return 3;
			case '4': return 4;
			case '5': return 5;
			case '6': return 6;
			case '7': return 7;
			case '8': return 8;
			case '9': return 9;
			case 'a': case 'A': return 10;
			case 'b': case 'B': return 11;
			case 'c': case 'C': return 12;
			case 'd': case 'D': return 13;
			case 'e': case 'E': return 14;
			case 'f': case 'F': return 15;
			default: return -1;
		}
	}

	private static String unescape(String s){
		StringBuffer sb = new StringBuffer(s.length());
		for(int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if (c == '\\'){
				i++;
				if (i < s.length()){
					c = s.charAt(i);
					switch (c){
						case 'n': {
							sb.append('\n');
						} break;
						case 'r': {
							sb.append('\r');
						} break;
						case 't': {
								sb.append('\t');
						} break;
						case 'f': {
							sb.append('\f');
						} break;
						case 'u': {
							boolean foundUnicode = false;
							if (i+4 < s.length()){
								int unicodeValue = 0;
								for (int j = 3; unicodeValue >= 0 && j >= 0; j--){
									int val = hexDigitValue(s.charAt(i+(4-j)));
									if (val == -1){
										unicodeValue = -1;
									} else {
										unicodeValue |= (val << (j << 2));
									}
								}
								if (unicodeValue >= 0) {
									i+=4;
										foundUnicode = true;
										sb.append((char)unicodeValue);
								}
							}
							if (!foundUnicode) sb.append(c);
						} break;
						default: {
							sb.append(c);
						} break;
					}
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Load these properties from a user file with default properties
	 * from a system resource.
	 * <p>
	 * Ex:
	 * <pre>load(
	 *     new String(){".java","tld","company","package","component.properties"}
		 *     "tld/company/package/component.properties",
	 * )</pre>
	 * This will load the properties file relative to the classpath as the
	 * defaults and the file &lt;%userhome%&gt;/.java/tld/company/package/component.properties
	 * if the file exists.  The .java directory is recommended as it is a common,
	 * possibly hidden, directory in the users home directory commonly used by
	 * Java programs.
	 *
	 * This method is meant to be used with the save(String systemResource) method
	 * which will save modified properties back to the user directory.
	 *
	 * @param userFile array of Strings representing a path and file name relative to the user home directory.
	 * @param systemResource name relative to classpath of default properties, or null to ignore.
	 * @throws IOException if an error occurs when reading.
	 * @throws NullPointerException if userFile is null.
	 * @throws IllegalArgumentException if userFile is empty.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void load(String[] userFile, String systemResource) throws IOException {
		int length = userFile.length;
		if (userFile.length == 0) throw new IllegalArgumentException();
		InputStream in = ClassLoader.getSystemResourceAsStream(systemResource);
		if (in==null) throw new FileNotFoundException(systemResource);
		if (systemResource != null) load(in);
		File f = new File(System.getProperty("user.home"));
		for (int i=0; f.exists() && i<length; i++){
			f = new File(f, userFile[i]);
		}
		if (f.exists()) load(new FileInputStream(f));
	}

	/**
	 * Add the properties from the input stream to this
	 * UberProperties.
	 *
	 * @param in InputStream containing properties.
	 * @param add whether parameters should add to parameters with the same name or replace them.
	 * @throws IOException if an error occurs when reading.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void load(InputStream in, boolean add) throws IOException {
		PropertiesLexer lex = new PropertiesLexer(new InputStreamReader(in, "ISO-8859-1"));
		PropertiesToken t;
		HashSet names = new HashSet();
		StringBuffer comment = new StringBuffer();
		boolean foundComment = false;
		StringBuffer name = new StringBuffer();
		StringBuffer value = new StringBuffer();
		int last = TYPE_COMMENT;
		boolean atStart = true;
		String lastSeparator = null;
		while ((t = lex.getNextToken()) != null){
			if (t.getID() == PropertiesToken.COMMENT){
				int start = 1;
				String commentText = t.getContents();
				if (commentText.startsWith("# ")) start = 2;
				comment.append(commentText.substring(start, commentText.length()));
				comment.append("\n");
				lex.getNextToken();
				foundComment = true;
			} else if (t.getID() == PropertiesToken.NAME){
				if (atStart){
					setComment(comment.toString());
					comment.setLength(0);
					atStart = false;
				}
				name.append(t.getContents());
			} else if (t.getID() == PropertiesToken.VALUE){
				if (atStart){
					setComment(comment.toString());
					comment.setLength(0);
					atStart = false;
				}
				value.append(t.getContents());
			} else if (t.getID() == PropertiesToken.SEPARATOR){
				lastSeparator = t.getContents();
			} else if (t.getID() == PropertiesToken.END_LINE_WHITE_SPACE){
				if (atStart){
					setComment(comment.toString());
					comment.setLength(0);
					atStart = false;
				}
				String stName = unescape(name.toString());
				String stValue = unescape(value.toString());
				if (lastSeparator != null || stName.length() > 0 || stValue.length() > 0 ){
					if (add || names.contains(stName)){
						addProperty(stName, stValue);
					} else {
						setProperty(stName, stValue);
						names.add(stName);
					}
					if (foundComment) setComment(stName, unescape(comment.toString()));
				}
				comment.setLength(0);
				name.setLength(0);
				value.setLength(0);
				foundComment = false;
				lastSeparator = null;
			}
		}
	}

	/**
	 * Add the properties from the input stream to this
	 * UberProperties.
	 * <p>
	 * Properties that are found replace any properties that
	 * were there before.
	 *
	 * @param in InputStream containing properties.
	 * @throws IOException if an error occurs when reading.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void load(InputStream in) throws IOException {
		load(in, false);
	}

	/**
	 * Save these properties from a user file.
	 * <p>
	 * Ex:
	 * <pre>save(
	 *     new String(){"tld","company","package","component.properties"}
	 * )</pre>
	 * This will save the properties file relative to the user directory:
	 * &lt;%userhome%&gt;/tld/company/package/component.properties
	 * Directories will be created as needed.
	 *
	 * @param userFile array of Strings representing a path and file name relative to the user home directory.
	 * @throws IOException if an error occurs when reading.
	 * @throws NullPointerException if userFile is null.
	 * @throws IllegalArgumentException if userFile is empty.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void save(String[] userFile) throws IOException {
		int length = userFile.length;
		if (length == 0) throw new IllegalArgumentException();
		File f = new File(System.getProperty("user.home"));
		for (int i=0; i<length; i++){
			f = new File(f, userFile[i]);
			if (i == length - 2 && !f.exists()){
				f.mkdirs();
			}
		}
		OutputStream out = new FileOutputStream(f);
		save(out);
		out.close();
	}

	/**
	 * Save these properties to the given stream.
	 *
	 * @param out OutputStream to which these properties should be written.
	 * @throws IOException if an error occurs when writing.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void save(OutputStream out) throws IOException {
		writeComment(out, comment);
		out.write('\n');
		String[] names = propertyNames();
		Arrays.sort(names);
		for (int i=0; i<names.length; i++){
			writeComment(out, getComment(names[i]));
			String[] values = getProperties(names[i]);
			for (int j=0; j<values.length; j++){
				writeProperty(out, names[i], values[j]);
			}
		}
		out.flush();
	}

	private static void writeProperty(OutputStream out, String name, String value) throws IOException {
		writeEscapedISO88591(out, name, TYPE_NAME);
		out.write('=');
		writeEscapedISO88591(out, value, TYPE_VALUE);
		out.write('\n');

	}

	private static void writeComment(OutputStream out, String comment) throws IOException {
		if (comment != null){
			java.util.StringTokenizer tok = new java.util.StringTokenizer(comment, "\r\n");
			while (tok.hasMoreTokens()){
				out.write('#');
				out.write(' ');
				writeEscapedISO88591(out, tok.nextToken(), TYPE_COMMENT);
				out.write('\n');
						}
		}
	}

	private static final int TYPE_COMMENT = 0;
	private static final int TYPE_NAME = 1;
	private static final int TYPE_VALUE = 2;

	private static void writeEscapedISO88591(OutputStream out, String s, int type) throws IOException {
		for (int i=0; i<s.length(); i++){
			int c = (int)s.charAt(i);
			if (c < 0x100){
				boolean escape = false;
				if (c == '\r' || c == '\n' || c == '\\'){
					escape = true;
				} else if (c == ' ' || c == '\t' || c == '\f'){
					if(type == TYPE_NAME){
						escape = true;
					} else if (type == TYPE_VALUE && (i==0 || i == s.length() - 1)){
						escape = true;
					}
				} else if (type == TYPE_NAME && (c == '=' || c == ':')){
					escape = true;
				}
				if (escape){
					switch (c){
						case '\n': {
							switch (type){
								case TYPE_COMMENT: {
									out.write('\n');
									out.write('#');
									out.write(' ');
								} break;
								case TYPE_NAME: {
									out.write('\\');
									out.write('n');
									out.write('\\');
									out.write('\n');
									out.write('\t');
								} break;
								case TYPE_VALUE: {
									out.write('\\');
									out.write('n');
									out.write('\\');
									out.write('\n');
									out.write('\t');
									out.write('\t');
								} break;
							}
						} break;
						case '\\': {
							out.write('\\');
							out.write('\\');
						} break;
						case '\r': {
							out.write('\\');
							out.write('r');
						} break;
						case '\t': {
							out.write('\\');
							out.write('t');
						} break;
						case '\f': {
							out.write('\\');
							out.write('f');
						} break;
						default : {
							out.write('\\');
							out.write((byte)c);
						} break;
					}
				} else {
					out.write((byte)c);
				}
			} else {
				out.write('\\');
				out.write('u');
				out.write(StringHelper.prepad(Integer.toHexString(c), 4, '0').getBytes("ISO-8859-1"));
			}
		}
	}

	/**
	 * Get the first property with the given name.
	 * If the property is not specified in this UberProperties
	 * but it is in the default UberProperties, the default is
	 * used.  If no default is found, null is returned.
	 *
	 * @return the first value of this property, or null if the property does not exist.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String getProperty(String name){
		String value = null;
		if (properties.containsKey(name)){
			value = ((Property)properties.get(name)).getValue();
		}
		return value;
	}

	/**
	 * Get the first property with the given name.
	 * If the property is not specified in this UberProperties
	 * but it is in the default UberProperties, the default
	 * UberProperties is consulted, otherwise, the supplied
	 * default is used.
	 *
	 * @return the first value of this property.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String getProperty(String name, String defaultValue){
		String value = getProperty(name);
		if (value == null) value = defaultValue;
		return value;
	}

	/**
	 * Get the values for a property.
	 * Properties returned in the same order in which
	 * they were added.
	 * <p>
	 * If the property is not specified in this UberProperties
	 * but it is in the default UberProperties, the default is
	 * used.  If no default is found, null is returned.
	 *
	 * @return all the values associated with the given key, or null if the property does not exist.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String[] getProperties(String name){
		String[] values = null;
		if (properties.containsKey(name)){
			values = ((Property)properties.get(name)).getValues();
		}
		return values;
	}

	/**
	 * Get the values for a property.
	 * Properties returned in the same order in which
	 * they were added.
	 * <p>
	 * If the property is not specified in this UberProperties
	 * but it is in the default UberProperties, the default
	 * UberProperties is consulted, otherwise, the supplied
	 * defaults are used.
	 *
	 * @return all the values associated with the given key, or null if the property does not exist.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String[] getProperties(String name, String[] defaultValues){
		String[] values = getProperties(name);
		if (values == null) values = defaultValues;
		return values;
	}

	/**
	 * Get the comment associated with this property.
	 * <p>
	 * If the property is not specified in this UberProperties
	 * but it is in the default UberProperties, the default is
	 * used.  If no default is found, null is returned.
	 *
	 * @return the comment for this property, or null if there is no comment or the property does not exist.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String getComment(String name){
		String comment = null;
		if (properties.containsKey(name)){
			comment = ((Property)properties.get(name)).getComment();
		}
		return comment;
	}

	/**
	 * Returns an enumeration of all the keys in this property list, including
	 * distinct keys in the default property list if a key of the same name has
	 * not already been found from the main properties list.
	 *
	 * @return an enumeration of all the keys in this property list, including the keys in the default property list.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String[] propertyNames(){
		Set names = properties.keySet();
		return (String[])names.toArray(new String[names.size()]);
	}

	/**
	 * Set the comment associated with this set of properties.
	 *
	 * @param comment the comment for entire set of properties, or null to clear.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void setComment(String comment){
		this.comment = comment;
	}

	/**
	 * Get the comment associated with this set of properties.
	 *
	 * @return comment for entire set of properties, or null if there is no comment.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String getComment(){
		return this.comment;
	}

	/**
	 * Get the number of unique names for properties stored
	 * in this UberProperties.
	 *
	 * @return number of names.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int getPropertyNameCount(){
		return properties.keySet().size();
	}

	/**
	 * Save these properties to a string.
	 *
	 * @return Serialized String version of these properties.
	 *
	 * @since ostermillerutils 1.02.23
	 */
	public String toString(){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			this.save(out);
		} catch (IOException iox){
			throw new Error("IO constructed on memory, this shouldn't happen.", iox);
		}
		String s = null;
		try {
			s = new String(out.toByteArray(), "ISO-8859-1");
		} catch (UnsupportedEncodingException uee){
			throw new Error("ISO-8859-1 should be recognized.", uee);
		}
		return s;
	}
}
