/*
!!V 27/05/97 rel. 1.00 - first release.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.crt5250;


import java.awt.*;
import java.util.*;
import java.awt.event.*;


/**
 * Usefull to add some paint over a field.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250FieldPaintListener extends EventListener {


  /**
   * One event for paint, the bounding rectangle is setted as the clip region.
   * Coordinate are relative to the field bounding rectangle
   * @see    XI5250Field#getBoundingRect
   */
  public void fieldPaint(XI5250FieldPaintEvent e);


  /**
   * One event for each row that makes up the field
   * (a field can be splitted over multiple rows)
   * Coordinate are relative to the field bounding rectangle
   * @see    XI5250Field#getRowsRects
   */
  public void rowPaint(XI5250FieldPaintEvent e);
}