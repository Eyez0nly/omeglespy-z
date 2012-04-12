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
package org.darkimport.omeglespy$z;

/**
 * @author user
 * 
 */
public abstract class LogHelper {
	private static LogHelper	_instance	= new DefaultLogHelper();

	public static void initialize(final LogHelper logHelper) {
		_instance = logHelper;
	}

	public static void log(final Class<?> clazz, final LogLevel logLevel, final Object msg, final Throwable t) {
		_instance.doLog(clazz, logLevel, msg, t);
	}

	public static void log(final Class<?> clazz, final LogLevel logLevel, final Object msg) {
		log(clazz, logLevel, msg, null);
	}

	public static boolean isLogLevelEnabled(final LogLevel logLevel, final Class<?> clazz) {
		return _instance.checkIsLogLevelEnabled(logLevel, clazz);
	}

	protected abstract boolean checkIsLogLevelEnabled(LogLevel logLevel, Class<?> clazz);

	protected abstract void doLog(final Class<?> clazz, final LogLevel logLevel, final Object msg, final Throwable t);
}
