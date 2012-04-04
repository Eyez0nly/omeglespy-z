/**
 * 
 */
package org.darkimport.omeglespy.util;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author user
 * 
 */
public class LogHelper {
	private static final Log			log				= LogFactory.getLog(LogHelper.class);

	private static final String			BTN_LINK		= "btn-link";
	private static final Pattern		SAVCON_REGEX	= Pattern.compile("save-convo-(\\d+)");
	private static final String			HTML_EXT		= ".html";
	private static final JFileChooser	fc;

	static {
		JFileChooser jfc;
		try {
			jfc = new JFileChooser();
			jfc.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(final File f) {
					final String n = f.getName().toLowerCase();
					return f.isDirectory() || n.endsWith(HTML_EXT);
				}

				@Override
				public String getDescription() {
					return "HTML files (*.html)";
				}
			});
		} catch (final Exception ex) {
			jfc = null;
			log.warn("Could not create a file selector window", ex);
		}
		fc = jfc;
	}

	/**
	 * @param e
	 * @param blocks
	 * @throws IOException
	 */
	public static Boolean saveLog(final Element e, final String baseHtml, final Map<Integer, String> blocks)
			throws IOException {
		final HTMLDocument.RunElement re = (HTMLDocument.RunElement) e;
		final AttributeSet atts = (AttributeSet) re.getAttributes().getAttribute(HTML.Tag.A);
		final String className = (String) atts.getAttribute(HTML.Attribute.CLASS);
		if (className != null && className.equals(BTN_LINK)) {
			final String id = (String) atts.getAttribute(HTML.Attribute.ID);
			Matcher m;
			if ((m = SAVCON_REGEX.matcher(id)).matches()) {
				final int ci = Integer.parseInt(m.group(1));
				final String ct = baseHtml.replace("<!--%s-->", blocks.get(ci));
				return guiWriteHtmlFile(ct, null);
			}
		}
		// If className is null, we probably clicked on the link to the project
		// site in the start.html.
		return null;
	}

	public static boolean guiWriteHtmlFile(final String text, final Component p) throws IOException {
		if (fc.showSaveDialog(p) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			if (!f.getName().toLowerCase().endsWith(HTML_EXT)) {
				f = new File(f.getParent(), f.getName() + HTML_EXT);
			}
			if (!(f.exists() && !showWarning(p, "The file " + f.getName() + " already exists. "
					+ "Are you sure you want to " + "overwrite it?"))) {
				final FileOutputStream fos = new FileOutputStream(f);
				fos.write(text.getBytes());
				return true;
			}
		}

		return false;
	}

	private static boolean showWarning(final Component p, final String msg) {
		final int result = JOptionPane.showConfirmDialog(p, msg, "Warning", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		return result == JOptionPane.YES_OPTION;
	}
}
