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
!!V 27/05/97 rel. 1.00 - first release.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.crt5250;


import java.util.*;


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