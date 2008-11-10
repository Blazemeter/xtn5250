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
!!V 09/09/97 rel. 1.04c- creation.
    24/09/97 rel. 1.05 - DNCX project.
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
*/


package net.infordata.em.tn5250ext;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import net.infordata.em.tn5250.XI5250Emulator;


/**
 * THE 5250 EMULATOR extension.
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250EmulatorExt extends XI5250Emulator implements Serializable {

  private static final long serialVersionUID = 1L;

  // Debug level 0 = none, 1 = , 2 = detailed
  static final int DEBUG = 2;

  private   boolean ivShowHints = true;
  private   boolean ivHintOnActiveField = false;

  transient private   ArrayList<XI5250PanelsDispatcher>  ivDispatchers = 
    new ArrayList<XI5250PanelsDispatcher>();

  public static final String  SHOW_HINTS            = "showHints";
  public static final String  HINT_ON_ACTIVE_FIELD  = "hintOnActiveField";


  /**
   * Default contructor.
   */
  public XI5250EmulatorExt() {
  }


  /**
   */
  protected synchronized void addDispatcher(XI5250PanelsDispatcher aDispatcher) {
    if (!ivDispatchers.contains(aDispatcher))
      ivDispatchers.add(aDispatcher);
  }


  /**
   */
  protected synchronized void removeDispatcher(XI5250PanelsDispatcher aDispatcher) {
    ivDispatchers.remove(aDispatcher);
  }


  /**
   */
  protected synchronized void refreshHint() {
    XI5250PanelsDispatcher disp;
    XI5250PanelHandler     hndl;
    for (Iterator<XI5250PanelsDispatcher> en = ivDispatchers.iterator(); en.hasNext(); ) {
      disp = en.next();
      hndl = disp.getCurrentPanelHandler();

      if (hndl != null)
        hndl.refreshHint();
    }
  }


  /**
   * Enables or disables the fields hints showing (default true).
   */
  public void setShowHints(boolean aFlag) {
    if (ivShowHints == aFlag)
      return;

    boolean oldShowHints = ivShowHints;
    ivShowHints = aFlag;

    //!!1.04d refreshHint();

    firePropertyChange(SHOW_HINTS, oldShowHints, ivShowHints);
  }


  /**
   */
  public boolean getShowHints() {
    return ivShowHints;
  }


  /**
   */
  public void setHintOnActiveField(boolean aFlag) {
    if (ivHintOnActiveField == aFlag)
      return;

    boolean oldHintOnActiveField = ivHintOnActiveField;
    ivHintOnActiveField = aFlag;

    //!!1.04d refreshHint();

    firePropertyChange(HINT_ON_ACTIVE_FIELD,
                       oldHintOnActiveField, ivHintOnActiveField);
  }


  /**
   */
  public boolean isHintOnActiveField() {
    return ivHintOnActiveField;
  }


//  /**
//   */
//  void writeObject(ObjectOutputStream oos) throws IOException {
//    oos.defaultWriteObject();
//  }
//
//  void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
//    ois.defaultReadObject();
//  }
}
