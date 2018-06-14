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

/**
 * 5250 Roll command
 *
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIRollCmd extends XI5250Cmd {

  boolean ivDown;
  int     ivNRows;
  int     ivTopRow;
  int     ivBottomRow;

  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    int[] bb = new int[3];
    int   i;

    for (i = 0; i < 3 && (bb[i] = inStream.read()) >= 0; i++)
      ;

    if (i < 3)
      throw new XI5250Exception("Roll parameter missing", 
          XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);

    ivDown = ((bb[0] & 0x80) != 0);

    ivNRows = (bb[0] & 0x1F);

    ivTopRow = bb[1];
    ivBottomRow = bb[2];

    if (ivTopRow > ivBottomRow)
      throw new XI5250Exception("TopRow greater then BottomRow", 
          XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
  }

  @Override
  protected void execute() {
    ivEmulator.scroll(ivDown, ivTopRow - 1, ivBottomRow, ivNRows);
  }

}
