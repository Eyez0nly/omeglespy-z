/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.util.Arrays;

/**
 * @author user
 * 
 */
public class DefaultConversantNameGenerator implements NameGenerator {
	String[]	names	= new String[] { "Stranger1", "Stranger2" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.omeglespy$z.NameGenerator#next(int, boolean)
	 */
	public String[] next(final int numberOfNames, final boolean uniqueNames) {
		if (numberOfNames > names.length) { throw new IllegalArgumentException(
				"The number of names requested exceeds the available number of names"); }

		return Arrays.copyOfRange(names, 0, numberOfNames);
	}

}
