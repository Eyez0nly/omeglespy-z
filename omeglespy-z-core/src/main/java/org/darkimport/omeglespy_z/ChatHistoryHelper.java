/*
 * #%L
 * omeglespy-z-core
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 darkimport
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
/**
 * 
 */
package org.darkimport.omeglespy_z;

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
