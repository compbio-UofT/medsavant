/*
 * Tests StringTokenizer.
 * Copyright (C) 2001 Stephen Ostermiller
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
 * A regression test for com.Ostermiller.util.StringTokenizer.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/StringTokenizer.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
class TokenizerTests {

	public static void main(String args[]){
		try {
			java.util.StringTokenizer oldTok;
			com.Ostermiller.util.StringTokenizer newTok;
			String token;

			//newTok.test();

			token = "this is a test";
			oldTok = new java.util.StringTokenizer(token);
			newTok = new com.Ostermiller.util.StringTokenizer(token);
			assertEqual("" + oldTok.countTokens(), "" + newTok.countTokens());
			compareState("Test 1", oldTok, newTok);
			while (oldTok.hasMoreTokens()){
				assertEqual(oldTok.nextToken(), newTok.nextToken());
				compareState("Test 1", oldTok, newTok);
			}

			token = "";
			oldTok = new java.util.StringTokenizer(token);
			newTok = new com.Ostermiller.util.StringTokenizer(token);
			assertEqual("" + oldTok.countTokens(), "" + newTok.countTokens());

			token = "no delims";
			oldTok = new java.util.StringTokenizer(token, "");
			newTok = new com.Ostermiller.util.StringTokenizer(token, "");
			assertEqual("" + oldTok.countTokens(), "" + newTok.countTokens());
			while (oldTok.hasMoreTokens()){
				assertEqual("" + oldTok.countTokens(), "" + newTok.countTokens());
				assertEqual(oldTok.nextToken(), newTok.nextToken());
			}

			token = "AstringB";
			oldTok = new java.util.StringTokenizer(token, "AB");
			newTok = new com.Ostermiller.util.StringTokenizer(token, "AB");
			assertEqual("" + oldTok.countTokens(), "" + newTok.countTokens());
			while (oldTok.hasMoreTokens()){
				assertEqual(oldTok.nextToken(), newTok.nextToken());
			}

			token = "AstringB";
			oldTok = new java.util.StringTokenizer(token, "AB", true);
			newTok = new com.Ostermiller.util.StringTokenizer(token, "AB", true);
			assertEqual("" + oldTok.countTokens(), "" + newTok.countTokens());
			while (oldTok.hasMoreTokens()){
				assertEqual(oldTok.nextToken(), newTok.nextToken());
			}

			token = "someURL?name=value&name=value";
			oldTok = new java.util.StringTokenizer(token);
			newTok = new com.Ostermiller.util.StringTokenizer(token);
			assertEqual(oldTok.nextToken("?"), newTok.nextToken("?"));
			assertEqual(oldTok.nextToken("=&"), newTok.nextToken("=&"));
			assertEqual(oldTok.nextToken("=&"), newTok.nextToken("=&"));
			assertEqual(oldTok.nextToken(), newTok.nextToken());
			assertEqual(oldTok.nextToken(), newTok.nextToken());

			newTok = new com.Ostermiller.util.StringTokenizer(token);
			assertEqual("someURL", newTok.nextToken("?"));
			newTok.skipDelimiters();
			assertEqual("name", newTok.nextToken("=&"));
			assertEqual("value", newTok.nextToken("=&"));
			assertEqual("name", newTok.nextToken());
			assertEqual("value", newTok.nextToken());

			token = "  (   aaa	\t	* (b+c1 ))";
			newTok = new com.Ostermiller.util.StringTokenizer(token, " \t\n\r\f", "()+*");
			assertEqual("9", "" + newTok.countTokens());
			assertEqual("(", newTok.nextToken());
			assertEqual("aaa", newTok.nextToken());
			assertEqual("*", newTok.nextToken());
			assertEqual("(", newTok.nextToken());
			assertEqual("b", newTok.nextToken());
			assertEqual("+", newTok.nextToken());
			assertEqual("c1", newTok.nextToken());
			assertEqual(")", newTok.nextToken());
			assertEqual(")", newTok.nextToken());

			token = "one,two,,four,five,,,eight,";
			oldTok = new java.util.StringTokenizer(token, ",");
			newTok = new com.Ostermiller.util.StringTokenizer(token, ",");
			assertEqual("" + oldTok.countTokens(), "" + newTok.countTokens());
			while (oldTok.hasMoreTokens()){
				assertEqual(oldTok.nextToken(), newTok.nextToken());
			}

			token = "one,two,,four,five,,,eight";
			newTok = new com.Ostermiller.util.StringTokenizer(token, ",");
			newTok.setReturnEmptyTokens(true);
			assertEqual("8", "" + newTok.countTokens());

			token = ",two,,four,five,,,eight,";
			newTok = new com.Ostermiller.util.StringTokenizer(token, ",");
			newTok.setReturnEmptyTokens(true);
			assertEqual("9", "" + newTok.countTokens());

			token = ",";
			newTok = new com.Ostermiller.util.StringTokenizer(token, ",");
			newTok.setReturnEmptyTokens(true);
			assertEqual("2", "" + newTok.countTokens());

			token = "";
			newTok = new com.Ostermiller.util.StringTokenizer(token, ",");
			newTok.setReturnEmptyTokens(true);
			assertEqual("1", "" + newTok.countTokens());

			token = ",two,,four,five,,,eight,";
			newTok = new com.Ostermiller.util.StringTokenizer(token, ",", true);
			newTok.setReturnEmptyTokens(true);
			assertEqual("17", "" + newTok.countTokens());

			token = ",one,,,four,";
			newTok = new com.Ostermiller.util.StringTokenizer(token, ",", null, false);
			assertEqual("one", newTok.nextToken());
			newTok.setReturnEmptyTokens(true);
			assertEqual("4", "" + newTok.countTokens());

			token = "list=";
			newTok = new com.Ostermiller.util.StringTokenizer(token, "=");
			newTok.setReturnEmptyTokens(true);
			assertEqual("list", newTok.nextToken());
			newTok.skipDelimiters();
			assertEqual("", newTok.nextToken(","));
			assertEqual("0", "" + newTok.countTokens());

			token = "list=,";
			newTok = new com.Ostermiller.util.StringTokenizer(token, "=");
			newTok.setReturnEmptyTokens(true);
			assertEqual("list", newTok.nextToken());
			newTok.skipDelimiters();
			assertEqual("", newTok.nextToken(","));
			assertEqual("1", "" + newTok.countTokens());

			token = "list=,two,";
			newTok = new com.Ostermiller.util.StringTokenizer(token, "=");
			newTok.setReturnEmptyTokens(true);
			assertEqual("list", newTok.nextToken());
			newTok.skipDelimiters();
			assertEqual("", newTok.nextToken(","));
			assertEqual("2", "" + newTok.countTokens());

			token = "this is a test";
			newTok = new com.Ostermiller.util.StringTokenizer(token);
			String[] tokens = newTok.toArray();
			newTok.setText(token);
			for (int i=0; i<tokens.length; i++){
				assertEqual(tokens[i], newTok.nextToken());
			}

			token = "token rest of string";
			newTok = new com.Ostermiller.util.StringTokenizer(token);
			assertEqual("token", newTok.nextToken());
			newTok.skipDelimiters();
			assertEqual("rest of string", newTok.restOfText());
			assertEqual("false", "" + newTok.hasMoreTokens());

			token = "testing the peek method";
			newTok = new com.Ostermiller.util.StringTokenizer(token);
			assertEqual("4", "" + newTok.countTokens());
			assertEqual("testing", newTok.peek());
			assertEqual("4", "" + newTok.countTokens());
			assertEqual("testing", newTok.nextToken());
			assertEqual("3", "" + newTok.countTokens());
			assertEqual("the", newTok.peek());
			assertEqual("the", newTok.peek());
			assertEqual("the", newTok.nextToken());
			assertEqual("peek", newTok.peek());
			assertEqual("peek", newTok.nextToken());
			assertEqual("true", "" + newTok.hasMoreTokens());
			assertEqual("method", newTok.peek());
			assertEqual("true", "" + newTok.hasMoreTokens());
			assertEqual("method", newTok.nextToken());
			assertEqual("false", "" + newTok.hasMoreTokens());

		} catch (Exception x){
			x.printStackTrace();
			System.exit(1);
		}
		System.exit(0);
	}

	private static void assertEqual(String one, String two) throws Exception {
		 if (one == null || !one.equals(two)){
			throw new Exception ("Expected equal: " + one + ", " + two);
		 }
	}

	private static void compareState(String testName, java.util.StringTokenizer oldTok, com.Ostermiller.util.StringTokenizer newTok) throws Exception {
		compareState(testName, newTok, oldTok.countTokens());
	}

	private static void compareState(String testName, com.Ostermiller.util.StringTokenizer newTok, int tokenCount) throws Exception {
		// count the number of tokens left first.
		int newNumTokens = newTok.countTokens();
		if (tokenCount != newNumTokens){
			throw new Exception(testName + ": TokenCount does not match. " + tokenCount + " vs " + newNumTokens);
		}
		boolean hasMoreTokens = (tokenCount > 0);
		boolean newTokHasMoreTokens = newTok.hasMoreTokens();
		if (hasMoreTokens != newTokHasMoreTokens){
			throw new Exception(testName + ": hasMoreTokens does not match. " + hasMoreTokens + " vs " + newTokHasMoreTokens);
		}
	}
}
