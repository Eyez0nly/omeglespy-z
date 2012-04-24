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

import java.util.EventObject;

/**
 * An event that is generated in the mediation-style context to (typically)
 * guide a UI.
 * 
 * @author user
 * @version $Id: $
 */
public class OmegleSpyEvent extends EventObject {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5472355439436372275L;

	/**
	 * <p>
	 * Constructor for OmegleSpyEvent.
	 * </p>
	 * 
	 * @param source
	 *            a {@link org.darkimport.omeglespy_z.OmegleConnection} object.
	 */
	public OmegleSpyEvent(final OmegleConnection source) {
		super(source);
	}

	/**
	 * <p>
	 * getConversantName
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getConversantName() {
		return source != null ? ((OmegleConnection) source).getConversantName() : null;
	}
}
