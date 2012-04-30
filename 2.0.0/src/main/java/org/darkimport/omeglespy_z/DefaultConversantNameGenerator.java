/*
 * #%L omeglespy-z-core
 * 
 * $Id$ $HeadURL$ %% Copyright (C) 2011 - 2012 darkimport %% This program is
 * free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation,
 * either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/gpl-2.0.html>. #L%
 */
/**
 * 
 */
package org.darkimport.omeglespy_z;

import java.util.Arrays;

/**
 * This is the default trivial implementation of the {@link NameGenerator} used
 * for generating strangers' names.
 * 
 * The only names that will be returned are Stranger1 and Stranger2.
 * 
 * @author user
 * @version $Id: $
 */
public class DefaultConversantNameGenerator implements NameGenerator {
	/**
	 * Constant
	 * <code>CONVERSANT_NAMES="new String[] { Stranger1, Stranger2 }"</code>
	 */
	public static String[]	CONVERSANT_NAMES	= new String[] { "Stranger1", "Stranger2" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.omeglespy_z.NameGenerator#next(int, boolean)
	 */
	/**
	 * {@inheritDoc}
	 * 
	 * If the requested number of names exceeds the number of elements in
	 * {@link DefaultConversantNameGenerator#CONVERSANT_NAMES}, an Exception
	 * will be thrown.
	 */
	public String[] next(final int numberOfNames, final boolean uniqueNames) {
		if (numberOfNames > CONVERSANT_NAMES.length) { throw new IllegalArgumentException(
				"The number of names requested exceeds the available number of names"); }

		return Arrays.copyOfRange(CONVERSANT_NAMES, 0, numberOfNames);
	}

}
