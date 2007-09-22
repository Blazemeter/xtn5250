/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.io.*;
import java.util.*;

import net.infordata.em.tnprot.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * Clear Format Table
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIClearFmtTableCmd extends XI5250Cmd {

  protected void readFrom5250Stream(InputStream inStream) {
  }


  protected void execute() {
    ivEmulator.setState(XI5250Emulator.ST_NORMAL_LOCKED);
    ivEmulator.ivPendingCmd = null;

    ivEmulator.setDefAttr(0x20);
    ivEmulator.removeFields();
    ivEmulator.setErrorRow(ivEmulator.getCrtSize().height - 1);
  }
}