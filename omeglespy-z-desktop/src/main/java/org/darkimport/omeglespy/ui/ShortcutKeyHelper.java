/*
 * #%L
 * omeglespy-z-desktop
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
package org.darkimport.omeglespy.ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.config.ConfigHelper;
import org.darkimport.omeglespy.ConfigConstants;
import org.darkimport.omeglespy.ui.util.KeyEventTranslator;
import org.darkimport.omeglespy.ui.util.KeyEventTranslator.Key;
import org.darkimport.omeglespy_z.Shortcut;
import org.javabuilders.BuildResult;

/**
 * For the time being, we are only mapping to methods that take either no
 * argument or a control as an argument.
 * 
 * @author user
 * 
 */
public final class ShortcutKeyHelper {
	private static final Log									log	= LogFactory.getLog(ShortcutKeyHelper.class);

	/**
	 * The object against which to invoke methods. Typically, this will be a
	 * {@link Container}.
	 */
	private final Object										recipient;

	/**
	 * A map keyed by shortcut to the method(s) that the shortcut will invoke.
	 */
	private final Map<String, List<Method>>						shortcutMethodMap;

	/**
	 * A map keyed by shortcut to a list of allowed sources tied to this
	 * shourtcut.
	 */
	private final Map<String, List<String>>						shortcutAllowedSources;

	/**
	 * A map keyed by shortcut to the arguments that will be supplied to the
	 * method. For now, arguments are either null or a control.
	 */
	private final Map<String, List<Object>>						shortcutEmulatedSourceMap;

	private final Map<String, List<Class<? extends Object>>>	shortcutParameterMap;

	public ShortcutKeyHelper(final Object recipient, final BuildResult result) {
		if (!ConfigHelper.isInitialized()) { throw new ExceptionInInitializerError(
				"The ConfigHelper is not initialized."); }
		this.recipient = recipient;
		shortcutMethodMap = new HashMap<String, List<Method>>();
		shortcutEmulatedSourceMap = new HashMap<String, List<Object>>();
		shortcutAllowedSources = new HashMap<String, List<String>>();
		shortcutParameterMap = new HashMap<String, List<Class<? extends Object>>>();

		initialize(recipient, result, shortcutMethodMap, shortcutEmulatedSourceMap, shortcutParameterMap,
				shortcutAllowedSources);
	}

	private static void initialize(final Object recipient, final BuildResult result,
			final Map<String, List<Method>> shortcutMethodMap,
			final Map<String, List<Object>> shortcutEmulatedSourceMap,
			final Map<String, List<Class<? extends Object>>> shortcutParameterMap,
			final Map<String, List<String>> shortcutAllowedSources) {
		final Properties rawShortcuts = ConfigHelper.getGroup(ConfigConstants.GROUP_SHORTCUTS);
		final Set<Object> keys = rawShortcuts.keySet();
		for (final Object key : keys) {
			final String[] shortCutRaw = rawShortcuts.get(key).toString().split("\\$");
			final String shortCut = shortCutRaw[0];
			final List<String> allowedSources = new Vector<String>();
			if (shortCutRaw.length > 1) {
				for (int i = 1; i < shortCutRaw.length; i++) {
					allowedSources.add(shortCutRaw[i]);
				}
			}

			final String[] keyParts = key.toString().split("\\~");
			final List<Method> methodList = new ArrayList<Method>();
			final List<Object> emulatedSourceList = new Vector<Object>();
			final List<Class<? extends Object>> parameterList = new ArrayList<Class<? extends Object>>();
			for (final String rawCommand : keyParts) {
				final String[] rawCommandParts = rawCommand.split("\\$");
				final String commandName = rawCommandParts[0];

				final Class<? extends Object> parameter;
				final Object emulatedSource;
				if (rawCommandParts.length > 1) {
					final String[] rawComponentName = rawCommandParts[1].split("\\_");
					emulatedSource = result.get(rawComponentName[0]);
					if (rawComponentName.length > 1) {
						try {
							parameter = Class.forName(rawComponentName[1]);
						} catch (final ClassNotFoundException e) {
							log.warn("The specified class, " + rawComponentName[1] + ", does not exist.");
							continue;
						}
					} else {
						parameter = emulatedSource != null ? emulatedSource.getClass() : null;
					}
				} else {
					emulatedSource = null;
					parameter = null;
				}

				Method command;
				try {
					command = recipient.getClass().getMethod(commandName,
							parameter != null ? new Class[] { parameter } : null);
					if (parameter != null) {
						parameterList.add(parameter);
					}
				} catch (final Exception e) {
					log.warn(commandName + " is not accessible from " + recipient.getClass().toString() + ".", e);
					continue;
				}

				methodList.add(command);
				emulatedSourceList.add(emulatedSource);
			}
			shortcutMethodMap.put(shortCut, methodList);
			shortcutEmulatedSourceMap.put(shortCut, emulatedSourceList);
			shortcutAllowedSources.put(shortCut, allowedSources);
			shortcutParameterMap.put(shortCut, parameterList);
		}
	}

	public boolean performShortcut(final KeyEvent e) {
		final String shortcutString = getShortcutString(e);
		final List<String> allowedSources = shortcutAllowedSources.get(shortcutString);
		boolean fromAllowedSource;
		if (allowedSources == null || allowedSources.size() == 0
				|| allowedSources.contains(((JComponent) e.getSource()).getName())) {
			fromAllowedSource = true;
		} else {
			fromAllowedSource = false;
		}

		final List<Method> methodList = shortcutMethodMap.get(shortcutString);
		if (methodList != null && fromAllowedSource) {
			final List<Object> emulatedSourceList = shortcutEmulatedSourceMap.get(shortcutString);
			final List<Class<? extends Object>> parameterList = shortcutParameterMap.get(shortcutString);
			for (int i = 0; i < methodList.size(); i++) {
				final Method method = methodList.get(i);
				final Object emulatedSource = emulatedSourceList.get(i);
				final Class<? extends Object> parameter = parameterList.get(i);
				Object arg;
				if (!parameter.equals(emulatedSource.getClass())) {
					arg = new ActionEvent(emulatedSource, 0, null);
				} else {
					arg = emulatedSource;
				}
				try {
					method.invoke(recipient, arg);
				} catch (final Exception ex) {
					log.warn("An error occurred while trying to invoke shortcut command " + shortcutString + " on "
							+ recipient + " (" + recipient.getClass().toString() + ") " + method.getName() + ".", ex);
				}
			}
			return true;
		} else {
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
			shortcutBuffer.append(key);
		}
		return shortcutBuffer.toString();
	}

	public boolean isShortcutDefined(final KeyEvent e) {
		return shortcutMethodMap.containsKey(getShortcutString(e))
				&& (shortcutAllowedSources.get(getShortcutString(e)) == null
						|| shortcutAllowedSources.get(getShortcutString(e)).size() == 0 || shortcutAllowedSources.get(
						getShortcutString(e)).contains(((JComponent) e.getSource()).getName()));
	}

	public List<Shortcut> getShortcuts() {
		// TODO Auto-generated method stub
		return null;
	}
}
