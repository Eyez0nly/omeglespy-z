/**
 * 
 */

package org.darkimport.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.MessageFormat;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author user
 * 
 */
public class NativeHelper {

	private static final String	JOGL_DLL		= "jogl.dll";
	private static final String	JOGL_CG_DLL		= "jogl_cg.dll";
	private static final String	JOGL_AWT_DLL	= "jogl_awt.dll";

	private static final String	LWJGL_LIB		= "{0}lwjgl{1}.{2}";
	private static final String	OPENAL_LIB		= "{0}OpenAL{1}.{2}";

	private static final String	JINPUT_LIB		= "{0}jinput-{1}{2}.{3}";

	// TODO Parameterize based on platform
	public static void initJoglNative(final String tempDir) {
		final String joglDllFileName = FilenameUtils.concat(tempDir, JOGL_DLL);
		final String joglCgDllFileName = FilenameUtils.concat(tempDir, JOGL_CG_DLL);
		final String joglAwtDllFileName = FilenameUtils.concat(tempDir, JOGL_AWT_DLL);

		if (!new File(joglDllFileName).exists()) {
			extractNative(joglDllFileName, JOGL_DLL);
		}
		if (!new File(joglCgDllFileName).exists()) {
			extractNative(joglCgDllFileName, JOGL_CG_DLL);
		}
		if (!new File(joglAwtDllFileName).exists()) {
			extractNative(joglAwtDllFileName, JOGL_AWT_DLL);
		}

		System.load(joglDllFileName);

		System.load(joglAwtDllFileName);

		// System.load(joglCgDllFileName);
	}

	/**
	 * @param libraryFileName
	 * @param out
	 */
	public static void extractNative(final String libraryFileName, final String sourceName) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourceName);
			out = new FileOutputStream(libraryFileName);
			IOUtils.copy(in, out);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
	}

	public static void addDir(final String s) throws IOException {
		try {
			// This enables the java.library.path to be modified at runtime
			// From a Sun engineer at
			// http://forums.sun.com/thread.jspa?threadID=707176
			//
			final Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			final String[] paths = (String[]) field.get(null);
			for (final String path : paths) {
				if (s.equals(path)) {
					return;
				}
			}
			final String[] tmp = new String[paths.length + 1];
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = s;
			field.set(null, tmp);
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
		} catch (final IllegalAccessException e) {
			throw new IOException("Failed to get permissions to set library path");
		} catch (final NoSuchFieldException e) {
			throw new IOException("Failed to get field handle to set library path");
		}
	}

	public static void init(final String renderer) {
		final String tempDir = prepareTempDir();

		if ("jogl".equalsIgnoreCase(renderer)) {
			initJoglNative(tempDir);
		} else if ("lwjgl".equalsIgnoreCase(renderer)) {
			initLwJglNative(System.getProperty("os.name"), System.getProperty("os.arch"), tempDir);
		}

		initInput(System.getProperty("os.name"), System.getProperty("os.arch"), tempDir);
	}

	// TODO Actually use the parameters to construct the name of the libs
	private static void initInput(final String osName, final String osArch, final String tempDir) {
		String jinputResourceName = MessageFormat.format(JINPUT_LIB, "", "dx8", "", "dll");

		String jinputFileName = FilenameUtils.concat(tempDir, jinputResourceName);

		if (!new File(jinputFileName).exists()) {
			extractNative(jinputFileName, jinputResourceName);
		}

		jinputResourceName = MessageFormat.format(JINPUT_LIB, "", "raw", "", "dll");

		jinputFileName = FilenameUtils.concat(tempDir, jinputResourceName);

		if (!new File(jinputFileName).exists()) {
			extractNative(jinputFileName, jinputResourceName);
		}
	}

	// TODO Actually use the parameters to construct the name of the libs
	private static void initLwJglNative(final String osName, final String osArch, final String tempDir) {
		final String lwjglResourceName = MessageFormat.format(LWJGL_LIB, "", "", "dll");
		final String openalResourceName = MessageFormat.format(OPENAL_LIB, "", "32", "dll");

		final String lwjglFileName = FilenameUtils.concat(tempDir, lwjglResourceName);
		final String openalFileName = FilenameUtils.concat(tempDir, openalResourceName);

		if (!new File(lwjglFileName).exists()) {
			extractNative(lwjglFileName, lwjglResourceName);
		}
		if (!new File(openalFileName).exists()) {
			extractNative(openalFileName, openalResourceName);
		}

		System.load(lwjglFileName);

		System.load(openalFileName);
	}

	/**
	 * @return
	 */
	private static String prepareTempDir() {
		final String tempDir = System.getProperty("java.io.tmpdir");
		try {
			addDir(tempDir);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return tempDir;
	}
}
