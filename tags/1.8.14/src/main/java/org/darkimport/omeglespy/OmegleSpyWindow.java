package org.darkimport.omeglespy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.darkimport.util.NativeHelper;

public class OmegleSpyWindow extends JFrame implements HyperlinkListener {
	private static final Log	log					= LogFactory.getLog(OmegleSpyWindow.class);

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7265092257783933165L;

	private static final String	TESSDLL_DLL			= "tessdll.dll";

	public void hyperlinkUpdate(final HyperlinkEvent ev) {
		if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				Common.openURL(ev.getURL().toString());
			} catch (final NullPointerException ex) {
				Common.showError(this, "Invalid URL");
			} catch (final Exception ex) {
				Common.showError(this, ex.getMessage());
			}
		}
	}

	public OmegleSpyWindow(final boolean useOcr) {
		final OmegleSpyPanel osp = new OmegleSpyPanel(useOcr);
		setLayout(new BorderLayout());
		add(osp);
		osp.addHyperlinkListener(this);
	}

	public static void main(final String[] args) {
		boolean useOcr = true;
		try {
			final String currentDirectory = FilenameUtils.normalize(new File(".").getAbsolutePath());
			final String tessDllPath = FilenameUtils.concat(currentDirectory, TESSDLL_DLL);
			NativeHelper.addDir(currentDirectory);
			NativeHelper.extractNative(tessDllPath, TESSDLL_DLL);
			System.load(tessDllPath);
		} catch (final Throwable e) {
			log.warn("Failed to load ocr libs. Will proceed without OCR.", e);
			useOcr = false;
		}

		log.info("**** Welcome to OmegleSpyX [v" + Common.APP_FULLVER + "] Ressurection  (by BrentBXR) ****\n\r");
		if (log.isDebugEnabled()) {
			log.debug("> Running in DEVELOPMENT release mode. Debug mode is [ON] & Private functions are [ENABLED]");
		} else {
			log.info("> Running in Standard release mode. Debug mode is [OFF] & Private functions are [DISABLED]");
		}
		// Begin loading OmegleSpyX
		final OmegleSpyWindow osw = new OmegleSpyWindow(useOcr);
		osw.setTitle(Common.APP_TITLE); // "OmegleSpyX - v1.5 Resurrection - By BrentBXR"
		osw.setSize(800, 600);
		final Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension s = osw.getSize();
		osw.setLocation((ss.width - s.width) / 2, (ss.height - s.height) / 2);
		osw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		osw.setVisible(true);
	}
}
