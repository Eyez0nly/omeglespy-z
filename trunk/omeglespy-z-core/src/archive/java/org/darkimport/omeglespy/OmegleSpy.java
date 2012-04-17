package org.darkimport.omeglespy;

import java.util.LinkedList;
import java.util.List;

import org.darkimport.omeglespy_z.DefaultFilterHelper;
import org.darkimport.omeglespy_z.LogHelper;
import org.darkimport.omeglespy_z.LogLevel;

@Deprecated
public class OmegleSpy implements OmegleListener {
	Omegle					chat, partner;
	String					name;

	// TODO Why do we need both a connected and a disconnected field???
	private boolean			connected;
	private boolean			disconnected;

	// TODO both blocking and filtering enabled should be application scope --
	// not session scope.
	private boolean			blocking;
	private boolean			filteringEnabled;
	List<OmegleSpyListener>	listeners;

	public OmegleSpy(final String name) {
		chat = new Omegle();
		chat.init();
		chat.addOmegleListener(this);

		this.name = name;

		partner = null;

		connected = false;
		blocking = false;
		disconnected = false;

		listeners = new LinkedList<OmegleSpyListener>();
	}

	public void setPartner(final Omegle o) {
		partner = o;
	}

	public String getID(final boolean mine) {
		if (chat.getChatId() != null || !disconnected) {
			if (mine) {
				return chat.getChatId();
			} else {
				return partner.getChatId();
			}
		} else {
			return "0";
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	public void setBlocking(final boolean b) {
		blocking = b;
		if (blocking) {
			partner.stoppedTyping();
		}
	}

	public void setFiltering(final boolean b) {
		filteringEnabled = b;
	}

	public boolean isBlocking() {
		return blocking;
	}

	public void addOmegleSpyListener(final OmegleSpyListener osl) {
		listeners.add(osl);
	}

	public void removeOmegleSpyListener(final OmegleSpyListener osl) {
		listeners.remove(osl);
	}

	private boolean sendMsg(final String msg) {
		return partner.sendMsg(msg);
	}

	public boolean startChat() {
		if (partner == null) { return false; }
		if (!chat.start()) { return false; }

		while (!connected) {
			try {
				Thread.sleep(50);
			} catch (final InterruptedException e) {}
		}

		for (final OmegleSpyListener omegleSpyListener : listeners) {
			omegleSpyListener.chatStarted(this);
		}
		return true;
	}

	public boolean sendExternalMessage(final String msg) {
		final boolean b = chat.sendMsg(msg);
		if (b) {
			for (final OmegleSpyListener osl : listeners) {
				osl.externalMessageSent(this, msg);
			}
		}

		return b;
	}

	// TODO Figure out if this triggers an event.
	public boolean disconnect() {
		final boolean b = chat.disconnect();
		if (b) {
			disconnected = true;
		}
		return b;
	}

	private void gotMessage(final String msg) {
		// Is filtering enabled? If so, does this message violate the filter?
		if (filteringEnabled && DefaultFilterHelper.isBadMessage(msg)) {
			for (final OmegleSpyListener osl : listeners) {
				osl.messageFiltered(this, msg);
			}
		} else if (!blocking) {
			sendMsg(msg);
			for (final OmegleSpyListener osl : listeners) {
				osl.messageTransferred(this, msg);
			}
		} else {
			for (final OmegleSpyListener osl : listeners) {
				osl.messageBlocked(this, msg);
			}
		}
	}

	// TODO The omegle spy should not implement the OmegleListener
	public void eventFired(final Omegle src, final String event, final String... args) {
		if (LogHelper.isLogLevelEnabled(LogLevel.DEBUG, OmegleSpy.class)) {
			String argslist = "";
			final String argstake[] = args;
			if (args.length > 0) {
				argslist = "ARGS";
				for (final String s : argstake) {
					argslist += "[" + s + "]";
				}
			}
			LogHelper.log(OmegleSpy.class, LogLevel.DEBUG, "* eventFire Request EVENT[" + event + "] " + argslist);
		}
		if (event.equals(Omegle.EV_CONNECTED)) {
			connected = true;
		} else if (event.equals(Omegle.EV_TYPING)) {
			if (!blocking) {
				partner.typing();
			}
			for (final OmegleSpyListener osl : listeners) {
				osl.isTyping(this);
			}
		} else if (event.equals(Omegle.EV_STOPPED_TYPING)) {
			partner.stoppedTyping();
			for (final OmegleSpyListener osl : listeners) {
				osl.stoppedTyping(this);
			}
		} else if (event.equals(Omegle.EV_MSG)) {
			gotMessage(args[0]);
		} else if (event.equals(Omegle.EV_RECAPTCHA)) {
			for (final OmegleSpyListener omegleSpyListener : listeners) {
				omegleSpyListener.recaptcha(this, args[0]);
			}
		} else if (event.equals(Omegle.EV_RECAPTCHAREJECT)) {
			for (final OmegleSpyListener omegleSpyListener : listeners) {
				omegleSpyListener.recaptchaRejected(this, args[0]);
			}
		} else if (event.equals(Omegle.EV_DISCONNECT)) {
			disconnected = true;
			for (final OmegleSpyListener omegleSpyListener : listeners) {
				omegleSpyListener.disconnected(this);
			}
		}
	}

	public void messageSent(final Omegle src, final String msg) {}

	public Omegle getChat() {
		return chat;
	}

	public boolean isDisconnected() {
		return disconnected;
	}
}
