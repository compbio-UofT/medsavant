/*
 * Converts an enumeration to an iterator.
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

/**
 * Converts an Enumeration to an iterator.
 * <p>
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/Iterator_Enumeration.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.03.00
 */
public class EnumerationIterator  implements Iterator {

	/**
	 * Enumeration being converted to iterator.
	 */
	private Enumeration enumeration;

	/**
	 * Create an Iterator from an Enumeration.
	 *
	 * @param enumeration Enumeration to convert to an Iterator.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public EnumerationIterator(Enumeration enumeration){
		this.enumeration = enumeration;
	}

	/**
	 * Tests if this Iterator contains more elements.
	 *
	 * @return true if and only if this Iterator object contains at least
	 * one more element to provide; false otherwise.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public boolean hasNext(){
		return enumeration.hasMoreElements();
	}

	/**
	 * Returns the next element of this Iterator if this Iterator
	 * object has at least one more element to provide.
	 *
	 * @return the next element of this Iterator.
	 * @throws NoSuchElementException if no more elements exist.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public Object next() throws NoSuchElementException {
		return enumeration.nextElement();
	}

	/**
	 * Operation not supported.
	 *
	 * @throws UnsupportedOperationException every time.
	 *
	 * @since ostermillerutils 1.03.00
	 */
	public void remove(){
		throw new UnsupportedOperationException("EnumerationIterator does not support remove()");
	}
}