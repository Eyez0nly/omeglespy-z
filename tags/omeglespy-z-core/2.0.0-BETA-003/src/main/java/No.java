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
import javax.swing.JOptionPane;

/**
 * This application class serves to alert users that this library is not an
 * executable library (HA).
 *
 * @author user
 * @version $Id: $
 */
public class No {

	/**
	 * <p>main</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		JOptionPane
				.showMessageDialog(
						null,
						"This library does nothing on its own. Visit http://code.google.com/p/omeglespy-z for more information.",
						"NO.", JOptionPane.ERROR_MESSAGE);
	}

}
