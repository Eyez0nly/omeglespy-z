/**
 * 
 */
package org.darkimport.omeglespy.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

/**
 * @author user
 * 
 */
public class OmegleSpyMainWindow extends JFrame {
	private static final Log	log					= LogFactory.getLog(OmegleSpyMainWindow.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 9092938195254654975L;

	@SuppressWarnings("unused")
	private final BuildResult	result;

	public OmegleSpyMainWindow() {

		result = SwingJavaBuilder.build(this);
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
	}

	protected void exit() {
		log.info("Quitting application.");
		System.exit(0);
	}

	public void disconnectStranger(final JButton button) {

	}

	public void swapStranger(final JButton button) {

	}

	public void toggleConnectionState(final JButton button) {

	}

	public void clearScreen() {

	}

	public void viewLogs() {

	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// activate internationalization
				SwingJavaBuilder.getConfig().addResourceBundle("OmegleSpyMainWindow");
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					new OmegleSpyMainWindow().setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
