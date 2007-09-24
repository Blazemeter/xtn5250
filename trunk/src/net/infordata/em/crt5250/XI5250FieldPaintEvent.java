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


import java.awt.*;
import java.util.*;


/**
 * XI5250Field painting event.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250FieldPaintEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  /**
   * Paint at field level can be added.
   */
  public static final int FIELD_PAINT       = 0;
  /**
   * Paint at field singol row level can be added.
   */
  public static final int ROW_PAINT         = 1;

  private static final String[] cvIdDescr = {"FIELD_PAINT",
                                             "ROW_PAINT"};

  private int         ivId;
  private Graphics    ivGr;


  /**
   */
  public XI5250FieldPaintEvent(int aId, XI5250Field aField, Graphics aGr) {
    super(aField);
    ivId = aId;
    ivGr = aGr;
  }


  /**
   */
  public int getID() {
    return ivId;
  }


  /**
   * Returns the Graphics that can be used to paint.
   */
  public Graphics getGraphics() {
    return ivGr;
  }


  /**
   */
  public XI5250Field getField() {
    return (XI5250Field)getSource();
  }


  /**
   */
  public String toString() {
    return super.toString() + "[" + cvIdDescr[ivId] + "," + getSource() + "]";
  }
}