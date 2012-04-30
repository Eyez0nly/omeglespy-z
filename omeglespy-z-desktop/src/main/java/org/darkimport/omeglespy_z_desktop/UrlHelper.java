/*
 * #%L omeglespy-z-desktop
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
package org.darkimport.omeglespy_z_desktop;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.darkimport.config.ConfigHelper;
import org.darkimport.omeglespy_z.LogHelper;
import org.darkimport.omeglespy_z.LogLevel;
import org.darkimport.omeglespy_z_desktop.constants.ConfigConstants;

public class UrlHelper {
	private static final Runtime	rt		= Runtime.getRuntime();
	private static final String		osName	= System.getProperty("os.name");

	private static String[]			browsers;
	static {
		InputStream is = null;
		try {
			final String browsersfile = ConfigHelper.getGroup(ConfigConstants.GROUP_MAIN).getProperty(
					ConfigConstants.MAIN_BROWSERSFILE);
			is = ConfigHelper.getConfigurationStream(browsersfile);
			final List<String> browserList = IOUtils.readLines(is, null);

			browsers = browserList.toArray(new String[browserList.size()]);
		} catch (final Exception e) {
			browsers = new String[0];
			LogHelper.log(UrlHelper.class, LogLevel.WARN,
					"Could not load browsers file. This only matters if you're running Linux.", e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private UrlHelper() {}

	public static void openURL(final String url) throws Exception {
		if (osName.startsWith("Mac OS")) {
			final Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
			final Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
			openURL.invoke(null, new Object[] { url });
		} else if (osName.startsWith("Windows")) {
			rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
		} else {
			// assume Unix or Linux
			boolean found = false;
			for (final String browser : browsers) {
				if (!found) {
					found = rt.exec(new String[] { "which", browser }).waitFor() == 0;
					if (found) {
						rt.exec(new String[] { browser, url });
					}
				}
			}
			if (!found) { throw new Exception("Could not launch any web browser"); }
		}
	}

}
