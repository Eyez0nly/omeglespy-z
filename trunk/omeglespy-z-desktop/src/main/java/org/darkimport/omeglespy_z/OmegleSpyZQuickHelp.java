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
