/*
!!V 04/06/97 rel. 1.00b- uses XI5250Emulator create5250Field factory method.
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
 * 5250 SF Order
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XISFOrd extends XI5250Ord {

  protected byte[] FFW = new byte[2];
  protected byte[] FCW = new byte[2];
  protected byte   ivScreenAttr;
  protected int    ivFieldLen;


  protected void readFrom5250Stream(InputStream inStream) throws IOException {
    byte bb;

    inStream.mark(1);
    bb = (byte)Math.max(0, inStream.read());
    // check if FFW is present
    if ((bb & 0xC0) == 0x40) {
      FFW[0] = bb;
      FFW[1] = (byte)Math.max(0, inStream.read());

      inStream.mark(1);
      bb = (byte)Math.max(0, inStream.read());
      // check if FCW is present
      if ((bb & 0xC0) == 0x80) {
        FCW[0] = bb;
        FCW[1] = (byte)Math.max(0, inStream.read());
      }
      else
        inStream.reset();
    }
    else
      inStream.reset();

    ivScreenAttr = (byte)Math.max(0, inStream.read());
    ivFieldLen = (Math.max(0, inStream.read()) << 8) + Math.max(0, inStream.read());
    //!!V effettuare check dei parametri
  }


  protected void execute() {
    if (ivScreenAttr != 0) {
      //NO ivEmulator.setDefAttr(XITelnet.toInt(ivScreenAttr));
      ivEmulator.drawString(String.valueOf(XI5250Emulator.ATTRIBUTE_PLACE_HOLDER),
                            ivEmulator.getSBACol(), ivEmulator.getSBARow(),
                            ivScreenAttr);
      ivEmulator.setSBA(ivEmulator.getSBA() + 1);
    }

    // -1 to force attribute reload
    ivEmulator.addField(ivEmulator.create5250Field((byte[])FFW.clone(),
                                                   (byte[])FCW.clone(),
                                                   ivEmulator.getSBACol(),
                                                   ivEmulator.getSBARow(),
                                                   ivFieldLen, -1));
  }


  public String toString() {
    return super.toString() + " [FFW=[" + XITelnet.toHex(FFW[0]) + "," +
        XITelnet.toHex(FFW[1]) + "]," +
        "FCW=[" + XITelnet.toHex(FCW[0]) + "," +
        XITelnet.toHex(FCW[1]) + "]," +
        XITelnet.toHex(ivScreenAttr) + "," + ivFieldLen + "]";
  }
}