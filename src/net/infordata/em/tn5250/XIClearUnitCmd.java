/*
!!V 14/07/97 rel. 1.02 - added support for 27x132 terminal type (IBM-3477-FC).
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.io.*;
import java.util.*;

import net.infordata.em.crt.*;
import net.infordata.em.tnprot.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 Clear unit command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIClearUnitCmd extends XI5250Cmd {

  protected void readFrom5250Stream(InputStream inStream) {
  }


  protected void execute() {
    ivEmulator.setState(XI5250Emulator.ST_NORMAL_LOCKED);
    ivEmulator.ivPendingCmd = null;

    ivEmulator.setCrtSize(80, 24);   //!!1.02

    ivEmulator.setDefAttr(0x20);
    ivEmulator.clear();
    ivEmulator.removeFields();
    ivEmulator.setErrorRow(ivEmulator.getCrtSize().height - 1);

    // switch back to the previous used font
    if (ivEmulator.ivPrevFont != null) { //!!1.03a
      ivEmulator.setFont(ivEmulator.ivPrevFont);
      ivEmulator.ivPrevFont = null;
    }
  }
}