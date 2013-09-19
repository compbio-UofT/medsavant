/*
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
 * An input stream which reads sequentially from multiple sources.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.04.00
 */
public class ConcatInputStream extends InputStream {

	/**
	 * List of sources.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	private InputStream[] in;

	/**
	 * Current index to the InputStream[] in
	 *
	 * @since ostermillerutils 1.04.00
	 */
	private int inIndex = 0;

	/**
	 * True iff this the close() method has been called on this stream.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	private boolean closed = false;

	/**
	 * Create a new InputStream with one source.
	 *
	 * @param in InputStream to use as a source.
	 *
	 * @throws NullPointerException if in is null
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public ConcatInputStream(InputStream in){
		this(new InputStream[]{in});
	}

	/**
	 * Create a new InputStream with two sources.
	 *
	 * @param in1 first InputStream to use as a source.
	 * @param in2 second InputStream to use as a source.
	 *
	 * @throws NullPointerException if either source is null.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public ConcatInputStream(InputStream in1, InputStream in2){
		this(new InputStream[]{in1, in2});
	}

	/**
	 * Create a new InputStream with an arbitrary number of sources.
	 *
	 * @param in InputStreams to use as a sources.
	 *
	 * @throws NullPointerException if the input array on any element is null.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public ConcatInputStream(InputStream[] in){
		if (in == null) throw new NullPointerException();
		for (int i=0; i<in.length; i++){
			if (in[i] == null) throw new NullPointerException();
		}
		this.in = in;
	}

	/**
	 * Reads the next byte of data from the underlying streams. The value byte is
	 * returned as an int in the range 0 to 255. If no byte is available because
	 * the end of the stream has been reached, the value -1 is returned. This method
	 * blocks until input data is available, the end of the stream is detected, or
	 * an exception is thrown.
	 *
	 * @return the next byte of data, or -1 if the end of the stream is reached.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public int read() throws IOException {
		if (closed) throw new IOException("InputStream closed");
		int r = -1;
		while (r == -1 && inIndex < in.length){
			r = in[inIndex].read();
			if (r == -1) inIndex++;
		}
		return r;
	}

	/**
	 * Reads some number of bytes from the underlying streams and stores them into
	 * the buffer array b. The number of bytes actually read is returned as an
	 * integer. This method blocks until input data is available, end of file is
	 * detected, or an exception is thrown.
	 * <p>
	 * If the length of b is zero,
	 * then no bytes are read and 0 is returned; otherwise, there is an attempt
	 * to read at least one byte.
	 * <p>
	 * The read(b) method for class InputStream has the same effect as:<br>
	 * read(b, 0, b.length)
	 *
	 * @param b - Destination buffer
	 * @return The number of bytes read, or -1 if the end of the stream has been reached
	 *
	 * @throws IOException - If an I/O error occurs
	 * @throws NullPointerException - If b is null.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Reads up to len bytes of data from the underlying streams into an array of bytes.
	 * An attempt is made to read as many as len bytes, but a smaller number may be read,
	 * possibly zero. The number of bytes actually read is returned as an integer.
	 * <p>
	 * If len is zero,
	 * then no bytes are read and 0 is returned; otherwise, there is an attempt
	 * to read at least one byte.
	 * <p>
	 * This method blocks until input data is available
	 *
	 * @param b Destination buffer
	 * @param off Offset at which to start storing bytes
	 * @param len Maximum number of bytes to read
	 * @return The number of bytes read, or -1 if the end of the stream has been reached
	 *
	 * @throws IOException - If an I/O error occurs
	 * @throws NullPointerException - If b is null.
	 * @throws IndexOutOfBoundsException - if len or offset are not possible.
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if (off < 0 || len < 0 || off + len > b.length) throw new IllegalArgumentException();
		if (closed) throw new IOException("InputStream closed");
		int r = -1;
		while (r == -1 && inIndex < in.length){
			r = in[inIndex].read(b, off, len);
			if (r == -1) inIndex++;
		}
		return r;
	}

	/**
	 * Skips over and discards n bytes of data from this input stream. The skip method
	 * may, for a variety of reasons, end up skipping over some smaller number of bytes,
	 * possibly 0. This may result from any of a number of conditions; reaching end of
	 * file before n bytes have been skipped is only one possibility. The actual number
	 * of bytes skipped is returned. If n is negative, no bytes are skipped.
	 *
	 * @param n he number of characters to skip
	 * @return The number of characters actually skipped
	 *
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public long skip(long n) throws IOException {
		if (closed) throw new IOException("InputStream closed");
		if (n <= 0) return 0;
		long s = -1;
		while (s <= 0 && inIndex < in.length){
			s = in[inIndex].skip(n);
			// When nothing was skipped it is a bit of a puzzle.
			// The most common cause is that the end of the underlying
			// stream was reached.  In which case calling skip on it
			// will always return zero.  If somebody were calling skip
			// until it skipped everything they needed, there would
			// be an infinite loop if we were to return zero here.
			// If we get zero, let us try to read one character so
			// we can see if we are at the end of the stream.  If so,
			// we will move to the next.
			if (s == 0) {
				// read() will adjust inIndex for us, so don't do it again
				s = ((read()==-1)?-1:1);
			}
		}
		if (s == -1) s = 0;
		return s;
	}

	/**
	 * Returns the number of bytes that can be read (or skipped over) from this input
	 * stream without blocking by the next caller of a method for this input stream.
	 * The next caller might be the same thread or or another thread.
	 *
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public int available() throws IOException {
		if (closed) throw new IOException("InputStream closed");
		return in[inIndex].available();
	}

	/**
	 * Closes this input stream and releases any system resources associated with the stream.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public void close() throws IOException {
		for (int i=0; i<in.length; i++){
			in[i].close();
		}
		closed = true;
		inIndex = in.length;
	}

	/**
	 * Mark not supported
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public void mark(int readlimit) {
	}

	/**
	 * Reset not supported.
	 *
	 * @throws IOException because reset is not supported.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public void reset() throws IOException {
		throw new IOException("Reset not supported");
	}

	/**
	 * Does not support mark.
	 *
	 * @return false
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public boolean markSupported(){
		return false;
	}

}
