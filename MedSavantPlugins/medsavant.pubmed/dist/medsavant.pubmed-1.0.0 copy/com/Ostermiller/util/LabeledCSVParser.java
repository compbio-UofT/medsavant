/*
 * Read files in comma separated value format with a fist line of labels.
 *
 * Copyright (C) 2004 Campbell, Allen T. <allenc28@yahoo.com>
 *
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Decorate a CSVParse object to provide an index of field names.  Many (most?)
 * CSV files have a list of field names (labels) as the first line.  A
 * LabeledCSVParser will consume this line automatically.  The methods
 * {@link #getLabels()}, {@link #getLabelIndex(String)} and
 * {@link #getValueByLabel(String)} allow these labels to be discovered and
 * used while parsing CSV data.  This class can also be used to conveniently
 * ignore field labels if they happen to be present in a CSV file and are not
 * desired.
 *
 * @author Campbell, Allen T. <allenc28@yahoo.com>
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.03.00
 */
public class LabeledCSVParser implements CSVParse {

	/**
	 * Class which actually does the parsing.  Called for most methods.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	private CSVParse parse;

	/**
	 * The first line of the CSV file - treated specially as labels.
	 * Set by setLabels.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	private String[] labels;

	/**
	 * Hash of the labels (String) to column number (Integer).
	 * Set by setLabels.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	private Map labelMap;

	/**
	 * The last line read from the CSV file.  Saved for getValueByLabel().
	 *
	 * @since ostermillerutils 1.03.00
	 */
	private String[] lastLine;

	/**
	 * Set whenever nextValue is called and checked when getValueByLabel() is
	 * called to enforce incompatibility between the methods.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	private int nextValueLine = -2;

	/**
	 * Construct a LabeledCSVParser on a CSVParse implementation.
	 *
	 * @param parse CSVParse implementation
	 * @throws IOException if an error occurs while reading.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public LabeledCSVParser(CSVParse parse) throws IOException {
		this.parse = parse;
		setLabels();
	}

	/**
	 * Change this parser so that it uses a new delimiter.
	 * <p>
	 * The initial character is a comma, the delimiter cannot be changed
	 * to a quote or other character that has special meaning in CSV.
	 *
	 * @param newDelim delimiter to which to switch.
	 * @throws BadDelimiterException if the character cannot be used as a delimiter.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public void changeDelimiter(char newDelim) throws BadDelimiterException {
		parse.changeDelimiter(newDelim);
	}

	/**
	 * Change this parser so that it uses a new character for quoting.
	 * <p>
	 * The initial character is a double quote ("), the delimiter cannot be changed
	 * to a comma or other character that has special meaning in CSV.
	 *
	 * @param newQuote character to use for quoting.
	 * @throws BadQuoteException if the character cannot be used as a quote.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public void changeQuote(char newQuote) throws BadQuoteException {
		parse.changeQuote(newQuote);
	}

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
	 * <p>
	 * The last line of the values is saved and may be accessed
	 * by getValueByLabel().
	 *
	 * @return all the values from the file or null if there are no more values.
	 * @throws IOException if an error occurs while reading.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String[][] getAllValues() throws IOException {
		String[][] allValues = parse.getAllValues();
		lastLine = allValues[allValues.length-1];
		return allValues;
	}

	/**
	 * Get the line number that the last token came from.
	 * <p>
	 * New line breaks that occur in the middle of a token are not
	 * counted in the line number count.
	 * <p>
	 * The first line of labels does not count towards the line number.
	 *
	 * @return line number or -1 if no tokens have been returned yet.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public int getLastLineNumber(){
		return lastLineNumber();
	}

	/**
	 * Get the line number that the last token came from.
	 * <p>
	 * New line breaks that occur in the middle of a token are not
	 * counted in the line number count.
	 * <p>
	 * The first line of labels does not count towards the line number.
	 *
	 * @return line number or -1 if no tokens have been returned yet.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public int lastLineNumber(){
		int lineNum = parse.getLastLineNumber();
		if (lineNum <= -1) return -1; // Nothing has been read yet
		if (lineNum == 1) return -1; // only labels have been read
		return lineNum - 1; // adjust line number to account for the label line
	}

	/**
	 * Get all the values from a line.
	 * <p> 
	 * If the line has already been partially read, only the values that have not
	 * already been read will be included. 
	 * <p>
	 * In addition to returning all the values from a line, LabeledCSVParser
	 * maintains a buffer of the values.  This feature allows
	 * {@link #getValueByLabel(String)} to function.  In this case
	 * {@link #getLine()} is used simply to iterate CSV data.  The iteration ends
	 * when null is returned.
	 * <p>
	 * <b>Note:</b> The methods {@link #nextValue()} and {@link #getAllValues()}
	 * are incompatible with {@link #getValueByLabel(String)} because the former
	 * methods cause the offset of field values to shift and corrupt the internal
	 * buffer maintained by {@link #getLine}.
	 *
	 * @return all the values from the line or null if there are no more values.
	 * @throws IOException if an error occurs while reading.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String[] getLine() throws IOException {
		lastLine = parse.getLine();
		return lastLine;
	}

	/**
	 * Read the next value from the file.  The line number from
	 * which this value was taken can be obtained from getLastLineNumber().
	 * <p>
	 * This method is not compatible with getValueByLabel().  Using this
	 * method will make getValueByLabel() throw an IllegalStateException
	 * for the rest of the line.
	 *
	 * @return the next value or null if there are no more values.
	 * @throws IOException if an error occurs while reading.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String nextValue() throws IOException {
		String nextValue = parse.nextValue();
		nextValueLine = getLastLineNumber();
		return nextValue;
	}

	/**
	 * Initialize the LabeledCSVParser.labels member and LabeledCSVParser.labelMap
	 * member.
	 *
	 * @throws java.io.IOException
	 *
	 * @since ostermillerutils 1.03.00
	 */
	private void setLabels() throws IOException {
		labels = parse.getLine();
		if (labels == null) return;
		labelMap = new HashMap();
		for (int i = 0; i < labels.length; i++){
			labelMap.put(labels[i], new Integer(i));
		}
	}

	/**
	 * Return an array of all field names from the top
	 * of the CSV file.
	 *
	 * @return Field names.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String[] getLabels() throws IOException {
		return labels;
	}

	/**
	 * Get the index of the column having the given label.
	 * The {@link #getLine()} method returns an
	 * array of field values for a single record of data.  This method returns
	 * the index of a member of that array based on the specified field name.
	 * The first field has the index 0.
	 *
	 * @param label The field name.
	 * @return The index of the field name, or -1 if the label does not exist.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public int getLabelIndex(String label){
		if (labelMap == null) return -1;
		if (!labelMap.containsKey(label)) return -1;
		return ((Integer)labelMap.get(label)).intValue();
	}

	/**
	 * Given the label for the column, get the column from the last line that
	 * was read.  If the column cannot be found in the line, null is returned.
	 *
	 * @param label The field name.
	 * @throws IllegalStateException if nextValue has been called as part of getting the last line.  nextValue is not compatible with this method.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public String getValueByLabel(String label) throws IllegalStateException {
		if (nextValueLine == getLastLineNumber()) throw new IllegalStateException("nextValue() was used to get values from this line.");
		if (lastLine == null) return null;
		int fieldIndex = getLabelIndex(label);
		if (fieldIndex == -1) return null;
		if (fieldIndex >= lastLine.length) return null;
		return lastLine[fieldIndex];
	}

	/**
	 * Close any stream upon which this parser is based.
	 *
	 * @throws IOException if an error occurs while closing the stream.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public void close() throws IOException {
		parse.close();
	}
}
