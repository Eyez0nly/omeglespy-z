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
import net.sourceforge.tess4j.TessDllAPI.CANCEL_FUNC;

/**
 * Description of the output of the OCR engine.
 * This structure is used as both a progress monitor and the final
 * output header, since it needs to be a valid progress monitor while
 * the OCR engine is storing its output to shared memory.<br /><br />
 * During progress, all the buffer info is -1. 
 * Progress starts at 0 and increases to 100 during OCR. No other constraint.
 * Every progress callback, the OCR engine must set <code>ocr_alive</code> to 1. 
 * The HP side will set <code>ocr_alive</code> to 0. Repeated failure to reset
 * to 1 indicates that the OCR engine is dead.<br /><br />
 * If the cancel function is not null, then it is called with the number of
 * user words found. If it returns true, then operation is canceled.
 */
public class ETEXT_DESC extends Structure {

    /** chars in this buffer(0). Total number of UTF-8 bytes for this run. */
    public short count;
    /** percent complete increasing (0-100) */
    public short progress;
    /** true if not last */
    public byte more_to_come;
    /** ocr sets to 1, HP 0 */
    public byte ocr_alive;
    /** for errcode use */
    public byte err_code;
    /** returns true to cancel */
    public CANCEL_FUNC cancel;
    /** this or other data for cancel */
    public Pointer cancel_this;
    /** time to stop if not 0 */
    public NativeLong end_time;
    /** character data */
    public EANYCODE_CHAR[] text = new EANYCODE_CHAR[1];
}
