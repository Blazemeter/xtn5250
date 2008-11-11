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
 * 5250 Restore screen command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIRestoreScreenCmd extends XI5250Cmd {

  protected int ivPos;


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    ivPos = inStream.read();
    if (ivPos == -1)
      throw new XI5250Exception("Restore screen position required");
  }


  @Override
  protected void execute() {
    try {
    	/*!!1.06a
      XI5250EmulatorMemento mm = (XI5250EmulatorMemento)ivEmulator.ivSavedScreenList.elementAt(ivPos);
      ivEmulator.ivSavedScreenList.setSize(ivPos + 1);
      */
      //!!1.06a
      XI5250EmulatorMemento mm = ivEmulator.ivSavedScreens[ivPos];

      ivEmulator.restoreMemento(mm);
    }
    catch (ArrayIndexOutOfBoundsException ex) {
    }
  }
}