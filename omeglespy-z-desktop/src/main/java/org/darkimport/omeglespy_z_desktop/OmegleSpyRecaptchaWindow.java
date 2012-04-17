/*
 * #%L
 * omeglespy-z-desktop
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 darkimport
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
/**
 * 
 */
package org.darkimport.omeglespy_z_desktop;

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
import org.darkimport.omeglespy_z.RecaptchaHelper;
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
