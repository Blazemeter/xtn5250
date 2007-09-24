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
!!V 14/05/97 rel. 1.00a- ...
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.tnprot;


import java.io.*;


/**
 * Interface that must be implemented by an emulator to receive notification from XITelnet.
 *
 * @see    XITelnet
 *
 * @version  1.00a
 * @author   Valentino Proietti - Infordada S.p.A.
 */
public interface XITelnetEmulator {
  /**
   * @see    XITelnet#connecting
   */
  public void connecting();

  /**
   * @see    XITelnet#connected
   */
  public void connected();

  /**
   * @see    XITelnet#disconnected
   */
  public void disconnected();

  /**
   * @see    XITelnet#catchedIOException
   */
  public void catchedIOException(IOException ex);

  /**
   * @see    XITelnet#receivedData
   */
  public void receivedData(byte[] buf, int len);

  /**
   * @see    XITelnet#receivedEOR
   */
  public void receivedEOR();

  /**
   * @see    XITelnet#unhandledRequest
   */
  public void unhandledRequest(byte aIACOpt, String aIACStr);


  /**
   * @see    XITelnet#localFlagsChanged
   */
  public void localFlagsChanged(byte aIACOpt);


  /**
   * @see    XITelnet#remoteFlagsChanged
   */
  public void remoteFlagsChanged(byte aIACOpt);
}