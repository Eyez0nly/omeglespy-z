package org.darkimport.omeglespy;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.darkimport.configuration.ConfigHelper;
import org.darkimport.omeglespy.constants.ConfigConstants;
import org.darkimport.omeglespy.log.LogHelper;
import org.darkimport.omeglespy.log.LogLevel;
import org.darkimport.omeglespy.network.CommunicationHelper;

class Omegle implements Runnable {
	public static final Pattern			str_regex			= Pattern.compile("(\")((?>(?:(?>[^\"\\\\]+)|\\\\.)*))\\1");
	public static final Pattern			escape_regex		= Pattern.compile("\\\\([\'\"\\\\bfnrt]|u(....))");
	public static final String			EV_CONNECTING, EV_WAITING, EV_CONNECTED, EV_TYPING, EV_STOPPED_TYPING, EV_MSG,
			EV_DISCONNECT, EV_RECAPTCHA, EV_RECAPTCHAREJECT;
	static {
		EV_CONNECTING = "connecting";
		EV_WAITING = "waiting";
		EV_CONNECTED = "connected";
		EV_TYPING = "typing";
		EV_MSG = "gotMessage";
		EV_STOPPED_TYPING = "stoppedTyping";
		EV_DISCONNECT = "strangerDisconnected";
		EV_RECAPTCHA = "recaptchaRequired";
		EV_RECAPTCHAREJECT = "recaptchaRejected";
	}

	public String						omegleServer;

	private URL							init_1, init_2, init_3, init_4;

	private URL							start_url, events_url, send_url, disc_url, type_url, stoptype_url,
										/* totalcount_url, count_url, */recaptcha_url;

	private String						chatId;
	private boolean						dead;
	private final List<OmegleListener>	listeners;

	// Initializing the server list with one server just in case we can't load
	// the servers from the list.
	private static String[]				omegleServerList	= new String[] { "quarks.omegle.com" };
	static {
		InputStream in = null;
		String servernamesfile = null;
		try {
			servernamesfile = ConfigHelper.getGroup(ConfigConstants.GROUP_MAIN).getProperty(
					ConfigConstants.MAIN_SERVERNAMESFILE);
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(servernamesfile);
			final List<String> loadedServers = IOUtils.readLines(in);
			if (loadedServers != null && loadedServers.size() > 0) {
				omegleServerList = loadedServers.toArray(new String[loadedServers.size()]);
			} else {
				LogHelper.log(Omegle.class, LogLevel.WARN,
						"No servers loaded. Check that the specified server name config file, " + servernamesfile
								+ ", is configured correctly.");
			}
		} catch (final Exception e) {
			LogHelper.log(Omegle.class, LogLevel.WARN, "Unable to load omegle servers from the list.", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public Omegle() {
		chatId = null;
		dead = false;
		listeners = new LinkedList<OmegleListener>();
	}

	public void setOmegleRoot() {
		try {
			start_url = new URL(omegleServer + "start?rcs=1&spid=");
			events_url = new URL(omegleServer + "events");
			send_url = new URL(omegleServer + "send");
			disc_url = new URL(omegleServer + "disconnect");
			type_url = new URL(omegleServer + "typing");
			stoptype_url = new URL(omegleServer + "stoppedtyping");
			// count_url = new URL(omegleServer + "count");
			// totalcount_url = new URL(omegleServer + "totalcount");
			setRecaptcha_url(new URL(omegleServer + "recaptcha"));
		} catch (final MalformedURLException ex) {}
	}

	public void addOmegleListener(final OmegleListener ol) {
		// check if chatId == null?
		listeners.add(ol);
	}

	public void removeOmegleListener(final OmegleListener ol) {
		// check if chatId == null?
		listeners.remove(ol);
	}

	public boolean isConnected() {
		return chatId != null;
	}

	public boolean start() {
		if (chatId != null || dead) { return false; }

		final String startr = CommunicationHelper.wget(start_url, true);
		if (startr == null) { return false; }
		final Matcher m = str_regex.matcher(startr);
		if (m.matches()) {
			chatId = m.group(2); // 2 is the actual string - Listner(sky) has
			// started his chat session here is the ID
			LogHelper.log(Omegle.class, LogLevel.DEBUG, "Chat Started - chatId[" + chatId + "]");
		} else {
			return false;
		}
		new Thread(this).start();
		return true;
	}

	public void run() {
		String eventr;
		while (chatId != null && (eventr = CommunicationHelper.wget(events_url, true, "id", chatId)) != null
				&& !eventr.equals("null")) {
			dispatch(eventr);
			try {
				// TODO randomize the sleep time.
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				LogHelper.log(Omegle.class, LogLevel.WARN, "Thread error.", e);
			}
		}
		if (chatId != null) {
			// We left the loop because the server no longer recognizes our chat
			// ID. TODO Or does it? Is it possible that there was a transmission
			// error?
			LogHelper.log(Omegle.class, LogLevel.WARN,
					"** Fuck... An event check returned a null - This is a session destroyer. **");
			LogHelper.log(Omegle.class, LogLevel.WARN,
					"** Restart OmegleSpyX and hope Omegle didnt ban your ass. (symptoms of a ban). **");
			chatId = null;
		}
	}

	public void dispatch(final String eventr) {
		LogHelper.log(Omegle.class, LogLevel.DEBUG, "Dispatch - eventr[" + eventr + "]");

		final List<List<String>> events = new LinkedList<List<String>>();
		List<String> currentEvent = null;
		final Matcher m = str_regex.matcher(eventr);
		while (m.find()) {
			if (eventr.charAt(m.start() - 1) == '[') {
				currentEvent = new LinkedList<String>();
				events.add(currentEvent);
			}
			currentEvent.add(unJsonify(m.group(2)));
		}
		// PARSE THE EVENT STRING HERE
		for (final List<String> ev : events) {
			final String name = ev.remove(0);
			final String[] args = ev.toArray(new String[0]);
			for (final OmegleListener ol : listeners) {
				ol.eventFired(this, name, args);
			}
		}
	}

	public boolean typing() {
		if (chatId == null) { return false; }

		final String r = CommunicationHelper.wget(type_url, true, "id", chatId);
		return r != null && r.equals("win");
	}

	public boolean stoppedTyping() {
		if (chatId == null) { return false; }

		final String r = CommunicationHelper.wget(stoptype_url, true, "id", chatId);
		return r != null && r.equals("win");
	}

	public boolean sendMsg(final String msg) {
		if (chatId == null) { return false; }

		// omegleListeners.messageInTheProcessOfSending()
		final String sendr = CommunicationHelper.wget(send_url, true, "id", chatId, "msg", msg);
		if (sendr == null) { return false; }

		final boolean b = sendr.equals("win");
		if (b) {
			for (final OmegleListener ol : listeners) {
				ol.messageSent(this, msg);
			}
		}

		return b;
	}

	public boolean disconnect() {
		if (chatId == null) { return false; }

		final String oldChatId = chatId;
		chatId = null;
		final String d = CommunicationHelper.wget(disc_url, true, "id", oldChatId);
		final boolean b = d != null && d.equals("win");
		if (b) {
			dead = true;
		} else {
			LogHelper.log(Omegle.class, LogLevel.WARN, "Disconnection Error - Rolling Back...");
			chatId = oldChatId;
		}
		return b;
	}

	@Override
	public void finalize() {
		if (chatId != null) {
			disconnect();
		}
	}

	public static String unJsonify(final String jsonString) {
		final Matcher m = escape_regex.matcher(jsonString);
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
				LogHelper.log(Omegle.class, LogLevel.WARN, "[" + new Date() + "]:");
				LogHelper.log(Omegle.class, LogLevel.WARN, "sb = " + sb.toString());
				LogHelper.log(Omegle.class, LogLevel.WARN, "e = " + e);
				LogHelper.log(Omegle.class, LogLevel.WARN, "c = " + c);
				LogHelper.log(Omegle.class, LogLevel.WARN, "escaped = " + escaped);
				LogHelper.log(Omegle.class, LogLevel.WARN, "m.group(0) = " + m.group(0), ex);
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public void init() {
		LogHelper.log(Omegle.class, LogLevel.INFO, "-- Initializing, Please wait...");
		final int randIndex = (int) (Math.random() * omegleServerList.length);
		final String omegle_root_tmp = omegleServerList[randIndex];
		LogHelper.log(Omegle.class, LogLevel.INFO, "* Chat server selected: " + omegle_root_tmp);
		omegleServer = "http://" + omegle_root_tmp + "/";
		try {
			init_1 = new URL("http://www.omegle.com");
			init_2 = new URL("http://www.omegle.com/static/frameset.js?1");
			init_3 = new URL(omegleServer + "static/style.css?25");
			init_4 = new URL(omegleServer + "static/omegle.js?83");
		} catch (final Exception e) {
			e.printStackTrace();
		}
		setOmegleRoot();
		CommunicationHelper.wget(init_1, false, true);
		CommunicationHelper.wget(init_2, false, true);
		CommunicationHelper.wget(init_3, false, true);
		CommunicationHelper.wget(init_4, false, true);
		LogHelper.log(Omegle.class, LogLevel.INFO, "-- Initialization process has been completed.");
	}

	/**
	 * @return the chatId
	 */
	public String getChatId() {
		return chatId;
	}

	/**
	 * @param chatId
	 *            the chatId to set
	 */
	public void setChatId(final String chatId) {
		this.chatId = chatId;
	}

	/**
	 * @return the recaptcha_url
	 */
	public URL getRecaptcha_url() {
		return recaptcha_url;
	}

	/**
	 * @param recaptcha_url
	 *            the recaptcha_url to set
	 */
	public void setRecaptcha_url(final URL recaptcha_url) {
		this.recaptcha_url = recaptcha_url;
	}
}
