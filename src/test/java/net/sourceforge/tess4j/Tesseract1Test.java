/**
 * Copyright @ 2010 Quan Nguyen
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
package net.sourceforge.tess4j;

import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;

import net.sourceforge.vietocr.ImageIOHelper;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class Tesseract1Test {

	public Tesseract1Test() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of doOCR method, of class Tesseract1.
	 */
	@Test
	public void testDoOCR_File() throws Exception {
		System.out.println("doOCR on a PNG image");
		final File imageFile = new File("eurotext.png");
		final Tesseract1 instance = new Tesseract1();
		final String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
		final String result = instance.doOCR(imageFile);
		System.out.println(result);
		assertEquals(expResult, result.substring(0, expResult.length()));
	}

	/**
	 * Test of doOCR method, of class Tesseract1.
	 */
	@Test
	public void testDoOCR_File_Rectangle() throws Exception {
		System.out.println("doOCR on a BMP image with bounding rectangle");
		final File imageFile = new File("eurotext.bmp");
		final Rectangle rect = new Rectangle(0, 0, 1024, 800);
		final Tesseract1 instance = new Tesseract1();
		final String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
		final String result = instance.doOCR(imageFile, rect);
		System.out.println(result);
		assertEquals(expResult, result.substring(0, expResult.length()));
	}

	/**
	 * Test of doOCR method, of class Tesseract1.
	 */
	@Test
	public void testDoOCR_List_Rectangle() throws Exception {
		System.out.println("doOCR on a PDF document");
		final File imageFile = new File("eurotext.pdf");
		final List<IIOImage> imageList = ImageIOHelper.getIIOImageList(imageFile);
		final Tesseract1 instance = new Tesseract1();
		final String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
		final String result = instance.doOCR(imageList, null);
		System.out.println(result);
		assertEquals(expResult, result.substring(0, expResult.length()));
	}

	/**
	 * Test of doOCR method, of class Tesseract1.
	 */
	@Test
	public void testDoOCR_BufferedImage() throws Exception {
		System.out.println("doOCR on a buffered image of a GIF");
		final File imageFile = new File("eurotext.gif");
		final BufferedImage bi = ImageIO.read(imageFile);
		final Tesseract1 instance = new Tesseract1();
		final String expResult = "The (quick) [brown] {fox} jumps!\nOver the $43,456.78 <lazy> #90 dog";
		final String result = instance.doOCR(bi);
		System.out.println(result);
		assertEquals(expResult, result.substring(0, expResult.length()));
	}

}