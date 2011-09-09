/*
 * Copyright (c) 1998 - 2011. University Corporation for Atmospheric Research/Unidata
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package ucar.nc2.grib.grib1;

import ucar.nc2.grib.GribNumbers;
import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;

/**
 * Description
 *
 * @author John
 * @since 9/3/11
 */
public class Grib1SectionBitMap {
  private final long startingPosition;

  public Grib1SectionBitMap(RandomAccessFile raf) throws IOException {
    startingPosition = raf.getFilePointer();

    // octets 1-3 (Length of section)
    int length = GribNumbers.int3(raf);

    raf.seek(startingPosition + length);
  }

  public Grib1SectionBitMap(long startingPosition) {
    this.startingPosition = startingPosition;
  }

  public long getStartingPosition() {
    return startingPosition;
  }

  /**
   * Read the bitmap array when needed
   */
  public boolean[] getBitmap(RandomAccessFile raf) throws IOException {
    if (startingPosition <= 0) return null;

    raf.seek(startingPosition);

    // octet 1-3 (length of section)
    int length = GribNumbers.uint3(raf);

    // octet 4 unused bits
    int unused = raf.read();

    // octets 5-6
    int bm = raf.readShort();
    if (bm != 0) {
      System.out.println("BMS pre-defined BM provided by center");
      if ((length - 6) == 0) {
        return null; // LOOK
      }
      byte[] data = new byte[length - 6];
      raf.read(data);
      return null; // LOOK
    }

    // read the bits as integers
    byte[] data = new byte[length - 6];
    raf.read(data);

    // create new bit map, octet 4 contains number of unused bits at the end
    boolean[] bitmap = new boolean[(length - 6) * 8 - unused];

    // fill bit map
    int[] bitmask = {128, 64, 32, 16, 8, 4, 2, 1};
    for (int i = 0; i < bitmap.length; i++) {
      bitmap[i] = (data[i / 8] & bitmask[i % 8]) != 0;
    }

    return bitmap;
  }

}
