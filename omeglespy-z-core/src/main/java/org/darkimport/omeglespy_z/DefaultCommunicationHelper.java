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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * This is the trivial default implementation of the {@link CommunicationHelper}
 * .
 * 
 * @author user
 * @version $Id: $
 */
public class DefaultCommunicationHelper extends CommunicationHelper {
	/** {@inheritDoc} */
	@Override
	protected String doWget(final URL url, final boolean post, final boolean ignore, final String... post_data)
			throws Exception {
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
			throw ex;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (final Exception e) {
					LogHelper.log(DefaultCommunicationHelper.class, LogLevel.DEBUG,
							"An error occurred while closing an input stream", e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (final Exception e) {
					LogHelper.log(DefaultCommunicationHelper.class, LogLevel.DEBUG,
							"An error occurred while closing an output stream", e);
				}
			}
		}
	}
}
