/*
 * Base64 regression test.
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

import java.util.*;
import java.io.*;

class Base64Tests {

	private static class TestCase {
		private String encoded;
		private byte[] decoded;
		public TestCase(String encoded, byte[] decoded){
			this.encoded = encoded;
			this.decoded = decoded;
		}
		private void test() throws Exception {
			String enc = Base64.encodeToString(decoded);
			if (!encoded.equals(enc)){
				throw new Exception("Decoding problem, expected '" + encoded + "' got '" + enc + "'.");
			}
			byte[] b = Base64.decodeToBytes(encoded);
			if (!byteArraysEqual(b, decoded)){
				throw new Exception("Encoding problem, started with '" + encoded + "'.");
			}
		}
	}

	private static boolean byteArraysEqual(byte[] b1, byte[] b2){
		if (b1.length != b2.length) return false;
		for (int i=0; i<b1.length; i++){
			if (b1[i] != b2[i]) return false;
		}
		return true;
	}

	private static final TestCase[] testCases = new TestCase[]{
		new TestCase("", new byte[]{}),
		new TestCase("aA==", new byte[]{'h'}),
		new TestCase("dGU=", new byte[]{'t','e'}),
		new TestCase("Y29i", new byte[]{'c','o','b'}),
	};

	public static void main(String[] args){
		try {
			for (int i=0; i<testCases.length; i++){
				testCases[i].test();
			}
			for (int i=0; i<1024; i++){
				byte[] before = randBytes();
				byte[] after = Base64.decodeToBytes(Base64.encodeToString(before));
				if (!byteArraysEqual(before,after)){
					throw new Exception("Could not decode and then reencode:\nbefore: " + bytesToString(before) + "\nafter:  " + bytesToString(after));
				}
			}


		} catch (Exception x){
			x.printStackTrace(System.err);
			System.exit(1);
		}
		System.exit(0);
	}

	private static String bytesToString(byte[] b){
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<b.length; i++){
			sb.append("'").append((int)b[i]).append("',");
		}
		return sb.toString();
	}

	private static byte[] randBytes(){
		Random rand = new Random();
		byte[] bytes = new byte[rand.nextInt(128)*3];
		for (int i=0; i<bytes.length; i++){
			bytes[i] = (byte)(rand.nextInt() & 0xff);
		}
		return bytes;
	}
}
