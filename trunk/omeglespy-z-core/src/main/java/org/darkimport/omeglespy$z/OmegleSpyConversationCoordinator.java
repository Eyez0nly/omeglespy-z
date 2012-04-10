/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.darkimport.omeglespy.OmegleSpy;
import org.darkimport.omeglespy.log.LogHelper;
import org.darkimport.omeglespy.log.LogLevel;
import org.darkimport.omeglespy.util.FilterHelper;

/**
 * @author user
 * 
 */
class OmegleSpyConversationCoordinator implements Observer {
	private final List<OmegleSpyConversationListener>	activeListeners;

	/**
	 * A {@link Map} of the connections keyed by the name of the conversant.
	 */
	private final Map<String, OmegleConnection>			connections;

	private boolean										conversantsBlocked;

	private boolean										conversationFiltered;

	/**
	 * Construct a coordinator.
	 * 
	 * @param activeListeners
	 *            A list of {@link OmegleSpyConversationListener}s. This list
	 *            may be null. Additionally, listeners may be added later.
	 * @param conversantNames
	 *            A list of conversants. Each of the names must be unique or
	 *            unexpected results may occur. We do not check for uniqueness.
	 *            TODO We should, though.
	 * @param serverNames
	 *            A list of host names or IP addresses to which to connect. This
	 *            list does not have to be equal in size with the
	 *            conversantNames list, but it must have at least one element.
	 *            If the list sizes are not equal, we associate each name with
	 *            the corresponding element in the server list until we get to
	 *            the last element. At that point, we associate each subsequent
	 *            name with the last element of the servername list.
	 */
	public OmegleSpyConversationCoordinator(final List<OmegleSpyConversationListener> activeListeners,
			final String[] conversantNames, final String[] serverNames) {
		if (serverNames == null || serverNames.length == 0) { throw new IllegalArgumentException(
				"The serverNames list must contain at least one element."); }
		String[] serverNamesToUse;
		if (serverNames.length == conversantNames.length) {
			serverNamesToUse = serverNames;
		} else if (serverNames.length < conversantNames.length) {
			serverNamesToUse = new String[conversantNames.length];
			for (int i = 0; i < serverNames.length; i++) {
				serverNamesToUse[i] = serverNames[i];
			}
			for (int i = serverNames.length; i < serverNamesToUse.length; i++) {
				serverNamesToUse[i] = serverNames[serverNames.length - 1];
			}
		}
		if (activeListeners != null) {
			this.activeListeners = activeListeners;
		} else {
			this.activeListeners = new ArrayList<OmegleSpyConversationListener>();
		}
		connections = new HashMap<String, OmegleConnection>();
		for (int i = 0; i < conversantNames.length; i++) {
			final OmegleConnection connection = new OmegleConnection(conversantNames[i], serverNames[i]);
			connections.put(conversantNames[i], connection);
			connection.addObserver(this);

			// Connection established here.
			new Thread(connection).start();
		}
	}

	/**
	 * Called when a connection generates an {@link OmegleEvent} (not to be
	 * confused with an {@link OmegleSpyEvent}). The event is then translated
	 * into an {@link OmegleSpyEvent}, passed on to the other connection, or
	 * both.
	 * 
	 * @param o
	 *            The {@link OmegleConnection} that generated an event
	 * @param arg
	 *            The OmegleEvent that was generated
	 */
	public void update(final Observable o, final Object arg) {
		final OmegleConnection connection = (OmegleConnection) o;
		final OmegleEvent omegleEvent = (OmegleEvent) arg;
		final OmegleSpyEvent evt = new OmegleSpyEvent(connection);
		if (LogHelper.isLogLevelEnabled(LogLevel.DEBUG, OmegleSpy.class)) {
			String argslist = "";
			final String argstake[] = omegleEvent.getArgs();
			if (argstake.length > 0) {
				argslist = "ARGS";
				for (final String s : argstake) {
					argslist += "[" + s + "]";
				}
			}
			LogHelper.log(OmegleSpyConversationCoordinator.class, LogLevel.DEBUG, "* eventFire Request EVENT["
					+ omegleEvent.getSource() + "] " + argslist);
		}
		switch (omegleEvent.getSource()) {
			case connecting:
				break;
			case waiting:
				break;
			case connected:
				break;
			case typing:
				// The notifying conversant is typing.

				// First, if the conversants aren't blocked, send typing
				// notification to the other conversants.
				if (!conversantsBlocked) {
					final List<OmegleConnection> otherConnections = getOtherConnections(connection, connections);

					for (final OmegleConnection otherConnection : otherConnections) {
						otherConnection.sendTypingNotification();
					}
				}

				// Then, send isTyping notification to the
				// OmegleSpyConversationListeners.
				for (final OmegleSpyConversationListener conversationListener : activeListeners) {
					conversationListener.isTyping(evt);
				}
				break;
			case gotMessage:
				// The notifying conversant sent a message.
				final String message = omegleEvent.getArgs()[0];

				// Is filtering enabled? If so, does this message violate the
				// filter?
				if (conversationFiltered && FilterHelper.isBadMessage(message)) {
					for (final OmegleSpyConversationListener conversationListener : activeListeners) {
						conversationListener.messageFiltered(evt, message);
					}
				} else if (!conversantsBlocked) {
					// First, if there's not filter violation and if we're not
					// blocking, send the message to the other conversants.
					final List<OmegleConnection> otherConnections = getOtherConnections(connection, connections);
					for (final OmegleConnection otherConnection : otherConnections) {
						otherConnection.sendMesssage(message);
					}

					// Then, notify the OmegleSpyListeners.
					for (final OmegleSpyConversationListener conversationListener : activeListeners) {
						conversationListener.messageTransferred(evt, message);
					}
				} else {
					for (final OmegleSpyConversationListener conversationListener : activeListeners) {
						conversationListener.messageBlocked(evt, message);
					}
				}
				break;
			case stoppedTyping:
				// The notifying conversant has stopped typing.

				// First, send stopped-typing
				// notification to the other conversants.
				final List<OmegleConnection> otherConnections = getOtherConnections(connection, connections);
				for (final OmegleConnection otherConnection : otherConnections) {
					otherConnection.sendStoppedTypingNotification();
				}
				for (final OmegleSpyConversationListener conversationListener : activeListeners) {
					conversationListener.stoppedTyping(evt);
				}
				break;
			case strangerDisconnected:
				// The notifying conversant has disconnected. Ensure that the
				// connection is closed and then notify the
				// OmegleSpyListeners.
				connection.disconnect();

				for (final OmegleSpyConversationListener conversationListener : activeListeners) {
					conversationListener.disconnected(evt);
				}
				break;
			case recaptchaRequired:
				// The notifying conversant challenged us with a recaptcha.
				// Notify the OmegleSpyListeners. Once we get back the challenge
				// response, all should be well.
				// TODO We should pause the event pinging queue at this point
				// until we get back a response.

				for (final OmegleSpyConversationListener conversationListener : activeListeners) {
					final String id = omegleEvent.getArgs()[0];
					conversationListener.recaptcha(evt, id);
				}
				break;
			case recaptchaRejected:
				// The notifying conversant challenged us with a recaptcha after
				// we failed to correctly answer the previous challenge. Notify
				// the OmegleSpyListeners. Once we get back the challenge
				// response, all should be well.
				// TODO We should pause the event pinging queue at this point
				// until we get back a response.

				for (final OmegleSpyConversationListener conversationListener : activeListeners) {
					final String id = omegleEvent.getArgs()[0];
					conversationListener.recaptchaRejected(evt, id);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * @param connection
	 * @return
	 */
	private static List<OmegleConnection> getOtherConnections(final OmegleConnection connection,
			final Map<String, OmegleConnection> connections) {
		final String actorName = connection.getConversantName();
		final List<OmegleConnection> otherConnections = new ArrayList<OmegleConnection>();
		final Set<String> names = connections.keySet();
		for (final String conversantName : names) {
			if (!actorName.equals(conversantName)) {
				otherConnections.add(connections.get(conversantName));
			}
		}
		return otherConnections;
	}

	/**
	 * Attempts to end the conversation.
	 * 
	 * @param force
	 *            true if we should ignore Exceptions. False if we should stop
	 *            on an Exception.
	 */
	public void endConversation(final boolean force) {
		for (final OmegleConnection connection : connections.values()) {
			try {
				connection.disconnect();
			} catch (final Exception e) {
				LogHelper.log(OmegleSpyConversationCoordinator.class, LogLevel.WARN,
						"An exception occurred while disconnecting from " + connection.getConversantName() + ".", e);
				if (!force) { throw new RuntimeException(e); }
			}
		}
	}

	/**
	 * Attempt to end the conversation. Stops on an Exception.
	 */
	public void endConversation() {
		endConversation(false);
	}

	public void setConversationFiltered(final boolean filtered) {
		conversationFiltered = filtered;
	}

	public void setConversantsBlocked(final boolean blocked) {
		conversantsBlocked = blocked;
	}

	public void swapConversant(final String strangerName, final String serverName) {
		OmegleConnection connection = connections.remove(strangerName);
		connection.disconnect();
		connection = new OmegleConnection(strangerName, serverName);
		connections.put(strangerName, connection);
		connection.addObserver(this);

		// Connection established here.
		new Thread(connection).start();
	}

	public void disconnectConversant(final String strangerName) {
		connections.get(strangerName).disconnect();
	}

	/**
	 * TODO Use the fromName in some way or remove it.
	 * 
	 * @param targetName
	 * @param fromName
	 * @param message
	 */
	public void sendExternalMessage(final String targetName, final String fromName, final String message) {
		final OmegleConnection connection = connections.get(targetName);
		connection.sendMesssage(message);
		for (final OmegleSpyConversationListener conversationListener : activeListeners) {
			conversationListener.externalMessageSent(new OmegleSpyEvent(connection), message);
		}
	}

	public void sendRecaptchaResponse(final String targetName, final String challenge, final String response) {
		final OmegleConnection connection = connections.get(targetName);
		connection.sendRecaptchaResponse(challenge, response);
	}
}
