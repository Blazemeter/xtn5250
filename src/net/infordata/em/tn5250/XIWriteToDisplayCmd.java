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
 * 5250 write to display command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIWriteToDisplayCmd extends XICCCmd {

  protected XI5250OrdList ivOrdList;


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    readCC(inStream);

    ivOrdList = ivEmulator.createOrdList(ivEmulator);
    ivOrdList.readFrom5250Stream(inStream);
  }


  /**
   */
  protected void execute() {
    //ivEmulator.setSBA(0, 0);

    executeCC1();

    // if format table is going to be altered then enter NORMAL_LOCKED state
    if (ivOrdList.isOrderPresent(XI5250Emulator.ORD_SF) ||
        ivOrdList.isOrderPresent(XI5250Emulator.ORD_SOH)) {
      ivEmulator.setState(XI5250Emulator.ST_NORMAL_LOCKED);
    }

    ivOrdList.execute();

    if (ivEmulator.getState() != XI5250Emulator.ST_NORMAL_UNLOCKED) {
      //if (!ivOrdList.isOrderPresent(XI5250Emulator.ORD_IC))
      if (!ivEmulator.ivCmdList.ivICOrderExecuted) {
        // search first not bypass field
        XI5250Field field;
        boolean     found = false;
        for (Enumeration e = ivEmulator.getFields(); e.hasMoreElements(); ) {
          field = (XI5250Field)e.nextElement();
          if (!field.isBypassField()) {
            ivEmulator.setCursorPos(field.getCol(), field.getRow());
            found = true;
            break;
          }
        }

        if (!found)
          ivEmulator.setCursorPos(0, 0);
      }
    }

    executeCC2();
  }
}