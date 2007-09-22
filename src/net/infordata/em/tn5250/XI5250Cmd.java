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
 * Abstract class for all 5250 commands
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public abstract class XI5250Cmd {

  protected XI5250Emulator ivEmulator;


  protected void init(XI5250Emulator aEmulator) {
    ivEmulator = aEmulator;
  }


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected abstract void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception;


  protected abstract void execute();


  protected void executePending(int anAidCode, boolean isMasked) {
    throw new RuntimeException("executePending() not supported");
  }
}