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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.config.ConfigHelper;
import org.darkimport.omeglespy_z.ChatHistoryHelper;
import org.darkimport.omeglespy_z.CommonsLoggingLogHelper;
import org.darkimport.omeglespy_z.CommunicationHelper;
import org.darkimport.omeglespy_z.DefaultCommunicationHelper;
import org.darkimport.omeglespy_z.LogHelper;
import org.darkimport.omeglespy_z.mediation.OmegleSpyConversationController;
import org.darkimport.omeglespy_z.mediation.OmegleSpyConversationListener;
import org.darkimport.omeglespy_z.mediation.OmegleSpyEvent;
import org.darkimport.omeglespy_z_desktop.constants.ConfigConstants;
import org.darkimport.omeglespy_z_desktop.constants.ControlNameConstants;
import org.darkimport.omeglespy_z_desktop.constants.ResourceConstants;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingAction;
import org.javabuilders.swing.SwingJavaBuilder;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

/**
 * @author user
 * 
 */
public class OmegleSpyZMainWindow extends JFrame implements OmegleSpyConversationListener {
	private static final Log						log							= LogFactory
																						.getLog(OmegleSpyZMainWindow.class);

	/**
	 * 
	 */
	private static final long						serialVersionUID			= 4484015747081915874L;

	private static final String[]					STYLE_NAMES					= new String[] { "stranger1",
			"stranger2"														};
	private static final String						BTN_LINK					= "btn-link";

	private static final String						BLOCKED_STYLE				= "{0}-blocked";
	private static final String						SECRET_STYLE				= "{0}-secret";

	private static final String						LOGBOX						= "logbox";

	private final BuildResult						result;
	private CardLayout								secretMessageCards;
	private JPanel									grpSecretMessagePane;
	private String									expertModeBuffer;

	private final Map<String, JLabel>				strangerTypingControls		= new HashMap<String, JLabel>();
	private final Map<String, JTextField>			strangerTextFields			= new HashMap<String, JTextField>();
	private final Map<String, JButton>				strangerSwapButtons			= new HashMap<String, JButton>();
	private final HashMap<String, JButton>			strangerDisconnectButtons	= new HashMap<String, JButton>();

	private final JFrame							helpWindow;

	IBindingFactory									bindingFactory;
	IUnmarshallingContext							unmarshallingContext;
	private ShortcutKeyHelper						shortcutKeyHelper;

	private final OmegleSpyConversationController	controller;

	private String[]								conversantNames;

	private JEditorPane								console;
	private boolean									autoScrollEnabled			= true;
	private final HtmlChatHistoryHelper				chatHistoryHelper;
	private final OldLogViewerWindow				logViewer					= new OldLogViewerWindow();

	int												conversationNumber;

	private int										convoNum;

	private final Map<Integer, Map<Date, String>>	conversations				= new HashMap<Integer, Map<Date, String>>();

	public OmegleSpyZMainWindow() {
		result = SwingJavaBuilder.build(this);

		String baseHtml;
		try {
			baseHtml = IOUtils.toString(getClass().getResourceAsStream("/base.html"));
			console.setText(baseHtml);
		} catch (final IOException ex) {
			log.fatal("Unable to load the base.html. Application exiting.", ex);
			throw new RuntimeException(ex);
		}
		chatHistoryHelper = new HtmlChatHistoryHelper(baseHtml, LOGBOX, (HTMLDocument) console.getDocument());
		try {
			bindingFactory = BindingDirectory.getFactory(ShortcutKeyHelper.class);
			unmarshallingContext = bindingFactory.createUnmarshallingContext();
			shortcutKeyHelper = (ShortcutKeyHelper) unmarshallingContext.unmarshalDocument(
					Thread.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									ConfigHelper.getGroup(ConfigConstants.GROUP_MAIN).getProperty(
											ConfigConstants.MAIN_KEYBOARDSHORTCUTSFILE)), null);
			shortcutKeyHelper.setRecipient(this);
			shortcutKeyHelper.setBuildContext(result);
			shortcutKeyHelper.initialize();
		} catch (final Exception e) {
			log.warn("Unable to get keyboard shortcut configuration. Expert mode is disabled.", e);
			((Action) result.get(ControlNameConstants.ACTION_TOGGLE_EXPERT_MODE)).setEnabled(false);
		}

		helpWindow = new OmegleSpyZQuickHelp(this, shortcutKeyHelper);

		// Instantiate the scrollbar used when autoscrolling.
		final JScrollBar autoScrollBar = ((JScrollPane) result.get(ControlNameConstants.CONSOLE_SCROLLER))
				.getVerticalScrollBar();
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
						if (LogViewerHelper.saveLog(e, chatHistoryHelper.getBaseHtml(), conversations) == null) {
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

		ChatHistoryHelper.initialize(chatHistoryHelper);
		LogHelper.initialize(new CommonsLoggingLogHelper());
		CommunicationHelper.initialize(new DefaultCommunicationHelper());

		final List<OmegleSpyConversationListener> activeListeners = new ArrayList<OmegleSpyConversationListener>();
		activeListeners.add(this);
		controller = new OmegleSpyConversationController(activeListeners, new FileListBasedNameGenerator(
				ConfigConstants.MAIN_NAMESFILE), new FileListBasedNameGenerator(ConfigConstants.MAIN_SERVERNAMESFILE));
	}

	/**
	 * Clears the chat history screen.
	 */
	public void clearScreen() {
		console.setText(chatHistoryHelper.getBaseHtml());
	}

	/**
	 * View a list of all chats generated during this session.
	 */
	public void viewLogs() {
		log.debug("Preparing to display log viewer.");
		logViewer.setVisible(true);
	}

	/**
	 * Toggles the conversations state. Ends the conversation if the
	 * conversation is started. Begins the conversation if the conversation has
	 * ended.
	 * 
	 * On connection:
	 * 
	 * Reset the associations. Associate the strangers with the appropriate
	 * label styles; associate the strangers with the Text, Swap, Typing, and
	 * Disconnect controls for each stranger.
	 * 
	 * Enable stranger blocked toggle, filter toggle, expert text control.
	 * 
	 * Enable swap buttons.
	 * 
	 * On disconnect:
	 * 
	 * Disable stranger blocked toggle, filter toggle, all swap buttons, expert
	 * text control.
	 * 
	 * Return disconnect and swap buttons to neutral text.
	 * 
	 * @param action
	 */
	public void toggleConnectionState(final SwingAction action) {
		if (controller.isConversationEnded()) {
			ChatHistoryHelper.printStatusMessage(result.getConfig().getResource(
					ResourceConstants.MESSAGE_CONVERSATION_STARTING));
			// The conversation is not started. Starting the conversation.
			conversantNames = controller.startConversation();

			// Clearing previous associations
			strangerTypingControls.clear();
			strangerTextFields.clear();
			strangerSwapButtons.clear();
			strangerDisconnectButtons.clear();
			chatHistoryHelper.clearAssociations();
			chatHistoryHelper.clearCurrentChat();
			convoNum++;

			for (int i = 0; i < conversantNames.length; i++) {
				// create the associations and enable the swap controls
				strangerTypingControls.put(conversantNames[i],
						(JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING, i)));
				chatHistoryHelper.addLabelStyleAssociation(conversantNames[i], STYLE_NAMES[i]);
				chatHistoryHelper.addLabelStyleAssociation(MessageFormat.format(
						result.getConfig().getResource(ResourceConstants.LABEL_STRANGER_BLOCKED), conversantNames[i]),
						MessageFormat.format(BLOCKED_STYLE, STYLE_NAMES[i]));
				chatHistoryHelper.addLabelStyleAssociation(MessageFormat.format(
						result.getConfig().getResource(ResourceConstants.LABEL_STRANGER_SECRET), conversantNames[i]),
						MessageFormat.format(SECRET_STYLE, STYLE_NAMES[i]));
				strangerTextFields.put(conversantNames[i],
						(JTextField) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, i)));
				strangerSwapButtons.put(conversantNames[i],
						(JButton) result.get(MessageFormat.format(ControlNameConstants.BTN_SWAP_STRANGER, i)));
				strangerSwapButtons.get(conversantNames[i]).setEnabled(true);
				strangerDisconnectButtons.put(conversantNames[i],
						(JButton) result.get(MessageFormat.format(ControlNameConstants.BTN_DISCONNECT_STRANGER, i)));
			}

			// Enable conversation-level controls
			((Action) result.get(ControlNameConstants.ACTION_BLOCKMESSAGE)).setEnabled(true);
			((Action) result.get(ControlNameConstants.ACTION_FILTERMESSAGES)).setEnabled(true);
			((JTextArea) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, StringUtils.EMPTY)))
					.setEnabled(true);
		} else {
			// The conversation is started. Ending the conversation.
			controller.endConversation();

			// Disable conversation-level controls
			((Action) result.get(ControlNameConstants.ACTION_BLOCKMESSAGE)).setEnabled(false);
			((Action) result.get(ControlNameConstants.ACTION_FILTERMESSAGES)).setEnabled(false);
			((JTextArea) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, StringUtils.EMPTY)))
					.setEnabled(false);
			final Collection<JButton> swapButtons = strangerSwapButtons.values();
			for (final JButton button : swapButtons) {
				button.setEnabled(false);
			}

			ChatHistoryHelper.printStatusMessage(result.getConfig().getResource(
					ResourceConstants.MESSAGE_CONVERSATION_ENDED));

			conversations.put(convoNum, chatHistoryHelper.getCurrentChat());

			final String conversationSaveLink = "<div>** Would you like to <a href='#' class='" + BTN_LINK
					+ "' id='save-convo-" + convoNum + "'>save this conversation</a>? **</div><br><hr>";
			chatHistoryHelper.printRaw(conversationSaveLink);

			logViewer.addHTML(chatHistoryHelper.getBaseHtml(), conversations.get(convoNum));
		}

	}

	/**
	 * Toggles whether or not the two strangers can communicate with each other.
	 * 
	 * Notify of blocked status
	 */
	public void toggleStrangersBlocked(final ActionEvent evt) {
		final AbstractButton button = (AbstractButton) evt.getSource();
		controller.setStrangersBlock(button.isSelected());
		ChatHistoryHelper.printStatusMessage(MessageFormat.format(
				SwingJavaBuilder.getConfig().getResource(ResourceConstants.MESSAGE_TOGGLE_BLOCKED),
				button.isSelected() ? SwingJavaBuilder.getConfig().getResource(ResourceConstants.MESSAGE_ENABLED)
						: SwingJavaBuilder.getConfig().getResource(ResourceConstants.MESSAGE_DISABLED)));
	}

	/**
	 * Toggles whether or not stranger chat should be filtered.
	 * 
	 * Notify of filtered status
	 */
	public void toggleFilter(final ActionEvent evt) {
		final AbstractButton button = (AbstractButton) evt.getSource();
		controller.setFilter(button.isSelected());
		ChatHistoryHelper.printStatusMessage(MessageFormat.format(
				SwingJavaBuilder.getConfig().getResource(ResourceConstants.MESSAGE_TOGGLE_FILTERED), button
						.isSelected() ? SwingJavaBuilder.getConfig().getResource(ResourceConstants.MESSAGE_ENABLED)
						: SwingJavaBuilder.getConfig().getResource(ResourceConstants.MESSAGE_DISABLED)));
	}

	/**
	 * Disconnects a stranger.
	 * 
	 * Notify of disconnect.
	 * 
	 * @param action
	 * @param evt
	 */
	public void disconnectStranger(final ActionEvent evt) {
		final JButton button = (JButton) evt.getSource();
		log.debug("Disconnect stranger initiated by " + button.getName());
		final int targetIndex = new Integer(button.getName().substring(button.getName().length() - 1));
		final String strangerName = conversantNames[targetIndex];
		log.debug("Disconnecting Stranger " + (targetIndex + 1) + " identified as " + strangerName + ".");

		controller.disconnectStranger(strangerName);
	}

	/**
	 * Swaps a stranger for another stranger.
	 * 
	 * Notify of stranger swap.
	 * 
	 * @param action
	 * @param evt
	 */
	public void swapStranger(final ActionEvent evt) {
		final JButton button = (JButton) evt.getSource();
		log.debug("Swap stranger initiated by " + button.getName());
		final int targetIndex = new Integer(button.getName().substring(button.getName().length() - 1));
		final String strangerName = conversantNames[targetIndex];
		log.debug("Swapping Stranger " + targetIndex + 1 + " identified as " + strangerName + ".");

		controller.swapStranger(strangerName);
	}

	/**
	 * Displays the about window.
	 */
	public void viewHelp() {
		helpWindow.setVisible(!helpWindow.isVisible());
	}

	/**
	 * Toggles expert mode.
	 */
	public void toggleExpertMode() {
		secretMessageCards.next(grpSecretMessagePane);
	}

	/**
	 * Exits the application.
	 */
	public void exit() {
		log.info("Quitting application.");
		try {
			helpWindow.setVisible(false);
			helpWindow.dispose();

			logViewer.setVisible(false);
			logViewer.dispose();

			if (!controller.isConversationEnded()) {
				controller.endConversation();
			}
		} catch (final Throwable e) {
			log.warn("An error occurred while attempting to end the conversation during application exit.", e);
		} finally {
			System.exit(0);
		}
	}

	/**
	 * Sends a message to a stranger from the spy.
	 * 
	 * @param textField
	 */
	public void sendSecretMessage(final JTextField textField) {
		log.debug("Send secret message action generated by " + textField.getName());
		final String message;
		if (StringUtils.isEmpty(expertModeBuffer)) {
			message = textField.getText();
		} else {
			message = expertModeBuffer;
		}
		if (message.trim().length() > 0) {
			final int targetIndex = new Integer(textField.getName().substring(textField.getName().length() - 1));
			final String strangerName = conversantNames[targetIndex];
			final String fromName = conversantNames[targetIndex == 0 ? 1 : 0];
			try {
				controller.sendSecretMessage(strangerName, fromName, message);
			} catch (final Exception e) {
				log.warn("Message delivery failure.", e);
				ChatHistoryHelper.printStatusMessage(MessageFormat.format(
						result.getConfig().getResource(ResourceConstants.MESSAGE_SECRET_DELIVERY_FAILURE), message,
						strangerName, fromName, e.getLocalizedMessage()));
			}
		}
	}

	/**
	 * The expert mode command interpreter.
	 * 
	 * Interpret command and set text to ""
	 * 
	 * @param evt
	 */
	public void expertKeyPressed(final KeyEvent evt) {
		expertModeBuffer = ((JTextComponent) evt.getSource()).getText();
		if (shortcutKeyHelper.isShortcutDefined(evt)) {
			shortcutKeyHelper.performShortcut(evt);
		}
	}

	/**
	 * Labelled message of the form:
	 * 
	 * <${style for stranger}>${strangerName}</${style for stranger>: ${message}
	 * 
	 * Set Typing control to not visible.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#messageTransferred
	 * (org.darkimport.omeglespy_z.OmegleSpyEvent, java.lang.String)
	 */
	public void messageTransferred(final OmegleSpyEvent evt, final String msg) {
		final String conversantName = evt.getConversantName();
		strangerTypingControls.get(conversantName).setVisible(false);
		ChatHistoryHelper.printLabelledMessage(conversantName, msg);
	}

	/**
	 * Labelled message of the form:
	 * 
	 * <${style for blocked stranger}>${strangerName}</${style for blocked
	 * stranger>: ${message}
	 * 
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#messageBlocked
	 * (org.darkimport.omeglespy_z.OmegleSpyEvent, java.lang.String)
	 */
	public void messageBlocked(final OmegleSpyEvent evt, final String msg) {
		final String conversantName = evt.getConversantName();
		ChatHistoryHelper.printLabelledMessage(MessageFormat.format(
				result.getConfig().getResource(ResourceConstants.LABEL_STRANGER_BLOCKED), conversantName), msg);
	}

	/**
	 * Labeled message of the form:
	 * 
	 * <${style for stranger}>As ${strangerName}</${style for stranger>:
	 * ${message}
	 * 
	 * Set text controls (associated and expert) to "".
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#externalMessageSent
	 * (org.darkimport.omeglespy_z.OmegleSpyEvent, java.lang.String)
	 */
	public void externalMessageSent(final OmegleSpyEvent evt, final String fromName, final String msg) {
		final String conversantName = evt.getConversantName();
		ChatHistoryHelper
				.printLabelledMessage(MessageFormat.format(
						result.getConfig().getResource(ResourceConstants.LABEL_STRANGER_SECRET), fromName), msg);
		strangerTextFields.get(conversantName).setText(StringUtils.EMPTY);
		((JTextArea) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, StringUtils.EMPTY)))
				.setText(StringUtils.EMPTY);
		expertModeBuffer = StringUtils.EMPTY;
	}

	/**
	 * Make the Typing label for this Stranger visible
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#isTyping(org
	 * .darkimport.omeglespy$z.OmegleSpyEvent)
	 */
	public void isTyping(final OmegleSpyEvent evt) {
		strangerTypingControls.get(evt.getConversantName()).setVisible(true);
	}

	/**
	 * Make the Typing label for this Stranger not visible.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#stoppedTyping
	 * (org.darkimport.omeglespy_z.OmegleSpyEvent)
	 */
	public void stoppedTyping(final OmegleSpyEvent evt) {
		strangerTypingControls.get(evt.getConversantName()).setVisible(false);
	}

	/**
	 * Notify that chat has started with the stranger.
	 * 
	 * enable the Disconnect Stranger control; Enable the Text control.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#chatStarted(
	 * org.darkimport.omeglespy_z.OmegleSpyEvent)
	 */
	public void chatStarted(final OmegleSpyEvent evt) {
		final String conversantName = evt.getConversantName();
		ChatHistoryHelper.printStatusMessage(MessageFormat.format(
				result.getConfig().getResource(ResourceConstants.MESSAGE_STRANGER_CONNECTED), conversantName));
		strangerDisconnectButtons.get(conversantName).setEnabled(true);
		strangerTextFields.get(conversantName).setEnabled(true);
		strangerTypingControls.get(conversantName).setText(
				MessageFormat.format(result.getConfig().getResource(ResourceConstants.LABEL_STRANGER_TYPING),
						conversantName));
	}

	/**
	 * Print status message notifying that the stranger has disconnected.
	 * 
	 * Hide Typing label; Disable Disconnect button; Disable Text
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#disconnected
	 * (org.darkimport.omeglespy_z.OmegleSpyEvent)
	 */
	public void strangerDisconnected(final OmegleSpyEvent evt) {
		final String conversantName = evt.getConversantName();
		ChatHistoryHelper.printStatusMessage(MessageFormat.format(
				result.getConfig().getResource(ResourceConstants.MESSAGE_STRANGER_DISCONNECTED), conversantName));
		strangerDisconnectButtons.get(conversantName).setEnabled(false);
		strangerTextFields.get(conversantName).setEnabled(false);
		strangerTypingControls.get(conversantName).setVisible(false);
	}

	public void userDisconnected(final OmegleSpyEvent evt) {
		// Has the same effect as the stranger disconnecting, so...
		strangerDisconnected(evt);
	}

	public void generalCommunicationFailure(final OmegleSpyEvent evt) {
		// Handling it as if the stranger has disconnected.
		strangerDisconnected(evt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#recaptchaRejected
	 * (org.darkimport.omeglespy_z.OmegleSpyEvent, java.lang.String)
	 */
	public void recaptchaRejected(final OmegleSpyEvent evt, final String id) {
		ChatHistoryHelper.printStatusMessage(SwingJavaBuilder.getConfig().getResource(
				ResourceConstants.MESSAGE_RECAPTCHA_REJECTED));
		recaptcha(evt, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#recaptcha(org
	 * .darkimport.omeglespy$z.OmegleSpyEvent, java.lang.String)
	 */
	public void recaptcha(final OmegleSpyEvent evt, final String id) {
		final OmegleSpyRecaptchaWindow recaptchaWindow = new OmegleSpyRecaptchaWindow(id);
		recaptchaWindow.setVisible(true);
		controller.sendRecaptchaResponse(evt.getConversantName(), recaptchaWindow.getChallenge(),
				recaptchaWindow.getResponse());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy_z.OmegleSpyConversationListener#messageFiltered
	 * (org.darkimport.omeglespy_z.OmegleSpyEvent, java.lang.String)
	 */
	public void messageFiltered(final OmegleSpyEvent evt, final String msg) {
		controller.disconnectStranger(evt.getConversantName());
		ChatHistoryHelper.printStatusMessage(MessageFormat.format(
				result.getConfig().getResource(ResourceConstants.MESSAGE_FILTER_DISCONNECT), evt.getConversantName(),
				msg));
	}

	public void initializationFailed(final OmegleSpyEvent evt) {
		ChatHistoryHelper.printStatusMessage(MessageFormat.format(
				result.getConfig().getResource(ResourceConstants.MESSAGE_CONVERSATION_INITIALIZATION_FAILURE),
				evt.getConversantName()));
	}

	/**
	 * @return the secretMessageCards
	 */
	public CardLayout getSecretMessageCards() {
		return secretMessageCards;
	}

	/**
	 * @param secretMessageCards
	 *            the secretMessageCards to set
	 */
	public void setSecretMessageCards(final CardLayout secretMessageCards) {
		this.secretMessageCards = secretMessageCards;
	}

	/**
	 * @return the grpSecretMessagePane
	 */
	public JPanel getGrpSecretMessagePane() {
		return grpSecretMessagePane;
	}

	/**
	 * @param grpSecretMessagePane
	 *            the grpSecretMessagePane to set
	 */
	public void setGrpSecretMessagePane(final JPanel grpSecretMessagePane) {
		this.grpSecretMessagePane = grpSecretMessagePane;
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
}
