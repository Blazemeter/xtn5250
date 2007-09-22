/*
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
 * 5250 Restore screen command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIRestoreScreenCmd extends XI5250Cmd {

  protected int ivPos;


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    ivPos = inStream.read();
    if (ivPos == -1)
      throw new XI5250Exception("Restore screen position required");
  }


  protected void execute() {
    try {
    	/*!!1.06a
      XI5250EmulatorMemento mm = (XI5250EmulatorMemento)ivEmulator.ivSavedScreenList.elementAt(ivPos);
      ivEmulator.ivSavedScreenList.setSize(ivPos + 1);
      */
      //!!1.06a
      XI5250EmulatorMemento mm = ivEmulator.ivSavedScreens[ivPos];

      ivEmulator.restoreMemento(mm);
    }
    catch (ArrayIndexOutOfBoundsException ex) {
    }
  }
}