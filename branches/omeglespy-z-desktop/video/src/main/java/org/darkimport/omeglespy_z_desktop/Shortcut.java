/*
 * #%L omeglespy-z-desktop
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
package org.darkimport.omeglespy_z_desktop;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.KeyStroke;

import org.apache.commons.lang.StringUtils;

/**
 * @author user
 * 
 */
public class Shortcut {
	public String		name;
	public String		function;
	public String		eventType;
	public String		emulatedSource;
	public String		chord;
	public String		description;
	private Method		method;
	private Object[]	args;

	public String getHumanReadableShortcutString() {
		final KeyStroke keyStroke = KeyEventTranslator.parseKeyStroke(chord);
		final int modifiers = keyStroke.getModifiers();
		final int keyCode = keyStroke.getKeyCode();
		final char keyChar = keyStroke.getKeyChar();

		final StringBuffer humanReadableShortcutString = new StringBuffer();
		final String keyModifiersText = KeyEvent.getKeyModifiersText(modifiers);
		if (StringUtils.isNotBlank(keyModifiersText)) {
			humanReadableShortcutString.append(keyModifiersText).append('+');
		}
		if (keyCode != 0) {
			humanReadableShortcutString.append(KeyEvent.getKeyText(keyCode));
		} else {
			humanReadableShortcutString.append(Character.toString(keyChar).toUpperCase());
		}
		return humanReadableShortcutString.toString();
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(final Method method) {
		this.method = method;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(final Object[] args) {
		this.args = args;
	}
}
