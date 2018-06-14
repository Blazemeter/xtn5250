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


import java.io.IOException;


/**
 * Interface that must be implemented by an emulator to receive notification from XITelnet.
 *
 * @author Valentino Proietti - Infordada S.p.A.
 * @see XITelnet
 */
public interface XITelnetEmulator {

  /**
   * @see XITelnet#connecting
   */
  void connecting();

  /**
   * @see XITelnet#connected
   */
  void connected();

  /**
   * @param remote whether the disconnection was caused by a local disconnect, or by the terminal
   * server.
   * @see XITelnet#disconnected(boolean)
   */
  void disconnected(boolean remote);

  /**
   * @param ex exception throw when communicating with terminal server.
   * @see XITelnet#caughtIOException
   */
  void caughtIOException(IOException ex);

  /**
   * @param buf bytes received from the terminal server.
   * @param len number of bytes received.
   * @see XITelnet#receivedData
   */
  void receivedData(byte[] buf, int len);

  /**
   * @see XITelnet#receivedEOR
   */
  void receivedEOR();

  /**
   * @param aIACOpt option of the IAC
   * @param aIACStr body of the IAC
   * @see XITelnet#unhandledRequest
   */
  void unhandledRequest(byte aIACOpt, String aIACStr);


  /**
   * @param aIACOpt IAC option that changed
   * @see XITelnet#localFlagsChanged
   */
  void localFlagsChanged(byte aIACOpt);


  /**
   * @param aIACOpt IAC option that changed
   * @see XITelnet#remoteFlagsChanged
   */
  void remoteFlagsChanged(byte aIACOpt);

}