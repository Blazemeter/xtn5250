/*
Copyright 2007 Infordata S.p.A.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.tn5250;

import java.io.IOException;
import java.io.InputStream;

import net.infordata.em.tnprot.XITelnet;

/**
 * EA - Erase to address TODO
 *
 * see: http://publibfp.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/co2e2001/15.6.8?DT=19950629163252
 *
 * @author Valentino Proietti - Infordata S.p.A.
 */
public class XIEAOrd extends XI5250Ord {

  protected int ivRow;
  protected int ivCol;
  protected int ivLen;

  protected byte[] ivAttributeTypes;

  /**
   * @param inStream the stream from where to read the order from.
   * @throws XI5250Exception raised if order parameters are wrong.
   * @throws IOException raised when there is an input/output problem.
   */
  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    byte[] buf = new byte[3];
    if (inStream.read(buf) < buf.length) {
      throw new XI5250Exception("EOF reached", XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
    }
    ivRow = XITelnet.toInt(buf[0]);
    ivCol = XITelnet.toInt(buf[1]);
    ivLen = XITelnet.toInt(buf[2]);

    if (ivLen < 2 || ivLen > 5) {
      throw new XI5250Exception("Invalid len: " + ivLen, XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
    }
    ivLen--;
    ivAttributeTypes = new byte[ivLen];
    if (inStream.read(ivAttributeTypes) < ivLen) {
      throw new XI5250Exception("EOF reached", XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
    }
  }

  @Override
  protected void execute() {
    //TODO
    throw new IllegalStateException("Not supported");
  }

  @Override
  public String toString() {
    return super.toString() + " [" + ivRow + "," + ivCol + "," + ivLen + "," + ",[" +
        XITelnet.toHex(ivAttributeTypes) + "]" + "]";
  }

}
