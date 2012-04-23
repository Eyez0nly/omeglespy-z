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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author user
 * 
 */
public class OmegleConnection extends Observable implements Runnable {
	private static final String	COMMUNICATION_SUCCESS_STRING	= "win";
	private static final String	PROTOCOL						= "http";
	public static final Pattern	STR_REGEX						= Pattern
																		.compile("(\")((?>(?:(?>[^\"\\\\]+)|\\\\.)*))\\1");
	public static final Pattern	ESCAPE_REGEX					= Pattern.compile("\\\\([\'\"\\\\bfnrt]|u(....))");

	private String				conversantName;
	private final String		serverName;

	private String				chatId;

	private URL					type_url;
	private URL					stoptype_url;
	private URL					disconnect_url;
	private URL					send_url;
	private URL					events_url;
	private URL					start_url;
	private URL					recaptcha_url;
	private boolean				paused;

	public OmegleConnection(final String conversantName, final String serverName) {
		this.conversantName = conversantName;
		this.serverName = serverName;
		try {
			start_url = new URL(PROTOCOL, serverName, "/start?rcs=1&spid=");
			events_url = new URL(PROTOCOL, serverName, "/events");
			send_url = new URL(PROTOCOL, serverName, "/send");
			disconnect_url = new URL(PROTOCOL, serverName, "/disconnect");
			type_url = new URL(PROTOCOL, serverName, "/typing");
			stoptype_url = new URL(PROTOCOL, serverName, "/stoppedtyping");
			recaptcha_url = new URL(PROTOCOL, serverName, "/recaptcha");
		} catch (final MalformedURLException ex) {}
	}

	public String getConversantName() {
		return conversantName;
	}

	/**
	 * @param conversantName
	 *            the name to set
	 */
	public void setConversantName(final String conversantName) {
		this.conversantName = conversantName;
	}

	/**
	 * During run, the omegle server will be periodically event-pinged. When an
	 * event occurs, observers are notified.
	 */
	public void run() {
		try {
			init();
		} catch (final Exception e) {
			LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during initialization.", e);
			setChanged();
			notifyObservers(new OmegleEvent(OmegleEventType._initializationFailure, null));
			return;
		}

		String startr;
		try {
			startr = CommunicationHelper.wget(start_url, true);
		} catch (final Exception e) {
			LogHelper.log(OmegleConnection.class, LogLevel.WARN,
					"An error occurred while attempting to start a conversation.", e);
			setChanged();
			notifyObservers(new OmegleEvent(OmegleEventType._conversationStartFailure, null));
			return;
		}

		Matcher m = STR_REGEX.matcher(startr);
		if (m.matches()) {
			chatId = m.group(2); // 2 is the actual string - Listner(sky) has
			// started his chat session here is the ID
			LogHelper.log(OmegleConnection.class, LogLevel.DEBUG, "Chat Started - chatId[" + chatId + "]");
		}

		while (chatId != null) {
			try {
				// TODO experimenting with randomized sleep time.
				Thread.sleep(50 + (int) (Math.random() * 100));
			} catch (final InterruptedException e) {
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "Thread error.", e);
			}

			if (!paused) {
				String eventr;
				try {
					eventr = CommunicationHelper.wget(events_url, true, "id", chatId);
				} catch (final Exception e) {
					LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during event polling.", e);
					setChanged();
					// TODO Establish threshold for general communication
					// failure tolerance.
					notifyObservers(new OmegleEvent(OmegleEventType._generalCommunicationFailure, null));
					continue;
				}

				if (eventr == null || eventr.equals("null")) {
					break;
				}
				LogHelper.log(OmegleConnection.class, LogLevel.DEBUG, "Dispatch - eventr[" + eventr + "]");

				final List<List<String>> events = new LinkedList<List<String>>();
				List<String> currentEvent = null;
				m = STR_REGEX.matcher(eventr);
				while (m.find()) {
					if (eventr.charAt(m.start() - 1) == '[') {
						currentEvent = new LinkedList<String>();
						events.add(currentEvent);
					}
					currentEvent.add(unJsonify(m.group(2)));
				}
				// PARSE THE EVENT STRING HERE
				for (final List<String> ev : events) {
					setChanged();
					final String name = ev.remove(0);
					final String[] args = ev.toArray(new String[0]);

					final OmegleEventType eventType = OmegleEventType.valueOf(name);
					notifyObservers(new OmegleEvent(eventType, args));
				}
			} else {
				LogHelper.log(OmegleConnection.class, LogLevel.TRACE, "The connection with " + conversantName
						+ " is paused.");
			}
		}
		if (chatId != null) {
			// We left the loop because the server no longer recognizes our chat
			// ID.
			LogHelper.log(OmegleConnection.class, LogLevel.WARN,
					"Session nullification detected without accompanying strangerDisconnect event.");
			chatId = null;
		}
	}

	public void init() throws Exception {
		LogHelper.log(OmegleConnection.class, LogLevel.INFO, "-- Initializing, Please wait...");
		LogHelper.log(OmegleConnection.class, LogLevel.INFO, "* Chat server selected: " + serverName);

		final URL init_1 = new URL("http://www.omegle.com");
		final URL init_2 = new URL("http://www.omegle.com/static/frameset.js?1");
		final URL init_3 = new URL(PROTOCOL, serverName, "/static/style.css?25");
		final URL init_4 = new URL(PROTOCOL, serverName, "/static/omegle.js?83");
		CommunicationHelper.wget(init_1, false, true);
		CommunicationHelper.wget(init_2, false, true);
		CommunicationHelper.wget(init_3, false, true);
		CommunicationHelper.wget(init_4, false, true);

		LogHelper.log(OmegleConnection.class, LogLevel.INFO, "-- Initialization process has been completed.");
	}

	private static String unJsonify(final String jsonString) {
		final Matcher m = ESCAPE_REGEX.matcher(jsonString);
		final StringBuffer sb = new StringBuffer();
		while (m.find()) {
			final String escaped = m.group(1);
			final char e = escaped.charAt(0);
			char c;
			switch (e) {
				case '\'':
				case '\"':
				case '\\':
					c = e;
					break;

				case 'r':
					c = '\r';
					break;
				case 'n':
					c = '\n';
					break;
				case 'b':
					c = '\b';
					break;
				case 'f':
					c = '\f';
					break;
				case 't':
					c = '\t';
					break;

				case 'u':
					final String hex = m.group(2);
					c = (char) Integer.parseInt(hex, 16);
					break;

				default:
					c = e;
					break;
			}
			try {
				m.appendReplacement(sb, "" + c);
			} catch (final Exception ex) {
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "[" + new Date() + "]:");
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "sb = " + sb.toString());
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "e = " + e);
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "c = " + c);
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "escaped = " + escaped);
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "m.group(0) = " + m.group(0), ex);
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public boolean sendTypingNotification() {
		String r = null;
		if (chatId != null) {
			try {
				r = CommunicationHelper.wget(type_url, true, "id", chatId);
			} catch (final Exception e) {
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during event polling.", e);
				setChanged();
				// TODO Establish threshold for general communication
				// failure tolerance.
				notifyObservers(new OmegleEvent(OmegleEventType._generalCommunicationFailure, null));
			}
		}
		return COMMUNICATION_SUCCESS_STRING.equals(r);
	}

	public boolean sendMesssage(final String msg) {
		String sendr = null;
		if (chatId != null) {
			try {
				sendr = CommunicationHelper.wget(send_url, true, "id", chatId, "msg", msg);
			} catch (final Exception e) {
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during event polling.", e);
				setChanged();
				// TODO Establish threshold for general communication
				// failure tolerance.
				notifyObservers(new OmegleEvent(OmegleEventType._generalCommunicationFailure, null));
			}
		}
		return COMMUNICATION_SUCCESS_STRING.equals(sendr);
	}

	public boolean sendStoppedTypingNotification() {
		String r = null;
		if (chatId != null) {
			try {
				r = CommunicationHelper.wget(stoptype_url, true, "id", chatId);
			} catch (final Exception e) {
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during event polling.", e);
				setChanged();
				// TODO Establish threshold for general communication
				// failure tolerance.
				notifyObservers(new OmegleEvent(OmegleEventType._generalCommunicationFailure, null));
			}
		}
		return COMMUNICATION_SUCCESS_STRING.equals(r);
	}

	public boolean disconnect() {
		String d = null;
		if (chatId != null) {
			// Setting up a reference to the chatId
			final String _chatId = chatId;
			// because we're nullifying it immediately so that event polling
			// will cease now.
			chatId = null;
			try {
				d = CommunicationHelper.wget(disconnect_url, true, "id", _chatId);
				setChanged();
				notifyObservers(new OmegleEvent(OmegleEventType._userDisconnected, null));
			} catch (final Exception e) {
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during event polling.", e);
				setChanged();
				// TODO Establish threshold for general communication
				// failure tolerance.
				notifyObservers(new OmegleEvent(OmegleEventType._generalCommunicationFailure, null));
			}
		} else {
			LogHelper.log(OmegleConnection.class, LogLevel.WARN, "Disconnect was invoked without having a connection.");
		}
		return COMMUNICATION_SUCCESS_STRING.equals(d);
	}

	public void stop() {
		chatId = null;
	}

	public void sendRecaptchaResponse(final String challenge, final String response) {
		try {
			CommunicationHelper.wget(recaptcha_url, true, "id", chatId, "challenge", challenge, "response", response);
		} catch (final Exception e) {
			LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during event polling.", e);
			setChanged();
			// TODO Establish threshold for general communication
			// failure tolerance.
			notifyObservers(new OmegleEvent(OmegleEventType._generalCommunicationFailure, null));
		}
		paused = false;
		LogHelper.log(OmegleConnection.class, LogLevel.DEBUG, "Unpaused the event pinger.");
	}

	public void pause() {
		paused = true;
		LogHelper.log(OmegleConnection.class, LogLevel.DEBUG, "Paused the event pinger.");
	}
}
