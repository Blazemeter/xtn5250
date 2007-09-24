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
!!V 03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.crt5250;


import java.util.*;
import java.awt.event.*;


/**
 */
public class XI5250CrtEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  /**
   * A field has been activated.
   */
  public static final int FIELD_ACTIVATED   = 0;
  /**
   * A field has been deactivated.
   */
  public static final int FIELD_DEACTIVATED = FIELD_ACTIVATED + 1;
  /**
   * The crt real-size is changed (ie. the font size is changed) (this event is posted).
   */
  public static final int SIZE_CHANGED      = FIELD_ACTIVATED + 2;
  /**
   * A key has been pressed.
   * !!! Use this event instead of AWT KeyEvent !!!.
   */
  public static final int KEY_EVENT         = FIELD_ACTIVATED + 3;
  /**
   * Mouse enters the field area.
   */
  public static final int MOUSE_ENTERS_FIELD = FIELD_ACTIVATED + 4;
  /**
   * Mouse exits from a field area.
   */
  public static final int MOUSE_EXITS_FIELD  = FIELD_ACTIVATED + 5;


  private static final String[] cvIdDescr = {"FIELD_ACTIVATED",
                                             "FIELD_DEACTIVATED",
                                             "SIZE_CHANGED",
                                             "KEY_EVENT",
                                             "MOUSE_ENTERS_FIELD",
                                             "MOUSE_EXITS_FIELD"};

  private int         ivId;
  private XI5250Field ivField;
  private KeyEvent    ivKeyEvent;


  /**
   */
  protected XI5250CrtEvent(int aId, XI5250Crt aCrt, XI5250Field aField,
                           KeyEvent aKeyEvent) {
    super(aCrt);
    ivId = aId;
    ivField = aField;
    ivKeyEvent = aKeyEvent;
  }


  /**
   */
  public XI5250CrtEvent(int aId, XI5250Crt aCrt, XI5250Field aField) {
    this(aId, aCrt, aField, null);
  }


  /**
   */
  public final int getID() {
    return ivId;
  }


  /**
   */
  public final XI5250Crt getCrt() {
    return (XI5250Crt)getSource();
  }


  /**
   */
  public final XI5250Field getField() {
    return ivField;
  }


  /**
   */
  public final KeyEvent getKeyEvent() {
    return ivKeyEvent;
  }


  /**
   */
  public String toString() {
    return super.toString() + "[" + cvIdDescr[getID()] + "," + ivField + "]";
  }
}