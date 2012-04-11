/**
 * 
 */
package org.darkimport.omeglespy$z;

/**
 * @author user
 * 
 */
public abstract class FilterHelper {
	private static FilterHelper	_instance	= new DefaultFilterHelper(null);

	public static boolean isBadMessage(final String message) {
		return _instance.checkIsBadMessage(message);
	}

	protected abstract boolean checkIsBadMessage(String message);
}
