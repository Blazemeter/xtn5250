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
!!V 07/07/98 rel. 1.07 - creation.
    04/02/99 rel. 1.11 - Swing 1.1 + ivSizeControlledFrame.
 */


package net.infordata.em.tn5250;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250CrtCtrl;
import net.infordata.em.crt5250.XI5250CrtFrame;
import net.infordata.em.util.XIUtil;



/**
 */
public class XI5250Frame extends XI5250CrtFrame {

  private static final long serialVersionUID = 1L;

  // images
  private static XIImagesBdl cvImagesBdl =
      net.infordata.em.tn5250.XIImagesBdl.getImagesBdl();

  private static ResourceBundle cvRes =
      ResourceBundle.getBundle("net.infordata.em.tn5250.resources.Res");

  private final String ivTitle;

  /**
   */
  public XI5250Frame(String aTitle, XI5250Emulator aCrt) {
    super(aTitle, aCrt);
    ivTitle = aTitle;
    init(aCrt);
  }


  /**
   */
  public XI5250Frame(String aTitle, XI5250Emulator aCrt,
                     boolean sizeControlledFrame) {
    super(aTitle, aCrt, sizeControlledFrame);
    ivTitle = aTitle;
    init(aCrt);
  }

  
  private void init(XI5250Emulator aCrt) {
    aCrt.addEmulatorListener(new XI5250EmulatorAdapter() {
      @Override
      public void connected(XI5250EmulatorEvent e) {
        setTitle(ivTitle + " - " + e.get5250Emulator().getHost());
      }

      @Override
      public void disconnected(XI5250EmulatorEvent e) {
        setTitle(ivTitle);
      }
    });
  }

  
  /**
   */
  @Override
  protected XI5250CrtCtrl createController(XI5250Crt crt) {
    return new XI5250EmulatorCtrl((XI5250Emulator)crt);
  }


  /**
   */
  protected final XI5250EmulatorCtrl getEmulatorCtrl() {
    return (XI5250EmulatorCtrl)getCrtCtrl();
  }


  /**
   */
  public final XI5250Emulator getEmulator() {
    return getEmulatorCtrl().getEmulator();
  }


  /**
   */
  @Override
  protected void processExitCmd() {
    if (getEmulator().isActive()) {
      //!!1.13a otherwise a dead-lock may occur !!
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          int ret = JOptionPane.showConfirmDialog(
                       XI5250Frame.this,
                       cvRes.getString("TXT_ConfirmExit"),
                       "", JOptionPane.YES_NO_OPTION);
          if (ret != JOptionPane.NO_OPTION) {
            setVisible(false);
            dispose();
          }
        }
      });
    }
    else {
      setVisible(false);
      dispose();
    }
  }


  /**
   */
  @Override
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    switch(e.getID()) {
      case WindowEvent.WINDOW_OPENED:
        if (getEmulator().getHost() == null)
          getCommandMgr().dispatchCommand(XI5250EmulatorCtrl.CONNECT_CMD);
        break;
    }
  }


  /**
   */
  @Override
  protected JMenuBar createMenuBar() {
    String str;

    str = cvRes.getString("TXT_Communications");
    JMenu commMenu = new JMenu(XIUtil.removeMnemonics(str));
    commMenu.setMnemonic(XIUtil.getMnemonic(str));
    {
      JMenuItem connItem =
          new JMenuItem(cvRes.getString("TXT_Connect"));
      JMenuItem disconnItem =
          new JMenuItem(cvRes.getString("TXT_Disconnect"));
      JMenuItem aboutItem =
          new JMenuItem(cvRes.getString("TXT_About"));
      JMenuItem exitItem =
          new JMenuItem(cvRes.getString("TXT_Exit"));
      exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
                                                     ActionEvent.ALT_MASK));

      commMenu.add(connItem);
      commMenu.add(disconnItem);
      commMenu.addSeparator();
      commMenu.add(aboutItem);
      commMenu.addSeparator();
      commMenu.add(exitItem);

      getCommandMgr().handleCommand(connItem,
                                    XI5250EmulatorCtrl.CONNECT_CMD);
      getCommandMgr().handleCommand(disconnItem,
                                    XI5250EmulatorCtrl.DISCONNECT_CMD);
      getCommandMgr().handleCommand(aboutItem,
                                    XI5250EmulatorCtrl.ABOUT_CMD);
      getCommandMgr().handleCommand(exitItem,
                                    EXIT_CMD);
    }

    str = cvRes.getString("TXT_Edit");
    JMenu editMenu = new JMenu(XIUtil.removeMnemonics(str));
    editMenu.setMnemonic(XIUtil.getMnemonic(str));
    {
      JMenuItem copyItem =
          new JMenuItem(cvRes.getString("TXT_Copy"));
      JMenuItem pasteItem =
          new JMenuItem(cvRes.getString("TXT_Paste"));
      JMenuItem snapShotItem =
          new JMenuItem(cvRes.getString("TXT_SnapShot"));

      editMenu.add(copyItem);
      editMenu.add(pasteItem);
      editMenu.addSeparator();
      editMenu.add(snapShotItem);

      getCommandMgr().handleCommand(copyItem,
                                    XI5250EmulatorCtrl.COPY_CMD);
      getCommandMgr().handleCommand(pasteItem,
                                    XI5250EmulatorCtrl.PASTE_CMD);
      getCommandMgr().handleCommand(snapShotItem,
                                    XI5250EmulatorCtrl.SNAPSHOT_CMD);
    }

    str = cvRes.getString("TXT_Options");
    JMenu optionsMenu = new JMenu(XIUtil.removeMnemonics(str));
    optionsMenu.setMnemonic(XIUtil.getMnemonic(str));
    {
      JCheckBoxMenuItem switch3DfxItem =
          new JCheckBoxMenuItem(cvRes.getString("TXT_3dFx"));
      JCheckBoxMenuItem referenceCursorItem =
          new JCheckBoxMenuItem(cvRes.getString("TXT_RefCursor"));

      optionsMenu.add(switch3DfxItem);
      optionsMenu.add(referenceCursorItem);

      getCommandMgr().handleCommand(switch3DfxItem,
                                    XI5250EmulatorCtrl.SWITCH_3DFX_CMD);
      getCommandMgr().handleCommand(referenceCursorItem,
                                    XI5250EmulatorCtrl.REFERENCE_CURSOR_CMD);
    }

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(commMenu);
    menuBar.add(editMenu);
    menuBar.add(optionsMenu);
    return menuBar;
  }


  /**
   * Inserisce nella tool-bar i bottoni di default.
   */
  @Override
  protected JToolBar createToolBar() {
    // bottoni della tool-bar
    AbstractButton[] buttons = new AbstractButton[] {
      new JButton(cvImagesBdl.getIcon("Connect")),
      new JButton(cvImagesBdl.getIcon("Disconnect")),
      null,
      new JButton(cvImagesBdl.getIcon("Copy")),
      new JButton(cvImagesBdl.getIcon("Paste")),
      null,
      new JButton(cvImagesBdl.getIcon("SnapShot")),
      null,
      new JToggleButton(cvImagesBdl.getIcon("3dFx")),
      new JToggleButton(cvImagesBdl.getIcon("RefCursor")),
    };
    // action commands associati con i bottoni della tool-bar.
    String[]   buttonsActCmd = new String[] {
      XI5250EmulatorCtrl.CONNECT_CMD,
      XI5250EmulatorCtrl.DISCONNECT_CMD,
      null,
      XI5250EmulatorCtrl.COPY_CMD,
      XI5250EmulatorCtrl.PASTE_CMD,
      null,
      XI5250EmulatorCtrl.SNAPSHOT_CMD,
      null,
      XI5250EmulatorCtrl.SWITCH_3DFX_CMD,
      XI5250EmulatorCtrl.REFERENCE_CURSOR_CMD,
    };
    // Hint associati ad i vari bottoni.
    String[] buttonHints = new String[] {
      cvRes.getString("TXT_Connect"),
      cvRes.getString("TXT_Disconnect"),
      null,
      cvRes.getString("TXT_Copy"),
      cvRes.getString("TXT_Paste"),
      null,
      cvRes.getString("TXT_SnapShot"),
      null,
      cvRes.getString("TXT_3dFx"),
      cvRes.getString("TXT_RefCursor"),
    };

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);

    Dimension size = new Dimension(26, 26);

    for (int i = 0; i < buttons.length; i++) {
      if (buttons[i] != null) {
        AbstractButton button = (AbstractButton)buttons[i];
        toolBar.add(button);
        button.setToolTipText(buttonHints[i]);
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setRequestFocusEnabled(false);
        getCommandMgr().handleCommand(button, buttonsActCmd[i]);
      }
      else
        toolBar.addSeparator();
    }

    return toolBar;
  }
}


