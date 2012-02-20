package org.darkimport.omeglespy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OmegleSpyPanel extends JPanel implements Runnable, ActionListener, OmegleSpyListener, ComponentListener,
		HyperlinkListener {
	private static final Log	log					= LogFactory.getLog(OmegleSpyPanel.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -555412878022005078L;

	public static final int		REST_TIME			= 80;

	public static final Font	FONT				= new Font("Verdana", Font.PLAIN, 10);
	public static final Font	FONTTYPE			= new Font("Verdana", Font.BOLD, 10);
	public static final Font	FONTHDR				= new Font("Verdana", Font.BOLD, 16);

	public static final Pattern	esc_html_regex		= Pattern.compile("[&<>\"\']");
	public static final Pattern	nl_regex			= Pattern.compile("\\r\\n|\\n\\r|\\n|\\r");
	public static final Pattern	url_regex			= Pattern
															.compile(
																	"([a-z]{2,6}://)?(?:[a-z0-9\\-]+\\.)+[a-z]{2,6}(?::\\d+)?(/\\S*)?",
																	Pattern.CASE_INSENSITIVE);

	// some constants:
	static final String[]		CLASS_NAMES			= { "youmsg", "strangermsg" };
	static final String			BTN_LINK			= "btn-link", CONVO_LINK = "convo-link";
	static final Pattern		delcon_regex		= Pattern.compile("delete-convo-(\\d+)");
	static final Pattern		savcon_regex		= Pattern.compile("save-convo-(\\d+)");
	static final Pattern		convo_regex			= Pattern.compile("convo-(\\d+)");

	JPanel						buttonPanel;
	JLabel						countLabel;
	JButton						reset, stopper, clearScreen, showDw;
	JCheckBox					blocker, linger, autopilot, autoscroll;

	JEditorPane					console;
	HTMLDocument				doc;
	Element						logbox, currentChat, currentConvo;

	private boolean				firstRun			= true;

	private int					convoNum;

	JScrollPane					scroller;
	JScrollBar					vbar;

	DesperationWindow			dw;

	OmegleSpy[]					spies;
	String[]					names;
	JPanel						usersPanel;
	JPanel[]					panels;
	JLabel[]					ls;
	JTextField[]				flds;
	JLabel[]					lbls;
	JButton[]					btns;
	JButton[]					swapbtns;

	String						baseHTML;

	List<HyperlinkListener>		hlListeners			= new LinkedList<HyperlinkListener>();

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ex) {
		}
	}

	private void TriggerTest() {
		log.debug("*** [DBGMODE] ***** [ Trigger START ] **********************");
		log.debug("*** No trigger function programmed. ***");
	}

	public String ts() {
		return "<span class='timestamp'>[" + Common.timestamp() + "]</span>";
	}

	private void printLogItem(final Element e, final String line) {
		printHTML(e, "<div class='logitem'>" + ts() + " " + line + "</div>");
	}

	private void printStatusLog(final Element e, final String sl) {
		printLogItem(e, "<span class='statuslog'>" + sl + "</span>");
	}

	private void printLabelledMsg(final String className, final String from, String msg) {
		// from = escapeHTML(from);
		msg = escapeHTML(msg);

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

	Map<Integer, String>	blocks	= new HashMap<Integer, String>();

	private final boolean	useOcr;

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

	public static String escapeHTML(final String text) {
		if (text == null) {
			return null;
		}
		Matcher m = esc_html_regex.matcher(text);
		final StringBuffer sb = new StringBuffer();
		while (m.find()) {
			final char c = m.group().charAt(0);
			String replace;
			switch (m.group().charAt(0)) {
				case '&':
					replace = "amp";
					break;
				case '<':
					replace = "lt";
					break;
				case '>':
					replace = "gt";
					break;
				case '"':
					replace = "quot";
					break;

				default:
					replace = "#" + (int) c;
					break;
			}
			m.appendReplacement(sb, "&" + replace + ";");
		}
		m.appendTail(sb);

		m = nl_regex.matcher(sb);
		return m.replaceAll("$0<br>");
	}

	public void hyperlinkUpdate(final HyperlinkEvent ev) {
		if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				final Element e = ev.getSourceElement();
				final HTMLDocument.RunElement re = (HTMLDocument.RunElement) e;
				final AttributeSet atts = (AttributeSet) re.getAttributes().getAttribute(HTML.Tag.A);
				final String className = (String) atts.getAttribute(HTML.Attribute.CLASS);
				if (className.equals(BTN_LINK)) {
					final String id = (String) atts.getAttribute(HTML.Attribute.ID);
					Matcher m;
					if ((m = savcon_regex.matcher(id)).matches()) {
						final int ci = Integer.parseInt(m.group(1));
						final String ct = baseHTML.replace("<!--%s-->", blocks.get(ci));
						Common.guiWriteHtmlFile(ct, this);
					}
					return;
				}
			} catch (final ClassCastException ex) {
				// then i want to keep going
			} catch (final NullPointerException ex) {
				// then i want to keep going
			} catch (final IOException ex) {
				Common.showError(this, "Could not save file: " + ex.getMessage());
				return;
			}
		}

		for (final HyperlinkListener hl : hlListeners) {
			hl.hyperlinkUpdate(ev);
		}
	}

	public void addHyperlinkListener(final HyperlinkListener l) {
		hlListeners.add(l);
	}

	public void removeHyperlinkListener(final HyperlinkListener l) {
		hlListeners.remove(l);
	}

	public void clearScreen() {
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

	public void actionPerformed(final ActionEvent ev) {
		final Object src = ev.getSource();
		if (src == flds[0] || src == flds[1]) {
			final JTextField fld = (JTextField) src;
			final int index = (fld == flds[0]) ? 0 : 1;
			if (fld.getText().length() > 0) {
				final OmegleSpy spy = spies[index];
				if (spy.sendExternalMessage(fld.getText())) {
					fld.setText("");
				}
			}
		} else if (src == btns[0] || src == btns[1]) {
			final JButton btn = (JButton) src;
			final int index = (btn == btns[0]) ? 0 : 1;
			final OmegleSpy otherSpy = spies[spies.length - index - 1];
			if (otherSpy != null) {
				linger.setSelected(true);
			}
			final OmegleSpy spy = spies[index];
			spy.disconnect();
		} else if (src == swapbtns[0] || src == swapbtns[1]) {
			final JButton sbtn = (JButton) src;
			final int mainIndex = (sbtn == swapbtns[0]) ? 0 : 1;
			final int otherIndex = (sbtn == swapbtns[0]) ? 1 : 0;

			if (log.isDebugEnabled()) {
				log.debug(" -- SWAP FUNCTION ALL SwapBTN[" + mainIndex + "][KEY_PRESS]");
			}

			if (currentConvo != null && spies[otherIndex] != null && spies[mainIndex] != null) {
				final String OldName = spies[mainIndex].getName(); // grab
																	// swappiees
																	// name
				printStatusLog(currentConvo, OldName + " is being swapped"); // tell
																				// user
																				// swapiee
																				// is
																				// being
																				// swapped
				final OmegleSpy oldSpy = spies[mainIndex];
				final OmegleSpy mainSpy = spies[mainIndex] = new OmegleSpy(OldName, useOcr); // create
				// a
				// new
				// chatter
				// spy
				// with
				// the
				// swappiees
				// old
				// name
				oldSpy.disconnect();
				mainSpy.addOmegleSpyListener(this); // setup the new chatter spy
													// as a event listener
				mainSpy.setPartner(spies[otherIndex].chat); // tell new chatter
															// spy to talk to
															// the other chatter
															// (non touched
															// chatter)
				spies[otherIndex].setPartner(mainSpy.chat); // do i need this?
															// //tell the other
															// chatter (non
															// touched chatter)
															// to talk to the
															// new chatter
				mainSpy.startChat(); // connect the new chatter into the chat
				printStatusLog(currentConvo, mainSpy.name + " connected"); // tell
																			// user
																			// a
																			// new
																			// chatter
																			// connected
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
		} else if (src == reset) {
			if (firstRun) {
				reset.setText("Initializing, Please wait...");
				// Omegle.init();
				reset.setText("Start new conversation");
				console.setText("");
				clearScreen();
				firstRun = false;
			} else {
				new Thread(this).start();
			}
		} else if (src == stopper) {
			for (final OmegleSpy s : spies) {
				if (s != null) {
					s.disconnect();
				}
			}
		} else if (src == blocker) {
			for (final OmegleSpy s : spies) {
				if (s != null) {
					s.setBlocking(blocker.isSelected());
				}
			}
		} else if (src == clearScreen) {
			clearScreen();
		} else if (src == showDw) {
			if (!log.isDebugEnabled()) {
				dw.setVisible(true);
			} else {
				TriggerTest();
			}
		}
	}

	public void componentHidden(final ComponentEvent ev) {
	}

	public void componentShown(final ComponentEvent ev) {
	}

	public void componentMoved(final ComponentEvent ev) {
	}

	public void componentResized(final ComponentEvent ev) {
		if (autoscroll.isSelected()) {
			vbar.setValue(vbar.getMaximum() - vbar.getVisibleAmount());
		}
	}

	private int indexOf(final OmegleSpy spy) {
		return spy == spies[0] ? 0 : 1;
	}

	public void run() {
		buttonPanel.removeAll();
		buttonPanel.add(stopper);
		stopper.setEnabled(false);

		validate();
		buttonPanel.repaint();

		while (true) {
			randomizeNames();
			for (int k = 0; k < spies.length; k++) {
				final String n = names[k], on = names[names.length - k - 1];
				final OmegleSpy spy = spies[k] = new OmegleSpy(n, useOcr);
				ls[k].setText("To " + n + "; From " + on);
				btns[k].setText("Disconnect " + n);
				swapbtns[k].setEnabled(true);
				swapbtns[k].setText("Swap " + n);
				spy.addOmegleSpyListener(this);
			}

			spies[0].setPartner(spies[1].chat);
			spies[1].setPartner(spies[0].chat);

			convoNum++;

			log.info("-- BEGIN CHAT ( convoNum: " + convoNum
					+ " ) -------------------------------------------------------------------------*********");
			log.info("SPY CREATED : spy[0]	[ID: " + spies[0].chat + "]		[NAME: '" + names[0] + "']");
			log.info("SPY CREATED : spy[1]	[ID: " + spies[1].chat + "]		[NAME: '" + names[1] + "']");

			final String chatID = "chat-" + convoNum;
			final String convoID = "convo-" + convoNum;
			printHTML(logbox, "<div id='" + chatID + "'>" + "<div id='" + convoID + "'>" + "</div></div>");
			currentChat = doc.getElement(chatID);
			currentConvo = doc.getElement(convoID);

			printStatusLog(currentConvo, "Finding two strangers...");
			for (final OmegleSpy spy : spies) {
				spy.startChat();
				printStatusLog(currentConvo, spy.name + " connected");
			}

			// CHAT ACTUALLY STARTS HERE
			for (final OmegleSpy s : spies) {
				s.setBlocking(blocker.isSelected());
			}

			usersPanel.setVisible(true);
			stopper.setEnabled(true);

			validate();
			buttonPanel.repaint();

			int index = -1;
			firstDisc: while (true) {
				for (int k = 0; k < spies.length; k++) {
					if (spies[k].disconnected) {
						index = k;
						break firstDisc;
					}
				}
				Common.rest(REST_TIME);
			}
			printStatusLog(currentConvo, spies[index].name + " disconnected");
			lbls[index].setText(" ");
			spies[index] = null;

			usersPanel.remove(panels[index]);
			validate();

			final int otherIndex = spies.length - index - 1;

			swapbtns[otherIndex].setEnabled(false);

			final OmegleSpy other = spies[otherIndex];
			if (linger.isSelected()) {
				while (!other.disconnected) {
					Common.rest(REST_TIME);
				}
			} else {
				other.disconnect();
			}
			printStatusLog(currentConvo, other.name + " disconnected");

			usersPanel.add(panels[index], index);

			lbls[otherIndex].setText(" ");
			spies[otherIndex] = null;

			log.info("-- CHAT ENDED ( convoNum: " + convoNum + " ) --------------------------------------------");

			stopper.setEnabled(false);

			usersPanel.setVisible(false);
			validate();
			buttonPanel.repaint();

			printHTML(currentChat, "<div>** Would you like to <a href='#' class='" + BTN_LINK + "' "
					+ "id='save-convo-" + convoNum + "'>" + "save this conversation</a>? **</div>");
			printHTML(currentChat, "<br><hr>");

			currentConvo = null;
			currentChat = null;

			dw.addHTML(baseHTML, blocks.get(convoNum));

			if (!autopilot.isSelected()) {
				break;
			}
		}

		buttonPanel.removeAll();
		buttonPanel.add(reset);
	}

	public void messageTransferred(final OmegleSpy src, final String msg) {
		printRegMsg(src, msg);
		final int index = indexOf(src);
		lbls[index].setText(" ");
	}

	public void messageBlocked(final OmegleSpy src, final String msg) {
		printBlockedMsg(src, msg);
		final int index = indexOf(src);
		lbls[index].setText(" ");
	}

	public void externalMessageSent(final OmegleSpy src, final String msg) {
		printSecretMsg(src, msg);
	}

	public void isTyping(final OmegleSpy src) {
		lbls[indexOf(src)].setText(src.name + " is typing...");
	}

	public void stoppedTyping(final OmegleSpy src) {
		lbls[indexOf(src)].setText(" ");
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

	public OmegleSpyPanel(final boolean useOcr) {
		this.useOcr = useOcr;
		setLayout(new BorderLayout());

		convoNum = 0;

		reset = new JButton("Initialize OmegleSpyX");
		reset.setFont(FONT);
		stopper = new JButton("Disconnect conversation");
		stopper.setFont(FONT);
		stopper.setEnabled(false);

		// Setup Buttons and so fourth

		countLabel = new JLabel(Common.APP_HEADER);
		countLabel.setFont(FONTHDR);

		blocker = new JCheckBox("Block message");
		blocker.setFont(FONT);
		// blocker.setSelected(true);

		linger = new JCheckBox("Linger");
		linger.setFont(FONT);
		// linger.setSelected(true);

		autopilot = new JCheckBox("Auto Reconnect");
		autopilot.setFont(FONT);
		autopilot.setSelected(true);

		autoscroll = new JCheckBox("Auto Scroll");
		autoscroll.setFont(FONT);
		autoscroll.setSelected(true);

		showDw = new JButton("Session Logs");
		if (log.isDebugEnabled()) {
			showDw.setText("TriggerTest");
		}
		showDw.setFont(FONT);
		showDw.addActionListener(this);

		try {
			baseHTML = IOUtils.toString(getClass().getResourceAsStream("/base.html"));
		} catch (final IOException ex) {
			ex.printStackTrace();
			System.exit(1);
			// stupid java:
			baseHTML = "";
		}
		final String html = baseHTML.replace("<!--%s-->", "<div id='logbox'></div>");
		console = new JEditorPane("text/html", html);
		console.addComponentListener(this);
		console.addHyperlinkListener(this);
		doc = (HTMLDocument) console.getDocument();
		logbox = doc.getElement("logbox");
		currentChat = null;
		currentConvo = null;
		console.setEditable(false);
		scroller = new JScrollPane(console);
		vbar = scroller.getVerticalScrollBar();
		vbar.setUnitIncrement(16);
		dw = new DesperationWindow();
		dw.setSize(640, 480);
		dw.setLocation(100, 100);

		spies = new OmegleSpy[2];
		names = new String[] { "Brent", "Melissa" };
		usersPanel = new JPanel(new GridLayout(1, 2));
		usersPanel.setVisible(false);
		panels = new JPanel[2];
		ls = new JLabel[2];
		flds = new JTextField[2];
		lbls = new JLabel[2];
		btns = new JButton[2];
		swapbtns = new JButton[2];

		buttonPanel = new JPanel(new BorderLayout());

		for (int k = 0; k < panels.length; k++) {
			final JTextField tf = flds[k] = new JTextField();
			tf.setFont(FONT);
			tf.setActionCommand("" + k);
			tf.addActionListener(this);

			final JLabel toLabel = ls[k] = new JLabel(" ");
			toLabel.setFont(FONT);
			toLabel.setForeground(Color.GRAY);
			final JPanel toPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			toPanel.add(toLabel);

			final JLabel label = lbls[k] = new JLabel(" ");
			label.setFont(FONTTYPE);
			final JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			typePanel.add(label);

			final JButton b = btns[k] = new JButton();
			b.setFont(FONT);
			b.setActionCommand("" + k);
			b.addActionListener(this);

			final JButton sb = swapbtns[k] = new JButton();
			sb.setFont(FONT);
			sb.setActionCommand("" + k);
			sb.addActionListener(this);

			final JPanel p = panels[k] = new JPanel(new GridLayout(4, 1));
			p.add(tf);
			p.add(toPanel);
			p.add(typePanel);
			p.add(b);
			p.add(sb);

			usersPanel.add(p);
		}

		reset.addActionListener(this);
		stopper.addActionListener(this);
		blocker.addActionListener(this);

		buttonPanel.add(reset);

		clearScreen = new JButton("Clear screen");
		clearScreen.setFont(FONT);
		clearScreen.addActionListener(this);

		final JPanel settingsPanel = new JPanel(new FlowLayout());
		settingsPanel.add(blocker);
		settingsPanel.add(linger);
		settingsPanel.add(autopilot);
		settingsPanel.add(clearScreen);
		settingsPanel.add(showDw);
		settingsPanel.add(autoscroll);
		final JPanel headerPanel = new JPanel(new FlowLayout());
		headerPanel.add(countLabel);
		final JPanel northPanel = new JPanel(new GridLayout(3, 1));
		northPanel.add(headerPanel);
		northPanel.add(settingsPanel);
		northPanel.add(buttonPanel);

		add(northPanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);
		add(usersPanel, BorderLayout.SOUTH);

		try {
			console.setText(IOUtils.toString(getClass().getResourceAsStream("/start.html")));
		} catch (final IOException ex) {
			ex.printStackTrace();
			System.exit(1);
			// stupid java:
			baseHTML = "";
		}
	}
}
