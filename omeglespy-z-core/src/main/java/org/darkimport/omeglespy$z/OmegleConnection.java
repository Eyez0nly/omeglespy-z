/**
 * 
 */
package org.darkimport.omeglespy$z;

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
class OmegleConnection extends Observable implements Runnable {
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
		// TODO Auto-generated method stub
		init();
		final String startr = CommunicationHelper.wget(start_url, true);
		Matcher m = STR_REGEX.matcher(startr);
		if (m.matches()) {
			chatId = m.group(2); // 2 is the actual string - Listner(sky) has
			// started his chat session here is the ID
			LogHelper.log(OmegleConnection.class, LogLevel.DEBUG, "Chat Started - chatId[" + chatId + "]");
		}

		String eventr;
		while (chatId != null && (eventr = CommunicationHelper.wget(events_url, true, "id", chatId)) != null
				&& !eventr.equals("null")) {
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
			try {
				// TODO experimenting with randomized sleep time.
				Thread.sleep(50 + (int) (Math.random() * 100));
			} catch (final InterruptedException e) {
				LogHelper.log(OmegleConnection.class, LogLevel.WARN, "Thread error.", e);
			}
		}
		if (chatId != null) {
			// We left the loop because the server no longer recognizes our chat
			// ID. TODO Or does it? Is it possible that there was a transmission
			// error?
			LogHelper.log(OmegleConnection.class, LogLevel.WARN,
					"Session nullification detected without accompanying strangerDisconnect event.");
			chatId = null;
		}
	}

	public void init() {
		LogHelper.log(OmegleConnection.class, LogLevel.INFO, "-- Initializing, Please wait...");
		LogHelper.log(OmegleConnection.class, LogLevel.INFO, "* Chat server selected: " + serverName);
		try {
			final URL init_1 = new URL("http://www.omegle.com");
			final URL init_2 = new URL("http://www.omegle.com/static/frameset.js?1");
			final URL init_3 = new URL(PROTOCOL, serverName, "/static/style.css?25");
			final URL init_4 = new URL(PROTOCOL, serverName, "/static/omegle.js?83");
			CommunicationHelper.wget(init_1, false, true);
			CommunicationHelper.wget(init_2, false, true);
			CommunicationHelper.wget(init_3, false, true);
			CommunicationHelper.wget(init_4, false, true);
		} catch (final Exception e) {
			LogHelper.log(OmegleConnection.class, LogLevel.WARN, "An error occurred during initialization.", e);
		}

		LogHelper.log(OmegleConnection.class, LogLevel.INFO, "-- Initialization process has been completed.");
	}

	public static String unJsonify(final String jsonString) {
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
		final String r = CommunicationHelper.wget(type_url, true, "id", chatId);
		return COMMUNICATION_SUCCESS_STRING.equals(r);
	}

	public boolean sendMesssage(final String msg) {
		final String sendr = CommunicationHelper.wget(send_url, true, "id", chatId, "msg", msg);
		return COMMUNICATION_SUCCESS_STRING.equals(sendr);
	}

	public boolean sendStoppedTypingNotification() {
		final String r = CommunicationHelper.wget(stoptype_url, true, "id", chatId);
		return COMMUNICATION_SUCCESS_STRING.equals(r);
	}

	public boolean disconnect() {
		final String d = CommunicationHelper.wget(disconnect_url, true, "id", chatId);
		chatId = null;
		return d != null && d.equals(COMMUNICATION_SUCCESS_STRING);
	}

	public void sendRecaptchaResponse(final String challenge, final String response) {
		CommunicationHelper.wget(recaptcha_url, true, "id", chatId, "challenge", challenge, "response", response);
	}
}
