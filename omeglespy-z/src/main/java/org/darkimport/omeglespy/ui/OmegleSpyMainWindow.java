/**
 * 
 */
package org.darkimport.omeglespy.ui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy.Common;
import org.darkimport.omeglespy.DesperationWindow;
import org.darkimport.omeglespy.OmegleSpy;
import org.darkimport.omeglespy.OmegleSpyListener;
import org.darkimport.omeglespy.constants.ControlNameConstants;
import org.darkimport.omeglespy.constants.ResourceConstants;
import org.darkimport.util.LogHelper;
import org.darkimport.util.NativeHelper;
import org.javabuilders.BuildResult;
import org.javabuilders.annotations.DoInBackground;
import org.javabuilders.event.BackgroundEvent;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class OmegleSpyMainWindow extends JFrame implements Runnable {
	public static final String			CBX_LINGER			= "cbxLinger";

	private static final Log			log					= LogFactory.getLog(OmegleSpyMainWindow.class);

	/**
	 * 
	 */
	private static final long			serialVersionUID	= 9092938195254654975L;

	private static final String			TESSDLL_DLL			= "tessdll.dll";

	public static final int				REST_TIME			= 80;

	private final BuildResult			result;

	private final boolean				useOcr;

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

	private static final String			CBX_BLOCKMESSAGE	= "cbxBlockMessage";

	private final OmegleSpy[]			spies				= new OmegleSpy[2];								;
	private final String[]				names				= new String[] { "Brent", "Melissa" };

	private boolean						autoScrollEnabled	= true;
	private final JScrollBar			autoScrollBar;

	private boolean						autoReconnect;

	private boolean						linger;

	private boolean						firstRun			= true;

	private final DesperationWindow		logViewer			= new DesperationWindow();

	public OmegleSpyMainWindow(final boolean useOcr) {
		this.useOcr = useOcr;

		result = SwingJavaBuilder.build(this);

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
								Common.openURL(ev.getURL().toString());
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

	public void disconnectStranger(final JButton button) {
		log.debug("Disconnect stranger initiated by " + button.getName());
		final int targetIndex = new Integer(button.getName().substring(button.getName().length() - 1));
		log.debug("Disconnecting Stranger " + targetIndex + 1);
		final int otherIndex = spies.length - targetIndex - 1;
		final OmegleSpy otherSpy = spies[otherIndex];
		if (otherSpy != null && !linger) {
			log.debug("The other stranger is still connected. Setting mode to linger.");
			linger = true;
			((JCheckBox) result.get(CBX_LINGER)).setSelected(true);
			final String swapButtonName = MessageFormat.format(ControlNameConstants.BTN_SWAP_STRANGER,
					String.valueOf(targetIndex));
			((JButton) result.get(swapButtonName)).setText(SwingJavaBuilder.getConfig().getResource(
					ResourceConstants.BUTTON_SWAP_STRANGER_NEUTRAL));
			final String targetedDisconnectButtonName = MessageFormat.format(
					ControlNameConstants.BTN_DISCONNECT_STRANGER, String.valueOf(targetIndex));
			final JButton targetedDisconnectButton = (JButton) result.get(targetedDisconnectButtonName);
			targetedDisconnectButton.setText(SwingJavaBuilder.getConfig().getResource(
					ResourceConstants.BUTTON_DISCONNECT_STRANGER_NEUTRAL));
			targetedDisconnectButton.setEnabled(false);
		}
		final OmegleSpy spy = spies[targetIndex];
		spy.disconnect();
	}

	@DoInBackground(cancelable = false, indeterminateProgress = true, blocking = false)
	public void swapStranger(final BackgroundEvent evt) {
		final JButton button = (JButton) evt.getSource();
		log.debug("Swap stranger initiated by " + button.getName());
		final int mainIndex = new Integer(button.getName().substring(button.getName().length() - 1));
		final int otherIndex = mainIndex == 0 ? 1 : 0;

		if (log.isDebugEnabled()) {
			log.debug(" -- SWAP FUNCTION ALL SwapBTN[" + mainIndex + "][KEY_PRESS]");
		}

		if (currentConvo != null && spies[otherIndex] != null && spies[mainIndex] != null) {
			// grab swappiees name
			final String OldName = spies[mainIndex].getName();

			// tell user swapiee is being swapped
			printStatusLog(currentConvo, OldName + " is being swapped");
			final OmegleSpy oldSpy = spies[mainIndex];

			// create a new chatter spy with the swappiees old name
			final OmegleSpy mainSpy = spies[mainIndex] = new OmegleSpy(OldName, useOcr);
			oldSpy.disconnect();

			// setup the new chatter spy as a event listener
			mainSpy.addOmegleSpyListener(new OmegleSpyListener() {

				public void stoppedTyping(final OmegleSpy src) {
					((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
							String.valueOf(mainIndex)))).setVisible(false);
				}

				public void messageTransferred(final OmegleSpy src, final String msg) {
					printRegMsg(src, msg);
					((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
							String.valueOf(mainIndex)))).setVisible(false);
				}

				public void messageBlocked(final OmegleSpy src, final String msg) {
					printBlockedMsg(src, msg);
					((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
							String.valueOf(mainIndex)))).setVisible(false);
				}

				public void isTyping(final OmegleSpy src) {
					((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
							String.valueOf(mainIndex)))).setVisible(true);
				}

				public void externalMessageSent(final OmegleSpy src, final String msg) {
					printSecretMsg(src, msg);
				}
			});

			// tell new chatter spy to talk to the other chatter (non touched
			// chatter)
			mainSpy.setPartner(spies[otherIndex].getChat());

			// do i need this? tell the other chatter (non touched chatter) to
			// talk to the new chatter
			spies[otherIndex].setPartner(mainSpy.getChat());

			// connect the new chatter into the chat
			mainSpy.startChat();

			// Enable his controls
			((JTextField) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER,
					String.valueOf(mainIndex)))).setEnabled(true);
			final JButton targetedDisconnectButton = (JButton) result.get(MessageFormat.format(
					ControlNameConstants.BTN_DISCONNECT_STRANGER, String.valueOf(mainIndex)));
			targetedDisconnectButton.setText(MessageFormat.format(
					SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_DISCONNECT_STRANGER),
					mainSpy.getName()));
			targetedDisconnectButton.setEnabled(true);
			final JButton swapButton = (JButton) result.get(MessageFormat.format(
					ControlNameConstants.BTN_SWAP_STRANGER, String.valueOf(mainIndex)));
			swapButton
					.setText(MessageFormat.format(
							SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_SWAP_STRANGER),
							mainSpy.getName()));
			swapButton.setEnabled(true);

			// tell user a new chatter connected
			printStatusLog(currentConvo, mainSpy.getName() + " connected");
		} else {
			if (log.isDebugEnabled()) {
				log.debug("*** Swap button " + mainIndex + " pressed but currently not in a chat. SWAP DISABLED...");
			}
			Common.showError(
					this,
					"Notice. SWAP is disabled.\n\r\n\r"
							+ "The SWAP function is only available during a chat with two people.\n\r\n\r"
							+ "If you are not in a chat or in a chat but in linger mode with only one chatter, then the swap capabilities are disabled.");
		}
	}

	public void toggleStrangersBlocked(final JCheckBox button) {
		log.debug("Toggling stranger block to " + button.isSelected());
		for (final OmegleSpy s : spies) {
			if (s != null) {
				s.setBlocking(button.isSelected());
			}
		}
	}

	public void toggleConnectionState(final JButton button) {
		log.debug("Toggle connection state initiated.");
		if (button.getText().equals(SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_CONNECT))) {
			if (firstRun) {
				console.setText("");
				clearScreen();
				initializeConsole();
				firstRun = false;
			}
			new Thread(this).start();
			button.setText(SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_DISCONNECT));
		} else {
			for (final OmegleSpy s : spies) {
				if (s != null) {
					s.disconnect();
					final String swapButtonName = MessageFormat.format(ControlNameConstants.BTN_SWAP_STRANGER,
							String.valueOf(indexOf(s)));
					final JButton swapButton = (JButton) result.get(swapButtonName);
					swapButton.setText(SwingJavaBuilder.getConfig().getResource(
							ResourceConstants.BUTTON_SWAP_STRANGER_NEUTRAL));
					swapButton.setEnabled(false);
					final String targetedDisconnectButtonName = MessageFormat.format(
							ControlNameConstants.BTN_DISCONNECT_STRANGER, String.valueOf(indexOf(s)));
					final JButton targetedDisconnectButton = (JButton) result.get(targetedDisconnectButtonName);
					targetedDisconnectButton.setText(SwingJavaBuilder.getConfig().getResource(
							ResourceConstants.BUTTON_DISCONNECT_STRANGER_NEUTRAL));
					targetedDisconnectButton.setEnabled(false);
				}
			}

			button.setText(SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_CONNECT));
		}
	}

	public void sendSecretMessage(final JTextField textField) {
		log.debug("Send secret message action generated by " + textField.getName());
		final int targetIndex = new Integer(textField.getName().substring(textField.getName().length() - 1));
		if (textField.getText().length() > 0) {
			final OmegleSpy spy = spies[targetIndex];
			if (spy.sendExternalMessage(textField.getText())) {
				textField.setText("");
			}
		}
	}

	public void clearScreen() {
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

	public void viewLogs() {
		log.debug("Preparing to display log viewer.");
		logViewer.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final boolean useOcr = readyOcr();

		log.info("**** Welcome to OmegleSpy-Z [v" + Common.APP_FULLVER + "] EP33N (by darkimport) ****\n\r");
		if (log.isDebugEnabled()) {
			log.debug("> Running in DEVELOPMENT release mode. Debug mode is [ON] & Private functions are [ENABLED]");
		} else {
			log.info("> Running in Standard release mode. Debug mode is [OFF] & Private functions are [DISABLED]");
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// activate internationalization
				SwingJavaBuilder.getConfig().addResourceBundle("OmegleSpyMainWindow");
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					new OmegleSpyMainWindow(useOcr).setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * @return
	 */
	private static boolean readyOcr() {
		boolean success = true;
		try {
			final String currentDirectory = FilenameUtils.normalize(new File(".").getAbsolutePath());
			final String tessDllPath = FilenameUtils.concat(currentDirectory, TESSDLL_DLL);
			NativeHelper.addDir(currentDirectory);
			NativeHelper.extractNative(tessDllPath, TESSDLL_DLL);
			System.load(tessDllPath);
		} catch (final Throwable e) {
			log.warn("Failed to load ocr libs. Will proceed without OCR.", e);
			success = false;
		}
		return success;
	}

	// Controls the chat. When the thread exits, the chat is over.
	// TODO This thread should just be a minder thread. Various signals can be
	// passed to it to tell it to disconnect a stranger, swap a stranger, etc.
	// TODO Also, the conversation should only be considered to be disconnected
	// when the user explicitly presses the disconnect button. In that way, the
	// conversation can sit in an idle state even when there are no active
	// participants.
	public void run() {
		while (true) {
			randomizeNames();
			final List<Integer> spyIndexes = new ArrayList<Integer>();
			for (int i = 0; i < spies.length; i++) {
				spyIndexes.add(i);
			}
			for (final int k : spyIndexes) {
				final String n = names[k];
				final OmegleSpy spy = spies[k] = new OmegleSpy(n, useOcr);
				((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING, String.valueOf(k))))
						.setText(MessageFormat.format(
								SwingJavaBuilder.getConfig().getResource(ResourceConstants.LABEL_STRANGER_TYPING), n));
				final JButton targetedDisconnectButton = (JButton) result.get(MessageFormat.format(
						ControlNameConstants.BTN_DISCONNECT_STRANGER, String.valueOf(k)));
				targetedDisconnectButton.setText(MessageFormat.format(
						SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_DISCONNECT_STRANGER), n));
				targetedDisconnectButton.setEnabled(true);
				final JButton swapButton = (JButton) result.get(MessageFormat.format(
						ControlNameConstants.BTN_SWAP_STRANGER, String.valueOf(k)));
				swapButton.setText(MessageFormat.format(
						SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_SWAP_STRANGER), n));
				swapButton.setEnabled(true);
				((JTextField) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, String.valueOf(k))))
						.setEnabled(true);
				spy.addOmegleSpyListener(new OmegleSpyListener() {

					public void stoppedTyping(final OmegleSpy src) {
						((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
								String.valueOf(k)))).setVisible(false);
					}

					public void messageTransferred(final OmegleSpy src, final String msg) {
						printRegMsg(src, msg);
						((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
								String.valueOf(k)))).setVisible(false);
					}

					public void messageBlocked(final OmegleSpy src, final String msg) {
						printBlockedMsg(src, msg);
						((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
								String.valueOf(k)))).setVisible(false);
					}

					public void isTyping(final OmegleSpy src) {
						((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
								String.valueOf(k)))).setVisible(true);
					}

					public void externalMessageSent(final OmegleSpy src, final String msg) {
						printSecretMsg(src, msg);
					}
				});
			}

			spies[0].setPartner(spies[1].getChat());
			spies[1].setPartner(spies[0].getChat());

			convoNum++;

			log.info("-- BEGIN CHAT ( convoNum: " + convoNum
					+ " ) -------------------------------------------------------------------------*********");
			for (int i = 0; i < spies.length; i++) {
				log.info("SPY CREATED : spy[" + i + "]	[ID: " + spies[i].getChat() + "]		[NAME: '" + names[i] + "']");
			}

			final String chatID = "chat-" + convoNum;
			final String convoID = "convo-" + convoNum;
			printHTML(logbox, "<div id='" + chatID + "'>" + "<div id='" + convoID + "'>" + "</div></div>");
			currentChat = doc.getElement(chatID);
			currentConvo = doc.getElement(convoID);

			printStatusLog(currentConvo, "Finding two strangers...");
			for (final OmegleSpy spy : spies) {
				spy.startChat();
				printLabelledMsg(CLASS_NAMES[indexOf(spy)], "System", spy.getName() + " connected");
			}

			// CHAT ACTUALLY STARTS HERE
			for (final OmegleSpy s : spies) {
				s.setBlocking(((JCheckBox) result.get(CBX_BLOCKMESSAGE)).isSelected());
			}

			int index = -1;
			firstDisc: while (true) {
				for (int k = 0; k < spies.length; k++) {
					if (spies[k].isDisconnected()) {
						index = k;
						break firstDisc;
					}
				}
				Common.rest(REST_TIME);
			}
			printStatusLog(currentConvo, spies[index].getName() + " disconnected");

			// A stranger disconnected, hide his typing label
			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING, String.valueOf(index))))
					.setVisible(false);
			// disable his swap button and set the text of his button to
			// neutral.
			String swapButtonName = MessageFormat.format(ControlNameConstants.BTN_SWAP_STRANGER, String.valueOf(index));
			JButton swapButton = (JButton) result.get(swapButtonName);
			swapButton
					.setText(SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_SWAP_STRANGER_NEUTRAL));
			swapButton.setEnabled(false);
			// disable his disconnect button and set the text of his disconnect
			// button to neutral.
			String targetedDisconnectButtonName = MessageFormat.format(ControlNameConstants.BTN_DISCONNECT_STRANGER,
					String.valueOf(index));
			JButton targetedDisconnectButton = (JButton) result.get(targetedDisconnectButtonName);
			targetedDisconnectButton.setText(SwingJavaBuilder.getConfig().getResource(
					ResourceConstants.BUTTON_DISCONNECT_STRANGER_NEUTRAL));
			targetedDisconnectButton.setEnabled(false);
			// disable his txtbox.
			((JTextField) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, String.valueOf(index))))
					.setEnabled(false);
			spies[index] = null;

			final int otherIndex = spies.length - index - 1;

			final OmegleSpy other = spies[otherIndex];
			if (linger) {
				while (!other.isDisconnected()) {
					Common.rest(REST_TIME);
				}
			} else {
				other.disconnect();
			}
			printStatusLog(currentConvo, other.getName() + " disconnected");

			// Other stranger disconnected, hide his typing label
			((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
					String.valueOf(otherIndex)))).setVisible(false);
			// set the text of his button to neutral.
			swapButtonName = MessageFormat.format(ControlNameConstants.BTN_SWAP_STRANGER, String.valueOf(otherIndex));
			swapButton = (JButton) result.get(swapButtonName);
			swapButton
					.setText(SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_SWAP_STRANGER_NEUTRAL));
			swapButton.setEnabled(false);
			// disable his disconnect button and set the text of his disconnect
			// button to neutral.
			targetedDisconnectButtonName = MessageFormat.format(ControlNameConstants.BTN_DISCONNECT_STRANGER,
					String.valueOf(otherIndex));
			targetedDisconnectButton = (JButton) result.get(targetedDisconnectButtonName);
			targetedDisconnectButton.setText(SwingJavaBuilder.getConfig().getResource(
					ResourceConstants.BUTTON_DISCONNECT_STRANGER_NEUTRAL));
			targetedDisconnectButton.setEnabled(false);
			// disable his txtbox.
			((JTextField) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER,
					String.valueOf(otherIndex)))).setEnabled(false);
			spies[otherIndex] = null;

			log.info("-- CHAT ENDED ( convoNum: " + convoNum + " ) --------------------------------------------");

			((JButton) result.get(ControlNameConstants.BTN_TOGGLE_CONNECTION)).setText(SwingJavaBuilder.getConfig()
					.getResource(ResourceConstants.BUTTON_CONNECT));

			printHTML(currentChat, "<div>** Would you like to <a href='#' class='" + BTN_LINK + "' "
					+ "id='save-convo-" + convoNum + "'>" + "save this conversation</a>? **</div>");
			printHTML(currentChat, "<br><hr>");

			currentConvo = null;
			currentChat = null;

			logViewer.addHTML(baseHTML, blocks.get(convoNum));

			if (!autoReconnect) {
				break;
			}
		}
	}

	private boolean randomizeNames() {
		final int firstIndex = (int) (Math.random() * Common.possibleNames.length);
		int secondIndex;
		do {
			secondIndex = (int) (Math.random() * Common.possibleNames.length);
		} while (firstIndex == secondIndex);
		names[0] = Common.possibleNames[firstIndex];
		names[1] = Common.possibleNames[secondIndex];
		return true;
	}

	private void printLabelledMsg(final String className, final String from, String msg) {
		// from = escapeHTML(from);
		msg = StringEscapeUtils.escapeHtml(msg);

		final StringBuffer sb = new StringBuffer();
		final Matcher m = url_regex.matcher(msg);
		while (m.find()) {
			String rep;
			if (m.group(1) != null || m.group(2) != null) {
				final String proto = (m.group(1) == null) ? "http://" : "";
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
		printLabelledMsg(CLASS_NAMES[indexOf(from)], from.getName(), msg);
	}

	private void printLogItem(final Element e, final String line) {
		printHTML(e, "<div class='logitem'>" + "<span class='timestamp'>[" + Common.timestamp() + "]</span>" + " "
				+ line + "</div>");
	}

	private void printBlockedMsg(final OmegleSpy from, final String msg) {
		final String className = CLASS_NAMES[indexOf(from)] + "-blocked";
		final String fromLbl = "<s>&lt;&lt;" + from.getName() + "&gt;&gt;</s>";
		printLabelledMsg(className, fromLbl, msg);
	}

	private void printSecretMsg(final OmegleSpy to, final String msg) {
		final int otherIndex = spies.length - indexOf(to) - 1;
		final String className = CLASS_NAMES[otherIndex] + "-secret";
		printLabelledMsg(className, "{{from " + names[otherIndex] + "}}", msg);
	}

	private void printHTML(final Element e, final String html) {
		try {
			if (e == currentConvo) {
				String record = blocks.get(convoNum);
				record = (record == null) ? html : record + html;
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

	private int indexOf(final OmegleSpy spy) {
		return spy == spies[0] ? 0 : 1;
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

	/**
	 * @return the autoReconnect
	 */
	public boolean isAutoReconnect() {
		return autoReconnect;
	}

	/**
	 * @param autoReconnect
	 *            the autoReconnect to set
	 */
	public void setAutoReconnect(final boolean autoReconnect) {
		this.autoReconnect = autoReconnect;
	}

	/**
	 * @return the linger
	 */
	public boolean isLinger() {
		return linger;
	}

	/**
	 * @param linger
	 *            the linger to set
	 */
	public void setLinger(final boolean linger) {
		this.linger = linger;
	}
}
