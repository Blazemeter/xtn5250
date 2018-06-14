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
import java.util.Iterator;

import net.infordata.em.crt5250.XI5250Field;

/**
 * 5250 write to display command
 *
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIWriteToDisplayCmd extends XICCCmd {

  protected XI5250OrdList ivOrdList;

  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    readCC(inStream);

    ivOrdList = ivEmulator.createOrdList(ivEmulator);
    ivOrdList.readFrom5250Stream(inStream);
  }

  @Override
  protected void execute() {
    executeCC1();

    // if format table is going to be altered then enter NORMAL_LOCKED state
    if (ivOrdList.isOrderPresent(XI5250Emulator.ORD_SF) ||
        ivOrdList.isOrderPresent(XI5250Emulator.ORD_SOH)) {
      ivEmulator.setState(XI5250Emulator.ST_NORMAL_LOCKED);
    }

    ivOrdList.execute();

    if (ivEmulator.getState() != XI5250Emulator.ST_NORMAL_UNLOCKED) {
      //if (!ivOrdList.isOrderPresent(XI5250Emulator.ORD_IC))
      if (!ivEmulator.ivCmdList.ivICOrderExecuted) {
        // search first not bypass field
        XI5250Field field;
        boolean     found = false;
        for (Iterator<XI5250Field> e = ivEmulator.getFields().iterator(); e.hasNext(); ) {
          field = e.next();
          if (!field.isBypassField()) {
            ivEmulator.setCursorPos(field.getCol(), field.getRow());
            found = true;
            break;
          }
        }

        if (!found)
          ivEmulator.setCursorPos(0, 0);
      }
    }

    executeCC2();
  }

}
