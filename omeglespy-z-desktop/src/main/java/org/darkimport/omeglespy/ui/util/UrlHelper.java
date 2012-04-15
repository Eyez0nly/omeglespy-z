package org.darkimport.omeglespy.ui.util;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.darkimport.config.ConfigHelper;
import org.darkimport.omeglespy.ConfigConstants;
import org.darkimport.omeglespy$z.LogHelper;
import org.darkimport.omeglespy$z.LogLevel;

public class UrlHelper {
	private static final Runtime	rt		= Runtime.getRuntime();
	private static final String		osName	= System.getProperty("os.name");

	private static String[]			browsers;
	static {
		InputStream is = null;
		try {
			final String browsersfile = ConfigHelper.getGroup(ConfigConstants.GROUP_MAIN).getProperty(
					ConfigConstants.MAIN_BROWSERSFILE);
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(browsersfile);
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
