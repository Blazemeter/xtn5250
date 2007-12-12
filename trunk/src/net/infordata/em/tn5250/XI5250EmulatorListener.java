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


import java.util.EventListener;


/**
 * Listener for XI5250EmulatorEvent.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250EmulatorListener extends EventListener {

  /**
   * @see     XI5250EmulatorEvent#CONNECTING
   */
  public void connecting(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#CONNECTED
   */
  public void connected(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#DISCONNECTED
   */
  public void disconnected(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#STATE_CHANGED
   */
  public void stateChanged(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#NEW_PANEL_RECEIVED
   */
  public void newPanelReceived(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#FIELDS_REMOVED
   */
  public void fieldsRemoved(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#DATA_SENDED
   */
  public void dataSended(XI5250EmulatorEvent e);
}