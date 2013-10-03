/*
 * Read files in comma separated value format.
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
 * Read files in comma separated value format.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/CSVLexer.html">ostermiller.org</a>.
 * This interface is designed to be set of general methods that all
 * CSV parsers should implement.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public interface CSVParse {

	/**
	 * Read the next value from the file.  The line number from
	 * which this value was taken can be obtained from getLastLineNumber().
	 *
	 * @return the next value or null if there are no more values.
	 * @throws IOException if an error occurs while reading.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String nextValue() throws IOException;

	/**
	 * Get the line number that the last token came from.
	 *
	 * @return line number or -1 if no tokens have been returned yet.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int lastLineNumber();

	/**
	 * Get all the values from a line.
	 * <p>
	 * If the line has already been partially read, only the
	 * values that have not already been read will be included.
	 *
	 * @return all the values from the line or null if there are no more values.
	 * @throws IOException if an error occurs while reading.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String[] getLine() throws IOException;

	/**
	 * Get the line number that the last token came from.
	 * <p>
	 * New line breaks that occur in the middle of a token are not
	 * counted in the line number count.
	 *
	 * @return line number or -1 if no tokens have been returned yet.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int getLastLineNumber();

	/**
	 * Get all the values from the file.
	 * <p>
	 * If the file has already been partially read, only the
	 * values that have not already been read will be included.
	 * <p>
	 * Each line of the file that has at least one value will be
	 * represented.  Comments and empty lines are ignored.
	 * <p>
	 * The resulting double array may be jagged.
	 *
	 * @return all the values from the file or null if there are no more values.
	 * @throws IOException if an error occurs while reading.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String[][] getAllValues() throws IOException;


	/**
	 * Change this parser so that it uses a new delimiter.
	 * <p>
	 * The initial character is a comma, the delimiter cannot be changed
	 * to a quote or other character that has special meaning in CSV.
	 *
	 * @param newDelim delimiter to which to switch.
	 * @throws BadDelimiterException if the character cannot be used as a delimiter.
	 *
	 * @since ostermillerutils 1.02.08
	 */
	public void changeDelimiter(char newDelim) throws BadDelimiterException;

	/**
	 * Change this parser so that it uses a new character for quoting.
	 * <p>
	 * The initial character is a double quote ("), the delimiter cannot be changed
	 * to a comma or other character that has special meaning in CSV.
	 *
	 * @param newQuote character to use for quoting.
	 * @throws BadQuoteException if the character cannot be used as a quote.
	 *
	 * @since ostermillerutils 1.02.16
	 */
	public void changeQuote(char newQuote) throws BadQuoteException;


	/**
	 * Close any stream upon which this parser is based.
	 *
	 * @since ostermillerutils 1.02.26
	 * @throws IOException if an error occurs while closing the stream.
	 */
	public void close() throws IOException;

}
