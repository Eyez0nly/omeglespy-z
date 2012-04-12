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


/**
 * An event generated by an omegle server from an event ping.
 * 
 * @author user
 * 
 */
class OmegleEvent {

	private final OmegleEventType	eventType;
	private final String[]			args;

	public OmegleEvent(final OmegleEventType eventType, final String[] args) {
		this.eventType = eventType;
		this.args = args;
	}

	/**
	 * @return the source
	 */
	public OmegleEventType getSource() {
		return eventType;
	}

	/**
	 * @return the args
	 */
	public String[] getArgs() {
		return args;
	}

}