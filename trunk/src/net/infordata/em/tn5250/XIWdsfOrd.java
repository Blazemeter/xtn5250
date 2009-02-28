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
 * WDSF - Write to Display Structured Field
 * TODO
 * 
 * see: http://publibfp.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/co2e2001/15.6.13?DT=19950629163252
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIWdsfOrd extends XI5250Ord {

  /**
   * @exception    XI5250Exception    raised if order parameters are wrong.
   */
  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    // If not in enhanced mode ...
    if (true)
      throw new XI5250Exception("Not supported", XI5250Emulator.ERR_INVALID_ROW_COL_ADDR);
  }


  @Override
  protected void execute() {
    throw new IllegalStateException("Not supported");
  }


  @Override
  public String toString() {
    return super.toString();
  }
}