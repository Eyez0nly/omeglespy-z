/*
 * #%L omeglespy-z-cl
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author user
 * 
 */
public class CommonsLoggingLogHelper extends LogHelper {
	private final Map<Class<?>, Log>		classLogMap					= new Hashtable<Class<?>, Log>();
	private static Map<LogLevel, String>	loggingMethodNameMap		= new HashMap<LogLevel, String>();
	static {
		loggingMethodNameMap.put(LogLevel.DEBUG, "debug");
		loggingMethodNameMap.put(LogLevel.ERROR, "error");
		loggingMethodNameMap.put(LogLevel.FATAL, "fatal");
		loggingMethodNameMap.put(LogLevel.TRACE, "trace");
		loggingMethodNameMap.put(LogLevel.INFO, "info");
		loggingMethodNameMap.put(LogLevel.WARN, "warn");
	}
	private static Map<LogLevel, Method>	loggingLevelCheckMethodMap	= new HashMap<LogLevel, Method>();
	static {
		try {
			final Class<?>[] params = new Class[0];
			loggingLevelCheckMethodMap.put(LogLevel.DEBUG, Log.class.getDeclaredMethod("isDebugEnabled", params));
			loggingLevelCheckMethodMap.put(LogLevel.ERROR, Log.class.getDeclaredMethod("isErrorEnabled", params));
			loggingLevelCheckMethodMap.put(LogLevel.FATAL, Log.class.getDeclaredMethod("isFatalEnabled", params));
			loggingLevelCheckMethodMap.put(LogLevel.TRACE, Log.class.getDeclaredMethod("isTraceEnabled", params));
			loggingLevelCheckMethodMap.put(LogLevel.INFO, Log.class.getDeclaredMethod("isInfoEnabled", params));
			loggingLevelCheckMethodMap.put(LogLevel.WARN, Log.class.getDeclaredMethod("isWarnEnabled", params));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	private static Object[]					logLevelCheckerArgs			= new Object[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy.log.LogHelper#checkIsLogLevelEnabled(org.darkimport
	 * .omeglespy.log.LogLevel)
	 */
	@Override
	protected boolean checkIsLogLevelEnabled(final LogLevel logLevel, final Class<?> clazz) {
		final Log log = getLog(clazz);
		try {
			return (Boolean) loggingLevelCheckMethodMap.get(logLevel).invoke(log, logLevelCheckerArgs);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
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
		final Log log = getLog(clazz);
		Object[] args;
		Class<?>[] params;
		if (t == null) {
			args = new Object[] { msg };
			params = new Class[] { Object.class };
		} else {
			args = new Object[] { msg, t };
			params = new Class[] { Object.class, Throwable.class };
		}
		try {
			Log.class.getDeclaredMethod(loggingMethodNameMap.get(logLevel), params).invoke(log, args);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param clazz
	 */
	private Log getLog(final Class<?> clazz) {
		Log log = classLogMap.get(clazz);
		if (log == null) {
			log = LogFactory.getLog(clazz);
			classLogMap.put(clazz, log);
		}
		return log;
	}

}
