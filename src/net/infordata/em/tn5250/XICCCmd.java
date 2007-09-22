/*
    01/09/97 rel. 1.04a- executeCC1() implemented.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.awt.*;
import java.io.*;
import java.util.*;

import net.infordata.em.crt5250.*;
import net.infordata.em.tnprot.*;



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
  protected abstract void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception;


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
      throw new XI5250Exception("CC required");
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
      for (Enumeration e = ivEmulator.getFields(); e.hasMoreElements(); ) {
        field = (XI5250Field)e.nextElement();
        if (!field.isOrgBypassField())
          field.resetMDT();
      }
    }

    // clear master mdt; reset mdt in all fields
    if (cc1 == 0x60 || cc1 == 0xE0) {
      //!!V gestire master mdt
      XI5250Field field;
      for (Enumeration e = ivEmulator.getFields(); e.hasMoreElements(); ) {
        field = (XI5250Field)e.nextElement();
        field.resetMDT();
      }
    }

    // null nonbypass-fields with mdt on
    if (cc1 == 0x80 || cc1 == 0xC0) {
      XI5250Field field;
      for (Enumeration e = ivEmulator.getFields(); e.hasMoreElements(); ) {
        field = (XI5250Field)e.nextElement();
        if (!field.isOrgBypassField() && field.isMDTOn())
          field.clear();
      }
    }

    // null all nonbypass-fields
    if (cc1 == 0xA0 || cc1 == 0xE0) {
      XI5250Field field;
      for (Enumeration e = ivEmulator.getFields(); e.hasMoreElements(); ) {
        field = (XI5250Field)e.nextElement();
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


  public String toString() {
    return super.toString() + " [CC=[" + XITelnet.toHex(ivCC[0]) + "," + XITelnet.toHex(ivCC[1]) + "]]";
  }
}