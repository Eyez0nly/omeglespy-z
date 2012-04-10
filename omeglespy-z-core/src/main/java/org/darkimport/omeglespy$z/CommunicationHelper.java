/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.net.URL;

/**
 * @author user
 * 
 */
public abstract class CommunicationHelper {
	private static CommunicationHelper	_instance;

	public static void initialize(final CommunicationHelper communicationHelper) {
		_instance = communicationHelper;
	}

	public static String wget(final URL url, final boolean post, final String... post_data) {
		return wget(url, post, false, post_data);
	}

	public static String wget(final URL url, final boolean post, final boolean ignore, final String... post_data) {
		return _instance.doWget(url, post, ignore, post_data);
	}

	protected abstract String doWget(final URL url, final boolean post, final boolean ignore, final String... post_data);
}
