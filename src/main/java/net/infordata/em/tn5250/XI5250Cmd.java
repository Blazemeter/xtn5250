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
 * Abstract class for all 5250 commands
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public abstract class XI5250Cmd {

  protected XI5250Emulator ivEmulator;


  protected void init(XI5250Emulator aEmulator) {
    ivEmulator = aEmulator;
  }


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  protected abstract void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception;


  protected abstract void execute();


  protected void executePending(int anAidCode, boolean isMasked) {
    throw new RuntimeException("executePending() not supported");
  }
}