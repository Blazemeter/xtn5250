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
 * 5250 Read Immediate
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIReadImmediateCmd extends XI5250Cmd {

  protected void readFrom5250Stream(InputStream inStream) throws IOException {
  }


  protected void execute() {
    ivEmulator.send5250Data(0x00, ivEmulator.isMasterMDTSet(), false);
  }
}