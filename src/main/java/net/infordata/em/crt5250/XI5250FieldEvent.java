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
!!V 14/05/97 rel. 0.96d- removed SIZE_CHANGED.
    27/05/97 rel. 1.00 - first release.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.crt5250;


import java.awt.event.KeyEvent;
import java.util.EventObject;


/**
 * XI5250Field notification event.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250FieldEvent extends EventObject {


  private static final long serialVersionUID = 1L;

  /**
   * The field has been activated (ie. the cursor enters the field area)
   */
  public static final int ACTIVATED              = 0;
  /**
   * The field has been deactivated (ie. the cursor exits the field area)
   */
  public static final int DEACTIVATED            = 1;
  /**
   * The field value has been changed.
   */
  public static final int VALUE_CHANGED          = 2;
  /**
   * The field enabled state has changed.
   */
  public static final int ENABLED_STATE_CHANGED  = 3;
  /**
   */
  public static final int KEY_EVENT              = 4;


  private static final String[] cvIdDescr = {"ACTIVATED",
                                             "DEACTIVATED",
                                             "VALUE_CHANGED",
                                             "ENABLED_STATE_CHANGED",
                                             "KEY_EVENT"};

  private int         ivId;
  private KeyEvent    ivKeyEvent;


  /**
   */
  public XI5250FieldEvent(int aId, XI5250Field aField) {
    super(aField);
    ivId = aId;
  }


  /**
   */
  public XI5250FieldEvent(int aId, XI5250Field aField, KeyEvent ke) {
    this(aId, aField);
    ivKeyEvent = ke;
  }


  /**
   */
  public final int getID() {
    return ivId;
  }


  /**
   */
  public final XI5250Field getField() {
    return (XI5250Field)getSource();
  }


  /**
   */
  public final KeyEvent getKeyEvent() {
    return ivKeyEvent;
  }


  /**
   */
  @Override
  public String toString() {
    return super.toString() + "[" + cvIdDescr[ivId] + "," + getSource() + "]";
  }
}