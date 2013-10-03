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
 * A reader which reads sequentially from multiple sources.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.04.00
 */
public class ConcatReader extends Reader {

	/**
	 * List of sources.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	private Reader[] in;

	/**
	 * Current index to the Reader[] in
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
	 * Create a new reader with one source.
	 *
	 * @param in reader to use as a source.
	 *
	 * @throws NullPointerException if in is null
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public ConcatReader(Reader in){
		this(new Reader[]{in});
	}

	/**
	 * Create a new reader with two sources.
	 *
	 * @param in1 first reader to use as a source.
	 * @param in2 second reader to use as a source.
	 *
	 * @throws NullPointerException if either source is null.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public ConcatReader(Reader in1, Reader in2){
		this(new Reader[]{in1, in2});
	}

	/**
	 * Create a new reader with an arbitrary number of sources.
	 *
	 * @param in readers to use as a sources.
	 *
	 * @throws NullPointerException if the input array on any element is null.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public ConcatReader(Reader[] in){
		if (in == null) throw new NullPointerException();
		for (int i=0; i<in.length; i++){
			if (in[i] == null) throw new NullPointerException();
		}
		this.in = in;
	}

	/**
	 * Read a single character. This method will block until a
	 * character is available, an I/O error occurs, or the end of all underlying
	 * streams are reached.
	 *
	 * @return The character read, as an integer in the range 0 to 65535 (0x00-0xffff),
	 *    or -1 if the end of the stream has been reached
	 *
	 * @throws IOException - If an I/O error occurs
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public int read() throws IOException {
		if (closed) throw new IOException("Reader closed");
		int r = -1;
		while (r == -1 && inIndex < in.length){
			r = in[inIndex].read();
			if (r == -1) inIndex++;
		}
		return r;
	}

	/**
	 * Read characters into an array. This method will block until some input is available, an
	 * I/O error occurs, or the end of all underlying
	 * streams are reached.
	 *
	 * @param cbuf - Destination buffer
	 * @return The number of characters read, or -1 if the end of the stream has been reached
	 *
	 * @throws IOException - If an I/O error occurs
	 * @throws NullPointerException - If cbuf is null.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public int read(char[] cbuf) throws IOException {
		return read(cbuf, 0, cbuf.length);
	}

	/**
	 * Read characters into a portion of an array. This method will block until
	 * some input is available, an I/O error occurs, or the end of all underlying
	 * streams are reached.
	 *
	 * @param cbuf Destination buffer
	 * @param off Offset at which to start storing characters
	 * @param len Maximum number of characters to read
	 * @return The number of characters read, or -1 if the end of the stream has been reached
	 *
	 * @throws IOException - If an I/O error occurs
	 * @throws NullPointerException - If cbuf is null.
	 * @throws IndexOutOfBoundsException - if len or offset are not possible.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (off < 0 || len < 0 || off + len > cbuf.length) throw new IndexOutOfBoundsException();
		if (closed) throw new IOException("Reader closed");
		int r = -1;
		while (r == -1 && inIndex < in.length){
			r = in[inIndex].read(cbuf, off, len);
			if (r == -1) inIndex++;
		}
		return r;
	}

	/**
	 * Skip characters. This method will block until some characters are
	 * available, an I/O error occurs, or the end of the stream is reached.
	 *
	 * @param n he number of characters to skip
	 * @return The number of characters actually skipped
	 *
	 * @throws IllegalArgumentException If n is negative.
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public long skip(long n) throws IOException {
		if (closed) throw new IOException("Reader closed");
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
	 * Tell whether this stream is ready to be read.
	 *
	 * @return True if the next read() is guaranteed not to block for input,
	 *    false otherwise. Note that returning false does not guarantee that the next
	 *    read will block.
	 *
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public boolean ready() throws IOException {
		if (closed) throw new IOException("Reader closed");
		return in[inIndex].ready();
	}

	/**
	 * Close the stream and any underlying streams.
	 * Once a stream has been closed, further read(), ready(), mark(), or reset()
	 * invocations will throw an IOException. Closing a previously-closed stream,
	 * however, has no effect.
	 *
	 * @throws IOException If an I/O error occurs
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
	 * Mark not supported.
	 *
	 * @throws IOException because mark is not supported.
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public void mark(int readlimit) throws IOException {
		throw new IOException("Mark not supported");
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
	 * Mark not supported.
	 *
	 * @return false
	 *
	 * @since ostermillerutils 1.04.00
	 */
	public boolean markSupported(){
		return false;
	}
}
