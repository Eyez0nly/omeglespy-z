package org.darkimport.omeglespy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Omegle implements Runnable {
	private static final Log			log				= LogFactory.getLog(Omegle.class);

	public static final Pattern			str_regex		= Pattern.compile("(\")((?>(?:(?>[^\"\\\\]+)|\\\\.)*))\\1");
	public static final Pattern			escape_regex	= Pattern.compile("\\\\([\'\"\\\\bfnrt]|u(....))");
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
		} catch (final MalformedURLException ex) {
		}
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
		if (chatId != null || dead) {
			return false;
		}

		final String startr = wget(start_url, true);
		if (startr == null) {
			return false;
		}
		final Matcher m = str_regex.matcher(startr);
		if (m.matches()) {
			chatId = m.group(2); // 2 is the actual string - Listner(sky) has
			// started his chat session here is the ID
			log.debug("Chat Started - chatId[" + chatId + "]");
		} else {
			return false;
		}
		new Thread(this).start();
		return true;
	}

	public void run() {
		String eventr;
		while (chatId != null && (eventr = wget(events_url, true, "id", chatId)) != null &&
		/* chatId != null && /* hahahaha i have to check it twice */
		!eventr.equals("null")) {
			dispatch(eventr);
		}
		if (chatId != null) {
			log.warn("** Fuck... An event check returned a null - This is a session destroyer. **");
			log.warn("** Restart OmegleSpyX and hope Omegle didnt ban your ass. (symptoms of a ban). **");
			chatId = null;
		}
	}

	public void dispatch(final String eventr) {
		log.debug("Dispatch - eventr[" + eventr + "]");

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
		if (chatId == null) {
			return false;
		}

		final String r = wget(type_url, true, "id", chatId);
		return r != null && r.equals("win");
	}

	public boolean stoppedTyping() {
		if (chatId == null) {
			return false;
		}

		final String r = wget(stoptype_url, true, "id", chatId);
		return r != null && r.equals("win");
	}

	public boolean sendMsg(final String msg) {
		if (chatId == null) {
			return false;
		}

		// omegleListeners.messageInTheProcessOfSending()
		final String sendr = wget(send_url, true, "id", chatId, "msg", msg);
		if (sendr == null) {
			return false;
		}

		final boolean b = sendr.equals("win");
		if (b) {
			for (final OmegleListener ol : listeners) {
				ol.messageSent(this, msg);
			}
		}

		return b;
	}

	public boolean disconnect() {
		if (chatId == null) {
			return false;
		}

		final String oldChatId = chatId;
		chatId = null;
		final String d = wget(disc_url, true, "id", oldChatId);
		final boolean b = d != null && d.equals("win");
		if (b) {
			dead = true;
		} else {
			log.warn("Disconnection Error - Rolling Back...");
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
				log.warn("[" + new Date() + "]:");
				log.warn("sb = " + sb.toString());
				log.warn("e = " + e);
				log.warn("c = " + c);
				log.warn("escaped = " + escaped);
				log.warn("m.group(0) = " + m.group(0), ex);
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public void init() {
		log.info(" -- Initializing, Please wait...");
		final int randIndex = (int) (Math.random() * Common.OMEGLE_SERVER_LIST.length);
		final String omegle_root_tmp = Common.OMEGLE_SERVER_LIST[randIndex];
		log.info("* Chat server selected: " + omegle_root_tmp);
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
		wget(init_1, false, true);
		wget(init_2, false, true);
		wget(init_3, false, true);
		wget(init_4, false, true);
		log.info(" -- Initialization  process has been completed.");
	}

	public static String wget(final URL url, final boolean post, final String... post_data) {
		return wget(url, post, false, post_data);
	}

	public static String wget(final URL url, final boolean post, final boolean ignore, final String... post_data) {
		String msg = "";
		InputStream in = null;
		OutputStream out = null;
		String data = null;
		try {
			final URLConnection urlcon = url.openConnection();

			if (post) {
				// String msg = "";
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
			if (log.isDebugEnabled()) {
				log.debug("WGET= URL[" + url.toString() + "?" + msg + "] RETURN[" + data + "]");
			}

			return data;
		} catch (final Exception ex) {
			log.warn("An error occurred while submitting " + msg + " request to " + url.toString()
					+ "with the following data: " + data, ex);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
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

interface OmegleListener {
	public void eventFired(Omegle src, String event, String... args);

	public void messageSent(Omegle src, String msg);
}
