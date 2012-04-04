/**
 * 
 */
package org.darkimport.omeglespy.log;

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
