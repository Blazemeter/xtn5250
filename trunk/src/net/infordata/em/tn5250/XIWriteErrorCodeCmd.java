/*
!!V 12/05/97 rel. 0.95d- setDefAttr() to 0x20.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.io.*;
import java.util.*;

import net.infordata.em.crt5250.*;
import net.infordata.em.tnprot.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 Write error code command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIWriteErrorCodeCmd extends XI5250Cmd {

  protected XI5250OrdList ivOrdList;


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    ivOrdList = ivEmulator.createOrdList(ivEmulator);
    ivOrdList.readFrom5250Stream(inStream);
  }


  protected void execute() {
    ivEmulator.setState(XI5250Emulator.ST_PRE_HELP);

    ivEmulator.setDefAttr(0x20);   //!!0.95d

    ivEmulator.setSBA(0, ivEmulator.getErrorRow());
    ivOrdList.execute();
  }
}