/**
 * 
 */
package org.darkimport.omeglespy;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.config.ConfigHelper;
import org.darkimport.omeglespy$z.LogHelper;
import org.darkimport.omeglespy.log.CommonsLoggingLogHelper;
import org.darkimport.omeglespy_z.OmegleSpyZMainWindow;
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
