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
 * NameGenerators are used to get names for strangers/conversants and names for
 * servers.
 * 
 * @author user
 * @version $Id: $
 */
public interface NameGenerator {

	/**
	 * Get the next numberOfNames names. If uniqueNames is true, this method
	 * will check that the provided names are unique prior to returning.
	 * 
	 * @param numberOfNames
	 *            a int. The number of names to return. The returned array will
	 *            have this many elements.
	 * @param uniqueNames
	 *            a boolean. Should each name be unique?
	 * @return an array of {@link java.lang.String} objects. The names.
	 */
	String[] next(int numberOfNames, boolean uniqueNames);

}
