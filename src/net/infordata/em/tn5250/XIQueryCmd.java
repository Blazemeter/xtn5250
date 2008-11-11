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


///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 Query command.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIQueryCmd extends XI5250Cmd {
  protected int[] ivPar = new int[5];


  @Override
  protected void readFrom5250Stream(InputStream inStream) throws IOException {
    for (int i = 0; i < 5; i++)
      ivPar[i] = inStream.read();

    if (ivPar[0] != 0x00 || ivPar[1] != 0x05 ||
        ivPar[2] != 0xD9 || ivPar[3] != 0x70 ||
        ivPar[4] != 0x00)
      ;  //!!V gestire errori
  }


  @Override
  protected void execute() {
    // see rfc 1205
    byte[] buf = {
      (byte)0x00, (byte)0x00,
      (byte)0x88,
      (byte)0x00, (byte)0x3A,
      (byte)0xD9,
      (byte)0x70,
      (byte)0x80,
      (byte)0x06, (byte)0x00,  // any other 5250 emulator
      (byte)0x01, (byte)0x00, (byte)0x00,  // version
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x01,
      (byte)0x31, (byte)0x79,  // 3179
      (byte)0x00, (byte)0x02,  // modello 2
      (byte)0x02,
      (byte)0x00,
      (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x01, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x01,   // Row 1 e col 1 support
      (byte)(0x01 | 0x40),  // 24x80 color supported
      (byte)0x00,
      (byte)0x00,
      (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
      (byte)0x00, (byte)0x00, (byte)0x00};

    ivEmulator.send5250Packet((byte)0x00, (byte)0x00, buf);
  }
}