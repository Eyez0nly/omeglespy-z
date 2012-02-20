/**
 * 
 */
package org.darkimport.util;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.darkimport.omeglespy.Common;

/**
 * @author user
 * 
 */
public class LogHelper {
	private static final String		BTN_LINK		= "btn-link";
	private static final Pattern	SAVCON_REGEX	= Pattern.compile("save-convo-(\\d+)");

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
				return Common.guiWriteHtmlFile(ct, null);
			}
		}
		// If className is null, we probably clicked on the link to the project
		// site in the start.html.
		return null;
	}
}
