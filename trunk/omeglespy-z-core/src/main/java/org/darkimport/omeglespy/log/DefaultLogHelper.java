/**
 * 
 */
package org.darkimport.omeglespy.log;

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
