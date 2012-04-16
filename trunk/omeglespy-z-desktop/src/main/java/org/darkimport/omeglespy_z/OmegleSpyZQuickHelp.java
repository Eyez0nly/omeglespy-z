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
package org.darkimport.omeglespy_z;

import java.text.MessageFormat;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy.ResourceConstants;
import org.darkimport.omeglespy.ui.ShortcutKeyHelper;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class OmegleSpyZQuickHelp extends JFrame {
	private static final Log	log					= LogFactory.getLog(OmegleSpyZQuickHelp.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -2538920292746790249L;

	private static final String	ROW_STARTER			= "<tr><td>";
	private static final String	ROW_DIVIDER			= "</td><td>";
	private static final String	ROW_ENDER			= "</td></tr>";
	private static final String	REPLACEMENT			= "<!--%s-->";

	private final BuildResult	result;
	private JEditorPane			helpText;

	public OmegleSpyZQuickHelp(final JFrame parent, final ShortcutKeyHelper shortcutKeyHelper) {
		result = SwingJavaBuilder.build(this);
		String helpHtml;
		try {
			final String rawHelpHtml = IOUtils.toString(getClass().getResourceAsStream("/expertHelp.html"));
			final StringBuffer expertHelpString = new StringBuffer();
			final List<Shortcut> shortcuts = shortcutKeyHelper.getShortcuts();
			if (shortcuts != null) {
				for (final Shortcut shortcut : shortcuts) {
					expertHelpString.append(ROW_STARTER);
					expertHelpString.append(shortcut.getHumanReadableShortcutString());
					expertHelpString.append(ROW_DIVIDER);
					expertHelpString.append(shortcut.getDescription());
					expertHelpString.append(ROW_ENDER);
				}
			}
			helpHtml = rawHelpHtml.replace(REPLACEMENT, expertHelpString.toString());
		} catch (final Exception ex) {
			log.fatal("Unable to load the expertHelp.html. Displaying error message instead.", ex);
			helpHtml = MessageFormat.format(result.getConfig().getResource(ResourceConstants.MESSAGE_ERROR_HELP_LOAD),
					ex.getLocalizedMessage());
		}

		helpText.setText(helpHtml);
		parent.requestFocus();
	}
}
