/**
 * Copyright @ 2010 Quan Nguyen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.sourceforge.tess4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class TessDllAPI1Test {

	public TessDllAPI1Test() {}

	@BeforeClass
	public static void setUpClass() throws Exception {}

	@AfterClass
	public static void tearDownClass() throws Exception {}

	@Before
	public void setUp() {}

	@After
	public void tearDown() {}

	/**
	 * Test of TessDllBeginPage method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllBeginPage() {
		System.out.println("TessDllBeginPage");
		final int xsize = 0;
		final int ysize = 0;
		final ByteBuffer buf = null;
		final int expResult = 0;
		final int result = TessDllAPI1.TessDllBeginPage(xsize, ysize, buf);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of TessDllBeginPageLang method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllBeginPageLang() {
		System.out.println("TessDllBeginPageLang");
		final int xsize = 0;
		final int ysize = 0;
		final ByteBuffer buf = null;
		final String lang = "";
		final int expResult = 0;
		final int result = TessDllAPI1.TessDllBeginPageLang(xsize, ysize, buf, lang);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of TessDllBeginPageUpright method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllBeginPageUpright() {
		System.out.println("TessDllBeginPageUpright");
		final int xsize = 0;
		final int ysize = 0;
		final ByteBuffer buf = null;
		final String lang = "";
		final int expResult = 0;
		final int result = TessDllAPI1.TessDllBeginPageUpright(xsize, ysize, buf, lang);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of TessDllBeginPageBPP method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllBeginPageBPP() {
		System.out.println("TessDllBeginPageBPP");
		final int xsize = 0;
		final int ysize = 0;
		final ByteBuffer buf = null;
		final byte bpp = 0;
		final int expResult = 0;
		final int result = TessDllAPI1.TessDllBeginPageBPP(xsize, ysize, buf, bpp);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of TessDllBeginPageLangBPP method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllBeginPageLangBPP() {
		System.out.println("TessDllBeginPageLangBPP");
		final int xsize = 0;
		final int ysize = 0;
		final ByteBuffer buf = null;
		final String lang = "";
		final byte bpp = 0;
		final int expResult = 0;
		final int result = TessDllAPI1.TessDllBeginPageLangBPP(xsize, ysize, buf, lang, bpp);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of TessDllBeginPageUprightBPP method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllBeginPageUprightBPP() {
		System.out.println("TessDllBeginPageUprightBPP");
		final int xsize = 0;
		final int ysize = 0;
		final ByteBuffer buf = null;
		final String lang = "";
		final byte bpp = 0;
		final int expResult = 0;
		final int result = TessDllAPI1.TessDllBeginPageUprightBPP(xsize, ysize, buf, lang, bpp);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of TessDllEndPage method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllEndPage() {
		System.out.println("TessDllEndPage");
		TessDllAPI1.TessDllEndPage();
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}

	/**
	 * Test of TessDllRecognize_a_Block method, of class TessDllAPI1.
	 */
	@Test
	public void testTessDllRecognize_a_Block() throws Exception {
		System.out.println("TessDllRecognize_a_Block");
		final String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
		final String lang = "eng";
		final File tiff = new File("eurotext.tif");
		final BufferedImage image = ImageIO.read(tiff); // require jai-imageio
														// lib to read TIFF
		final MappedByteBuffer buf = new FileInputStream(tiff).getChannel().map(MapMode.READ_ONLY, 0, tiff.length());
		final int resultRead = TessDllAPI1.TessDllBeginPageUpright(image.getWidth(), image.getHeight(), buf, lang);
		final ETEXT_DESC output = TessDllAPI1.TessDllRecognize_a_Block(91, 91 + 832, 170, 170 + 614); // (left,
																										// right,
																										// top,
																										// bottom)
																										// specifies
																										// a
																										// rectangle
																										// enclosing
																										// the
																										// text
		final EANYCODE_CHAR[] text = (EANYCODE_CHAR[]) output.text[0].toArray(output.count);

		final List<Byte> unistr = new ArrayList<Byte>();
		int j = 0;

		for (int i = 0; i < output.count; i = j) {
			final EANYCODE_CHAR ch = text[i];

			for (int b = 0; b < ch.blanks; ++b) {
				unistr.add((byte) ' ');
			}

			for (j = i; j < output.count; j++) {
				final EANYCODE_CHAR unich = text[j];

				if (ch.left != unich.left || ch.right != unich.right || ch.top != unich.top
						|| ch.bottom != unich.bottom) {
					break;
				}
				unistr.add(unich.char_code); // aggregate all the utf-8 bytes
												// for all characters
			}

			if ((ch.formatting & 64) == 64) {
				unistr.add((byte) '\n');
			} else if ((ch.formatting & 128) == 128) {
				unistr.add((byte) '\n');
				unistr.add((byte) '\n');
			}
		}

		final byte[] bb = Tesseract.wrapperListToByteArray(unistr);
		final String result = new String(bb, "utf8"); // convert to a string
		System.out.println(result);
		assertEquals(expResult, result.substring(0, expResult.length()));
	}

	/**
	 * Test of TessDllRecognize_all_Words method, of class TessDllAPI1.
	 */
	@Test
	public void testTessDllRecognize_all_Words() throws Exception {
		System.out.println("TessDllRecognize_all_Words");
		final String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
		final String lang = "eng";
		final File tiff = new File("eurotext.tif");
		final BufferedImage image = ImageIO.read(new FileInputStream(tiff)); // require
																				// jai-imageio
																				// lib
																				// to
																				// read
																				// TIFF
		final MappedByteBuffer buf = new FileInputStream(tiff).getChannel().map(MapMode.READ_ONLY, 0, tiff.length());
		final int resultRead = TessDllAPI1.TessDllBeginPageUpright(image.getWidth(), image.getHeight(), buf, lang);
		final ETEXT_DESC output = TessDllAPI1.TessDllRecognize_all_Words();
		final EANYCODE_CHAR[] text = (EANYCODE_CHAR[]) output.text[0].toArray(output.count);

		final StringBuilder sb = new StringBuilder();
		int j = 0;

		for (int i = 0; i < output.count; i = j) {
			final EANYCODE_CHAR ch = text[i];
			final List<Byte> unistr = new ArrayList<Byte>();

			for (int b = 0; b < ch.blanks; ++b) {
				sb.append(" ");
			}

			for (j = i; j < output.count; j++) {
				final EANYCODE_CHAR unich = text[j];

				if (ch.left != unich.left || ch.right != unich.right || ch.top != unich.top
						|| ch.bottom != unich.bottom) {
					break;
				}
				unistr.add(unich.char_code); // aggregate all the utf-8 bytes
												// for a character
			}

			final byte[] bb = Tesseract.wrapperListToByteArray(unistr);
			final String chr = new String(bb, "utf8"); // convert to a char
			sb.append(chr);

			if ((ch.formatting & 64) == 64) {
				sb.append('\n'); // new line
			} else if ((ch.formatting & 128) == 128) {
				sb.append("\n\n"); // new para
			}
		}
		final String result = sb.toString();
		System.out.println(result);
		assertEquals(expResult, result.substring(0, expResult.length()));
	}

	/**
	 * Test of TessDllRelease method, of class TessDllAPI1.
	 */
	@Ignore
	@Test
	public void testTessDllRelease() {
		System.out.println("TessDllRelease");
		TessDllAPI1.TessDllRelease();
		// TODO review the generated test code and remove the default call to
		// fail.
		fail("The test case is a prototype.");
	}
}
