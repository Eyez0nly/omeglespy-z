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
package org.darkimport.omeglespy_z.mediation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.darkimport.omeglespy_z.DefaultConversantNameGenerator;
import org.darkimport.omeglespy_z.DefaultServerNameGenerator;
import org.darkimport.omeglespy_z.LogHelper;
import org.darkimport.omeglespy_z.LogLevel;
import org.darkimport.omeglespy_z.NameGenerator;

/**
 * The entry point into the omeglespy-z application framework for connection
 * mediation.
 * 
 * Implementing applications will use an instance of this class to interact with
 * the framework.
 * 
 * @author user
 * @version $Id: $
 */
public class OmegleSpyConversationController {
	private static final String							TARGET_STRANGER_NAME	= "targetStrangerName";
	private static final String							FROM_STRANGER_NAME		= "fromStrangerName";
	private static final String							IS_BLOCKED				= "isBlocked";
	private static final String							IS_FILTERED				= "isFiltered";
	private static final String							MESSAGE					= "message";
	private static final String							RECAPTCHA_CHALLENGE		= "recaptchaChallenge";
	private static final String							RECAPTCHA_RESPONSE		= "recaptchaResponse";
	private static final int							NUMBER_OF_STRANGERS		= 2;
	private static final boolean						UNIQUE_CONVERSANT_NAMES	= true;
	private static final boolean						UNIQUE_SERVER_NAMES		= true;

	private final List<OmegleSpyConversationListener>	activeListeners;
	private final NameGenerator							conversantNameGenerator;
	private final NameGenerator							serverNameGenerator;

	private OmegleSpyConversationCoordinator			conversationCoordinator;
	private boolean										conversationEnded		= true;

	/**
	 * Contains a map of completed work requests. The state of the value
	 * indicates success/failure.
	 * 
	 * A null indicates that the work expired. Work can expire if the
	 * conversation is ended before the work can be completed.
	 */
	private final Map<Long, Boolean>					completedTasks			= new Hashtable<Long, Boolean>();
	private final Map<Long, Throwable>					erroredTasks			= new Hashtable<Long, Throwable>();
	private long										lastWorkId				= 0;

	private final Map<Long, WorkEvent>					workQueue				= new Hashtable<Long, WorkEvent>();

	/**
	 * <p>
	 * Constructor for OmegleSpyConversationController.
	 * </p>
	 * 
	 * @param activeListeners
	 *            a {@link java.util.List} object.
	 * @param conversantNameGenerator
	 *            a {@link org.darkimport.omeglespy_z.NameGenerator} object.
	 * @param serverNameGenerator
	 *            a {@link org.darkimport.omeglespy_z.NameGenerator} object.
	 */
	public OmegleSpyConversationController(final List<OmegleSpyConversationListener> activeListeners,
			final NameGenerator conversantNameGenerator, final NameGenerator serverNameGenerator) {
		this.activeListeners = activeListeners;
		this.conversantNameGenerator = conversantNameGenerator;
		this.serverNameGenerator = serverNameGenerator;
	}

	/**
	 * <p>
	 * Constructor for OmegleSpyConversationController.
	 * </p>
	 * 
	 * @param activeListeners
	 *            a {@link java.util.List} object.
	 */
	public OmegleSpyConversationController(final List<OmegleSpyConversationListener> activeListeners) {
		this(activeListeners, new DefaultConversantNameGenerator(), new DefaultServerNameGenerator());
	}

	/**
	 * Start a conversation. The unique names used to identify the conversants
	 * are returned.
	 * 
	 * @return an array containing the names of the conversants.
	 */
	public String[] startConversation() {
		if (conversationCoordinator != null) {
			LogHelper.log(OmegleSpyConversationController.class, LogLevel.WARN,
					"A previous conversation was not properly ended. Attempting to end it now.");
			try {
				conversationCoordinator.endConversation();
				LogHelper.log(OmegleSpyConversationController.class, LogLevel.INFO,
						"Successfully forced the ending of the previous conversation.");
			} catch (final Exception e) {
				LogHelper
						.log(OmegleSpyConversationController.class,
								LogLevel.WARN,
								"Unable to end the last conversation. Discarding the old reference. NOTE: If you see this message multiple times, you may begin to notice unexpected behavior.",
								e);
			}
			conversationCoordinator = null;
		}
		final String[] conversantNames = conversantNameGenerator.next(NUMBER_OF_STRANGERS, UNIQUE_CONVERSANT_NAMES);

		// The conversation coordinator begins establishing the required
		// connections (concurrently).
		conversationCoordinator = new OmegleSpyConversationCoordinator(activeListeners, conversantNames,
				serverNameGenerator.next(NUMBER_OF_STRANGERS, UNIQUE_SERVER_NAMES));

		new Thread(new WorkerThread()).start();

		return conversantNames;
	}

	/**
	 * Disconnects the named stranger.
	 * 
	 * @param strangerName
	 *            a {@link java.lang.String} object.
	 */
	public void disconnectStranger(final String strangerName) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(TARGET_STRANGER_NAME, strangerName);
		doWork(WorkEvent._disconnectStranger, params);
	}

	/**
	 * Swaps the named stranger.
	 * 
	 * @param strangerName
	 *            a {@link java.lang.String} object.
	 */
	public void swapStranger(final String strangerName) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(TARGET_STRANGER_NAME, strangerName);
		doWork(WorkEvent._swapStranger, params);
	}

	/**
	 * Sends a message as the stranger specified in fromName to the stranger
	 * specified in targetName.
	 * 
	 * @param targetName
	 *            a {@link java.lang.String} object.
	 * @param fromName
	 *            a {@link java.lang.String} object.
	 * @param message
	 *            a {@link java.lang.String} object.
	 */
	public void sendSecretMessage(final String targetName, final String fromName, final String message) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(TARGET_STRANGER_NAME, targetName);
		params.put(FROM_STRANGER_NAME, fromName);
		params.put(MESSAGE, message);
		doWork(WorkEvent._sendSecretMessage, params);
	}

	/**
	 * Sends a recaptcha response for the conversation associate with the target
	 * stranger.
	 * 
	 * @param targetName
	 *            a {@link java.lang.String} object.
	 * @param challenge
	 *            a {@link java.lang.String} object.
	 * @param response
	 *            a {@link java.lang.String} object.
	 */
	public void sendRecaptchaResponse(final String targetName, final String challenge, final String response) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(TARGET_STRANGER_NAME, targetName);
		params.put(RECAPTCHA_CHALLENGE, challenge);
		params.put(RECAPTCHA_RESPONSE, response);
		doWork(WorkEvent._sendRecaptchaResponse, params);
	}

	/**
	 * Sets whether or not the messages sent by one stranger are passed on to
	 * the other stranger.
	 * 
	 * @param selected
	 *            a boolean.
	 */
	public void setStrangersBlock(final boolean selected) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(IS_BLOCKED, selected);
		doWork(WorkEvent._toggleStrangersBlock, params);
	}

	/**
	 * Sets whether or not filtering should be applied to messages sent by
	 * strangers.
	 * 
	 * @param selected
	 *            a boolean.
	 */
	public void setFilter(final boolean selected) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(IS_FILTERED, selected);
		doWork(WorkEvent._toggleFilter, params);
	}

	/**
	 * Ends the current conversation. All underlying connections are
	 * disconnected where appropriate.
	 */
	public void endConversation() {
		doWork(WorkEvent._endConversation, null);
	}

	/**
	 * Tells whether or not a conversation is in progress.
	 * 
	 * @return a boolean. True if the conversation is not in progress.
	 */
	public boolean isConversationEnded() {
		return conversationEnded;
	}

	private void doWork(final WorkEvent workEvent, final Map<String, Object> params) {
		if (conversationEnded) { throw new IllegalStateException("The conversation is not started."); }

		if (params != null && params.size() > 0) {
			workEvent.params = params;
		}
		final long id = addWorkRequest(workEvent);
		waitForWorkCompletion(id);
	}

	private void waitForWorkCompletion(final long id) {
		while (!completedTasks.containsKey(id)) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				LogHelper.log(OmegleSpyConversationController.class, LogLevel.WARN, "Thread error.", e);
			}
		}

		final Boolean workState = completedTasks.remove(id);
		if (workState == false) {
			LogHelper.log(OmegleSpyConversationController.class, LogLevel.WARN,
					"An error occurred while completing the work, " + id);
			throw new RuntimeException(erroredTasks.remove(id));
		}
	}

	private long addWorkRequest(final WorkEvent workEvent) {
		long workId = -1;
		synchronized (this) {
			workId = lastWorkId++;
		}
		if (workId == -1) { throw new RuntimeException(new InterruptedException("Unable to obtain a valid work ID.")); }
		workQueue.put(workId, workEvent);

		return workId;
	}

	private enum WorkEvent {
		_disconnectStranger, _swapStranger, _toggleStrangersBlock, _toggleFilter, _endConversation, _sendSecretMessage, _sendRecaptchaResponse;
		private Map<String, Object>	params;
	}

	protected class WorkerThread implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		/**
		 * This thread will do the initial instantiation of the strangers, their
		 * connection, and their association.
		 * 
		 * It then monitors an event stack to handle its other duties:
		 * 
		 * - Disconnecting a stranger
		 * 
		 * - Swapping a stranger
		 * 
		 * - Ending a conversation
		 */
		public void run() {
			if (conversationEnded) {
				conversationEnded = false;
				while (!conversationEnded) {
					try {
						Thread.sleep(100);
					} catch (final InterruptedException e) {
						LogHelper.log(OmegleSpyConversationController.class, LogLevel.WARN, "Thread error.", e);
					}
					if (!workQueue.isEmpty()) {
						final Map<Long, WorkEvent> tempWorkQueue = new HashMap<Long, WorkEvent>(workQueue);
						final Set<Long> keys = tempWorkQueue.keySet();
						final Map<Long, Boolean> workCompleted = new HashMap<Long, Boolean>();
						final Map<Long, Throwable> detectedErrors = new HashMap<Long, Throwable>();
						for (final long key : keys) {
							final WorkEvent workEvent = tempWorkQueue.get(key);
							final Map<String, Object> params = workEvent.params;
							try {
								final Method workMethod = getClass().getDeclaredMethod(workEvent.name(), Map.class);
								workMethod.invoke(this, params);
								workCompleted.put(key, true);
							} catch (final Exception e) {
								if (LogHelper.isLogLevelEnabled(LogLevel.DEBUG, OmegleSpyConversationController.class)) {
									LogHelper.log(OmegleSpyConversationController.class, LogLevel.DEBUG,
											"An error occurred while invoking " + workEvent + " with params " + params
													+ " for workId " + key + ".", e);
								}
								workCompleted.put(key, false);
								detectedErrors.put(key, e);
							}

							// If we disconnected the conversation, we're not
							// doing
							// anymore work.
							if (conversationEnded) {
								break;
							}
						}

						completedTasks.putAll(workCompleted);
						erroredTasks.putAll(detectedErrors);
						final Set<Long> finishedWorkSet = workCompleted.keySet();
						for (final long id : finishedWorkSet) {
							workQueue.remove(id);
						}
					}
				}
				// We're done. Dump all pending work.
				if (!workQueue.isEmpty()) {
					final Set<Long> keys = workQueue.keySet();
					for (final long key : keys) {
						completedTasks.put(key, null);
					}
					workQueue.clear();
				}
			} else {
				LogHelper.log(OmegleSpyConversationController.class, LogLevel.WARN,
						"The controller reports that the conversation is already started.");
			}
		}

		protected void _disconnectStranger(final Map<String, Object> params) {
			final String strangerName = (String) params.get(TARGET_STRANGER_NAME);
			conversationCoordinator.disconnectConversant(strangerName);
		}

		protected void _swapStranger(final Map<String, Object> params) {
			final String strangerName = (String) params.get(TARGET_STRANGER_NAME);
			conversationCoordinator.swapConversant(strangerName, serverNameGenerator.next(1, true)[0]);
		}

		protected void _toggleStrangersBlock(final Map<String, Object> params) {
			final boolean blocked = (Boolean) params.get(IS_BLOCKED);
			conversationCoordinator.setConversantsBlocked(blocked);
		}

		protected void _toggleFilter(final Map<String, Object> params) {
			final boolean filtered = (Boolean) params.get(IS_FILTERED);
			conversationCoordinator.setConversationFiltered(filtered);
		}

		protected void _sendSecretMessage(final Map<String, Object> params) {
			final String targetName = (String) params.get(TARGET_STRANGER_NAME);
			final String fromName = (String) params.get(FROM_STRANGER_NAME);
			final String message = (String) params.get(MESSAGE);
			conversationCoordinator.sendExternalMessage(targetName, fromName, message);
		}

		protected void _sendRecaptchaResponse(final Map<String, Object> params) {
			final String targetName = (String) params.get(TARGET_STRANGER_NAME);
			final String challenge = (String) params.get(RECAPTCHA_CHALLENGE);
			final String response = (String) params.get(RECAPTCHA_RESPONSE);
			conversationCoordinator.sendRecaptchaResponse(targetName, challenge, response);
		}

		protected void _endConversation(final Map<String, Object> params) {
			try {
				conversationCoordinator.endConversation();
			} catch (final Exception e) {
				LogHelper.log(OmegleSpyConversationController.class, LogLevel.WARN,
						"An error occurred and the conversation may not have been disconnected successfully", e);
			}
			conversationEnded = true;
		}

	}
}
