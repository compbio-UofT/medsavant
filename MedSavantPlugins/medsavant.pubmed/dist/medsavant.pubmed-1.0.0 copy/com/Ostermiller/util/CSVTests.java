/*
 * CSV Regression test.
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
 * Regression test for CSV.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/CSV.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
class CSVTests {
	public static void main(String[] args){
		try {
			StringWriter sw = new StringWriter();
			CSVPrinter csvOut = new CSVPrinter(sw, '#');

			csvOut.printlnComment("Comma Separated Value Test");
			csvOut.println();
			csvOut.printlnComment("Five Cities");
			csvOut.println(new String[] {
				"Boston",
				"San Francisco",
				"New York",
				"Chicago",
				"Houston",
			});
			csvOut.println();
			csvOut.println(""); // an empty value on a line by itself.
			csvOut.println(new String[] {
				"Two\nTokens",
				"On the\nSame Line"
			});
			csvOut.printlnComment("A two line comment\njust to see that it works");

			CSVParser shredder = new CSVParser(new StringReader(sw.toString()));
			shredder.setCommentStart("#;!");
			shredder.setEscapes("nrtf", "\n\r\t\f");
			String t;
			compare(shredder.nextValue(), shredder.lastLineNumber(), "Boston", 4);
			compare(shredder.nextValue(), shredder.lastLineNumber(), "San Francisco", 4);
			compare(shredder.nextValue(), shredder.lastLineNumber(), "New York", 4);
			compare(shredder.nextValue(), shredder.lastLineNumber(), "Chicago", 4);
			compare(shredder.nextValue(), shredder.lastLineNumber(), "Houston", 4);
			compare(shredder.nextValue(), shredder.lastLineNumber(), "", 6);
			compare(shredder.nextValue(), shredder.lastLineNumber(), "Two\nTokens", 7);
			compare(shredder.nextValue(), shredder.lastLineNumber(), "On the\nSame Line", 7);
			compare(shredder.nextValue(), shredder.lastLineNumber(), null, 9);

			String normalInput = ",\"a\",\",\t'\\\"\"";
			String[][] normalOutput = new String[][]{{"", "a", ",\t'\""}};
			shredder = new CSVParser(new StringReader(normalInput));
			compare("normal", normalOutput, shredder.getAllValues());

			String tabInput = "\t\"a\"\t\",\t'\\\"\"";
			shredder = new CSVParser(new StringReader(tabInput));
			shredder.changeDelimiter('\t');
			compare("tabs", normalOutput, shredder.getAllValues());

			String aposInput = ",'a',',\t\\'\"'";
			shredder = new CSVParser(new StringReader(aposInput));
			shredder.changeQuote('\'');
			compare("apostrophes", normalOutput, shredder.getAllValues());

			String swappedInput = "\",a,\",\\,\t'\\\",";
			shredder = new CSVParser(new StringReader(swappedInput));
			shredder.changeDelimiter('\t');
			shredder.changeQuote(',');
			shredder.changeDelimiter('"');
			compare("commas and quotes swapped", normalOutput, shredder.getAllValues());

			normalInput = "\"test\\\\\",test";
			normalOutput = new String[][]{{"test\\", "test"}};
			shredder = new CSVParser(new StringReader(normalInput));
			compare("backslash at end of quoted", normalOutput, shredder.getAllValues());

			normalInput = "field1,field2 ,    field3,field4   ,  field5   ,field6";
			normalOutput = new String[][]{{"field1", "field2", "field3", "field4", "field5", "field6"}};
			shredder = new CSVParser(new StringReader(normalInput));
			compare("white space around fields", normalOutput, shredder.getAllValues());

			normalInput = ",field2,, ,field5,";
			normalOutput = new String[][]{{"", "field2", "", "", "field5", ""}};
			shredder = new CSVParser(new StringReader(normalInput));
			compare("empty fields", normalOutput, shredder.getAllValues());

			normalInput = "1,to,tre,four,five5,sixsix";
			normalOutput = new String[][]{{"1", "to", "tre", "four", "five5", "sixsix"}};
			shredder = new CSVParser(new StringReader(normalInput));
			compare("various lengths", normalOutput, shredder.getAllValues());

			normalInput = "!comment\n !field1\n;comment\n ;field2\n#comment\n #field3";
			normalOutput = new String[][]{{"!field1"},{";field2"},{"#field3"}};
			shredder = new CSVParser(new StringReader(normalInput));
			shredder.setCommentStart("#;!");
			compare("comment must start at beginning of line", normalOutput, shredder.getAllValues());

		} catch (Exception x){
			System.err.println(x.getMessage());
			x.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

	private static void compare(String value, int line, String expectedValue, int expectedLine) throws Exception {
		if (line != expectedLine) {
			throw new Exception("Line numbers do not match");
		}
		if (expectedValue == null && value == null){
			return;
		}
		if (!value.equals(expectedValue)){
			throw new Exception("Value and expected value do not match");
		}
	}

	private static void compare(String testName, String[][] a, String[][] b) throws Exception {
		if (a.length != b.length) {
			throw new Exception(testName + ": unexpected number of lines " + a.length + " found " + b.length + " expected");
		}
		for(int i=0; i<a.length; i++){
			if (a[i].length != b[i].length) {
				throw new Exception(testName + ": unexpected number of values in line: " + b[i].length);
			}
			for (int j=0; j<a[i].length; j++){
				if (!a[i][j].equals(b[i][j])) {
					System.err.println(a[i][j]);
					System.err.println(b[i][j]);
					throw new Exception(testName + ": values do not match.");
				}
			}
		}
	}
}
