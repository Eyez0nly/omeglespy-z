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
 * The FilterHelper is used to filter messages based on certain criteria
 * (determined by the subclass).
 * 
 * @author user
 * @version $Id: $
 */
public abstract class FilterHelper {
	private static FilterHelper	_instance	= new DefaultFilterHelper(null);

	/**
	 * <p>
	 * isBadMessage
	 * </p>
	 * 
	 * @param message
	 *            a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isBadMessage(final String message) {
		return _instance.checkIsBadMessage(message);
	}

	/**
	 * Sub classes override this method to check if a message should be
	 * filtered.
	 * 
	 * @param message
	 *            a {@link java.lang.String} object.
	 * @return a boolean. True if the message should be filtered.
	 */
	protected abstract boolean checkIsBadMessage(String message);
}
