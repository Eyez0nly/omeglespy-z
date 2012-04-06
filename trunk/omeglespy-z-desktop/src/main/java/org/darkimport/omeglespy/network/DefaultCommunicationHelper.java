/**
 * 
 */
package org.darkimport.omeglespy.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.darkimport.omeglespy.log.LogHelper;
import org.darkimport.omeglespy.log.LogLevel;

/**
 * @author user
 * 
 */
public class DefaultCommunicationHelper extends CommunicationHelper {
	@Override
	protected String doWget(final URL url, final boolean post, final boolean ignore, final String... post_data) {
		String msg = "";
		InputStream in = null;
		OutputStream out = null;
		String data = null;
		try {
			final URLConnection urlcon = url.openConnection();

			if (post) {
				boolean key = false;
				for (final String s : post_data) {
					msg += URLEncoder.encode(s, "UTF-8");
					if (key = !key) {
						msg += "=";
					} else {
						msg += "&";
					}
				}
				urlcon.setDoOutput(true);
				out = urlcon.getOutputStream();
				out.write(msg.getBytes());
			}

			in = urlcon.getInputStream();
			data = ignore ? null : "";
			int len;
			final byte[] buffer = new byte[1023];
			while ((len = in.read(buffer)) >= 0) {
				if (!ignore) {
					data += new String(buffer, 0, len);
				}
			}
			if (LogHelper.isLogLevelEnabled(LogLevel.DEBUG, DefaultCommunicationHelper.class)) {
				LogHelper.log(DefaultCommunicationHelper.class, LogLevel.DEBUG, "WGET= URL[" + url.toString() + "?"
						+ msg + "] RETURN[" + data + "]");
			}

			return data;
		} catch (final Exception ex) {
			LogHelper.log(DefaultCommunicationHelper.class, LogLevel.WARN, "An error occurred while submitting " + msg
					+ " request to " + url.toString() + " with the following data: " + data, ex);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
}
