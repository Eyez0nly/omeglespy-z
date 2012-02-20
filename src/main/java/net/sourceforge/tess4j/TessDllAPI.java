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

import com.sun.jna.*;
import java.nio.ByteBuffer;

/**
 * A Java wrapper for <code>Tesseract OCR DLL</code> using <code>JNA Interface Mapping</code>.
 */
public interface TessDllAPI extends Library {

    /**
     * DLL library name.
     */
    public static final String LIB_NAME = "tessdll";
    /**
     * An instance of the class library.
     */
    public static TessDllAPI INSTANCE = (TessDllAPI) Native.loadLibrary(LIB_NAME, TessDllAPI.class);

    /**
     * Callback for cancel_func.
     */
    public interface CANCEL_FUNC extends Callback {

        boolean invoke(Pointer cancel_this, int words);
    };

    /**
     * BeginPage assumes the first memory address is the bottom of the image (MS DIB format).
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf a buffer of bytes for a 1 bit per pixel bitmap
     * @return
     */
    int TessDllBeginPage(int xsize, int ysize, ByteBuffer buf);

    /**
     * BeginPage assumes the first memory address is the bottom of the image (MS DIB format).
     *
     * @param xsize width of image
     * @param ysize height of image
     * @param buf a buffer of bytes for a 1 bit per pixel bitmap
     * @param lang the code of the language for which the data will be loaded. 
     * Codes follow ISO 639-3 standard. If it is <code>null</code>, English (eng) will be loaded.
     * @return
     */
    int TessDllBeginPageLang(int xsize, int ysize, ByteBuffer buf, String lang);

    /**
     * BeginPageUpright assumes the first memory address is the top of the image (TIFF format).
     * @param xsize width of image
     * @param ysize height of image
     * @param buf a buffer of bytes for a 1 bit per pixel bitmap
     * @param lang the code of the language for which the data will be loaded. 
     * Codes follow ISO 639-3 standard. If it is <code>null</code>, English (eng) will be loaded.
     * @return
     */
    int TessDllBeginPageUpright(int xsize, int ysize, ByteBuffer buf, String lang);

    /**
     * BeginPage assumes the first memory address is the bottom of the image (MS DIB format).
     * @param xsize width of image
     * @param ysize height of image
     * @param buf a buffer of bytes for a bitmap
     * @param bpp bit depth (bits per pixel): 1 for binary bitmap; 8 for gray; 24 for color RGB
     * @return
     */
    int TessDllBeginPageBPP(int xsize, int ysize, ByteBuffer buf, byte bpp);

    /**
     * BeginPage assumes the first memory address is the bottom of the image (MS DIB format).
     * @param xsize width of image
     * @param ysize height of image
     * @param buf a buffer of bytes for a bitmap
     * @param lang the code of the language for which the data will be loaded.
     * Codes follow ISO 639-3 standard. If it is <code>null</code>, English (eng) will be loaded.
     * @param bpp bit depth (bits per pixel): 1 for binary bitmap; 8 for gray; 24 for color RGB
     * @return
     */
    int TessDllBeginPageLangBPP(int xsize, int ysize, ByteBuffer buf, String lang, byte bpp);

    /**
     * BeginPageUpright assumes the first memory address is the top of the image (TIFF format).
     * @param xsize width of image
     * @param ysize height of image
     * @param buf a buffer of bytes for a bitmap
     * @param lang the code of the language for which the data will be loaded.
     * Codes follow ISO 639-3 standard. If it is <code>null</code>, English (eng) will be loaded.
     * @param bpp bit depth (bits per pixel): 1 for binary bitmap; 8 for gray; 24 for color RGB
     * @return
     */
    int TessDllBeginPageUprightBPP(int xsize, int ysize, ByteBuffer buf, String lang, byte bpp);

    /**
     * Ends page.
     */
    void TessDllEndPage();

    /**
     * Recognizes one word or section from the bitmap or the whole page.
     * To extract the whole page, just enter zeros for left, right, top, bottom.
     * Limit of 32000 characters can be returned.<br />
     * Note: Getting one word at time is not yet optimized for speed.
     * @param left left of block
     * @param right right of block
     * @param top top of block
     * @param bottom bottom of block
     * @return
     */
    ETEXT_DESC TessDllRecognize_a_Block(int left, int right, int top, int bottom);

    /**
     * Recognizes the whole page.
     * Limit of 32000 characters can be returned.
     * @return
     */
    ETEXT_DESC TessDllRecognize_all_Words();

    /**
     * Releases any memory associated with the recognize class object.
     */
    void TessDllRelease();
}
