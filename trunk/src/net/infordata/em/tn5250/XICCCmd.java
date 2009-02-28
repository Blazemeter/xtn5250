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
    01/09/97 rel. 1.04a- executeCC1() implemented.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tnprot.XITelnet;



///////////////////////////////////////////////////////////////////////////////

/**
 * Abstract base class for all 5250 commands with CC parameter.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public abstract class XICCCmd extends XI5250Cmd {

  protected byte[]  ivCC;


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  @Override
  protected abstract void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception;


  @Override
  protected abstract void execute();


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected void readCC(InputStream inStream)
      throws IOException, XI5250Exception {

    int    bb;
    int    i;
    ivCC = new byte[2];

    for (i = 0; (i < 2) && ((bb = inStream.read()) != -1); i++)
      ivCC[i] = (byte)bb;

    if (i < 2)
      throw new XI5250Exception("CC required", XI5250Emulator.ERR_INVALID_COMMAND);
  }


  protected void executeCC1() {
    //!!1.04a
    int cc1 = ivCC[0] & 0xE0;

    // reset pending aid; lock keyboard
    if (cc1 != 0) {
      ivEmulator.ivPendingCmd = null;
      ivEmulator.setState(XI5250Emulator.ST_NORMAL_LOCKED);
    }

    // clear master mdt; reset mdt in nonbypass-fields
    if (cc1 == 0x40 || cc1 == 0xA0 || cc1 == 0xC0) {
      //!!V gestire master mdt
      XI5250Field field;
      for (Iterator<XI5250Field> e = ivEmulator.getFields().iterator(); e.hasNext(); ) {
        field = e.next();
        if (!field.isOrgBypassField())
          field.resetMDT();
      }
    }

    // clear master mdt; reset mdt in all fields
    if (cc1 == 0x60 || cc1 == 0xE0) {
      //!!V gestire master mdt
      XI5250Field field;
      for (Iterator<XI5250Field> e = ivEmulator.getFields().iterator(); e.hasNext(); ) {
        field = e.next();
        field.resetMDT();
      }
    }

    // null nonbypass-fields with mdt on
    if (cc1 == 0x80 || cc1 == 0xC0) {
      XI5250Field field;
      for (Iterator<XI5250Field> e = ivEmulator.getFields().iterator(); e.hasNext(); ) {
        field = e.next();
        if (!field.isOrgBypassField() && field.isMDTOn())
          field.clear();
      }
    }

    // null all nonbypass-fields
    if (cc1 == 0xA0 || cc1 == 0xE0) {
      XI5250Field field;
      for (Iterator<XI5250Field> e = ivEmulator.getFields().iterator(); e.hasNext(); ) {
        field = e.next();
        if (!field.isBypassField())
          field.clear();
      }
    }
  }


  protected void executeCC2() {
    if ((ivCC[1] & 0x10) != 0)
      ivEmulator.setBlinkingCursor(true);
    else if ((ivCC[1] & 0x20) != 0)
      ivEmulator.setBlinkingCursor(false);

    // unlock the keyboard
    if ((ivCC[1] & 0x08) != 0)
      ivEmulator.setState(XI5250Emulator.ST_NORMAL_UNLOCKED);

    if ((ivCC[1] & 0x04) != 0)
      Toolkit.getDefaultToolkit().beep();
  }


  @Override
  public String toString() {
    return super.toString() + " [CC=[" + XITelnet.toHex(ivCC[0]) + "," + XITelnet.toHex(ivCC[1]) + "]]";
  }
}