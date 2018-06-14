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

/**
!!V 03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.crt5250;

import java.util.EventListener;

/**
 * XI5250CrtEvent listener
 *
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250CrtListener extends EventListener {

  /**
   * @param e generated event.
   * @see    XI5250CrtEvent#FIELD_ACTIVATED
   */
  void fieldActivated(XI5250CrtEvent e);

  /**
   * @param e generated event.
   * @see    XI5250CrtEvent#FIELD_DEACTIVATED
   */
  void fieldDeactivated(XI5250CrtEvent e);
  
  /**
   * @param e generated event.
   * @see    XI5250CrtEvent#SIZE_CHANGED
   */
  void sizeChanged(XI5250CrtEvent e);

  /**
   * @param e generated event.
   * @see    XI5250CrtEvent#KEY_EVENT
   */
  void keyEvent(XI5250CrtEvent e);
  
  /**
   * @param e generated event.
   * @see    XI5250CrtEvent#MOUSE_ENTERS_FIELD
   */
  void mouseEntersField(XI5250CrtEvent e);

  /**
   * @param e generated event.
   * @see    XI5250CrtEvent#MOUSE_EXITS_FIELD
   */
  void mouseExitsField(XI5250CrtEvent e);

}
