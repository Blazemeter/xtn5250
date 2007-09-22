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
 * 5250 Roll command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIRollCmd extends XI5250Cmd {

  boolean ivDown;
  int     ivNRows;
  int     ivTopRow;
  int     ivBottomRow;


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    int[] bb = new int[3];
    int   i;

    for (i = 0; i < 3 && (bb[i] = inStream.read()) >= 0; i++)
      ;

    if (i < 3)
      throw new XI5250Exception("Roll parameter missing");

    ivDown = ((bb[0] & 0x80) != 0);

    ivNRows = (bb[0] & 0x1F);

    ivTopRow = bb[1];
    ivBottomRow = bb[2];

    if (ivTopRow > ivBottomRow)
      throw new XI5250Exception("TopRow greater then BottomRow");
  }


  protected void execute() {
    ivEmulator.scroll(ivDown, ivTopRow - 1, ivBottomRow, ivNRows);
  }
}