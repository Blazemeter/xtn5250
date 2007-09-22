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
 * 5250 RA Order
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIRAOrd extends XI5250Ord {

  protected int  ivEndRow, ivEndCol;
  protected char ivChar;


  protected void readFrom5250Stream(InputStream inStream) throws IOException {
    ivEndRow = Math.max(0, inStream.read());
    ivEndCol = Math.max(0, inStream.read());
    ivChar = XIEbcdicTranslator.
                 getTranslator().toChar((byte)Math.max(0, inStream.read()));
    //!!V effettuare check dei parametri
  }


  protected void execute() {
    int start = ivEmulator.toLinearPos(ivEmulator.getSBACol(), ivEmulator.getSBARow());
    int end   = ivEmulator.toLinearPos(ivEndCol - 1, ivEndRow - 1);
    String str = "";
    for (int i = 0; i < (end - start + 1); i++)
      str += ivChar;

    ivEmulator.drawString(str, ivEmulator.getSBACol(), ivEmulator.getSBARow());
    ivEmulator.setSBA(ivEmulator.getSBA() + (end - start + 1));
  }


  public String toString() {
    return super.toString() +
           " [" + ivEndRow + "," + ivEndCol + ",'" + ivChar + "'" + "]";
  }
}