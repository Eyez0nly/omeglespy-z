/**
 * Copyright @ 2009 Quan Nguyen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.vietocr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.sf.ghost4j.Ghostscript;
import net.sf.ghost4j.GhostscriptException;

public class PdfUtilities {

	public static final String	GS_INSTALL	= "\nPlease download, install GPL Ghostscript from http://sourceforge.net/projects/ghostscript/files\nand/or set the appropriate environment variable.";

	/**
	 * Convert PDF to TIFF format.
	 * 
	 * @param inputPdfFile
	 * @return a multi-page TIFF image
	 */
	public static File convertPdf2Tiff(final File inputPdfFile) throws IOException {
		File[] pngFiles = null;

		try {
			pngFiles = convertPdf2Png(inputPdfFile);
			final File tiffFile = File.createTempFile("multipage", ".tif");

			// put PNG images into a single multi-page TIFF image for return
			ImageIOHelper.mergeTiff(pngFiles, tiffFile);
			return tiffFile;
		} catch (final UnsatisfiedLinkError ule) {
			throw new RuntimeException(getMessage(ule.getMessage()));
		} catch (final NoClassDefFoundError ncdfe) {
			throw new RuntimeException(getMessage(ncdfe.getMessage()));
		} finally {
			if (pngFiles != null) {
				// delete temporary PNG images
				for (final File tempFile : pngFiles) {
					tempFile.delete();
				}
			}
		}
	}

	/**
	 * Convert PDF to PNG format.
	 * 
	 * @param inputPdfFile
	 * @return an array of PNG images
	 */
	public static File[] convertPdf2Png(final File inputPdfFile) {
		File imageDir = inputPdfFile.getParentFile();

		if (imageDir == null) {
			final String userDir = System.getProperty("user.dir");
			imageDir = new File(userDir);
		}

		// get Ghostscript instance
		final Ghostscript gs = Ghostscript.getInstance();

		// prepare Ghostscript interpreter parameters
		// refer to Ghostscript documentation for parameter usage
		final List<String> gsArgs = new ArrayList<String>();
		gsArgs.add("-gs");
		gsArgs.add("-dNOPAUSE");
		gsArgs.add("-dBATCH");
		gsArgs.add("-dSAFER");
		gsArgs.add("-sDEVICE=pnggray");
		gsArgs.add("-r300");
		gsArgs.add("-dGraphicsAlphaBits=4");
		gsArgs.add("-dTextAlphaBits=4");
		gsArgs.add("-sOutputFile=" + imageDir.getPath() + "/workingimage%03d.png");
		gsArgs.add(inputPdfFile.getPath());

		// execute and exit interpreter
		try {
			gs.initialize(gsArgs.toArray(new String[0]));
			gs.exit();
		} catch (final GhostscriptException e) {
			System.err.println("ERROR: " + e.getMessage());
		}

		// find working files
		final File[] workingFiles = imageDir.listFiles(new FilenameFilter() {

			public boolean accept(final File dir, final String name) {
				return name.toLowerCase().matches("workingimage\\d{3}\\.png$");
			}
		});

		Arrays.sort(workingFiles, new Comparator<File>() {

			public int compare(final File f1, final File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});

		return workingFiles;
	}

	/**
	 * Split PDF.
	 * 
	 * @param inputPdfFile
	 * @param outputPdfFile
	 * @param firstPage
	 * @param lastPage
	 */
	public static void splitPdf(final String inputPdfFile, final String outputPdfFile, final String firstPage,
			final String lastPage) {
		// get Ghostscript instance
		final Ghostscript gs = Ghostscript.getInstance();

		// prepare Ghostscript interpreter parameters
		// refer to Ghostscript documentation for parameter usage
		// gs -sDEVICE=pdfwrite -dNOPAUSE -dQUIET -dBATCH -dFirstPage=m
		// -dLastPage=n -sOutputFile=out.pdf in.pdf
		final List<String> gsArgs = new ArrayList<String>();
		gsArgs.add("-gs");
		gsArgs.add("-dNOPAUSE");
		gsArgs.add("-dQUIET");
		gsArgs.add("-dBATCH");
		gsArgs.add("-sDEVICE=pdfwrite");

		if (!firstPage.trim().isEmpty()) {
			gsArgs.add("-dFirstPage=" + firstPage);
		}

		if (!lastPage.trim().isEmpty()) {
			gsArgs.add("-dLastPage=" + lastPage);
		}

		gsArgs.add("-sOutputFile=" + outputPdfFile);
		gsArgs.add(inputPdfFile);

		// execute and exit interpreter
		try {
			gs.initialize(gsArgs.toArray(new String[0]));
			gs.exit();
		} catch (final GhostscriptException e) {
			System.err.println("ERROR: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (final UnsatisfiedLinkError ule) {
			throw new RuntimeException(getMessage(ule.getMessage()));
		} catch (final NoClassDefFoundError ncdfe) {
			throw new RuntimeException(getMessage(ncdfe.getMessage()));
		}
	}

	/**
	 * Get PDF Page Count.
	 * 
	 * @param inputPdfFile
	 * @return number of pages
	 */
	public static int getPdfPageCount(final String inputPdfFile) {
		// get Ghostscript instance
		final Ghostscript gs = Ghostscript.getInstance();

		// prepare Ghostscript interpreter parameters
		// refer to Ghostscript documentation for parameter usage
		// gs -q -sPDFname=test.pdf pdfpagecount.ps
		final List<String> gsArgs = new ArrayList<String>();
		gsArgs.add("-gs");
		gsArgs.add("-dNOPAUSE");
		gsArgs.add("-dQUIET");
		gsArgs.add("-dBATCH");
		gsArgs.add("-sPDFname=" + inputPdfFile);
		gsArgs.add("lib/pdfpagecount.ps");

		int pageCount = 0;
		ByteArrayOutputStream os = null;

		// execute and exit interpreter
		try {
			// output
			os = new ByteArrayOutputStream();
			gs.setStdOut(os);
			gs.initialize(gsArgs.toArray(new String[0]));
			pageCount = Integer.parseInt(os.toString().replace("%%Pages: ", ""));
			os.close();
		} catch (final GhostscriptException e) {
			System.err.println("ERROR: " + e.getMessage());
		} catch (final Exception e) {
			System.err.println("ERROR: " + e.getMessage());
		}

		return pageCount;
	}

	/**
	 * Merge PDF files.
	 * 
	 * @param inputPdfFiles
	 * @param outputPdfFile
	 */
	public static void mergePdf(final File[] inputPdfFiles, final File outputPdfFile) {
		// get Ghostscript instance
		final Ghostscript gs = Ghostscript.getInstance();

		// prepare Ghostscript interpreter parameters
		// refer to Ghostscript documentation for parameter usage
		// gs -sDEVICE=pdfwrite -dNOPAUSE -dQUIET -dBATCH -sOutputFile=out.pdf
		// in1.pdf in2.pdf in3.pdf
		final List<String> gsArgs = new ArrayList<String>();
		gsArgs.add("-gs");
		gsArgs.add("-dNOPAUSE");
		gsArgs.add("-dQUIET");
		gsArgs.add("-dBATCH");
		gsArgs.add("-sDEVICE=pdfwrite");
		gsArgs.add("-sOutputFile=" + outputPdfFile.getPath());

		for (final File inputPdfFile : inputPdfFiles) {
			gsArgs.add(inputPdfFile.getPath());
		}

		// execute and exit interpreter
		try {
			gs.initialize(gsArgs.toArray(new String[0]));
			gs.exit();
		} catch (final GhostscriptException e) {
			System.err.println("ERROR: " + e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (final UnsatisfiedLinkError ule) {
			throw new RuntimeException(getMessage(ule.getMessage()));
		} catch (final NoClassDefFoundError ncdfe) {
			throw new RuntimeException(getMessage(ncdfe.getMessage()));
		}
	}

	static String getMessage(final String message) {
		if (message.contains("library 'gs") || message.contains("ghost4j")) {
			return message + GS_INSTALL;
		}
		return message;
	}
}
