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
import org.darkimport.omeglespy$z.RecaptchaHelper;
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

	private static final String	CAPTCHA_LABEL			= "lblCaptcha";

	private final BuildResult	result;

	private final String		challenge;
	private String				response;

	public OmegleSpyRecaptchaWindow(final String id) {
		result = SwingJavaBuilder.build(this);

		challenge = RecaptchaHelper.getImageChallengeString(id);

		final JLabel label = (JLabel) result.get(CAPTCHA_LABEL);
		ImageIcon image;
		try {
			final String googleRecaptchaUrl = RecaptchaHelper.getChallengeAssetUrlString(challenge);
			image = new ImageIcon(new URL(googleRecaptchaUrl));
		} catch (final MalformedURLException e) {
			log.warn("The supplied google recaptcha URL is invalid.", e);
			throw new RuntimeException(e);
		}
		final Dimension d = new Dimension(image.getIconWidth(), image.getIconHeight());
		label.setMinimumSize(d);
		label.setMaximumSize(d);
		label.setIcon(image);
	}

	public void keyPressed(final KeyEvent e) {
		final int key = e.getKeyCode();

		if (key == KeyEvent.VK_ENTER) {
			response = ((JTextField) e.getSource()).getText();
			if (!StringUtils.isEmpty(response)) {
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

	public String getChallenge() {
		return challenge;
	}
}
