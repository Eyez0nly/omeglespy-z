/**
 * 
 */
package org.darkimport.omeglespy.ui;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.omeglespy.network.CommunicationHelper;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class OmegleSpyRecaptchaWindow extends JDialog {
	private static final Log	log						= LogFactory.getLog(OmegleSpyRecaptchaWindow.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID		= -6871551133131632766L;

	private static final String	GOOGLE_RECAPTCHA_URL	= "http://www.google.com/recaptcha/api/image?c=";
	private static final String	CAPTCHA_LABEL			= "lblCaptcha";

	private final BuildResult	result;

	private final String		omegleRecaptchaChallengeURL;

	private final String		challenge;
	private String				response;

	private final String		chatId;

	public OmegleSpyRecaptchaWindow(final String omegleRecaptchaChallengeURL, final String chatId, final String id) {
		result = SwingJavaBuilder.build(this);
		this.omegleRecaptchaChallengeURL = omegleRecaptchaChallengeURL;
		this.chatId = chatId;
		challenge = getChallenge(id);

		final JLabel label = (JLabel) result.get(CAPTCHA_LABEL);
		ImageIcon image;
		try {
			image = new ImageIcon(new URL(GOOGLE_RECAPTCHA_URL + challenge));
		} catch (final MalformedURLException e) {
			log.warn("The supplied google recaptcha URL is invalid.", e);
			throw new RuntimeException(e);
		}
		final Dimension d = new Dimension(image.getIconWidth(), image.getIconHeight());
		label.setMinimumSize(d);
		label.setMaximumSize(d);
		label.setIcon(image);
	}

	private String getChallenge(final String id) {
		try {
			// TODO Strip out this string for parameterization
			final String captcha = CommunicationHelper.wget(new URL("http://www.google.com/recaptcha/api/challenge?k="
					+ id + "&ajax=1&cachestop=0.34919850158610977"), false);
			final int idx0 = captcha.indexOf("challenge : '") + "challenge : '".length();
			final int idx1 = captcha.indexOf('\'', idx0);
			final String challenge = captcha.substring(idx0, idx1);
			return challenge;
		} catch (final Exception e) {
			log.warn("Unable to generate challenge.", e);
			throw new RuntimeException(e);
		}
	}

	public void keyPressed(final KeyEvent e) {
		final int key = e.getKeyCode();

		if (key == KeyEvent.VK_ENTER) {
			response = ((JTextField) e.getSource()).getText();
			if (!StringUtils.isEmpty(response)) {
				log.debug("Sending " + response);
				CommunicationHelper.wget(omegleRecaptchaChallengeURL, true, "id", chatId, "challenge", challenge,
						"response", response);
				dispose();
			}
		}
	}

	/**
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(final String response) {
		this.response = response;
	}
}
