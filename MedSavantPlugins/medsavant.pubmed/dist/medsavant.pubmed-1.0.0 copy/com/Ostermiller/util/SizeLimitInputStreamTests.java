/*
 * SizeLimitInputStream tests
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
 * Regression test for SizeLimitInputStreams.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/SizeLimitInputStream.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.04.00
 */
class SizeLimitInputStreamTests {
	public static void main (String[] args){
		try {
			SizeLimitInputStream slis;

			slis = new SizeLimitInputStream(
				new ByteArrayInputStream(
					new byte[] {
						1,2,3,4
					}
				),
				3
			);
			if (slis.read() != 1) throw new Exception ("Expected 1");
			if (slis.read() != 2) throw new Exception ("Expected 2");
			if (slis.read() != 3) throw new Exception ("Expected 3");
			if (slis.read() != -1) throw new Exception ("Expected -1");

			slis = new SizeLimitInputStream(
				new ByteArrayInputStream(
					new byte[] {
						1,2,3,4,5,6,7,8,9
					}
				),
				6
			);
			if (slis.read() != 1) throw new Exception ("Expected 1");
			if (slis.read(new byte[4]) != 4) throw new Exception("Expected 4 read");
			if (slis.read(new byte[4]) != 1) throw new Exception("Expected 2 read");
			if (slis.read() != -1) throw new Exception ("Expected -1");

			InputStream in = new ByteArrayInputStream(
				"onetwothreefourfivesixseven".getBytes("ASCII")
			);
			compare("one", readFully(new SizeLimitInputStream(in,3)));
			compare("", readFully(new SizeLimitInputStream(in,0)));
			compare("two", readFully(new SizeLimitInputStream(in,3)));
			compare("three", readFully(new SizeLimitInputStream(in,5)));
			compare("four", readFully(new SizeLimitInputStream(in,4)));
			compare("five", readFully(new SizeLimitInputStream(in,4)));
			compare("six", readFully(new SizeLimitInputStream(in,3)));
			compare("s", readFully(new SizeLimitInputStream(in,1)));
			compare("even", readFully(new SizeLimitInputStream(in,4)));

		} catch (Exception x){
			System.err.println(x.getMessage());
			System.exit(1);
		}

	}

	private static String readFully(InputStream in) throws IOException {
		StringBuffer sb = new StringBuffer();
		int read;
		while ((read = in.read()) != -1){
			sb.append((char)read);
		}
		return sb.toString();
	}

	private static void compare(String s1, String s2) throws Exception {
		if (!s1.equals(s2)) throw new Exception ("Expected " + s1 + " but got " + s2);
	}

}
