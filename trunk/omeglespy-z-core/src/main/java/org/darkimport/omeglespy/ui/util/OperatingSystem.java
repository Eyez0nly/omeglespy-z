/*
 * OperatingSystem.java - OS detection :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 * 
 * Copyright (C) 2002, 2005 Slava Pestov Copyright (C) 2012 Dark Import
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.darkimport.omeglespy.ui.util;


/**
 * Operating system detection routines.
 * 
 * @author Slava Pestov
 * @author Dark Import
 * @version $Id$
 * @since jEdit 4.0pre4
 * @since omeglespy-z 2.0.0
 */
public class OperatingSystem {
	private static final int	MAC_OS_X	= 0xABC;
	private static int			os;

	static {
		if (System.getProperty("mrj.version") != null) {
			os = MAC_OS_X;
		}
	}

	/**
	 * Returns if we're running MacOS X.
	 */
	public static boolean isMacOS() {
		return os == MAC_OS_X;
	} // }}}
}
