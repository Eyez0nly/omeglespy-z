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
