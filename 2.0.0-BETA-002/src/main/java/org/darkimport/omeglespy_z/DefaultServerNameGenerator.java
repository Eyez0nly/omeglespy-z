/*
 * #%L
 * omeglespy-z-core
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 darkimport
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
/**
 * 
 */
package org.darkimport.omeglespy_z;

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
	 * @see org.darkimport.omeglespy_z.NameGenerator#next(int, boolean)
	 */
	public String[] next(final int numberOfNames, final boolean uniqueNames) {
		final String[] servers = new String[numberOfNames];
		Arrays.fill(servers, serverName);
		return servers;
	}

}
