/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.util.EventObject;

/**
 * An event that is generated to (typically) guide a UI.
 * 
 * @author user
 * 
 */
public class OmegleSpyEvent extends EventObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5472355439436372275L;

	public OmegleSpyEvent(final OmegleConnection source) {
		super(source);
	}

	public String getConversantName() {
		return source != null ? ((OmegleConnection) source).getConversantName() : null;
	}

	public String getRecaptchaChallengeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getChatId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOtherConversantName() {
		// TODO Auto-generated method stub
		return null;
	}
}
