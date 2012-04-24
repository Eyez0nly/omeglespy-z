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

import java.net.URL;

/**
 * Requests for communication with an external service are passed through this
 * class. The most basic implementation will simply use http to pass
 * communication requests to the service. However, more complicated
 * implementations might employ a proxy or some kind of browser emulation
 * pattern.
 * 
 * Note that this API will probably change in the future to be more transport
 * agnostic.
 * 
 * @author user
 * @version $Id: $
 */
public abstract class CommunicationHelper {
	private static CommunicationHelper	_instance	= new DefaultCommunicationHelper();

	/**
	 * Initializes the CommunicationHelper with the given implementation.
	 * 
	 * @param communicationHelper
	 *            a {@link org.darkimport.omeglespy_z.CommunicationHelper}
	 *            object.
	 */
	public static void initialize(final CommunicationHelper communicationHelper) {
		_instance = communicationHelper;
	}

	/**
	 * Make a request.
	 * 
	 * 
	 * @param url
	 *            a {@link java.net.URL} object. The requested URL.
	 * @param post
	 *            a boolean. Is the request HTTP POST?
	 * @param post_data
	 *            a {@link java.lang.String} object. The data to be posted. key,
	 *            value, key, value, etc.
	 * @return a {@link java.lang.String} object. The response.
	 * @throws java.lang.Exception
	 *             if any communication errors have occurred.
	 */
	public static String wget(final URL url, final boolean post, final String... post_data) throws Exception {
		return wget(url, post, false, post_data);
	}

	/**
	 * Make a request.
	 * 
	 * @param url
	 *            a {@link java.net.URL} object. The requested URL.
	 * @param post
	 *            a boolean. Is the request HTTP POST?
	 * @param ignore
	 *            a boolean. Do we ignore the response?
	 * @param post_data
	 *            a {@link java.lang.String} object. The data to be posted. key,
	 *            value, key, value, etc.
	 * @return a {@link java.lang.String} object.
	 * @throws java.lang.Exception
	 *             if any communication errors have occurred.
	 */
	public static String wget(final URL url, final boolean post, final boolean ignore, final String... post_data)
			throws Exception {
		return _instance.doWget(url, post, ignore, post_data);
	}

	/**
	 * Sub classes make the request here.
	 * 
	 * @param url
	 *            a {@link java.net.URL} object. The requested URL.
	 * @param post
	 *            a boolean. Is the request HTTP POST?
	 * @param ignore
	 *            a boolean. Do we ignore the response?
	 * @param post_data
	 *            a {@link java.lang.String} object.The data to be posted. key,
	 *            value, key, value, etc.
	 * @return a {@link java.lang.String} object.
	 * @throws java.lang.Exception
	 *             if any communication errors have occurred.
	 */
	protected abstract String doWget(final URL url, final boolean post, final boolean ignore, final String... post_data)
			throws Exception;
}
