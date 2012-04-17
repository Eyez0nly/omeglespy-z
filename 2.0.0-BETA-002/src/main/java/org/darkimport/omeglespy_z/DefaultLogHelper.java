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

/**
 * @author user
 * 
 */
public class DefaultLogHelper extends LogHelper {
	private static long	startTime	= System.currentTimeMillis();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy.log.LogHelper#checkIsLogLevelEnabled(org.darkimport
	 * .omeglespy.log.LogLevel, java.lang.Class)
	 */
	@Override
	protected boolean checkIsLogLevelEnabled(final LogLevel logLevel, final Class<?> clazz) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.omeglespy.log.LogHelper#doLog(java.lang.Class,
	 * org.darkimport.omeglespy.log.LogLevel, java.lang.Object,
	 * java.lang.Throwable)
	 */
	@Override
	protected void doLog(final Class<?> clazz, final LogLevel logLevel, final Object msg, final Throwable t) {
		final StringBuffer message = new StringBuffer();
		message.append(System.currentTimeMillis() - startTime).append(" - ").append(logLevel.name()).append(" - ")
				.append(clazz.getName()).append(" - ").append(msg.toString());
		System.out.println(message.toString());
		if (t != null) {
			t.printStackTrace(System.out);
		}
	}

}
