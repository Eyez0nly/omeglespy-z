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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.config.ConfigHelper;
import org.darkimport.omeglespy_z.LogHelper;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class App {
	private static final String	SETTINGS_PROPERTIES	= "settings.properties";
	private static final Log	log					= LogFactory.getLog(App.class);

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		LogHelper.initialize(new CommonsLoggingLogHelper());
		log.info("**** OmegleSpy-Z (DESKTOP) starting ****\n\r");
		if (log.isDebugEnabled()) {
			log.debug("> Running in DEVELOPMENT release mode. Debug mode is [ON] & Private functions are [ENABLED]");
		} else {
			log.info("> Running in Standard release mode. Debug mode is [OFF] & Private functions are [DISABLED]");
		}

		String settingsFilename;
		if (args.length > 0 && StringUtils.isNotBlank(args[0])) {
			settingsFilename = args[0];
		} else {
			settingsFilename = SETTINGS_PROPERTIES;
		}

		log.debug("Using " + settingsFilename + " for settings.");

		ConfigHelper.initialize(settingsFilename);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// activate internationalization
				SwingJavaBuilder.getConfig().addResourceBundle("OmegleSpyMainWindow");
				SwingJavaBuilder.getConfig().addResourceBundle("OmegleSpyRecaptchaWindow");
				SwingJavaBuilder.getConfig().addResourceBundle("LogViewerWindow");
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					// new OmegleSpyMainWindow().setVisible(true);
					new OmegleSpyZMainWindow().setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
