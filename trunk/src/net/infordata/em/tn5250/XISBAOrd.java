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
 * 5250 SBA Order
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XISBAOrd extends XI5250Ord {

  protected int ivRow, ivCol;


  protected void readFrom5250Stream(InputStream inStream) throws IOException {
    ivRow = Math.max(0, inStream.read());
    ivCol = Math.max(0, inStream.read());
    //!!V effettuare check dei parametri
  }


  protected void execute() {
    ivEmulator.setSBA(ivCol - 1, ivRow - 1);
  }


  public String toString() {
    return super.toString() + " [" + ivRow + "," + ivCol + "]";
  }
}