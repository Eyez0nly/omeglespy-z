/**
 * 
 */
package org.darkimport.omeglespy.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.darkimport.configuration.ConfigHelper;
import org.darkimport.omeglespy.constants.ConfigConstants;
import org.darkimport.omeglespy.log.LogHelper;
import org.darkimport.omeglespy.log.LogLevel;

/**
 * For now, we just do string matching on some known bot tells.
 * 
 * The tells are loaded from filter.txt at the first invokation of isBadMessage.
 * 
 * @author user
 * 
 */
public class FilterHelper {
	private static boolean		initialized	= false;
	private static List<String>	badMessages;

	public static boolean isBadMessage(final String msg) {
		if (!initialized) {
			initialize();
		}

		return badMessages.contains(msg.trim());
	}

	// TODO Get the filename from the settings file instead of hard coding it.
	private static synchronized void initialize() {
		badMessages = new ArrayList<String>();
		InputStream in = null;
		try {
			final String filterfile = ConfigHelper.getGroup(ConfigConstants.GROUP_MAIN).getProperty(
					ConfigConstants.MAIN_FILTERFILE);
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(filterfile);
			final List<String> loadedBadMessages = IOUtils.readLines(in);
			badMessages.addAll(loadedBadMessages);
		} catch (final Exception e) {
			LogHelper.log(FilterHelper.class, LogLevel.WARN, "Failed to load bad words.", e);
		} finally {
			// Set initialized to true regardless of the outcome.
			// TODO Later we will prevent changing the filter setting in the app
			// if initialization failed.
			initialized = true;
			IOUtils.closeQuietly(in);
		}
	}

}
