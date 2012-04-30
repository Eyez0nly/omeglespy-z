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

/**
 * This abstract class receives various "chat" related notifications.
 * Implementing classes will usually print these messages in some meaningful way
 * for an end-user's consumption.
 * 
 * Note: nothing in the core library utilizes the ChatHistoryHelper. It is the
 * responsibility of implementing applications to utilize this class
 * appropriately.
 * 
 * Note: the note above may change before the 2.0.0 release
 * 
 * @since 2.0.0
 * @author user
 * @version $Id: $
 */
public abstract class ChatHistoryHelper {
	/**
	 * The instance of the ChatHistoryHelper that will be used. It is static, so
	 * it is expected that there will be only one ChatHistoryHelper per
	 * application (unless a custom implementation does some forking).
	 */
	private static ChatHistoryHelper	_instance;

	/**
	 * Initialize the ChatHistoryHelper with the given implementation.
	 * 
	 * @param chatHistoryHelper
	 *            a {@link org.darkimport.omeglespy_z.ChatHistoryHelper} object.
	 * @since 2.0.0
	 */
	public static void initialize(final ChatHistoryHelper chatHistoryHelper) {
		_instance = chatHistoryHelper;
	}

	/**
	 * Prints a labeled message (e.g. Stranger1: hi there asl).
	 * 
	 * @param label
	 *            a {@link java.lang.String} object that contains the label.
	 * @param message
	 *            a {@link java.lang.String} object that contains the message.
	 */
	public static void printLabelledMessage(final String label, final String message) {
		_instance.doPrintLabelledMessage(label, message);
	}

	/**
	 * Prints a system generated status message (e.g. <b>Stranger1 has
	 * left</b>).
	 * 
	 * @param message
	 *            a {@link java.lang.String} object that contains the message.
	 */
	public static void printStatusMessage(final String message) {
		_instance.doPrintStatusMessage(message);
	}

	/**
	 * Sub classes do the work of printing a labeled message here.
	 * 
	 * @param label
	 *            a {@link java.lang.String} object.
	 * @param message
	 *            a {@link java.lang.String} object.
	 */
	protected abstract void doPrintLabelledMessage(String label, String message);

	/**
	 * Sub classes do the work of printing a status message here.
	 * 
	 * @param message
	 *            a {@link java.lang.String} object.
	 */
	protected abstract void doPrintStatusMessage(String message);
}
