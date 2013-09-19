/*
 * MD5 regression test.
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

class MD5Tests {

	private static class TestCase {
		private String md5;
		private byte[] bytes;
		public TestCase(String md5, byte[] bytes){
			this.md5 = md5;
			this.bytes = bytes;
		}
		private void test() throws Exception {
			String hashString = MD5.getHashString(bytes);
			if (!md5.equals(hashString)){
				throw new Exception("Failed test.  Should be " + md5 + " was " + hashString + ".");
			}
		}
	}

	private static final TestCase[] testCases = new TestCase[]{
		new TestCase("d41d8cd98f00b204e9800998ecf8427e", new byte[]{}),
		new TestCase("0cc175b9c0f1b6a831c399e269772661", new byte[]{'a'}),
		new TestCase("900150983cd24fb0d6963f7d28e17f72", new byte[]{'a','b','c'}),
		new TestCase("f96b697d7cb7938d525a2f31aaf161d0", new byte[]{'m','e','s','s','a','g','e',' ','d','i','g','e','s','t'}),
		new TestCase("c3fcd3d76192e4007dfb496cca67e13b", new byte[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'}),
		new TestCase("d174ab98d277d9f5a5611c2c9f419d9f", new byte[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9'}),
		new TestCase("57edf4a22be3c955ac49da2e2107b67a", new byte[]{'1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0','1','2','3','4','5','6','7','8','9','0'}),
	};

	public static void main(String[] args){
		try {
			for (int i=0; i<testCases.length; i++){
				testCases[i].test();
			}
		} catch (Exception x){
			x.printStackTrace(System.err);
			System.exit(1);
		}
		System.exit(0);
	}
}
