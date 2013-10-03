/*
 * Implements MD5 functionality on a stream.
 *
 * written Santeri Paavolainen, Helsinki Finland 1996
 * (c) Santeri Paavolainen, Helsinki Finland 1996
 * modifications Copyright (C) 2002 Stephen Ostermiller
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
 *
 * The original work by Santeri Paavolainen can be found a
 * http://www.helsinki.fi/~sjpaavol/programs/md5/
 */
package com.Ostermiller.util;

import java.io.*;

/**
 * Implements MD5 functionality on a stream.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/MD5.html">ostermiller.org</a>.
 * <p>
 * This class produces a 128-bit "fingerprint" or "message digest" for
 * all data read from this stream.
 * It is conjectured that it is computationally infeasible to produce
 * two messages having the same message digest, or to produce any
 * message having a given pre-specified target message digest. The MD5
 * algorithm is intended for digital signature applications, where a
 * large file must be "compressed" in a secure manner before being
 * encrypted with a private (secret) key under a public-key cryptosystem
 * such as RSA.
 * <p>
 * For more information see RFC1321.
 *
 * @see MD5
 * @see MD5OutputStream
 *
 * @author Santeri Paavolainen http://www.helsinki.fi/~sjpaavol/programs/md5/
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class MD5InputStream extends FilterInputStream {
	/**
	 * MD5 context
	 */
	private MD5	md5;

	/**
	 * Creates a MD5InputStream
	 * @param in the underlying input stream
	 */
	public MD5InputStream (InputStream in) {
		super(in);
		md5 = new MD5();
	}

	/**
	 * Reads the next byte of data from this input stream. The value byte
	 * is returned as an int in the range 0 to 255. If no byte is available
	 * because the end of the stream has been reached, the value -1 is returned.
	 * This method blocks until input data is available, the end of the stream is
	 * detected, or an exception is thrown.
	 * <p>
	 * This method simply performs in.read() and returns the result.
	 *
	 * @return the next byte of data, or -1 if the end of the stream is reached.
	 * @throws IOException if an I/O error occurs.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int read() throws IOException {
		int c = in.read();
		if (c == -1) {
			return -1;
		} else {
			md5.update((byte)(c & 0xff));
			return c;
		}
	}

	/**
	 * Reads up to len bytes of data from this input stream into an
	 * array of bytes. This method blocks until some input is available.
	 *
	 * @param bytes the buffer into which the data is read.
	 * @param offset the start offset of the data.
	 * @param length the maximum number of bytes read.
	 * @throws IOException if an I/O error occurs.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public int read(byte[] bytes, int offset, int length) throws IOException {
		int	r;
		if ((r = in.read(bytes, offset, length)) == -1) {
			return r;
		} else {
			md5.update(bytes, offset, r);
			return r;
		}
	}

	/**
	 * Returns array of bytes representing hash of the stream so far.
	 *
	 * @return Array of 16 bytes, the hash of all read bytes.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public byte[] getHash(){
		return md5.getHash();
	}

	/**
	 * Get a 32-character hex representation representing hash of the stream so far.
	 *
	 * @return A string containing  the hash of all written bytes.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public String getHashString(){
		return md5.getHashString();
	}
}
