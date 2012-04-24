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

import java.util.EventListener;

/**
 * This listener interface is specifically implemented in mediation type
 * applications.
 * 
 * @author user
 * @version $Id: $
 */
public interface OmegleSpyConversationListener extends EventListener {
	/**
	 * Received a message from an Omegle conversant and transferred it to the
	 * other conversant.
	 * 
	 * @param msg
	 *            The message content
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void messageTransferred(OmegleSpyEvent evt, String msg);

	/**
	 * A message received from a conversant was blocked from being relayed to
	 * the other conversant. This occurs when the source conversant is in block
	 * mode.
	 * 
	 * @param msg
	 *            The message that the conversant sent.
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void messageBlocked(OmegleSpyEvent evt, String msg);

	/**
	 * We sent a message to a conversant.
	 * 
	 * @param fromName
	 *            The conversant as whom we sent the message.
	 * @param message
	 *            The message we sent
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void externalMessageSent(OmegleSpyEvent evt, String fromName, String message);

	/**
	 * A conversant is typing.
	 * 
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void isTyping(OmegleSpyEvent evt);

	/**
	 * A conversant has stopped typing.
	 * 
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void stoppedTyping(OmegleSpyEvent evt);

	/**
	 * A connection with a conversant has been made.
	 * 
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void chatStarted(OmegleSpyEvent evt);

	/**
	 * The connection with a conversant has been closed or lost.
	 * 
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void strangerDisconnected(OmegleSpyEvent evt);

	/**
	 * A recaptcha solution that we proposed was rejected.
	 * 
	 * @param id
	 *            The id of the new recaptcha challenge.
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void recaptchaRejected(OmegleSpyEvent evt, String id);

	/**
	 * We were challenged with a recaptcha
	 * 
	 * @param id
	 *            The id of the recaptcha challenge.
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void recaptcha(OmegleSpyEvent evt, String id);

	/**
	 * A message submitted by a conversant fell into the message filter.
	 * 
	 * @param msg
	 *            The message they sent.
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void messageFiltered(OmegleSpyEvent evt, String msg);

	/**
	 * <p>
	 * initializationFailed
	 * </p>
	 * 
	 * @param evt
	 *            a {@link org.darkimport.omeglespy_z.mediation.OmegleSpyEvent}
	 *            object.
	 */
	public void initializationFailed(OmegleSpyEvent evt);

	public void userDisconnected(OmegleSpyEvent evt);

	public void generalCommunicationFailure(OmegleSpyEvent evt);
}
