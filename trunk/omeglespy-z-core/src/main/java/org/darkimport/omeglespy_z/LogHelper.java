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

/**
 * This LogHelper is used throughout the core library for logging in lieu of
 * establishing a dependency on a logging framework. The offshoot is that,
 * unless you want this library's output logged to System.out, you must provide
 * a concrete implementation of LogHelper that utilizes your logging framework
 * of choice.
 * 
 * If this class seems to be missing elements that would make it more convenient
 * to use your desired logging framework, please fill out an issue at
 * http://code.google.com/p/omeglespy-z/issues/list
 * 
 * @author user
 * @version $Id: $
 */
public abstract class LogHelper {
	private static LogHelper	_instance	= new DefaultLogHelper();

	/**
	 * Sets the LogHelper instance to the given LogHelper.
	 * 
	 * @param logHelper
	 *            a {@link org.darkimport.omeglespy_z.LogHelper} object.
	 */
	public static void initialize(final LogHelper logHelper) {
		_instance = logHelper;
	}

	/**
	 * Logs a message. Similar to commons-logging's Log.foo(Object, Throwable)
	 * (where foo is your desired LogLevel).
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @param logLevel
	 *            a {@link org.darkimport.omeglespy_z.LogLevel} object.
	 * @param msg
	 *            a {@link java.lang.Object} object.
	 * @param t
	 *            a {@link java.lang.Throwable} object.
	 */
	public static void log(final Class<?> clazz, final LogLevel logLevel, final Object msg, final Throwable t) {
		_instance.doLog(clazz, logLevel, msg, t);
	}

	/**
	 * Logs a message. Similar to commons-logging's Log.foo(Object) (where foo
	 * is your desired LogLevel).
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @param logLevel
	 *            a {@link org.darkimport.omeglespy_z.LogLevel} object.
	 * @param msg
	 *            a {@link java.lang.Object} object.
	 */
	public static void log(final Class<?> clazz, final LogLevel logLevel, final Object msg) {
		log(clazz, logLevel, msg, null);
	}

	/**
	 * Asks if the given log level enabled. Similar to commons-logging's
	 * Log.isFooEnabled() (where foo is your desired LogLevel).
	 * 
	 * @param logLevel
	 *            a {@link org.darkimport.omeglespy_z.LogLevel} object.
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @return a boolean.
	 */
	public static boolean isLogLevelEnabled(final LogLevel logLevel, final Class<?> clazz) {
		return _instance.checkIsLogLevelEnabled(logLevel, clazz);
	}

	/**
	 * Sub classes override this method to answer the question of whether or not
	 * the given log level for the logger associated with the given class is
	 * enabled.
	 * 
	 * @param logLevel
	 *            a {@link org.darkimport.omeglespy_z.LogLevel} object.
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @return a boolean.
	 */
	protected abstract boolean checkIsLogLevelEnabled(LogLevel logLevel, Class<?> clazz);

	/**
	 * Sub classes override this method to log a message with the logger
	 * associated with the given class.
	 * 
	 * @param clazz
	 *            a {@link java.lang.Class} object.
	 * @param logLevel
	 *            a {@link org.darkimport.omeglespy_z.LogLevel} object.
	 * @param msg
	 *            a {@link java.lang.Object} object.
	 * @param t
	 *            a {@link java.lang.Throwable} object.
	 */
	protected abstract void doLog(final Class<?> clazz, final LogLevel logLevel, final Object msg, final Throwable t);
}
