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


///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 RA Order
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIRAOrd extends XI5250Ord {

  protected int  ivEndRow, ivEndCol;
  protected char ivChar;


  @Override
  protected void readFrom5250Stream(InputStream inStream) throws IOException, XI5250Exception {
    ivEndRow = Math.max(0, inStream.read());
    ivEndCol = Math.max(0, inStream.read());
    ivChar = ivEmulator.getTranslator().toChar((byte)Math.max(0, inStream.read()));
    // Cannot deal with real dimensions, since they can be not applied yet 
    if (ivEndRow <= 0 || ivEndRow > XI5250Emulator.MAX_ROWS || 
        ivEndCol <= 0 || ivEndCol > XI5250Emulator.MAX_COLS)
      throw new XI5250Exception("Invalid screen coord: " + ivEndRow + "," + ivEndCol, 
          XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
  }


  @Override
  protected void execute() {
    int start = ivEmulator.toLinearPos(ivEmulator.getSBACol(), ivEmulator.getSBARow());
    int end   = ivEmulator.toLinearPos(ivEndCol - 1, ivEndRow - 1);
    StringBuilder str = new StringBuilder(end - start + 1);
    for (int i = 0; i < (end - start + 1); i++)
      str.append(ivChar);

    ivEmulator.drawString(str.toString(), ivEmulator.getSBACol(), ivEmulator.getSBARow());
    ivEmulator.setSBA(ivEmulator.getSBA() + (end - start + 1));
  }


  @Override
  public String toString() {
    return super.toString() +
           " [" + ivEndRow + "," + ivEndCol + ",'" + ivChar + "'" + "]";
  }
}