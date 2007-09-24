/*
Copyright 2007 Infordata S.p.A.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.io.*;

import net.infordata.em.tnprot.*;


///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 SOH Order
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XISOHOrd extends XI5250Ord {

  protected byte[] ivData;
  protected int    ivLen;


  /**
   * @exception    XI5250Exception    raised if order parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    int  i = 0;
    int  bb;

    ivLen = inStream.read();
    if (ivLen > 0) {
      ivData = new byte[ivLen];
      for (i = 0; (i < ivLen) && ((bb = inStream.read()) != -1); i++)
        ivData[i] = (byte)bb;
    }
    // parameters check
    if (ivLen < 0 || ivLen > 255 || i < ivLen)
      throw new XI5250Exception("Bad SOH Order");
  }


  protected void execute() {
    // I didn' t found them on docs, but i need them
    ivEmulator.ivCmdList.ivICOrderExecuted = false;
    ivEmulator.removeFields();

    ivEmulator.ivPendingCmd = null;                      //!!0.92a

    if (ivLen >= 2) {
      // resequencing byte present
      if (ivLen >= 3) {
        // error line address present
        if (ivLen >= 4) {
          ivEmulator.setErrorRow(ivData[3] - 1);
          // function keys mask present
          if (ivLen >= 7) {
            int xx = XITelnet.toInt(ivData[4]) << 16 |    //!!1.05a
                     XITelnet.toInt(ivData[5]) << 8 |
                     XITelnet.toInt(ivData[6]);

            ivEmulator.setFunctionKeysMask(xx);
          }
        }
      }
    }
  }


  public String toString() {
    String str = "";
    for (int i = 0; i < ivLen; i++)
      str += XITelnet.toHex(ivData[i]) + ",";
    return super.toString() + " [" + ivLen + ",[" + str + "]]";
  }
}