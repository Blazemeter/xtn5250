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
 * 5250 Read MDT fields command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIReadMdtFieldsCmd extends XICCCmd {
  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    readCC(inStream);
  }


  protected void execute() {
    ivEmulator.ivPendingCmd = this;
    executeCC1();
    executeCC2();
  }


  protected void executePending(int anAidCode, boolean isMasked) {
    ivEmulator.setState(XI5250Emulator.ST_TEMPORARY_LOCK);
    ivEmulator.send5250Data(anAidCode,
                            ivEmulator.isMasterMDTSet() && !isMasked, true);
    //!!0.92a ivEmulator.ivPendingCmd = null;
    //!!V1 ivEmulator.removeFields();
  }
}