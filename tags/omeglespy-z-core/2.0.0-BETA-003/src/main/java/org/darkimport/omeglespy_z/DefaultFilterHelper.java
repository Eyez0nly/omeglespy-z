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

import java.util.List;

/**
 * A useful implementation of the {@link FilterHelper}.
 * 
 * For now, we just do string matching on some known bot tells.
 * 
 * 
 * 
 * @author user
 * @version $Id: $
 */
public class DefaultFilterHelper extends FilterHelper {
	private final List<String>	badMessages;

	/**
	 * Loads this {@link DefaultFilterHelper} with the given list of bad
	 * messages.
	 * 
	 * @param badMessages
	 *            a {@link java.util.List} object.
	 */
	public DefaultFilterHelper(final List<String> badMessages) {
		this.badMessages = badMessages;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * If the msg is contained in the previously loaded list, return true.
	 */
	@Override
	protected boolean checkIsBadMessage(final String msg) {
		return badMessages != null && badMessages.contains(msg.trim());
	}
}
