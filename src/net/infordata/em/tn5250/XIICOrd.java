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


///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 IC Order
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIICOrd extends XI5250Ord {

  protected int ivRow, ivCol;


  protected void readFrom5250Stream(InputStream inStream) throws IOException {
    ivRow = Math.max(0, inStream.read());
    ivCol = Math.max(0, inStream.read());
    //!!V effettuare check dei parametri
  }


  protected void execute() {
    ivEmulator.setCursorPos(ivCol - 1, ivRow - 1);
    ivEmulator.ivCmdList.ivICOrderExecuted = true;
  }


  public String toString() {
    return super.toString() + " [" + ivRow + "," + ivCol + "]";
  }
}