/**
 * 
 */
package org.darkimport.omeglespy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author user
 * 
 */
public class SpyController implements Runnable {
	private static final Log			log				= LogFactory.getLog(SpyController.class);

	private final Map<Long, WorkEvent>	workQueue		= new Hashtable<Long, WorkEvent>();
	private final Map<Long, Boolean>	completedTasks	= new Hashtable<Long, Boolean>();

	/**
	 * Tells us if the conversation is active. Also tells us if this
	 * spycontroller is accepting any new work requests.
	 */
	private boolean						conversationEnded;

	public void disconnectStranger(final int strangerIndex) {
		// TODO
	}

	public void swapStranger(final int strangerIndex) {
		// TODO
	}

	public void endConversation() {
		// TODO
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
		initializeStrangers();
		while (conversationEnded) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				log.warn("Thread error.", e);
			}
			if (!workQueue.isEmpty()) {
				final Map<Long, WorkEvent> tempWorkQueue = new HashMap<Long, SpyController.WorkEvent>(workQueue);
				final Set<Long> keys = tempWorkQueue.keySet();
				final Map<Long, Boolean> workCompleted = new HashMap<Long, Boolean>();
				for (final long key : keys) {
					final WorkEvent workEvent = tempWorkQueue.get(key);
					final Integer strangerIndex = workEvent.strangerIndex;
					try {
						final Method workMethod = getClass().getDeclaredMethod(workEvent.name(),
								(strangerIndex != null ? strangerIndex.getClass() : null));
						workMethod.invoke(this, strangerIndex);
						workCompleted.put(key, true);
					} catch (final Exception e) {
						if (log.isDebugEnabled()) {
							log.debug("An error occurred while invoking " + workEvent + " with strangerIndex "
									+ strangerIndex + " for workId " + key + ".", e);
						}
						workCompleted.put(key, false);
					}

					// If we disconnected the conversation, we're not doing
					// anymore work.
					if (!conversationEnded) {
						break;
					}
				}

				completedTasks.putAll(workCompleted);
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

	private void initializeStrangers() {
		// TODO Auto-generated method stub

	}

	protected void _disconnectStranger(final Integer strangerIndex) {
		// TODO
	}

	protected void _swapStranger(final Integer strangerIndex) {
		// TODO
	}

	protected void _endConversation() {
		// TODO
	}

	private enum WorkEvent {
		_disconnectStranger, _swapStranger, _endConversation;
		private Integer	strangerIndex;
	}
}
