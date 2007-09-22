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
 * Abstract base class for all 5250 Orders
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public abstract class XI5250Ord {
  protected XI5250Emulator ivEmulator;


  protected void init(XI5250Emulator aEmulator) {
    ivEmulator = aEmulator;
  }


  /**
   * @exception    XI5250Exception    raised if order parameters are wrong.
   */
  protected abstract void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception;


  protected abstract void execute();
}