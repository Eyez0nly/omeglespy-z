/**
 * 
 */
package org.darkimport.omeglespy;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.configuration.ConfigHelper;
import org.darkimport.omeglespy.constants.ConfigConstants;

/**
 * @author user
 * 
 */
public class SpyController {
	private static final Log			log				= LogFactory.getLog(SpyController.class);

	private static final String			STRANGER_INDEX	= "strangerIndex";
	private static final String			SPY_LISTENER	= "spyListener";
	private static final String			IS_BLOCKED		= "isBlocked";
	private static final String			IS_FILTERED		= "isFiltered";

	private static final String			MESSAGE			= "message";

	public static String[]				possibleNames;

	private final Map<Long, WorkEvent>	workQueue		= new Hashtable<Long, WorkEvent>();

	/**
	 * Contains a map of completed work requests. The state of the value
	 * indicates success/failure.
	 * 
	 * A null indicates that the work expired. Work can expire if the
	 * conversation is ended before the work can be completed.
	 */
	private final Map<Long, Boolean>	completedTasks	= new Hashtable<Long, Boolean>();
	private final Map<Long, Throwable>	erroredTasks	= new Hashtable<Long, Throwable>();
	private long						lastWorkId		= 0;

	private final OmegleSpy[]			spies			= new OmegleSpy[2];
	private final String[]				names			= new String[] { "Bret", "Jane" };

	/**
	 * Tells us if the conversation is active. Also tells us if this
	 * spycontroller is accepting any new work requests.
	 */
	private boolean						conversationEnded;

	static {
		InputStream is = null;
		try {
			final String namesfile = ConfigHelper.getGroup(ConfigConstants.GROUP_MAIN).getProperty(
					ConfigConstants.MAIN_NAMESFILE);
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(namesfile);
			final List<String> namesList = IOUtils.readLines(is, null);

			possibleNames = namesList.toArray(new String[namesList.size()]);
		} catch (final Exception ex) {
			possibleNames = new String[] { "Stranger 1", "Stranger 2" };
			log.warn("Could not load names file: " + ex.getMessage());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	public void startConversation(final List<OmegleSpyListener> initialListeners) {
		new Thread(new WorkerThread(initialListeners)).start();
	}

	public void disconnectStranger(final int strangerIndex) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(STRANGER_INDEX, strangerIndex);
		doWork(WorkEvent._disconnectStranger, params);
	}

	public void swapStranger(final int strangerIndex, final OmegleSpyListener spyListener) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(STRANGER_INDEX, strangerIndex);
		params.put(SPY_LISTENER, spyListener);
		doWork(WorkEvent._swapStranger, params);
	}

	public void sendSecretMessage(final int targetIndex, final String message) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(STRANGER_INDEX, targetIndex);
		params.put(MESSAGE, message);
		doWork(WorkEvent._sendSecretMessage, params);
	}

	public void toggleStrangersBlock(final boolean selected) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(IS_BLOCKED, selected);
		doWork(WorkEvent._toggleStrangersBlock, params);
	}

	public void toggleFilter(final boolean selected) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(IS_FILTERED, selected);
		doWork(WorkEvent._toggleFilter, params);
	}

	public void endConversation() {
		doWork(WorkEvent._endConversation, null);
	}

	private void doWork(final WorkEvent workEvent, final Map<String, Object> params) {
		if (params != null && params.size() > 0) {
			workEvent.params = params;
		}
		final long id = addWorkRequest(workEvent);
		waitForWorkCompletion(id);
	}

	private void initializeStrangers(final List<OmegleSpyListener> listeners) {
		randomizeNames();
		for (int i = 0; i < spies.length; i++) {
			final String n = names[i];
			spies[i] = new OmegleSpy(n);
			spies[i].addOmegleSpyListener(listeners.get(i));
		}

		spies[0].setPartner(spies[1].getChat());
		spies[1].setPartner(spies[0].getChat());

		for (int i = 0; i < spies.length; i++) {
			log.info("SPY CREATED : spy[" + i + "]	[ID: " + spies[i].getChat() + "]		[NAME: '" + names[i] + "']");
			spies[i].startChat();
		}
	}

	private void waitForWorkCompletion(final long id) {
		while (!completedTasks.containsKey(id)) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				log.warn("Thread error.", e);
			}
		}

		final Boolean workState = completedTasks.remove(id);
		if (workState == false) {
			log.warn("An error occurred while completing the work, " + id);
			throw new RuntimeException(erroredTasks.remove(id));
		}
	}

	private long addWorkRequest(final WorkEvent workEvent) {
		if (!conversationEnded) {
			long workId = -1;
			synchronized (this) {
				workId = lastWorkId++;
			}
			if (workId == -1) { throw new RuntimeException(
					new InterruptedException("Unable to obtain a valid work ID.")); }
			workQueue.put(workId, workEvent);

			return workId;
		}

		throw new RuntimeException("The conversation is ended.");
	}

	public String getStrangerName(final int mainIndex) {
		return spies[mainIndex].getName();
	}

	public int indexOf(final OmegleSpy spy) {
		return spy == spies[0] ? 0 : 1;
	}

	private boolean randomizeNames() {
		final int firstIndex = (int) (Math.random() * possibleNames.length);
		int secondIndex;
		do {
			secondIndex = (int) (Math.random() * possibleNames.length);
		} while (firstIndex == secondIndex);
		names[0] = possibleNames[firstIndex];
		names[1] = possibleNames[secondIndex];
		return true;
	}

	private enum WorkEvent {
		_disconnectStranger, _swapStranger, _toggleStrangersBlock, _toggleFilter, _endConversation, _sendSecretMessage;
		private Map<String, Object>	params;
	}

	protected class WorkerThread implements Runnable {
		private final List<OmegleSpyListener>	initialListeners;

		private WorkerThread(final List<OmegleSpyListener> initialListeners) {
			this.initialListeners = initialListeners;
		}

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
			initializeStrangers(initialListeners);
			conversationEnded = false;
			while (!conversationEnded) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					log.warn("Thread error.", e);
				}
				if (!workQueue.isEmpty()) {
					final Map<Long, WorkEvent> tempWorkQueue = new HashMap<Long, SpyController.WorkEvent>(workQueue);
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
							if (log.isDebugEnabled()) {
								log.debug("An error occurred while invoking " + workEvent + " with params " + params
										+ " for workId " + key + ".", e);
							}
							workCompleted.put(key, false);
							detectedErrors.put(key, e);
						}

						// If we disconnected the conversation, we're not doing
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
		}

		protected void _disconnectStranger(final Map<String, Object> params) {
			final int strangerIndex = (Integer) params.get(STRANGER_INDEX);
			final OmegleSpy spy = spies[strangerIndex];
			spy.disconnect();
		}

		protected void _swapStranger(final Map<String, Object> params) {
			final int strangerIndex = (Integer) params.get(STRANGER_INDEX);
			final int otherIndex = strangerIndex == 0 ? 1 : 0;
			// grab swappiees name
			final String oldName = spies[strangerIndex].getName();

			final OmegleSpy oldSpy = spies[strangerIndex];

			// create a new chatter spy with the swappiees old name
			final OmegleSpy mainSpy = spies[strangerIndex] = new OmegleSpy(oldName);
			oldSpy.disconnect();

			// setup the new chatter spy as a event listener
			mainSpy.addOmegleSpyListener((OmegleSpyListener) params.get(SPY_LISTENER));

			// tell new chatter spy to talk to the other chatter (non touched
			// chatter)
			mainSpy.setPartner(spies[otherIndex].getChat());

			// do i need this? tell the other chatter (non touched chatter) to
			// talk to the new chatter
			spies[otherIndex].setPartner(mainSpy.getChat());

			// connect the new chatter into the chat
			mainSpy.startChat();
		}

		protected void _toggleStrangersBlock(final Map<String, Object> params) {
			final boolean blocked = (Boolean) params.get(IS_BLOCKED);
			for (final OmegleSpy s : spies) {
				if (s != null) {
					s.setBlocking(blocked);
				}
			}
		}

		protected void _toggleFilter(final Map<String, Object> params) {
			final boolean filtered = (Boolean) params.get(IS_FILTERED);
			for (final OmegleSpy s : spies) {
				if (s != null) {
					s.setFiltering(filtered);
				}
			}
		}

		protected void _sendSecretMessage(final Map<String, Object> params) {
			final int targetIndex = (Integer) params.get(STRANGER_INDEX);
			final OmegleSpy spy = spies[targetIndex];
			final String message = (String) params.get(MESSAGE);
			if (!spy.sendExternalMessage(message)) { throw new RuntimeException("Failed to deliver the message, "
					+ message + ", to " + spy.getName()); }
		}

		protected void _endConversation(final Map<String, Object> params) {
			for (final OmegleSpy s : spies) {
				if (s != null) {
					s.disconnect();
				}
			}
			conversationEnded = true;
		}
	}
}
