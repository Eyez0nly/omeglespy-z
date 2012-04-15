import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy$z.CommunicationHelper;
import org.darkimport.omeglespy$z.LogHelper;
import org.darkimport.omeglespy$z.OmegleSpyConversationController;
import org.darkimport.omeglespy$z.OmegleSpyConversationListener;
import org.darkimport.omeglespy$z.OmegleSpyEvent;
import org.darkimport.omeglespy.log.CommonsLoggingLogHelper;
import org.darkimport.omeglespy.network.DefaultCommunicationHelper;
import org.darkimport.omeglespy.ui.OmegleSpyRecaptchaWindow;

/**
 * 
 */

/**
 * @author user
 * 
 */
public class NewCoreTest {
	private static final Log	log	= LogFactory.getLog(NewCoreTest.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final List<OmegleSpyConversationListener> activeListeners = new ArrayList<OmegleSpyConversationListener>();
		LogHelper.initialize(new CommonsLoggingLogHelper());

		final OmegleSpyConversationController conversationController = new OmegleSpyConversationController(
				activeListeners);

		activeListeners.add(new OmegleSpyConversationListener() {

			public void messageTransferred(final OmegleSpyEvent evt, final String msg) {
				log.info("Message from " + evt.getConversantName() + " transferred: " + msg);
			}

			public void messageBlocked(final OmegleSpyEvent evt, final String msg) {
				log.info("Message from " + evt.getConversantName() + " blocked: " + msg);
			}

			public void externalMessageSent(final OmegleSpyEvent evt, final String msg) {
				log.info("Sent a secret message to " + evt.getConversantName() + ": " + msg);
			}

			public void isTyping(final OmegleSpyEvent evt) {
				log.info(evt.getConversantName() + " is typing.");
			}

			public void stoppedTyping(final OmegleSpyEvent evt) {
				log.info(evt.getConversantName() + " stopped typing.");
			}

			public void chatStarted(final OmegleSpyEvent evt) {
				log.info("Chat with " + evt.getConversantName() + " started.");
			}

			public void disconnected(final OmegleSpyEvent evt) {
				log.info("Chat with " + evt.getConversantName() + " disconnected.");
			}

			public void recaptchaRejected(final OmegleSpyEvent evt, final String id) {
				log.info("Recaptcha rejected :(" + evt.getConversantName() + " " + id);
				recaptcha(evt, id);
			}

			public void recaptcha(final OmegleSpyEvent evt, final String id) {
				log.info("Recaptcha required :(" + evt.getConversantName() + " " + id);
				final OmegleSpyRecaptchaWindow recaptchaWindow = new OmegleSpyRecaptchaWindow(id);
				recaptchaWindow.setVisible(true);
				conversationController.sendRecaptchaResponse(evt.getConversantName(), recaptchaWindow.getChallenge(),
						recaptchaWindow.getResponse());
			}

			public void messageFiltered(final OmegleSpyEvent evt, final String msg) {
				log.info("Message from " + evt.getConversantName() + " filtered: " + msg);
			}

		});

		CommunicationHelper.initialize(new DefaultCommunicationHelper());
		conversationController.startConversation();
	}

}
