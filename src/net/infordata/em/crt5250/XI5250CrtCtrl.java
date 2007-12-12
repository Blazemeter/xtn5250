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
!!V 15/06/99 rel. 1.13 - creation.
 */


package net.infordata.em.crt5250;


import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.UIManager;

import net.infordata.em.util.XICommand;
import net.infordata.em.util.XICommandMgr;


/**
 * Handles common commands.
 */
public class XI5250CrtCtrl {

  private XI5250Crt    ivCrt;

  private XICommandMgr ivCommandMgr = new XICommandMgr();


  public static final String SWITCH_3DFX_CMD      = "SWITCH_3DFX_CMD";
  public static final String REFERENCE_CURSOR_CMD = "REFERENCE_CURSOR_CMD";

  public static final String COPY_CMD             = "COPY_CMD";
  public static final String PASTE_CMD            = "PASTE_CMD";


  /**
   */
  public XI5250CrtCtrl(XI5250Crt aCrt) {
    if (aCrt == null)
      throw new IllegalArgumentException("An XI5250Crt instance is required.");

    ivCrt = aCrt;

    ivCrt.addPropertyChangeListener(new PropertyListener());

    // Copy command
    getCommandMgr().enableCommand(
        COPY_CMD, ivCrt.getSelectedArea() != null);
    getCommandMgr().setCommand(COPY_CMD, new XICommand() {
      public void execute() {
        processCopyCmd();
      }
    });

    // Paste command
    getCommandMgr().setCommand(PASTE_CMD, new XICommand() {
      public void execute() {
        processPasteCmd();
      }
    });

    // 3Dfx command
    getCommandMgr().setCommandState(SWITCH_3DFX_CMD, is3DFX());
    getCommandMgr().setCommand(SWITCH_3DFX_CMD,  new XICommand() {
      public void execute() {
        processSwitch3dFxCmd();
      }
    });

    // Reference cursor command
    getCommandMgr().setCommandState(
        REFERENCE_CURSOR_CMD, ivCrt.isReferenceCursor());
    getCommandMgr().setCommand(REFERENCE_CURSOR_CMD,  new XICommand() {
      public void execute() {
        processReferenceCursorCmd();
      }
    });
  }


  /**
   */
  public final XI5250Crt getCrt() {
    return ivCrt;
  }


  /**
   */
  public final XICommandMgr getCommandMgr() {
    return ivCommandMgr;
  }


  /**
   */
  public final boolean is3DFX() {
    return
        (getCrt().getDefFieldsBorderStyle() == XI5250Field.LOWERED_BORDER);
  }


  /**
   */
  protected void processCopyCmd() {
    getCrt().processRawKeyEvent(
        new KeyEvent(getCrt(), KeyEvent.KEY_PRESSED,
                     0, KeyEvent.CTRL_MASK, KeyEvent.VK_INSERT, (char)KeyEvent.VK_INSERT));
  }


  /**
   */
  protected void processPasteCmd() {
    getCrt().processRawKeyEvent(
        new KeyEvent(getCrt(), KeyEvent.KEY_PRESSED,
                     0, KeyEvent.SHIFT_MASK, KeyEvent.VK_INSERT, (char)KeyEvent.VK_INSERT));
  }


  /**
   */
  protected void processSwitch3dFxCmd() {
    boolean flag = getCommandMgr().getCommandState(SWITCH_3DFX_CMD);

    if (flag) {
      getCrt().setDefFieldsBorderStyle(XI5250Field.LOWERED_BORDER);
      getCrt().setDefBackground(UIManager.getColor("control"));
    }
    else {
      getCrt().setDefFieldsBorderStyle(XI5250Field.NO_BORDER);
      getCrt().setDefBackground(SystemColor.black);
    }
  }


  /**
   */
  protected void processReferenceCursorCmd() {
    boolean flag = getCommandMgr().getCommandState(REFERENCE_CURSOR_CMD);

    getCrt().setReferenceCursor(flag);
  }


  /**
   */
  protected void emulatorPropertyChanged(PropertyChangeEvent e) {
    String propertyName = e.getPropertyName();

    if (propertyName == XI5250Crt.SELECTED_AREA) {
      getCommandMgr().enableCommand(
          COPY_CMD, getCrt().getSelectedArea() != null);
    }
    else if (propertyName == XI5250Crt.DEF_FIELDS_BORDER_STYLE) {
      getCommandMgr().setCommandState(SWITCH_3DFX_CMD, is3DFX());
    }
    else if (propertyName == XI5250Crt.REFERENCE_CURSOR) {
      getCommandMgr().setCommandState(
          REFERENCE_CURSOR_CMD, getCrt().isReferenceCursor());
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Usata per sincronizzare i comandi con le property dell' emulatore.
   */
  class PropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {
      emulatorPropertyChanged(e);
    }
  }
}


