/*
!!V 15/07/97 rel. 1.02c- XIDataOrd includes 0x1F char.
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
 * 5250 Data Order
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIDataOrd extends XI5250Ord {

  protected String ivData;
  protected byte   ivColor;


  protected void readFrom5250Stream(InputStream inStream) throws IOException {
    XIEbcdicTranslator translator = XIEbcdicTranslator.getTranslator();
    int bb;

    ivColor = 0;
    ivData = "";

    for (int i = 0;  ; i++) {
      inStream.mark(1);
      bb = inStream.read();

      if (bb == -1)
        break;

      // see IBM SA21-9247-6 pg. 2.13
      if (bb == 0x00 || bb == 0x1C || bb >= 0x1F) {
        // is it a color ?
        if (bb > 0x1F && bb <= 0x3F) {
          if (i == 0)
            ivColor = (byte)bb;
          else {
            // cut string if different color
            inStream.reset();
            break;
          }
        }
        else
          ivData += translator.toChar((byte)bb);
      }
      else {
        inStream.reset();
        break;
      }
    }
  }


  protected void execute() {
    if (ivColor != 0) {
      ivEmulator.setDefAttr(XITelnet.toInt(ivColor));
      ivEmulator.drawString(String.valueOf(XI5250Emulator.ATTRIBUTE_PLACE_HOLDER),
                            ivEmulator.getSBACol(), ivEmulator.getSBARow());
      ivEmulator.setSBA(ivEmulator.getSBA() + 1);
    }
    ivEmulator.drawString(ivData, ivEmulator.getSBACol(), ivEmulator.getSBARow());
    ivEmulator.setSBA(ivEmulator.getSBA() + ivData.length());
  }


  public String toString() {
    return super.toString() + " [" + XITelnet.toHex(ivColor) + "," + ",\"" + ivData + "\"" + "]";
  }
}