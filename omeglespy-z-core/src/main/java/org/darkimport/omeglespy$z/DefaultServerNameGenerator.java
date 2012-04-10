/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.util.Arrays;

/**
 * @author user
 * 
 */
public class DefaultServerNameGenerator implements NameGenerator {
	private final String	serverName	= "promenade.omegle.com";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.omeglespy$z.NameGenerator#next(int, boolean)
	 */
	public String[] next(final int numberOfNames, final boolean uniqueNames) {
		final String[] servers = new String[numberOfNames];
		Arrays.fill(servers, serverName);
		return servers;
	}

}
