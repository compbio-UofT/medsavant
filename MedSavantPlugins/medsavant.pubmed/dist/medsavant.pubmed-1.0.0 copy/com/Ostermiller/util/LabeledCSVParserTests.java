/*
 * Tests reading files in comma separated value format with a fist line of labels.
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

import java.io.*;

/**
 * Tests reading files in comma separated value format with a fist line of labels.
 *
 * @author Campbell, Allen T. <allenc28@yahoo.com>
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.03.00
 */
class LabeledCSVParserTests {

	public static void main(String[] args){
		try {
			test01();
			test02();
			test03();
		} catch (Exception x){
			x.printStackTrace();
			System.exit(1);
		}
	}

	private static void test01() throws Exception {
		// test01: empty input file
		LabeledCSVParser parse = new LabeledCSVParser(
			new CSVParser(
				new StringReader(
					""
				)
			)
		);

		if (parse.getLine() != null){
			throw new Exception("Expected null from getLine()");
		}
		if (parse.getLabels() != null){
			throw new Exception("Expected null from getLabels()");
		}

		if (parse.getLastLineNumber() != -1){
			throw new Exception("Expected -1 from getLastLineNumber()");
		}
		parse.close();
	}

	private static void test02() throws Exception {
		// test02: input has only labels
		LabeledCSVParser parse = new LabeledCSVParser(
			new CSVParser(
				new StringReader(
					"FIELD01,FIELD02,FIELD03"
				)
			)
		);
		if (parse.getLine() != null){
			throw new Exception("Expected null from getLine()");
		}
		String[] labels = parse.getLabels();
		if (labels.length != 3 || !"FIELD01".equals(labels[0]) || !"FIELD02".equals(labels[1]) || !"FIELD03".equals(labels[2])){
			throw new Exception("Didn't get expected labels.");
		}
		if (parse.getLastLineNumber() != -1){
			throw new Exception("Expected -1 from getLastLineNumber()");
		}
		parse.close();
	}

	private static void test03() throws Exception {
		// test03: labels + data
		LabeledCSVParser parse = new LabeledCSVParser(
			new CSVParser(
				new StringReader(
					"FIELD01,FIELD02,FIELD03\n" +
					"9.23,\"FOO\",\"BAR\"\n" +
					"10.5,\"BAZ\",\"FOO2\"\n"
				)
			)
		);
		String[] labels = parse.getLabels();
		if (labels.length != 3 || !"FIELD01".equals(labels[0]) || !"FIELD02".equals(labels[1]) || !"FIELD03".equals(labels[2])){
			throw new Exception("Didn't get expected labels.");
		}
		if (parse.getLabelIndex("FIELD01") != 0){
			throw new Exception("FIELD01 expected index of zero");
		}
		if (parse.getLabelIndex("FIELD02") != 1){
			throw new Exception("FIELD02 expected index of one");
		}
		if (parse.getLabelIndex("FIELD03") != 2){
			throw new Exception("FIELD03 expected index of two");
		}
		if (parse.getLabelIndex("FIELD04") != -1){
			throw new Exception("FIELD04 expected not to be present");
		}
		if (parse.getValueByLabel("FIELD01") != null){
			throw new Exception("Line not yet read, expected value of FIELD01 to be null.");
		}

		String[] line = parse.getLine();
		if (line == null){
			throw new Exception("getLine() null on line 1");
		}
		if (parse.getLastLineNumber() != 1){
			throw new Exception("Expected 1 from getLastLineNumber()");
		}
		if (line.length != 3 || !"9.23".equals(line[0]) || !"FOO".equals(line[1]) || !"BAR".equals(line[2])){
			throw new Exception("Didn't get expected line.");
		}
		if (!"9.23".equals(parse.getValueByLabel("FIELD01"))){
			throw new Exception("FIELD01 expected to have value 9.23.");
		}

		String value = parse.nextValue();
		if (!"10.5".equals(value)){
			throw new Exception("Expected nextValue to return 10.5.");            
		}
		try {
			parse.getValueByLabel("FIELD01");
			throw new Exception("IllegalStateException expected");
		} catch (IllegalStateException iex){
		}        
		if (parse.getLastLineNumber() != 2){
			throw new Exception("Expected 2 from getLastLineNumber()");
		}
		line = parse.getLine();
		if (line == null){
			throw new Exception("getLine() null on line 2");
		}
		if (parse.getLastLineNumber() != 2){
			throw new Exception("Expected 2 from getLastLineNumber()");
		}
		if (line.length != 2 || !"BAZ".equals(line[0]) || !"FOO2".equals(line[1])){
			throw new Exception("Didn't get expected line.");
		}
		try {
			parse.getValueByLabel("FIELD01");
			throw new Exception("IllegalStateException expected");
		} catch (IllegalStateException iex){
		}

		if (parse.getLine() != null){
			throw new Exception("Expected null from getLine()");
		}
		parse.close();
	}
}
