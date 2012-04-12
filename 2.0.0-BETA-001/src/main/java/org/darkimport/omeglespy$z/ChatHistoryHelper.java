/**
 * 
 */
package org.darkimport.omeglespy$z;

/**
 * @author user
 * 
 */
public abstract class ChatHistoryHelper {
	private static ChatHistoryHelper	_instance;

	public static void initialize(final ChatHistoryHelper chatHistoryHelper) {
		_instance = chatHistoryHelper;
	}

	public static void printLabelledMessage(final String label, final String message) {
		_instance.doPrintLabelledMessage(label, message);
	}

	public static void printStatusMessage(final String message) {
		_instance.doPrintStatusMessage(message);
	}

	protected abstract void doPrintLabelledMessage(String label, String message);

	protected abstract void doPrintStatusMessage(String message);
}
