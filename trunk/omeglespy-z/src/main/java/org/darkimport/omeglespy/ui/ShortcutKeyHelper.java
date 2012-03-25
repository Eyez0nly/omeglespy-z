/**
 * 
 */
package org.darkimport.omeglespy.ui;

import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.configuration.ConfigHelper;
import org.darkimport.omeglespy.constants.ConfigConstants;
import org.darkimport.omeglespy.ui.util.KeyEventTranslator;
import org.darkimport.omeglespy.ui.util.KeyEventTranslator.Key;

/**
 * For the time being, we are only mapping to methods that take a Map argument.
 * Additionally, all arguments are static.
 * 
 * @author user
 * 
 */
public final class ShortcutKeyHelper {
	private static final Log								log	= LogFactory.getLog(ShortcutKeyHelper.class);

	private final Object									recipient;
	private final Map<String, List<Method>>					shortcutMethodMap;
	private final Map<String, List<Map<String, String>>>	shortcutArgumentMap;

	public ShortcutKeyHelper(final Object recipient) {
		if (!ConfigHelper.isInitialized()) { throw new ExceptionInInitializerError(
				"The ConfigHelper is not initialized."); }
		this.recipient = recipient;
		shortcutMethodMap = new HashMap<String, List<Method>>();
		shortcutArgumentMap = new HashMap<String, List<Map<String, String>>>();

		initialize(recipient, shortcutMethodMap, shortcutArgumentMap);
	}

	private void initialize(final Object recipient, final Map<String, List<Method>> shortcutMethodMap,
			final Map<String, List<Map<String, String>>> shortcutParameterMap) {
		final Properties rawShortcuts = ConfigHelper.getGroup(ConfigConstants.GROUP_SHORTCUTS);
		final Set<Object> keys = rawShortcuts.keySet();
		for (final Object key : keys) {
			final String shortCut = rawShortcuts.get(key).toString();
			final String[] keyParts = key.toString().split("\\.");
			final List<Method> methodList = new ArrayList<Method>();
			final List<Map<String, String>> argumentList = new ArrayList<Map<String, String>>();
			for (final String rawCommand : keyParts) {
				final String[] rawCommandParts = rawCommand.split("\\$");
				final String commandName = rawCommandParts[0];
				Method command;
				try {
					command = recipient.getClass().getMethod(commandName, new Class[] { Map.class });
				} catch (final Exception e) {
					log.warn(commandName + " is not accessible from " + recipient.getClass().toString() + ".", e);
					continue;
				}

				methodList.add(command);
				final Map<String, String> args = new HashMap<String, String>();
				if (rawCommandParts.length > 1) {
					for (int i = 1; i < rawCommandParts.length; i++) {
						final String[] argumentParts = rawCommandParts[i].split("_");
						args.put(argumentParts[0], argumentParts[1]);
					}
				}
				argumentList.add(args);
			}
			shortcutMethodMap.put(shortCut, methodList);
			shortcutParameterMap.put(shortCut, argumentList);
		}
	}

	public void performShortcut(final KeyEvent e) {
		final String shortcutString = getShortcutString(e);

		final List<Method> methodList = shortcutMethodMap.get(shortcutString);
		if (methodList != null) {
			final List<Map<String, String>> argumentList = shortcutArgumentMap.get(shortcutString);
			for (int i = 0; i < methodList.size(); i++) {
				final Method method = methodList.get(i);
				final Map<String, String> args = argumentList.get(i);
				try {
					method.invoke(recipient, args);
				} catch (final Exception ex) {
					log.warn("An error occurred while trying to invoke shortcut command " + shortcutString + " on "
							+ recipient + " (" + recipient.getClass().toString() + ") " + method.getName() + ".", ex);
				}
			}
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
		return shortcutMethodMap.containsKey(getShortcutString(e));
	}
}
