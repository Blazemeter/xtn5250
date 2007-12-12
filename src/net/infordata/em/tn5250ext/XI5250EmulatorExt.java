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


import java.awt.SystemColor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250Frame;


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

  transient private   Vector  ivDispatchers = new Vector();

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
      ivDispatchers.addElement(aDispatcher);
  }


  /**
   */
  protected synchronized void removeDispatcher(XI5250PanelsDispatcher aDispatcher) {
    ivDispatchers.removeElement(aDispatcher);
  }


  /**
   */
  private synchronized void refreshHint() {
    XI5250PanelsDispatcher disp;
    XI5250PanelHandler     hndl;
    for (Enumeration en = ivDispatchers.elements(); en.hasMoreElements(); ) {
      disp = (XI5250PanelsDispatcher)en.nextElement();
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


  /**
   */
  void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }

  void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
  }


  /**
   * Used only for test purposes.
   */
  public static void main(String[] argv) {

    /*!!1.12
    if (System.getProperty("java.version").compareTo("1.1.1") < 0 ||
        System.getProperty("java.version").compareTo("1.1_Final") == 0) {
      System.err.println("!!! Use JDK 1.1.1 or newer !!!");
    }
    */
//    checkJDK();

    /*
    if (argv.length < 1) {
      System.err.println("Host name or ip address is required.");
      System.exit(-1);
    }
    */

    XI5250EmulatorExt em  = new XI5250EmulatorExt();
    em.setTerminalType("IBM-3477-FC");
    em.setKeyboardQueue(true);

    if (DEBUG >= 2) {
      em.setHintOnActiveField(true);
      XI5250PanelsDispatcher disp = new XI5250PanelsDispatcher(em);
      new TestHandler(disp);
    }

    if (argv.length >= 1) {
      em.setHost(argv[0]);
      em.setActive(true);
    }

    XI5250Frame frm = new XI5250Frame("tn5250ext" + " " +
                                      XI5250Emulator.VERSION, em);

    //3D FX
    if (argv.length >= 2 && "3DFX".equals(argv[1].toUpperCase())) {
      em.setDefFieldsBorderStyle(XI5250Field.LOWERED_BORDER);
      em.setDefBackground(SystemColor.control);
    }

    frm.setBounds(0, 0, 570, 510);
    frm.centerOnScreen();
    frm.show();
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  private static class TestHandler extends XI5250PanelHandler {

    public TestHandler(XI5250PanelsDispatcher disp) {
      super(disp, "");
    }

    protected boolean detailedTest() {
      return true;
    }

    protected void start() {
      for (Iterator<XI5250Field> e = getFields().iterator(); e.hasNext(); ) {
        XI5250Field field = e.next();
        setFieldHint(field, new XIHint(field.toString()));

        JPopupMenu pm = new JPopupMenu();
        pm.add(new JMenuItem(field.toString()));
        setFieldPopupMenu(field, pm);

        JButton btn = new JButton();
        new XI5250FieldConnection(this, field, btn);

        new XI5250PanelConnection(this,
                                  new JButton("+-"),
                                  15, 15, 10, 10);
      }
    }

    protected void stop() {
    }
  }
}
