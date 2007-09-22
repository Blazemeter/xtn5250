/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.io.*;
import java.util.*;

import net.infordata.em.tnprot.*;



////////////////////////////////////////////////////////////////////////////////

/**
 * 5250 Read fields
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIReadFieldsCmd extends XICCCmd {
  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    readCC(inStream);
  }


  protected void execute() {
    ivEmulator.ivPendingCmd = this;
    executeCC1();
    executeCC2();
  }

  protected void executePending(int anAidCode, boolean isMasked) {
    ivEmulator.setState(XI5250Emulator.ST_TEMPORARY_LOCK);
    ivEmulator.send5250Data(anAidCode, ivEmulator.isMasterMDTSet() && !isMasked, false);
    //!!0.92a ivEmulator.ivPendingCmd = null;
    //ivEmulator.removeFields();
  }
}