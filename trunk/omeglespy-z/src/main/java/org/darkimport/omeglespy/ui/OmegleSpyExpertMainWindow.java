/**
 * 
 */
package org.darkimport.omeglespy.ui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy.DesperationWindow;
import org.darkimport.omeglespy.OmegleSpy;
import org.darkimport.omeglespy.OmegleSpyListener;
import org.darkimport.omeglespy.SpyController;
import org.darkimport.omeglespy.constants.ControlNameConstants;
import org.darkimport.omeglespy.constants.ResourceConstants;
import org.darkimport.omeglespy.util.LogHelper;
import org.darkimport.omeglespy.util.UrlHelper;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class OmegleSpyExpertMainWindow extends JFrame {
	private static final Log			log					= LogFactory.getLog(OmegleSpyExpertMainWindow.class);

	/**
	 * 
	 */
	private static final long			serialVersionUID	= 9092938195254654975L;

	public static final int				REST_TIME			= 80;

	private final BuildResult			result;

	private int							convoNum;

	private String						baseHTML;
	private JEditorPane					console;
	private HTMLDocument				doc;
	private Element						logbox, currentChat, currentConvo;
	private final Map<Integer, String>	blocks				= new HashMap<Integer, String>();
	private static final String[]		CLASS_NAMES			= { "youmsg", "strangermsg" };
	private static final String			BTN_LINK			= "btn-link";
	private static final String			CONVO_LINK			= "convo-link";
	public static final Pattern			url_regex			= Pattern
																	.compile(
																			"([a-z]{2,6}://)?(?:[a-z0-9\\-]+\\.)+[a-z]{2,6}(?::\\d+)?(/\\S*)?",
																			Pattern.CASE_INSENSITIVE);

	private final SpyController			controller			= new SpyController();

	private boolean						autoScrollEnabled	= true;
	private final JScrollBar			autoScrollBar;

	private boolean						firstRun			= true;

	private final DesperationWindow		logViewer			= new DesperationWindow();

	private boolean						strangersBlocked;

	private boolean						filtered;

	private final ShortcutKeyHelper		shortcutKeyHelper;

	public OmegleSpyExpertMainWindow() {
		result = SwingJavaBuilder.build(this);

		shortcutKeyHelper = new ShortcutKeyHelper(this);

		try {
			baseHTML = IOUtils.toString(getClass().getResourceAsStream("/base.html"));
		} catch (final IOException ex) {
			log.fatal("Unable to load the base.html. Application exiting.", ex);
			throw new RuntimeException(ex);
		}

		// Instantiate the scrollbar used when autoscrolling.
		autoScrollBar = ((JScrollPane) result.get("consoleScroller")).getVerticalScrollBar();
		autoScrollBar.setUnitIncrement(16);

		// The builder has instantiated the console
		// already.
		console.addComponentListener(new ComponentAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ComponentAdapter#componentResized(java.awt.event
			 * .ComponentEvent)
			 */
			@Override
			public void componentResized(final ComponentEvent e) {
				if (autoScrollEnabled) {
					autoScrollBar.setValue(autoScrollBar.getMaximum() - autoScrollBar.getVisibleAmount());
				}
			}
		});
		console.addHyperlinkListener(new HyperlinkListener() {

			public void hyperlinkUpdate(final HyperlinkEvent ev) {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					log.debug("Clicked a link: " + ev.getURL());
					try {
						final Element e = ev.getSourceElement();
						if (LogHelper.saveLog(e, baseHTML, blocks) == null) {
							try {
								UrlHelper.openURL(ev.getURL().toString());
							} catch (final Exception ex) {
								log.warn("Unable to open the URL in the browser.", ex);
							}
						}
					} catch (final Exception e) {
						log.warn("Could not save file.", e);
					}
				}
			}
		});

		// Display the welcome message.
		try {
			console.setText(IOUtils.toString(getClass().getResourceAsStream("/start.html")));
		} catch (final IOException ex) {
			log.fatal("Unable to load the start.html. Application exiting.", ex);
			throw new RuntimeException(ex);
		}

		addWindowListener(new WindowAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent
			 * )
			 */
			@Override
			public void windowClosed(final WindowEvent e) {
				exit();
			}
		});
	}

	/**
	 * 
	 */
	private void initializeConsole() {
		final String html = baseHTML.replace("<!--%s-->", "<div id='logbox'></div>");

		console.setText(html);

		doc = (HTMLDocument) console.getDocument();
		logbox = doc.getElement("logbox");
		currentChat = null;
		currentConvo = null;
	}

	public void exit() {
		log.info("Quitting application.");
		System.exit(0);
	}

	public void disconnectStranger(final Map<String, String> args) {
		final int targetIndex = new Integer(args.get("index"));
		disconnectStranger(targetIndex);
	}

	/**
	 * @param targetIndex
	 */
	public void disconnectStranger(final int targetIndex) {
		log.debug("Disconnecting Stranger " + targetIndex + 1);

		// Request disconnect of targeted stranger
		controller.disconnectStranger(targetIndex);
	}

	public void swapStranger(final Map<String, String> args) {
		final int mainIndex = new Integer(args.get("index"));

		if (log.isDebugEnabled()) {
			log.debug("-- SWAP FUNCTION ALL SwapBTN[" + mainIndex + "][KEY_PRESS]");
		}

		// tell user swapiee is being swapped
		printStatusLog(currentConvo, controller.getStrangerName(mainIndex) + " is being swapped");

		controller.swapStranger(mainIndex, new MainWindowOmegleSpyListener() {
			@Override
			protected int getStrangerIndex() {
				return mainIndex;
			}
		});

		// tell user a new chatter connected
		printStatusLog(currentConvo, controller.getStrangerName(mainIndex) + " connected");
	}

	public synchronized void toggleStrangersBlocked(final Map<String, String> args) {
		strangersBlocked = !strangersBlocked;
		log.debug("Toggling stranger block to " + strangersBlocked);
		controller.toggleStrangersBlock(strangersBlocked);
		printStatusLog(currentConvo, "Strangers " + (strangersBlocked ? "blocked" : "unblocked") + ".");
	}

	public synchronized void toggleFilter(final Map<String, String> args) {
		filtered = !filtered;
		log.debug("Toggling filter to " + filtered);
		controller.toggleFilter(filtered);
		printStatusLog(currentConvo, "Filter " + (filtered ? "enabled" : "disabled") + ".");
	}

	public synchronized void toggleConnectionState(final Map<String, String> args) {
		log.debug("Toggle connection state initiated.");
		if (!controller.isConnected()) {
			if (firstRun) {
				console.setText("");
				clearScreen(null);
				initializeConsole();
				firstRun = false;
			}
			convoNum++;

			log.info("-- BEGIN CHAT ( convoNum: " + convoNum
					+ " ) -------------------------------------------------------------------------*********");

			final String chatID = "chat-" + convoNum;
			final String convoID = "convo-" + convoNum;
			printHTML(logbox, "<div id='" + chatID + "'>" + "<div id='" + convoID + "'>" + "</div></div>");
			currentChat = doc.getElement(chatID);
			currentConvo = doc.getElement(convoID);

			printStatusLog(currentConvo, "Finding two strangers...");

			// 2 is the magic number of conversants
			final List<OmegleSpyListener> initialListeners = new ArrayList<OmegleSpyListener>();
			for (int i = 0; i < 2; i++) {
				final int index = i;
				initialListeners.add(new MainWindowOmegleSpyListener() {
					@Override
					protected int getStrangerIndex() {
						return index;
					}
				});
			}

			// CHAT ACTUALLY STARTS HERE
			controller.startConversation(initialListeners);
		} else {
			controller.endConversation();

			log.info("-- CHAT ENDED ( convoNum: " + convoNum + " ) --------------------------------------------");

			printHTML(currentChat, "<div>** Would you like to <a href='#' class='" + BTN_LINK + "' "
					+ "id='save-convo-" + convoNum + "'>" + "save this conversation</a>? **</div>");
			printHTML(currentChat, "<br><hr>");

			currentConvo = null;
			currentChat = null;

			logViewer.addHTML(baseHTML, blocks.get(convoNum));
		}
	}

	public void sendSecretMessage(final Map<String, String> args) {
		final JTextField textField = (JTextField) result.get("txtToStranger");
		if (textField.getText().trim().length() > 0) {
			final int targetIndex = new Integer(args.get("index"));
			try {
				controller.sendSecretMessage(targetIndex, textField.getText());
				textField.setText("");
			} catch (final Exception e) {
				log.warn("Message delivery failure.", e);
			}
		}
	}

	public void clearScreen(final Map<String, String> args) {
		log.debug("Clear screen initiated.");
		if (currentConvo != null) {
			final String convoText = blocks.get(convoNum);
			final String html = baseHTML.replace("<!--%s-->", "<div id='logbox'><div id='chat-" + convoNum + "'>"
					+ "<div id='convo-" + convoNum + "'>" + convoText + "</div></div></div>");
			console.setText(html);
			doc = (HTMLDocument) console.getDocument();
			logbox = doc.getElement("logbox");
			currentChat = doc.getElement("chat-" + convoNum);
			currentConvo = doc.getElement("convo-" + convoNum);
			blocks.clear();
			blocks.put(convoNum, convoText);
		} else {
			final String html = baseHTML.replace("<!--%s-->", "<div id='logbox'></div>");
			console.setText(html);
			doc = (HTMLDocument) console.getDocument();
			logbox = doc.getElement("logbox");
		}
	}

	public void viewLogs(final Map<String, String> args) {
		log.debug("Preparing to display log viewer.");
		logViewer.setVisible(true);
	}

	private void printLabelledMsg(final String className, final String from, String msg) {
		// from = escapeHTML(from);
		msg = StringEscapeUtils.escapeHtml(msg);

		final StringBuffer sb = new StringBuffer();
		final Matcher m = url_regex.matcher(msg);
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
		msg = sb.toString();

		final Element e = currentConvo;
		printLogItem(e, "<span class='" + className + "'>" + from + ":</span> " + msg);
	}

	private void printRegMsg(final OmegleSpy from, final String msg) {
		printLabelledMsg(CLASS_NAMES[controller.indexOf(from)], from.getName(), msg);
	}

	private void printLogItem(final Element e, final String line) {
		final DateFormat timestamp = DateFormat.getTimeInstance(DateFormat.SHORT);
		printHTML(e, "<div class='logitem'>" + "<span class='timestamp'>[" + timestamp.format(new Date()) + "]</span>"
				+ " " + line + "</div>");
	}

	private void printBlockedMsg(final OmegleSpy from, final String msg) {
		final String className = CLASS_NAMES[controller.indexOf(from)] + "-blocked";
		final String fromLbl = "<s>&lt;&lt;" + from.getName() + "&gt;&gt;</s>";
		printLabelledMsg(className, fromLbl, msg);
	}

	private void printSecretMsg(final OmegleSpy to, final String msg) {
		// 2 is the magic number of conversants we have connected
		final int otherIndex = 2 - controller.indexOf(to) - 1;
		final String className = CLASS_NAMES[otherIndex] + "-secret";
		printLabelledMsg(className, "{{from " + controller.getStrangerName(otherIndex) + "}}", msg);
	}

	private void printHTML(final Element e, final String html) {
		try {
			if (e == currentConvo) {
				String record = blocks.get(convoNum);
				record = record == null ? html : record + html;
				blocks.put(convoNum, record);
			}
			doc.insertBeforeEnd(e, html);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private void printStatusLog(final Element e, final String sl) {
		printLogItem(e, "<span class='statuslog'>" + sl + "</span>");
	}

	/**
	 * @return the autoScrollEnabled
	 */
	public boolean isAutoScrollEnabled() {
		return autoScrollEnabled;
	}

	/**
	 * @param autoScrollEnabled
	 *            the autoScrollEnabled to set
	 */
	public void setAutoScrollEnabled(final boolean autoScrollEnabled) {
		this.autoScrollEnabled = autoScrollEnabled;
	}

	public void keyPressed(final KeyEvent e) {
		log.debug("Received " + ShortcutKeyHelper.getShortcutString(e));
		if (shortcutKeyHelper.isShortcutDefined(e)) {
			shortcutKeyHelper.performShortcut(e);
		}
	}

	private abstract class MainWindowOmegleSpyListener implements OmegleSpyListener {
		protected abstract int getStrangerIndex();

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#messageTransferred(org
		 * .darkimport .omeglespy.OmegleSpy, java.lang.String)
		 */
		public void messageTransferred(final OmegleSpy src, final String msg) {
			printRegMsg(src, msg);
			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
					String.valueOf(getStrangerIndex())))).setVisible(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#chatStarted(org.darkimport
		 * .omeglespy.OmegleSpy)
		 */
		public void chatStarted(final OmegleSpy src) {
			final int k = controller.indexOf(src);
			final String n = src.getName();

			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING, String.valueOf(k))))
					.setText(MessageFormat.format(
							SwingJavaBuilder.getConfig().getResource(ResourceConstants.LABEL_STRANGER_TYPING), n));
			printLabelledMsg(CLASS_NAMES[controller.indexOf(src)], "System", src.getName() + " connected");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#disconnected(org.darkimport
		 * .omeglespy.OmegleSpy)
		 */
		public void disconnected(final OmegleSpy src) {
			printStatusLog(currentConvo, src.getName() + " disconnected");
			final int index = controller.indexOf(src);

			// A stranger disconnected, hide his typing label
			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING, String.valueOf(index))))
					.setVisible(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#messageBlocked(org.darkimport
		 * .omeglespy.OmegleSpy, java.lang.String)
		 */
		public void messageBlocked(final OmegleSpy src, final String msg) {
			printBlockedMsg(src, msg);
			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
					String.valueOf(getStrangerIndex())))).setVisible(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#externalMessageSent(org
		 * .darkimport .omeglespy.OmegleSpy, java.lang.String)
		 */
		public void externalMessageSent(final OmegleSpy src, final String msg) {
			printSecretMsg(src, msg);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#isTyping(org.darkimport
		 * .omeglespy .OmegleSpy)
		 */
		public void isTyping(final OmegleSpy src) {
			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
					String.valueOf(getStrangerIndex())))).setVisible(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#stoppedTyping(org.darkimport
		 * .omeglespy.OmegleSpy)
		 */
		public void stoppedTyping(final OmegleSpy src) {
			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
					String.valueOf(getStrangerIndex())))).setVisible(false);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#recaptchaRejected(java
		 * .lang.String)
		 */
		public void recaptchaRejected(final OmegleSpy src, final String id) {
			final OmegleSpyRecaptchaWindow recaptchaWindow = new OmegleSpyRecaptchaWindow(src, id);
			recaptchaWindow.setVisible(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#recaptcha(java.lang.String
		 * )
		 */
		public void recaptcha(final OmegleSpy src, final String id) {
			final OmegleSpyRecaptchaWindow recaptchaWindow = new OmegleSpyRecaptchaWindow(src, id);
			recaptchaWindow.setVisible(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.darkimport.omeglespy.OmegleSpyListener#messageFiltered(org.darkimport
		 * .omeglespy.OmegleSpy, java.lang.String)
		 */
		public void messageFiltered(final OmegleSpy omegleSpy, final String msg) {
			final int targetIndex = controller.indexOf(omegleSpy);
			disconnectStranger(targetIndex);
		}

	}
}
