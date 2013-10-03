/*
 * Streams that have a different close mechanism.
 * Copyright (C) 2003 Stephen Ostermiller
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
 * A wrapper for a stream (either input or output)
 * which has a close method with no effect.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/NoCloseStream.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.01.00
 */
public interface NoCloseStream {

	/**
	 * Actually closes this stream and releases any system
	 * resources associated with the stream, as opposed to
	 * the close() method, which does nothing.
	 *
	 * @throws IOException if an I/O error occurs.
	 *
	 * @since ostermillerutils 1.01.00
	 */
	public void reallyClose() throws IOException;

}
