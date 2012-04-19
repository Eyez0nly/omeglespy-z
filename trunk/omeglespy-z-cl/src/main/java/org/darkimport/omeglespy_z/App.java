/*
 * #%L omeglespy-z-cl
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
package org.darkimport.omeglespy_z;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy$z.ChatHistoryHelper;
import org.darkimport.omeglespy$z.CommunicationHelper;
import org.darkimport.omeglespy$z.DefaultCommunicationHelper;
import org.darkimport.omeglespy$z.LogHelper;
import org.darkimport.omeglespy$z.OmegleSpyConversationController;
import org.darkimport.omeglespy$z.OmegleSpyConversationListener;
import org.darkimport.omeglespy$z.OmegleSpyEvent;
import org.darkimport.omeglespy$z.RecaptchaHelper;

/**
 * Hello world!
 * 
 */
public class App {
	private static final Log					log								= LogFactory.getLog(App.class);
	private static final String					COMMAND_INITIATOR				= "\\";
	private static final String					TARGETS							= "targets";
	private static final String					ARGUMENT						= "argument";
	private static final String					CONTROLLER						= "controller";
	private static final String					ALL_CONVERSANTS					= "allConversants";
	private static boolean						running							= true;
	private static boolean						blocked;
	private static boolean						filtered;
	private static final Map<String, String>	outstandingRecaptchaChallenges	= new HashMap<String, String>();
	private static final Map<String, String>	outstandingRecaptchas			= new HashMap<String, String>();
	private static CLChatHistoryHelper			clChatHistoryHelper				= new CLChatHistoryHelper();

	public static void main(final String[] args) {
		ChatHistoryHelper.initialize(clChatHistoryHelper);
		LogHelper.initialize(new CommonsLoggingLogHelper());
		CommunicationHelper.initialize(new DefaultCommunicationHelper());

		final List<OmegleSpyConversationListener> activeListeners = new ArrayList<OmegleSpyConversationListener>();

		final OmegleSpyConversationController conversationController = new OmegleSpyConversationController(
				activeListeners);
		final String[] conversantNames = conversationController.startConversation();

		activeListeners.add(new OmegleSpyConversationListener() {

			public void messageTransferred(final OmegleSpyEvent evt, final String msg) {
				log.info("Message from " + evt.getConversantName() + " transferred: " + msg);
				ChatHistoryHelper.printLabelledMessage(evt.getConversantName(), msg);
			}

			public void messageBlocked(final OmegleSpyEvent evt, final String msg) {
				log.info("Message from " + evt.getConversantName() + " blocked: " + msg);
				ChatHistoryHelper.printLabelledMessage(new StringBuffer('-').append(evt.getConversantName())
						.append('-').toString(), msg);
			}

			public void externalMessageSent(final OmegleSpyEvent evt, final String msg) {
				log.info("Sent a secret message to " + evt.getConversantName() + ": " + msg);
				String otherConversant = StringUtils.EMPTY;
				for (final String name : conversantNames) {
					if (!name.equals(evt.getConversantName())) {
						otherConversant = name;
					}
				}
				ChatHistoryHelper.printLabelledMessage(new StringBuffer("As ").append(otherConversant).toString(), msg);
			}

			public void isTyping(final OmegleSpyEvent evt) {}

			public void stoppedTyping(final OmegleSpyEvent evt) {}

			public void chatStarted(final OmegleSpyEvent evt) {
				log.info("Chat with " + evt.getConversantName() + " started.");
				ChatHistoryHelper.printStatusMessage(new StringBuffer(evt.getConversantName()).append(" joined.")
						.toString());
			}

			public void disconnected(final OmegleSpyEvent evt) {
				log.info("Chat with " + evt.getConversantName() + " disconnected.");
				ChatHistoryHelper.printStatusMessage(new StringBuffer(evt.getConversantName()).append(" left")
						.toString());
			}

			public void recaptchaRejected(final OmegleSpyEvent evt, final String id) {
				log.info("Recaptcha rejected :(" + evt.getConversantName() + " " + id);
				recaptcha(evt, id);
			}

			public void recaptcha(final OmegleSpyEvent evt, final String id) {
				log.info("Recaptcha required :(" + evt.getConversantName() + " " + id);
				outstandingRecaptchas.put(evt.getConversantName(), id);
				outstandingRecaptchaChallenges.put(evt.getConversantName(), RecaptchaHelper.getImageChallengeString(id));
				ChatHistoryHelper.printStatusMessage("Recaptcha required for " + evt.getConversantName());
			}

			public void messageFiltered(final OmegleSpyEvent evt, final String msg) {
				log.info("Message from " + evt.getConversantName() + " filtered: " + msg);
				conversationController.disconnectStranger(evt.getConversantName());
			}
		});

		ChatHistoryHelper.printStatusMessage("Connected. Type \\? for help.");
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while (running) {
			String rawCommand;
			try {
				rawCommand = in.readLine();
			} catch (final IOException e) {
				log.warn("Unable to read from stdin.", e);
				ChatHistoryHelper.printStatusMessage("An error occurred while reading user input.");
				break;
			}
			final Command command = findCommand(rawCommand);
			if (command == null) {
				ChatHistoryHelper.printStatusMessage("You have typed an invalid command.");
				continue;
			}
			final String[] targets = findTargets(rawCommand, conversantNames);
			final String argument = findArgument(rawCommand);
			executeCommand(command, targets, conversantNames, argument, conversationController);
		}
	}

	private static void executeCommand(final Command command, final String[] targets, final String[] allConversants,
			final String argument, final OmegleSpyConversationController conversationController) {
		Method commandMethod = null;
		boolean error = false;
		try {
			commandMethod = App.class.getDeclaredMethod(command.name(), Map.class);
		} catch (final Exception e) {
			log.warn("Unable to find the method " + command + ".", e);
			error = true;
		}
		if (!error) {
			final Map<String, Object> args = new HashMap<String, Object>();
			args.put(TARGETS, targets);
			args.put(ALL_CONVERSANTS, allConversants);
			args.put(ARGUMENT, argument);
			args.put(CONTROLLER, conversationController);
			try {
				commandMethod.invoke(null, args);
			} catch (final Exception e) {
				log.warn("Unable to invoke the method " + command + ".", e);
				error = true;
			}
		}

		if (error) {
			ChatHistoryHelper.printStatusMessage("An error occurred while invoking " + command + ".");
		}
	}

	private static String findArgument(final String rawCommand) {
		final int beginIndex = !rawCommand.startsWith(COMMAND_INITIATOR) ? 0 : 2;
		int endIndex = rawCommand.length();
		final Set<Character> specifiers = CONVERSANT_SPECIFIERS.keySet();
		for (final Character specifier : specifiers) {
			if (rawCommand.endsWith(specifier.toString())) {
				endIndex = rawCommand.length() - 1;
				break;
			}
		}
		return rawCommand.substring(beginIndex, endIndex);
	}

	private static Command findCommand(final String rawCommand) {
		final String commandString;
		if (rawCommand.startsWith(COMMAND_INITIATOR)) {
			commandString = rawCommand.substring(0, 2);
		} else {
			commandString = null;
		}
		final Command[] commands = Command.values();
		for (final Command command : commands) {
			if (StringUtils.equalsIgnoreCase(commandString, command.keyStroke)) { return command; }
		}
		return null;
	}

	private static final Map<Character, Integer>	CONVERSANT_SPECIFIERS	= new HashMap<Character, Integer>();
	static {
		CONVERSANT_SPECIFIERS.put('<', 0);
		CONVERSANT_SPECIFIERS.put('>', 1);
	}

	private static String[] findTargets(final String rawCommand, final String[] conversantNames) {
		final Set<Character> specifierList = CONVERSANT_SPECIFIERS.keySet();
		for (final Character targetSpecifier : specifierList) {
			if (rawCommand.endsWith(targetSpecifier.toString())) { return new String[] { conversantNames[CONVERSANT_SPECIFIERS
					.get(targetSpecifier)] }; }
		}

		return Arrays.copyOf(conversantNames, conversantNames.length);
	}

	private static enum Command {
		_swapStranger("\\s", "Swaps the specified stranger (< is Stranger1 and > is Stranger2)"), _disconnectStranger(
				"\\d", "Disconnects the specified stranger (< is Stranger1 and > is Stranger2)"), _endConversation(
				"\\e", "Ends the conversation"), _startConversation("\\t", "Starts a new conversation"), _quit("\\q",
				"Quits the application"), _recaptcha("\\r",
				"Submits the recaptcha for the specified stranger (< is Stranger1 and > is Stranger2). For example \\rblahblah<"), _viewRecaptcha(
				"\\v", "Views the recaptcha for the specified stranger (< is Stranger1 and > is Stranger2)"), _toggleBlock(
				"\\b", "Toggles blocking."), _toggleFilter("\\f", "Toggles filtering"), _sendSecretMessage(
				null,
				"A command without a leading \\ sends a message. < targets Stranger1 and > targets Stranger2 and no end modifier sends the message to both strangers."), _showHelp(
				"\\?", "Shows this help message.");

		public String	keyStroke;
		public String	description;

		private Command(final String keyStroke, final String description) {
			this.keyStroke = keyStroke;
			this.description = description;
		}
	}

	protected static void _showHelp(final Map<String, Object> args) {
		final Command[] allCommands = Command.values();
		for (final Command command : allCommands) {
			ChatHistoryHelper.printLabelledMessage(command.keyStroke, command.description);
		}
	}

	protected static void _swapStranger(final Map<String, Object> args) {
		final String[] targets = (String[]) args.get(TARGETS);
		if (targets.length != 1) {
			ChatHistoryHelper.printStatusMessage("Specify a target to swap.");
			return;
		}

		final String target = targets[0];
		final OmegleSpyConversationController conversationController = (OmegleSpyConversationController) args
				.get(CONTROLLER);
		conversationController.swapStranger(target);
		ChatHistoryHelper.printStatusMessage(target + " swapped.");
	}

	protected static void _disconnectStranger(final Map<String, Object> args) {
		final String[] targets = (String[]) args.get(TARGETS);
		if (targets.length != 1) {
			ChatHistoryHelper.printStatusMessage("Specify a target to disconnect.");
			return;
		}

		final String target = targets[0];
		final OmegleSpyConversationController conversationController = (OmegleSpyConversationController) args
				.get(CONTROLLER);
		conversationController.disconnectStranger(target);
		ChatHistoryHelper.printStatusMessage(target + " disconnected.");
	}

	protected static void _endConversation(final Map<String, Object> args) {
		final OmegleSpyConversationController conversationController = (OmegleSpyConversationController) args
				.get(CONTROLLER);
		conversationController.endConversation();
		ChatHistoryHelper.printStatusMessage("Conversation ended.");
	}

	protected static void _quit(final Map<String, Object> args) {
		_endConversation(args);
		ChatHistoryHelper.printStatusMessage("Goodbye.");
		running = false;
	}

	protected static void _recaptcha(final Map<String, Object> args) {
		final String[] targets = (String[]) args.get(TARGETS);
		if (targets.length != 1) {
			ChatHistoryHelper.printStatusMessage("Specify a target to recaptcha.");
			return;
		}

		final String target = targets[0];
		final String recaptchaResponse = (String) args.get(ARGUMENT);
		final OmegleSpyConversationController conversationController = (OmegleSpyConversationController) args
				.get(CONTROLLER);

		conversationController.sendRecaptchaResponse(target, outstandingRecaptchaChallenges.get(target),
				recaptchaResponse);
	}

	protected static void _viewRecaptcha(final Map<String, Object> args) {
		final String[] targets = (String[]) args.get(TARGETS);
		if (targets.length != 1) {
			ChatHistoryHelper.printStatusMessage("Specify a target recaptcha to view.");
			return;
		}

		final String target = targets[0];
		try {
			final String challengeAssetUrlString = RecaptchaHelper
					.getChallengeAssetUrlString(outstandingRecaptchaChallenges.get(target));
			final URL recaptchaImageLocation = new URL(challengeAssetUrlString);
			// Load and ASCII-ify the image
			final String ascii_fiedImage = Image2Ascii.assciiFyImage(recaptchaImageLocation);
			clChatHistoryHelper.getOut().println(ascii_fiedImage);
		} catch (final Exception e) {
			log.warn("Unable to get recaptcha image", e);
			ChatHistoryHelper.printStatusMessage("An error occurred while loading recaptcha image");
			return;
		}

	}

	protected static void _toggleBlock(final Map<String, Object> args) {
		final OmegleSpyConversationController conversationController = (OmegleSpyConversationController) args
				.get(CONTROLLER);
		blocked = !blocked;
		conversationController.toggleStrangersBlock(blocked);
		ChatHistoryHelper.printStatusMessage("Set block to " + blocked);
	}

	protected static void _toggleFilter(final Map<String, Object> args) {
		final OmegleSpyConversationController conversationController = (OmegleSpyConversationController) args
				.get(CONTROLLER);
		filtered = !filtered;
		conversationController.toggleFilter(filtered);
		ChatHistoryHelper.printStatusMessage("Set block to " + filtered);
	}

	protected static void _sendSecretMessage(final Map<String, Object> args) {
		final String[] targets = (String[]) args.get(TARGETS);
		final String message = (String) args.get(ARGUMENT);
		final OmegleSpyConversationController conversationController = (OmegleSpyConversationController) args
				.get(CONTROLLER);
		final String[] allConversants = (String[]) args.get(ALL_CONVERSANTS);
		for (final String target : targets) {
			String fromName = StringUtils.EMPTY;
			for (final String name : allConversants) {
				if (!name.equals(target)) {
					fromName = name;
					break;
				}
			}
			conversationController.sendSecretMessage(target, fromName, message);
		}
	}

	/*
	 * protected static void _startConversation(final Map<String, Object> args)
	 * { args.put(TARGETS, targets); args.put(ARGUMENT, argument);
	 * args.put(CONTROLLER, conversationController); // TODO }
	 */
}
