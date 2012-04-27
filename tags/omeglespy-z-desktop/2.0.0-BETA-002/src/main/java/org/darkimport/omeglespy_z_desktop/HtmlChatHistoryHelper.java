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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy_z.ChatHistoryHelper;

/**
 * @author user
 * 
 */
public class HtmlChatHistoryHelper extends ChatHistoryHelper {
	private static final Log			log						= LogFactory.getLog(HtmlChatHistoryHelper.class);

	public static final Pattern			URL_REGEX				= Pattern
																		.compile(
																				"([a-z]{2,6}://)?(?:[a-z0-9\\-]+\\.)+[a-z]{2,6}(?::\\d+)?(/\\S*)?",
																				Pattern.CASE_INSENSITIVE);
	private static final String			CONVO_LINK				= "convo-link";
	private static final String			DEFAULT_CLASS			= StringUtils.EMPTY;

	private static final String			SYSTEM					= "System";

	private static final String			SYSTEM_CLASS			= "system";

	private final Map<String, String>	labelStyleAssociations	= new HashMap<String, String>();
	private final String				baseHtml;
	private final String				baseElementId;
	private final HTMLDocument			htmlDocument;

	private final Map<Date, String>		currentChat;

	public HtmlChatHistoryHelper(final String baseHtml, final String baseElementId, final HTMLDocument htmlDocument) {
		this.baseHtml = baseHtml;
		this.baseElementId = baseElementId;
		this.htmlDocument = htmlDocument;
		currentChat = new HashMap<Date, String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.ChatHistoryHelper#doPrintLabelledMessage(java
	 * .lang.String, java.lang.String)
	 */
	@Override
	protected void doPrintLabelledMessage(final String label, final String message) {
		String finalMessage = StringEscapeUtils.escapeHtml(message);

		final StringBuffer sb = new StringBuffer();
		final Matcher m = URL_REGEX.matcher(finalMessage);
		while (m.find()) {
			String rep;
			if (m.group(1) != null || m.group(2) != null) {
				final String proto = m.group(1) == null ? "http://" : "";
				rep = "<a href='" + proto + "$0' target='_blank' " + "class='" + CONVO_LINK + "'>$0</a>";
			} else {
				rep = m.group();
			}
			m.appendReplacement(sb, rep);
		}
		m.appendTail(sb);
		finalMessage = sb.toString();
		final String className = labelStyleAssociations.containsKey(label) ? labelStyleAssociations.get(label)
				: DEFAULT_CLASS;
		printLogItem(label, finalMessage, className);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.ChatHistoryHelper#doPrintStatusMessage(java
	 * .lang.String)
	 */
	@Override
	protected void doPrintStatusMessage(final String message) {
		printLogItem(SYSTEM, message, SYSTEM_CLASS);
	}

	/**
	 * @param label
	 * @param finalMessage
	 * @param element
	 * @param className
	 */
	private void printLogItem(final String label, final String finalMessage, final String className) {
		final Element element = htmlDocument.getElement(baseElementId);
		final DateFormat timestamp = DateFormat.getTimeInstance(DateFormat.SHORT);
		final Date date = new Date();
		final String htmlText = "<div class='logitem'>" + "<span class='timestamp'>[" + timestamp.format(date)
				+ "]</span>" + " " + "<span class='" + className + "'>" + label + ":</span> " + finalMessage + "</div>";
		currentChat.put(date, htmlText);
		try {
			htmlDocument.insertBeforeEnd(element, htmlText);
		} catch (final Exception e) {
			log.warn("Unable to print a log item, " + htmlText + " into " + baseElementId, e);
		}
	}

	public void printRaw(final String rawText) {
		final Element element = htmlDocument.getElement(baseElementId);
		try {
			htmlDocument.insertBeforeEnd(element, rawText);
		} catch (final Exception e) {
			log.warn("Unable to print raw text, " + rawText + " into " + baseElementId, e);
		}
	}

	public void clearAssociations() {
		labelStyleAssociations.clear();
	}

	public void addLabelStyleAssociation(final String label, final String styleToUse) {
		labelStyleAssociations.put(label, styleToUse);
	}

	public void clearCurrentChat() {
		currentChat.clear();
	}

	public Map<Date, String> getCurrentChat() {
		return currentChat;
	}

	public String getBaseHtml() {
		return baseHtml;
	}
}
