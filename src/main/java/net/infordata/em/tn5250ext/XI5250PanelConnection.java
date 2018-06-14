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
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.tn5250ext;

import java.awt.Component;
import java.awt.Point;

/**
 * Connect an AWT Component to the XI5250EmulatorExt panel
 * moving and resizing the Component as the font size changes.
 * <pre>
 *   new XI5250PanelConnection(this, new XIImage(logo), 29, 12, 20, 10);
 * </pre>
 *
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250PanelConnection {

  private Component ivComponent;

  private int ivCol;
  private int ivRow;
  private int ivNCols;
  private int ivNRows;

  public XI5250PanelConnection(XI5250PanelHandler aPanelHndl,
                               Component aComponent,
                               int aCol, int aRow,
                               int aNCols, int aNRows) {
    ivCol = aCol;
    ivRow = aRow;
    ivNCols = aNCols;
    ivNRows = aNRows;
    ivComponent = aComponent;
    aPanelHndl.connect(this);
  }

  protected void recalcBounds(XI5250EmulatorExt aEm) {
    Point     pt  = aEm.toPoints(ivCol, ivRow);

    ivComponent.setBounds(pt.x, pt.y,
                          ivNCols * aEm.getCharSize().width,
                          ivNRows * aEm.getCharSize().height);
  }

  public final Component getComponent() {
    return ivComponent;
  }

}
