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
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250PanelConnection {

  private Component ivComponent;

  private int ivCol;
  private int ivRow;
  private int ivNCols;
  private int ivNRows;


  /**
   */
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


  /**
   */
  protected void recalcBounds(XI5250EmulatorExt aEm) {
    Point     pt  = aEm.toPoints(ivCol, ivRow);

    ivComponent.setBounds(pt.x, pt.y,
                          ivNCols * aEm.getCharSize().width,
                          ivNRows * aEm.getCharSize().height);
  }


  /**
   */
  public final Component getComponent() {
    return ivComponent;
  }
}