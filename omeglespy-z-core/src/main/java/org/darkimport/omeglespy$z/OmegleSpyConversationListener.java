/**
 * 
 */
package org.darkimport.omeglespy$z;

import java.util.EventListener;

/**
 * @author user
 * 
 */
public interface OmegleSpyConversationListener extends EventListener {
	/**
	 * Received a message from an Omegle conversant and transferred it to the
	 * other conversant.
	 * 
	 * @param src
	 *            The conversant from whom we received the message
	 * @param msg
	 *            The message content
	 */
	public void messageTransferred(OmegleSpyEvent evt, String msg);

	/**
	 * A message received from a conversant was blocked from being relayed to
	 * the other conversant. This occurs when the source conversant is in block
	 * mode.
	 * 
	 * @param src
	 *            The conversant whose message was blocked.
	 * @param msg
	 *            The message that the conversant sent.
	 */
	public void messageBlocked(OmegleSpyEvent evt, String msg);

	/**
	 * We sent a message to a conversant.
	 * 
	 * @param src
	 *            The conversant to whom we sent the message
	 * @param msg
	 *            The message we sent
	 */
	public void externalMessageSent(OmegleSpyEvent evt, String msg);

	/**
	 * A conversant is typing.
	 * 
	 * @param src
	 *            The OmegleConversant who is typing
	 */
	public void isTyping(OmegleSpyEvent evt);

	/**
	 * A conversant has stopped typing.
	 * 
	 * @param src
	 *            The OmegleConversant who has stopped typing
	 */
	public void stoppedTyping(OmegleSpyEvent evt);

	/**
	 * A connection with a conversant has been made.
	 * 
	 * @param src
	 *            The conversant with whom we connected.
	 */
	public void chatStarted(OmegleSpyEvent evt);

	/**
	 * The connection with a conversant has been closed or lost.
	 * 
	 * @param src
	 *            The conversant from whom we were disconnected.
	 */
	public void disconnected(OmegleSpyEvent evt);

	/**
	 * A recaptcha solution that we proposed was rejected.
	 * 
	 * @param src
	 *            The conversant with whom we were attempting to connect.
	 * @param id
	 *            The id of the new recaptcha challenge.
	 */
	public void recaptchaRejected(OmegleSpyEvent evt, String id);

	/**
	 * We were challenged with a recaptcha
	 * 
	 * @param src
	 *            The conversant with whom we were attempting to connect.
	 * @param id
	 *            The id of the recaptcha challenge.
	 */
	public void recaptcha(OmegleSpyEvent evt, String id);

	/**
	 * A message submitted by a conversant fell into the message filter.
	 * 
	 * @param omegleSpy
	 *            The conversant who sent the offending message.
	 * @param msg
	 *            The message they sent.
	 */
	public void messageFiltered(OmegleSpyEvent evt, String msg);
}
