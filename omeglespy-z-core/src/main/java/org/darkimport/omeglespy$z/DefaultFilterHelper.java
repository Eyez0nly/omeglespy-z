/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.util.List;

/**
 * For now, we just do string matching on some known bot tells.
 * 
 * The tells are loaded from filter.txt at the first invokation of isBadMessage.
 * 
 * @author user
 * 
 */
public class DefaultFilterHelper extends FilterHelper {
	private final List<String>	badMessages;

	public DefaultFilterHelper(final List<String> badMessages) {
		this.badMessages = badMessages;
	}

	@Override
	protected boolean checkIsBadMessage(final String msg) {
		return badMessages != null && badMessages.contains(msg.trim());
	}
}
