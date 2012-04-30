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

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy_z_desktop.KeyEventTranslator.Key;
import org.javabuilders.BuildResult;

/**
 * For the time being, we are only mapping to methods that take either no
 * argument or a control as an argument.
 * 
 * @author user
 * 
 */
public final class ShortcutKeyHelper {
	private static final Log		log	= LogFactory.getLog(ShortcutKeyHelper.class);

	private List<Shortcut>			shortcuts;
	private Map<String, Shortcut>	keyedShortcuts;

	/**
	 * The object against which to invoke methods. Typically, this will be a
	 * {@link Container}.
	 */
	private Object					recipient;

	private BuildResult				buildContext;

	public ShortcutKeyHelper() {}

	public void initialize() {
		keyedShortcuts = new HashMap<String, Shortcut>();
		if (shortcuts != null) {
			for (final Shortcut shortcut : shortcuts) {
				// TODO Find the correct method to execute without cues from the
				// XML.
				final Method method;
				final Class<?>[] params;
				final Object[] args;

				final Object emulatedSource = buildContext.get(shortcut.emulatedSource);
				if (shortcut.eventType != null && !StringUtils.isEmpty(shortcut.eventType)) {
					try {
						final Class<?> proxyEventClass = Class.forName(shortcut.eventType);
						final Class<?> eventClass = proxyEventClass.getSuperclass();
						params = new Class[] { eventClass };
						args = new Object[] { proxyEventClass.getConstructor(new Class[] { Object.class }).newInstance(
								emulatedSource) };
					} catch (final Exception e) {
						log.warn("Unable to instantiate the event class " + shortcut.eventType + " for " + shortcut, e);
						continue;
					}
				} else {
					try {
						params = new Class[] { emulatedSource.getClass() };
						args = new Object[] { emulatedSource };
					} catch (final Exception e) {
						log.warn("Unable to find the emulated source object " + shortcut.emulatedSource + " for "
								+ shortcut, e);
						continue;
					}
				}

				try {
					method = recipient.getClass().getDeclaredMethod(shortcut.function, params);
				} catch (final Exception e) {
					if (recipient != null) {
						log.warn("Unable to find the method " + shortcut.function + " in " + recipient.getClass(), e);
						continue;
					} else {
						log.warn("The recipient is not defined.", e);
						break;
					}
				}

				shortcut.setMethod(method);
				shortcut.setArgs(args);
			}
		} else {
			shortcuts = new ArrayList<Shortcut>();
		}

		for (final Shortcut shortcut : shortcuts) {
			keyedShortcuts.put(shortcut.chord, shortcut);
		}
	}

	public boolean performShortcut(final KeyEvent evt) {
		final String shortcutString = getShortcutString(evt);
		final Shortcut shortcut = keyedShortcuts.get(shortcutString);
		try {
			shortcut.getMethod().invoke(recipient, shortcut.getArgs());
			return true;
		} catch (final Exception e) {
			return false;
		}

	}

	/**
	 * @param e
	 * @return
	 */
	public static String getShortcutString(final KeyEvent e) {
		final Key _key = KeyEventTranslator.translateKeyEvent(e);
		final String modifiers;
		final char input;
		final int key;
		if (_key != null) {
			modifiers = _key.modifiers != null ? _key.modifiers : StringUtils.EMPTY;
			input = _key.input;
			key = _key.key;
		} else {
			modifiers = StringUtils.EMPTY;
			input = 0;
			key = 0;
		}

		final StringBuffer shortcutBuffer = new StringBuffer();
		if (StringUtils.isNotBlank(modifiers)) {
			shortcutBuffer.append(modifiers).append('+');
		}
		if (input != 0) {
			shortcutBuffer.append(input);
		} else {
			shortcutBuffer.append(KeyEvent.getKeyText(key).toUpperCase());
		}
		return shortcutBuffer.toString();
	}

	public boolean isShortcutDefined(final KeyEvent e) {
		if (keyedShortcuts == null) { throw new IllegalStateException(
				"This ShortcutKeyHelper has not been initialized."); }
		return keyedShortcuts.containsKey(getShortcutString(e));
	}

	public List<Shortcut> getShortcuts() {
		return shortcuts;
	}

	/**
	 * @param shortcuts
	 *            the shortcuts to set
	 */
	public void setShortcuts(final List<Shortcut> shortcuts) {
		this.shortcuts = shortcuts;
	}

	/**
	 * @param recipient
	 *            the recipient to set
	 */
	public void setRecipient(final Object recipient) {
		this.recipient = recipient;
	}

	/**
	 * @param buildContext
	 *            the buildContext to set
	 */
	public void setBuildContext(final BuildResult buildContext) {
		this.buildContext = buildContext;
	}
}
