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

import com.sun.jna.Structure;

/**
 *  It should be noted that the format for char_code for version 2.0 and beyond is UTF-8,
 *  which means that ASCII characters will come out as one structure but other characters
 *  will be returned in two or more instances of this structure with a single byte of the
 *  UTF-8 code in each, but each will have the same bounding box.<br /><br />
 *  Programs which want to handle languages with different characters sets will need to
 *  handle extended characters appropriately, but <strong>all</strong> code needs to be prepared to
 *  receive UTF-8 coded characters for characters such as bullet and fancy quotes.
 */
public class EANYCODE_CHAR extends Structure {

    /** character itself, one single UTF-8 byte long.
     * A Unicode character may consist of one or more UTF-8 bytes.
     * Bytes of a character will have the same bounding box.
     */
    public byte char_code;
    /** left of char (-1) */
    public short left;
    /** right of char (-1) */
    public short right;
    /** top of char (-1) */
    public short top;
    /** bottom of char (-1) */
    public short bottom;
    /** what font (0) */
    public short font_index;
    /** classification confidence: 0=perfect, 100=reject (0/100) */
    public byte confidence;
    /** point size of char, 72 = 1 inch, (10) */
    public byte point_size;
    /** number of spaces before this char (1) */
    public byte blanks;
    /** char formatting (0) */
    public byte formatting;
}
