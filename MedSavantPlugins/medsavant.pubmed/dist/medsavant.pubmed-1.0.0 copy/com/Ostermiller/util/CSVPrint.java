/*
 * Write files in comma separated value format.
 * Copyright (C) 2002-2004 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * Copyright (C) 2003 Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
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
 * Print values as a comma separated list.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/CSVLexer.html">ostermiller.org</a>.
 * This interface is designed to be set of general methods that all
 * CSV printers should implement.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
 * @since ostermillerutils 1.00.00
 */
public interface CSVPrint {

	/**
	 * Change this printer so that it uses a new delimiter.
	 *
	 * @param newDelimiter The new delimiter character to use.
	 * @throws BadDelimiterException if the character cannot be used as a delimiter.
	 *
	 * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
	 * @since ostermillerutils 1.02.18
	 */
	public void changeDelimiter(char newDelimiter) throws BadDelimiterException;

	/**
	 * Change this printer so that it uses a new character for quoting.
	 *
	 * @param newQuote The new character to use for quoting.
	 * @throws BadQuoteException if the character cannot be used as a quote.
	 *
	 * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
	 * @since ostermillerutils 1.02.18
	 */
	public void changeQuote(char newQuote) throws BadQuoteException;

	/**
	 * Set flushing behavior.  Iff set, a flush command
	 * will be issued to any underlying stream after each
	 * print or write command.
	 *
	 * @param autoFlush should auto flushing be enabled.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void setAutoFlush(boolean autoFlush);

	/**
	 * Flush the stream if it's not closed and check its error state.
	 * Errors are cumulative; once the stream encounters an error,
	 * this routine will return true on all successive calls.
	 *
	 * @return True if the print stream has encountered an error,
	 * either on the underlying output stream or during a format conversion.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public boolean checkError();

	/**
	 * Print the string as the last value on the line.	The value
	 * will be quoted if needed.  If value is null, an empty value is printed.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writeln method.
	 *
	 * @param value value to be outputted.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void println(String value);

	/**
	 * Print the string as the last value on the line.	The value
	 * will be quoted if needed.  If value is null, an empty value is printed.
	 *
	 * @param value value to be outputted.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writeln(String value) throws IOException;

	/**
	 * Output a blank line.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writeln method.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void println();

	/**
	 * Output a blank line.
	 *
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writeln() throws IOException;

	/**
	 * Print a single line of comma separated values.
	 * The values will be quoted if needed.  Quotes and
	 * and other characters that need it will be escaped.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writeln method.
	 *
	 * @param values values to be outputted.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void println(String[] values);

	/**
	 * Print a single line of comma separated values.
	 * The values will be quoted if needed.  Quotes and
	 * and other characters that need it will be escaped.
	 *
	 * @param values values to be outputted.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writeln(String[] values) throws IOException;

	/**
	 * Print several lines of comma separated values.
	 * The values will be quoted if needed.  Quotes and
	 * newLine characters will be escaped.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writeln method.
	 *
	 * @param values values to be outputted.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void println(String[][] values);

	/**
	 * Print several lines of comma separated values.
	 * The values will be quoted if needed.  Quotes and
	 * newLine characters will be escaped.
	 *
	 * @param values values to be outputted.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writeln(String[][] values) throws IOException;

	/**
	 * If the CSV format supports comments, write the comment
	 * to the file on its own line, otherwise, start a new line.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writelnComment method.
	 *
	 * @param comment the comment to output.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void printlnComment(String comment);

	/**
	 * If the CSV format supports comments, write the comment
	 * to the file on its own line, otherwise, start a new line.
	 *
	 * @param comment the comment to output.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writelnComment(String comment) throws IOException;

	/**
	 * Print the string as the next value on the line.	The value
	 * will be quoted if needed.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding println method.
	 *
	 * @param value value to be outputted.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void print(String value);

	/**
	 * Print the string as the next value on the line.	The value
	 * will be quoted if needed.
	 *
	 * @param value value to be outputted.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void write(String value) throws IOException;

	/**
	 * Flush any data written out to underlying streams.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void flush() throws IOException;

	/**
	 * Close any underlying streams.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void close() throws IOException;

	/**
	 * Print multiple delimited values values.
	 * The values will be quoted if needed.  Quotes and
	 * and other characters that need it will be escaped.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding write method.
	 *
	 * @param values values to be outputted.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void print(String[] values);

	/**
	 * Print multiple delimited values values.
	 * The values will be quoted if needed.  Quotes and
	 * and other characters that need it will be escaped.
	 *
	 * @param values values to be outputted.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void write(String[] values) throws IOException;

	/**
	 * Set whether values printers should always be quoted, or
	 * whether the printer may, at its discretion, omit quotes
	 * around the value.
	 *
	 * @param alwaysQuote true if quotes should be used even when not strictly needed.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void setAlwaysQuote(boolean alwaysQuote);
}
