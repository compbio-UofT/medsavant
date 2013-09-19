/*
 * Write files in Excel comma separated value format.
 * Copyright (C) 2001-2004 Stephen Ostermiller
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
 * Print values as a comma separated list that can be read by the
 * Excel spreadsheet.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/ExcelCSV.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
 * @since ostermillerutils 1.00.00
 */
public class ExcelCSVPrinter implements CSVPrint {

	/**
	 * If auto flushing is enabled.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	protected boolean autoFlush = true;

	/**
	 * If auto flushing is enabled.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	protected boolean alwaysQuote = false;

	/**
	 * true iff an error has occurred.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	protected boolean error = false;

	/**
	 * Delimiter character written.
	 *
	 * @since ostermillerutils 1.02.18
	 */
	protected char delimiterChar = ',';

	/**
	 * Quoting character written.
	 *
	 * @since ostermillerutils 1.02.18
	 */
	protected char quoteChar = '"';

	/**
	 * The place that the values get written.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected Writer out;

	/**
	 * True iff we just began a new line.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	protected boolean newLine = true;

	/**
	 * Create a printer that will print values to the given
	 * stream.	 Character to byte conversion is done using
	 * the default character encoding.  The delimiter will
	 * be the comma, the quote character will be double quotes,
	 * quotes will be used when needed, and auto flushing
	 * will be enabled.
	 *
	 * @param out stream to which to print.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public ExcelCSVPrinter(OutputStream out){
		this.out = new OutputStreamWriter(out);
	}

	/**
	 * Create a printer that will print values to the given
	 * stream.	  The delimiter will
	 * be the comma, the quote character will be double quotes,
	 * quotes will be used when needed, and auto flushing
	 * will be enabled.
	 *
	 * @param out stream to which to print.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public ExcelCSVPrinter(Writer out){
		this.out = out;
	}

	/**
	 * Create a printer that will print values to the given
	 * stream.	  The delimiter will
	 * be the comma, and the quote character will be double quotes.
	 *
	 * @param out stream to which to print.
	 * @param alwaysQuote true if quotes should be used even when not strictly needed.
	 * @param autoFlush should auto flushing be enabled.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public ExcelCSVPrinter(Writer out, boolean alwaysQuote, boolean autoFlush){
		this.out = out;
		setAlwaysQuote(alwaysQuote);
		setAutoFlush(autoFlush);
	}

	/**
	 * Create a printer that will print values to the given
	 * stream.	Quotes will be used when needed, and auto flushing
	 * will be enabled.
	 *
	 * @param out stream to which to print.
	 * @param delimiter The new delimiter character to use.
	 * @param quote The new character to use for quoting.
	 * @throws BadQuoteException if the character cannot be used as a quote.
	 * @throws BadDelimiterException if the character cannot be used as a delimiter.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public ExcelCSVPrinter(Writer out, char quote, char delimiter) throws BadDelimiterException, BadQuoteException {
		this.out = out;
		changeQuote(quote);
		changeDelimiter(delimiter);
	}

	/**
	 * Create a printer that will print values to the given
	 * stream.
	 *
	 * @param out stream to which to print.
	 * @param delimiter The new delimiter character to use.
	 * @param quote The new character to use for quoting.
	 * @param alwaysQuote true if quotes should be used even when not strictly needed.
	 * @param autoFlush should auto flushing be enabled.
	 * @throws BadQuoteException if the character cannot be used as a quote.
	 * @throws BadDelimiterException if the character cannot be used as a delimiter.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public ExcelCSVPrinter(Writer out, char quote, char delimiter, boolean alwaysQuote, boolean autoFlush) throws BadDelimiterException, BadQuoteException {
		this.out = out;
		changeQuote(quote);
		changeDelimiter(delimiter);
		setAlwaysQuote(alwaysQuote);
		setAutoFlush(autoFlush);
	}

	/**
	 * Change this printer so that it uses a new delimiter.
	 *
	 * @param newDelimiter The new delimiter character to use.
	 * @throws BadDelimiterException if the character cannot be used as a delimiter.
	 *
	 * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
	 * @since ostermillerutils 1.02.18
	 */
	public void changeDelimiter(char newDelimiter) throws BadDelimiterException {
		if (delimiterChar == newDelimiter) return; // no need to do anything.
		if (newDelimiter == '\n' || newDelimiter == '\r' ||
				newDelimiter == delimiterChar || newDelimiter == quoteChar){
			throw new BadDelimiterException();
		}
		delimiterChar = newDelimiter;
	}

	/**
	 * Change this printer so that it uses a new character for quoting.
	 *
	 * @param newQuote The new character to use for quoting.
	 * @throws BadQuoteException if the character cannot be used as a quote.
	 *
	 * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
	 * @since ostermillerutils 1.02.18
	 */
	public void changeQuote(char newQuote) throws BadQuoteException {
		if (quoteChar == newQuote) return; // no need to do anything.
		if (newQuote == '\n' || newQuote == '\r' ||
				newQuote == delimiterChar || newQuote == quoteChar){
			throw new BadQuoteException();
		}
		quoteChar = newQuote;
	}

	/**
	 * Print the string as the last value on the line.	The value
	 * will be quoted if needed.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writeln method.
	 *
	 * @param value value to be outputted.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void println(String value){
		try {
			writeln(value);
		} catch (IOException iox){
			error = true;
		}
	}

	/**
	 * Print the string as the last value on the line.	The value
	 * will be quoted if needed.
	 *
	 * @param value value to be outputted.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writeln(String value) throws IOException {
		try {
			write(value);
			writeln();
		} catch (IOException iox){
			error = true;
			throw iox;
		}
	}

	/**
	 * Output a blank line.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writeln method.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void println(){
		try {
			writeln();
		} catch (IOException iox){
			error = true;
		}
	}

	/**
	 * Output a blank line.
	 *
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writeln() throws IOException {
		try {
			out.write("\n");
			if (autoFlush) flush();
			newLine = true;
		} catch (IOException iox){
			error = true;
			throw iox;
		}
	}

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
	public void println(String[] values){
		try {
			writeln(values);
		} catch (IOException iox){
			error = true;
		}
	}

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
	public void writeln(String[] values) throws IOException {
		try {
			print(values);
			writeln();
		} catch (IOException iox){
			error = true;
			throw iox;
		}
	}

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
	public void print(String[] values){
		try {
			write(values);
		} catch (IOException iox){
			error = true;
		}
	}

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
	public void write(String[] values) throws IOException {
		try {
			for (int i=0; i<values.length; i++){
				write(values[i]);
			}
		} catch (IOException iox){
			error = true;
			throw iox;
		}
	}

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
	public void println(String[][] values){
		try {
			writeln(values);
		} catch (IOException iox){
			error = true;
		}
	}

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
	public void writeln(String[][] values) throws IOException {
		try {
			for (int i=0; i<values.length; i++){
				writeln(values[i]);
			}
			if (values.length == 0){
				writeln();
			}
		} catch (IOException iox){
			error = true;
			throw iox;
		}
	}

	/**
	 * Since ExcelCSV format does not support comments,
	 * this method will ignore the comment and start
	 * a new row.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding writelnComment method.
	 *
	 * @param comment the comment to output (ignored).
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void printlnComment(String comment){
		println();
	}

	/**
	 * Since ExcelCSV format does not support comments,
	 * this method will ignore the comment and start
	 * a new row.
	 *
	 * @param comment the comment to output (ignored).
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void writelnComment(String comment) throws IOException {
		writeln();
	}

	/**
	 * Print the string as the next value on the line.	The value
	 * will be quoted if needed.  If value is null, an empty value is printed.
	 * <p>
	 * This method never throws an I/O exception. The client may inquire as to whether
	 * any errors have occurred by invoking checkError().  If an I/O Exception is
	 * desired, the client should use the corresponding println method.
	 *
	 * @param value value to be outputted.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public void print(String value){
		try {
			write(value);
		} catch (IOException iox){
			error = true;
		}
	}

	/**
	 * Print the string as the next value on the line.	The value
	 * will be quoted if needed.  If value is null, an empty value is printed.
	 *
	 * @param value value to be outputted.
	 * @throws IOException if an error occurs while writing.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void write(String value) throws IOException {
		try {
			if (value == null) value = "";
			boolean quote = false;
			if (alwaysQuote){
				quote = true;
			} else if (value.length() > 0){
				for (int i=0; i<value.length(); i++){
					char c = value.charAt(i);
					if (c==quoteChar || c==delimiterChar || c=='\n' || c=='\r'){
						quote = true;
					}
				}
			} else if (newLine) {
				// always quote an empty token that is the firs
				// on the line, as it may be the only thing on the
				// line.  If it were not quoted in that case,
				// an empty line has no tokens.
				quote = true;
			}
			if (newLine){
				newLine = false;
			} else {
				out.write(delimiterChar);
			}
			if (quote){
				out.write(escapeAndQuote(value));
			} else {
				out.write(value);
			}
			if (autoFlush) flush();
		} catch (IOException iox){
			error = true;
			throw iox;
		}
	}

	/**
	 * Enclose the value in quotes and escape the quote
	 * and comma characters that are inside.
	 *
	 * @param value needs to be escaped and quoted.
	 *
	 * @return the value, escaped and quoted.
	 * @since ostermillerutils 1.00.00
	 */
	private String escapeAndQuote(String value){
		String s = StringHelper.replace(value, Character.toString(quoteChar), Character.toString(quoteChar) + Character.toString(quoteChar));
		return (new StringBuffer(2 + s.length())).append(quoteChar).append(s).append(quoteChar).toString();
	}

	/**
	 * Flush any data written out to underlying streams.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void flush() throws IOException {
		out.flush();
	}

	/**
	 * Close any underlying streams.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void close() throws IOException {
		out.close();
	}

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
	public boolean checkError(){
		try {
			if (error) return true;
			flush();
			if (error) return true;
			if (out instanceof PrintWriter){
				error = ((PrintWriter)out).checkError();
			}
		} catch (IOException iox){
			error = true;
		}
		return error;
	}

	/**
	 * Set whether values printers should always be quoted, or
	 * whether the printer may, at its discretion, omit quotes
	 * around the value.
	 *
	 * @param alwaysQuote true if quotes should be used even when not strictly needed.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void setAlwaysQuote(boolean alwaysQuote){
		this.alwaysQuote = alwaysQuote;
	}

	/**
	 * Set flushing behavior.  Iff set, a flush command
	 * will be issued to any underlying stream after each
	 * print or write command.
	 *
	 * @param autoFlush should auto flushing be enabled.
	 *
	 * @since ostermillerutils 1.02.26
	 */
	public void setAutoFlush(boolean autoFlush){
		this.autoFlush = autoFlush;
	}

	/**
	 * Write some test data to the given file.
	 *
	 * @param args First argument is the file name.  System.out used if no filename given.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private static void main(String[] args) {
		OutputStream out;
		try {
			if (args.length > 0){
				File f = new File(args[0]);
				if (!f.exists()){
					f.createNewFile();
					if (f.canWrite()){
						out = new FileOutputStream(f);
					} else {
						throw new IOException("Could not open " + args[0]);
					}
				} else {
					throw new IOException("File already exists: " + args[0]);
				}
			} else {
				out = System.out;
			}
			ExcelCSVPrinter p  = new ExcelCSVPrinter(out);
			p.print("unquoted");
			p.print("escaped\"quote");
			p.println("comma,comma");
			p.print("!quoted");
			p.print("!unquoted");
			p.print(" quoted");
			p.println("quoted ");
			p.print("one");
			p.print("");
			p.print("");
			p.print("");
			p.println("");
			p.println("two");
			p.print("\nthree\nline\n");
			p.println("\ttab");
		} catch (IOException e){
			System.out.println(e.getMessage());
		}
	}
}
