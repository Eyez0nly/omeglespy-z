package org.darkimport.omeglespy.ui;

import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.darkimport.omeglespy.ResourceConstants;
import org.darkimport.omeglespy$z.OmegleSpyConversationListener;
import org.darkimport.omeglespy$z.OmegleSpyEvent;
import org.darkimport.omeglespy.ui.util.ChatHistoryHelper;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

class MainWindowOmegleSpyListener implements OmegleSpyConversationListener {

	private final Element		currentConversation;
	private final BuildResult	result;
	private final HTMLDocument	doc;
	private final UICallback	callback;

	/**
	 * @param currentConversation
	 * @param result
	 * @param doc
	 * @param callback
	 */
	public MainWindowOmegleSpyListener(final Element currentConversation, final BuildResult result,
			final HTMLDocument doc, final UICallback callback) {
		this.currentConversation = currentConversation;
		this.result = result;
		this.doc = doc;
		this.callback = callback;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.omeglespy.OmegleSpyListener#messageTransferred(org
	 * .darkimport .omeglespy.OmegleSpy, java.lang.String)
	 */
	public void messageTransferred(final OmegleSpyEvent evt, final String msg) {
		ChatHistoryHelper.printLabelledMsg(ChatHistoryHelper.CLASS_NAMES[evt.getConversantIndex()],
				evt.getConversantName(), msg, currentConversation, doc);
		((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
				String.valueOf(evt.getConversantIndex())))).setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy.OmegleSpyListener#chatStarted(org.darkimport
	 * .omeglespy.OmegleSpy)
	 */
	public void chatStarted(final OmegleSpyEvent evt) {
		final int k = evt.getConversantIndex();
		final String n = evt.getConversantName();

		((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING, String.valueOf(k))))
				.setText(MessageFormat.format(
						SwingJavaBuilder.getConfig().getResource(ResourceConstants.LABEL_STRANGER_TYPING), n));
		final JButton targetedDisconnectButton = (JButton) result.get(MessageFormat.format(
				ControlNameConstants.BTN_DISCONNECT_STRANGER, String.valueOf(k)));
		targetedDisconnectButton.setText(MessageFormat.format(
				SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_DISCONNECT_STRANGER), n));
		targetedDisconnectButton.setEnabled(true);
		final JButton swapButton = (JButton) result.get(MessageFormat.format(ControlNameConstants.BTN_SWAP_STRANGER,
				String.valueOf(k)));
		swapButton.setText(MessageFormat.format(
				SwingJavaBuilder.getConfig().getResource(ResourceConstants.BUTTON_SWAP_STRANGER), n));
		swapButton.setEnabled(true);
		((JTextField) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, String.valueOf(k))))
				.setEnabled(true);
		ChatHistoryHelper.printLabelledMsg(ChatHistoryHelper.CLASS_NAMES[k], "System", n + " connected",
				currentConversation, doc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy.OmegleSpyListener#disconnected(org.darkimport
	 * .omeglespy.OmegleSpy)
	 */
	public void disconnected(final OmegleSpyEvent evt) {
		ChatHistoryHelper.printStatusLog(currentConversation, doc, evt.getConversantName() + " disconnected");
		final int index = evt.getConversantIndex();

		// A stranger disconnected, hide his typing label
		((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING, String.valueOf(index))))
				.setVisible(false);
		// disable his disconnect button and set the text of his disconnect
		// button to neutral.
		final String targetedDisconnectButtonName = MessageFormat.format(ControlNameConstants.BTN_DISCONNECT_STRANGER,
				String.valueOf(index));
		final JButton targetedDisconnectButton = (JButton) result.get(targetedDisconnectButtonName);
		targetedDisconnectButton.setText(SwingJavaBuilder.getConfig().getResource(
				ResourceConstants.BUTTON_DISCONNECT_STRANGER_NEUTRAL));
		targetedDisconnectButton.setEnabled(false);
		// disable his txtbox.
		((JTextField) result.get(MessageFormat.format(ControlNameConstants.TXT_TO_STRANGER, String.valueOf(index))))
				.setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy.OmegleSpyListener#messageBlocked(org.darkimport
	 * .omeglespy.OmegleSpy, java.lang.String)
	 */
	public void messageBlocked(final OmegleSpyEvent evt, final String msg) {
		ChatHistoryHelper.printBlockedMsg(evt.getConversantIndex(), evt.getConversantName(), msg, currentConversation,
				doc);
		((JLabel) result.get(MessageFormat.format(ControlNameConstants.LBL_STRANGER_TYPING,
				String.valueOf(evt.getConversantIndex())))).setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.darkimport.omeglespy.OmegleSpyListener#externalMessageSent(org
	 * .darkimport .omeglespy.OmegleSpy, java.lang.String)
	 */
	public void externalMessageSent(final OmegleSpyEvent evt, final String msg) {
		ChatHistoryHelper.printSecretMsg(evt.getOtherConversantIndex(), evt.getOtherConversantName(), msg,
				currentConversation, doc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.darkimport.omeglespy.OmegleSpyListener#messageFiltered(org.darkimport
	 * .omeglespy.OmegleSpy, java.lang.String)
	 */
	public void messageFiltered(final OmegleSpyEvent evt, final String msg) {
		callback.doCallback(evt);
	}

}
