package org.darkimport.omeglespy;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Common {
	private static final Log			log				= LogFactory.getLog(Common.class);

	public static int					UNIT_INCREMENT	= 16;
	public static String[]				possibleNames;
	static {
		loadNames();
	}
	public static final JFileChooser	fc;
	public static final String			HTML_EXT		= ".html";

	static final String[]				browsers		= { "firefox", "opera", "konqueror", "epiphany", "seamonkey",
			"galeon", "kazehakase", "mozilla", "netscape", "chrome" };
	static final Runtime				rt				= Runtime.getRuntime();
	static final String					osName			= System.getProperty("os.name");

	static {
		JFileChooser jfc;
		try {
			jfc = new JFileChooser();
			jfc.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(final File f) {
					final String n = f.getName().toLowerCase();
					return f.isDirectory() || n.endsWith(HTML_EXT);
				}

				@Override
				public String getDescription() {
					return "HTML files (*.html)";
				}
			});
		} catch (final Exception ex) {
			jfc = null;
			log.warn("Could not create a file selector window", ex);
		}
		fc = jfc;
	}

	private Common() {}

	public static JScrollPane scroller(final Component c) {
		final JScrollPane jsp = new JScrollPane(c);
		jsp.getVerticalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		jsp.getHorizontalScrollBar().setUnitIncrement(UNIT_INCREMENT);
		return jsp;
	}

	public static void showError(final Component p, final String msg) {
		JOptionPane.showMessageDialog(p, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static boolean showWarning(final Component p, final String msg) {
		final int result = JOptionPane.showConfirmDialog(p, msg, "Warning", JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		return result == JOptionPane.YES_OPTION;
	}

	public static void rest(final long millis) {
		try {
			Thread.sleep(millis);
		} catch (final InterruptedException ex) {}
	}

	public static String timestamp(final Calendar c) {
		final int hour = c.get(Calendar.HOUR_OF_DAY);
		final int min = c.get(Calendar.MINUTE);
		final int sec = c.get(Calendar.SECOND);
		return (hour < 10 ? "0" : "") + hour + ":" + (min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec;
	}

	public static String timestamp() {
		return timestamp(new GregorianCalendar());
	}

	public static void stringToFile(final String text, final File f) throws IOException {
		final FileOutputStream fos = new FileOutputStream(f);
		fos.write(text.getBytes());
	}

	public static boolean guiWriteHtmlFile(final String text, final Component p) throws IOException {
		if (fc.showSaveDialog(p) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			if (!f.getName().toLowerCase().endsWith(HTML_EXT)) {
				f = new File(f.getParent(), f.getName() + HTML_EXT);
			}
			if (!(f.exists() && !showWarning(p, "The file " + f.getName() + " already exists. "
					+ "Are you sure you want to " + "overwrite it?"))) {
				stringToFile(text, f);
				return true;
			}
		}

		return false;
	}

	public static void openURL(final String url) throws Exception {
		if (osName.startsWith("Mac OS")) {
			final Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
			final Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
			openURL.invoke(null, new Object[] { url });
		} else if (osName.startsWith("Windows")) {
			rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
		} else // assume Unix or Linux
		{
			boolean found = false;
			for (final String browser : browsers) {
				if (!found) {
					found = rt.exec(new String[] { "which", browser }).waitFor() == 0;
					if (found) {
						rt.exec(new String[] { browser, url });
					}
				}
			}
			if (!found) { throw new Exception("Could not launch any web browser"); }
		}
	}

	private static boolean loadNames() {
		try {
			final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("names.txt");
			final List<String> namesList = IOUtils.readLines(is, null);

			possibleNames = namesList.toArray(new String[namesList.size()]);
			return true;
		} catch (final Exception ex) {
			possibleNames = new String[] { "Stranger 1", "Stranger 2" };
			log.warn("Could not load names file: " + ex.getMessage());
			return false;
		}
	}
}
