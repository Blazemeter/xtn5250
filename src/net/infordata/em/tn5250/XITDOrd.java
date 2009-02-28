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


import java.io.IOException;
import java.io.InputStream;

import net.infordata.em.crt5250.XIEbcdicTranslator;
import net.infordata.em.tnprot.XITelnet;


///////////////////////////////////////////////////////////////////////////////

/**
 * TD - Transparent data
 * 
 * see: http://publibfp.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/co2e2001/15.6.10?DT=19950629163252
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XITDOrd extends XI5250Ord {
  
  protected String ivData;
  protected int ivLen;

  /**
   * @exception    XI5250Exception    raised if order parameters are wrong.
   */
  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    {
      byte[] buf = new byte[2];
      if (inStream.read(buf) < buf.length)
        throw new XI5250Exception("EOF reached", XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
      ivLen = (XITelnet.toInt(buf[0]) << 8) | XITelnet.toInt(buf[1]); 
      // Cannot deal with real dimensions, since they can be not applied yet 
      if (ivLen < 0 || ivLen > (XI5250Emulator.MAX_ROWS * XI5250Emulator.MAX_COLS))
        throw new XI5250Exception("Invalid len", XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
    }
    {
      byte[] buf = new byte[ivLen];
      int count = inStream.read(buf);
      if (count < buf.length)  
        throw new XI5250Exception("EOF reached, requested: " + ivLen + 
            " readden:" + count, XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
      XIEbcdicTranslator translator = ivEmulator.getTranslator();
      StringBuilder sb = new StringBuilder(ivLen);
      for (int i = 0; i < count ; i++) {
        sb.append(translator.toChar(buf[i]));
      }
      ivData = sb.toString();
    }
  }


  @Override
  protected void execute() {
    ivEmulator.drawString(ivData, ivEmulator.getSBACol(), ivEmulator.getSBARow());
    ivEmulator.setSBA(ivEmulator.getSBA() + ivData.length());
  }


  @Override
  public String toString() {
    return super.toString() + " [" + ivLen + ",\"" + ivData + "\"" + "]";
  }
}