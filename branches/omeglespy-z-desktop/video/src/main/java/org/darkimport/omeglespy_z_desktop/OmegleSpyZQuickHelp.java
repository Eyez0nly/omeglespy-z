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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy_z_desktop.constants.ResourceConstants;
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
			if (shortcutKeyHelper != null) {
				final List<Shortcut> shortcuts = shortcutKeyHelper.getShortcuts();
				if (shortcuts != null) {
					for (final Shortcut shortcut : shortcuts) {
						expertHelpString.append(ROW_STARTER);
						expertHelpString.append(shortcut.getHumanReadableShortcutString());
						expertHelpString.append(ROW_DIVIDER);
						expertHelpString.append(shortcut.description);
						expertHelpString.append(ROW_ENDER);
					}
				}
			} else {
				expertHelpString.append(ROW_STARTER);
				expertHelpString.append(SwingJavaBuilder.getConfig().getResource(
						ResourceConstants.MESSAGE_HELP_NONE_LOADED));
				expertHelpString.append(ROW_ENDER);
			}
			helpHtml = rawHelpHtml.replace(REPLACEMENT, expertHelpString.toString());
		} catch (final Exception ex) {
			log.fatal("Unable to load the expertHelp.html. Displaying error message instead.", ex);
			helpHtml = MessageFormat.format(result.getConfig().getResource(ResourceConstants.MESSAGE_ERROR_HELP_LOAD),
					ex.getLocalizedMessage());
		}

		helpText.setText(helpHtml);

		addWindowFocusListener(new WindowAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowGainedFocus(java.awt.event
			 * .WindowEvent)
			 */
			@Override
			public void windowGainedFocus(final WindowEvent e) {
				final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				final Dimension parentSize = parent.getSize();
				final Point parentLocation = parent.getLocation();
				final Point myLocation = new Point();
				final Dimension mySize = getSize();
				myLocation.x = parentLocation.x + parentSize.width;
				myLocation.y = parentLocation.y;
				if (myLocation.x + mySize.width > screenSize.width) {
					myLocation.x = screenSize.width - mySize.width;
				}

				setLocation(myLocation);
				if (parent.getBounds().intersects(getBounds())) {
					setAlwaysOnTop(false);
				} else {
					setAlwaysOnTop(true);
				}

				parent.requestFocus();
				parent.toFront();
			}
		});
	}
}
