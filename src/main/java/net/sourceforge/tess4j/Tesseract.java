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

import java.awt.Rectangle;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import javax.imageio.IIOImage;
import net.sourceforge.vietocr.ImageIOHelper;

/**
 * An object layer on top of <code>TessDllAPI</code>, provides character recognition support for common image formats, and multi-page TIFF images beyond the uncompressed,
 * binary TIFF format supported by Tesseract OCR engine. The extended capabilities are provided by the <code>Java Advanced Imaging Image I/O Tools</code>.
 * <br /><br />
 * Support for PDF documents is available through <code>Ghost4J</code>, a <code>JNA</code> wrapper for <code>GPL Ghostscript</code>, which should be installed and included in system path.
 * <br /><br />
 * Any program that uses the library will need to ensure that the required libraries (the <code>.jar</code> files for <code>jna</code>, <code>jai-imageio</code>, and <code>ghost4j</code>) are in its compile and run-time <code>classpath</code>.
 */
public class Tesseract {

    private static Tesseract instance;
    private final static Rectangle EMPTY_RECTANGLE = new Rectangle();
    private String language = "eng";

    /**
     * Private constructor.
     */
    private Tesseract() {
    }

    /**
     * Gets an instance of the class library.
     * @return instance
     */
    public static synchronized Tesseract getInstance() {
        if (instance == null) {
            instance = new Tesseract();
        }

        return instance;
    }

    /**
     * Sets language for OCR.
     *
     * @param language the language code, which follows ISO 639-3 standard.
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Performs OCR operation.
     *
     * @param imageFile an image file
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(File imageFile) throws TesseractException {
        return doOCR(imageFile, null);
    }

    /**
     * Performs OCR operation.
     *
     * @param imageFile an image file
     * @param rect the bounding rectangle defines the region of the image to be recognized. A rectangle of zero dimension or <code>null</code> indicates the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(File imageFile, Rectangle rect) throws TesseractException {
        try {
            return doOCR(ImageIOHelper.getIIOImageList(imageFile), rect);
        } catch (IOException ioe) {
            throw new TesseractException(ioe);
        }
    }

    /**
     * Performs OCR operation.
     *
     * @param bi a buffered image
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(BufferedImage bi) throws TesseractException {
        return doOCR(bi, null);
    }

    /**
     * Performs OCR operation.
     * 
     * @param bi a buffered image
     * @param rect the bounding rectangle defines the region of the image to be recognized. A rectangle of zero dimension or <code>null</code> indicates the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(BufferedImage bi, Rectangle rect) throws TesseractException {
        IIOImage oimage = new IIOImage(bi, null, null);
        List<IIOImage> imageList = new ArrayList<IIOImage>();
        imageList.add(oimage);
        return doOCR(imageList, rect);
    }

    /**
     * Performs OCR operation.
     * 
     * @param imageList a list of <code>IIOImage</code> objects
     * @param rect the bounding rectangle defines the region of the image to be recognized. A rectangle of zero dimension or <code>null</code> indicates the whole image.
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(List<IIOImage> imageList, Rectangle rect) throws TesseractException {
        StringBuilder sb = new StringBuilder();

        for (IIOImage oimage : imageList) {
            try {
                ByteBuffer buf = ImageIOHelper.getImageByteBuffer(oimage);
                RenderedImage ri = oimage.getRenderedImage();
                String pageText = doOCR(ri.getWidth(), ri.getHeight(), buf, rect, ri.getColorModel().getPixelSize());
                sb.append(pageText);
            } catch (IOException ioe) {
                //skip the problematic image
                System.err.println(ioe.getMessage());
            }
        }

        return sb.toString();
    }

    /**
     * Performs OCR operation.
     * 
     * @param xsize width of image
     * @param ysize height of image
     * @param buf pixel data
     * @param rect the bounding rectangle defines the region of the image to be recognized. A rectangle of zero dimension or <code>null</code> indicates the whole image.
     * @param bpp bits per pixel, represents the bit depth of the image, with 1 for binary bitmap, 8 for gray, and 24 for color RGB.
     * @return the recognized text
     * @throws TesseractException
     */
    public String doOCR(int xsize, int ysize, ByteBuffer buf, Rectangle rect, int bpp) throws TesseractException {
        TessDllAPI api = TessDllAPI.INSTANCE;
        int resultRead = api.TessDllBeginPageUprightBPP(xsize, ysize, buf, language, (byte) bpp);

        if (resultRead == 0) {
            return null; // can't read image
        }

        ETEXT_DESC output;

        if (rect == null || rect.equals(EMPTY_RECTANGLE)) {
            output = api.TessDllRecognize_all_Words();
        } else {
            // (left, right, top, bottom) specifies a region enclosing the text
            output = api.TessDllRecognize_a_Block(rect.x, rect.x + rect.width, rect.y, rect.y + rect.height);
        }

        final short count = output.count;

        EANYCODE_CHAR[] text = (EANYCODE_CHAR[]) output.text[0].toArray(count);

        List<Byte> unistr = new ArrayList<Byte>();
        int j = 0;

        for (int i = 0; i < count; i = j) {
            final EANYCODE_CHAR ch = text[i];

            for (int b = 0; b < ch.blanks; ++b) {
                unistr.add((byte) ' ');
            }

            for (j = i; j < count; j++) {
                final EANYCODE_CHAR unich = text[j];

                if (ch.left != unich.left || ch.right != unich.right || ch.top != unich.top || ch.bottom != unich.bottom) {
                    break;  // bytes making up the Unicode character have the same coordinates
                }
                unistr.add(unich.char_code); // aggregate all the utf-8 bytes of a character
            }

            if ((ch.formatting & 64) == 64) {
                // new line
                unistr.add((byte) '\n');
            } else if ((ch.formatting & 128) == 128) {
                // new paragraph
                unistr.add((byte) '\n');
                unistr.add((byte) '\n');
            }
        }

        try {
            return new String(wrapperListToByteArray(unistr), "utf8");
        } catch (UnsupportedEncodingException uee) {
            throw new TesseractException(uee);
        }
    }

    /**
     * A utility method to convert a generic Byte list to a byte array.
     * 
     * @param list a List<Byte>
     * @return an array of bytes
     */
    public static byte[] wrapperListToByteArray(List<Byte> list) {
        int size = list.size();
        byte[] byteArray = new byte[size];
        for (int i = 0; i < size; i++) {
            byteArray[i] = list.get(i);
        }
        return byteArray;
    }
}
