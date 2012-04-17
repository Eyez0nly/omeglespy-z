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
package org.darkimport.omeglespy_z;

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
	 * @param fromName
	 *            The conversant as whom we sent the message.
	 * 
	 * @param message
	 *            The message we sent
	 */
	public void externalMessageSent(OmegleSpyEvent evt, String fromName, String message);

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

	public void initializationFailed(OmegleSpyEvent evt);
}
