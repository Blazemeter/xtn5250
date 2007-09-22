/*
!!V 14/07/97 rel. 1.02 - added support for 27x132 terminal type (IBM-3477-FC).
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.awt.*;
import java.io.*;
import java.util.*;

import net.infordata.em.tnprot.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * Clear Unit Alternative
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIClearUnitAltCmd extends XI5250Cmd {
  protected int ivPar;

  protected void readFrom5250Stream(InputStream inStream) throws IOException {
    ivPar = Math.max(0, inStream.read());   //!!1.02
  }


  protected void execute() {
    ivEmulator.setState(XI5250Emulator.ST_NORMAL_LOCKED);
    ivEmulator.ivPendingCmd = null;

    if (ivEmulator.ivPrevFont == null) {       //!!1.04a
      Font ft = ivEmulator.getFont();
      ivEmulator.ivPrevFont = new Font(ft.getName(), ft.getStyle(), ft.getSize());  //!!1.03a
    }

    ivEmulator.setCrtSize(132, 27);   //!!1.02

    ivEmulator.setDefAttr(0x20);
    ivEmulator.clear();
    ivEmulator.removeFields();
    ivEmulator.setErrorRow(ivEmulator.getCrtSize().height - 1);
  }
}