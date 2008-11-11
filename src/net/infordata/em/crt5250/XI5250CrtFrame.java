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


import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.infordata.em.util.XICommand;
import net.infordata.em.util.XICommandMgr;
import net.infordata.em.util.XIUtil;



/**
 */
public class XI5250CrtFrame extends JFrame {


  private static final long serialVersionUID = 1L;

  // images
  private static XIImagesBdl cvImagesBdl = XIImagesBdl.getImagesBdl();

  private static ResourceBundle cvRes =
      ResourceBundle.getBundle("net.infordata.em.crt5250.resources.Res");

  private boolean ivPending;
  private boolean ivOpened;

  private boolean ivFirstTime = true;             //!!1.11
  private boolean ivSizeControlledFrame = false;  //!!1.11

  private XI5250CrtCtrl ivCrtCtrl;

  public static final   String EXIT_CMD             = "EXIT_CMD";


  /**
   */
  public XI5250CrtFrame(String aTitle, XI5250Crt aCrt) {
    this(aTitle, aCrt, false);
  }


  /**
   */
  public XI5250CrtFrame(String aTitle, XI5250Crt aCrt,
                        boolean sizeControlledFrame) {
    super(aTitle);

    ivSizeControlledFrame = sizeControlledFrame;

    if (aCrt == null)
      throw new IllegalArgumentException("An XI5250Crt instance is required.");

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    ivCrtCtrl = createController(aCrt);

    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

    JPanel panel = new XI5250Crt.SupportPanel(getCrt());
    panel.setBackground(getCrt().getBackground());
    getContentPane().add(panel, BorderLayout.CENTER);

    getCrt().addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        emulatorPropertyChanged(evt);
      }
    });

    //
    JToolBar toolBar = createToolBar();
    JMenuBar menuBar = createMenuBar();

    setJMenuBar(menuBar);
    getContentPane().add(toolBar, BorderLayout.NORTH);

    // Exit command
    getCommandMgr().setCommand(EXIT_CMD,  new XICommand() {
      public void execute() {
        processExitCmd();
      }
    });
  }


  /**
   */
  protected void processExitCmd() {
    setVisible(false);
    dispose();
  }


  /**
   */
  protected XI5250CrtCtrl createController(XI5250Crt crt) {
    return new XI5250CrtCtrl(crt);
  }


  /**
   */
  protected final XI5250CrtCtrl getCrtCtrl() {
    return ivCrtCtrl;
  }


  /**
   */
  public final XI5250Crt getCrt() {
    return ivCrtCtrl.getCrt();
  }


  /**
   */
  private void emulatorPropertyChanged(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();
    if ("background".equals(propertyName))
      getCrt().getParent().setBackground(getCrt().getBackground());
    else if ("font".equals(propertyName) ||
             XI5250Crt.CRT_SIZE.equals(propertyName)) {
      getCrt().revalidate();
      sizeChanged();
    }
  }


  /**
   */
  public final XICommandMgr getCommandMgr() {
    return ivCrtCtrl.getCommandMgr();
  }


  /**
   */
  @Override
  public void invalidate() {
    super.invalidate();
    sizeChanged();
  }


  /**
   */
  protected void sizeChanged() {
    if (ivOpened && !ivPending) {
      ivPending = true;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          try {
            if (ivFirstTime || ivSizeControlledFrame) {
              ivFirstTime = false;
              pack();   //!!1.03b
            }
          }
          finally {
            ivPending = false;
          }
        }
      });
    }
  }


  /**
   */
  public void centerOnScreen() {
    Dimension ss  = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension dim = getSize();

    setBounds((ss.width - dim.width) / 2, (ss.height - dim.height) / 2 ,
              dim.width, dim.height);
  }


  /**
   */
  public void centerOnScreen(int perc) {
    Dimension ss  = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension dim = getSize();
    dim.width = (ss.width * perc) / 100;
    dim.height = (ss.height * perc) / 100;

    setBounds((ss.width - dim.width) / 2, (ss.height - dim.height) / 2 ,
              dim.width, dim.height);
  }


  /**
   */
  @Override
  protected void processWindowEvent(WindowEvent e) {
    switch(e.getID()) {
      case WindowEvent.WINDOW_OPENED:
        getCrt().requestFocus();
        ivOpened = true;
        sizeChanged();
        break;
      case WindowEvent.WINDOW_CLOSING:
        getCommandMgr().dispatchCommand(EXIT_CMD);
        break;
      case WindowEvent.WINDOW_CLOSED:
        break;
    }
    super.processWindowEvent(e);
  }


  /**
   */
  protected JMenuBar createMenuBar() {
    String str;

    str = cvRes.getString("TXT_Edit");
    JMenu editMenu = new JMenu(XIUtil.removeMnemonics(str));
    editMenu.setMnemonic(XIUtil.getMnemonic(str));
    {
      JMenuItem copyItem =
          new JMenuItem(cvRes.getString("TXT_Copy"));
      JMenuItem pasteItem =
          new JMenuItem(cvRes.getString("TXT_Paste"));

      editMenu.add(copyItem);
      editMenu.add(pasteItem);

      getCommandMgr().handleCommand(copyItem,
                                    XI5250CrtCtrl.COPY_CMD);
      getCommandMgr().handleCommand(pasteItem,
                                    XI5250CrtCtrl.PASTE_CMD);
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
                                    XI5250CrtCtrl.SWITCH_3DFX_CMD);
      getCommandMgr().handleCommand(referenceCursorItem,
                                    XI5250CrtCtrl.REFERENCE_CURSOR_CMD);
    }

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(editMenu);
    menuBar.add(optionsMenu);
    return menuBar;
  }


  /**
   * Inserisce nella tool-bar i bottoni di default.
   */
  protected JToolBar createToolBar() {
    // bottoni della tool-bar
    AbstractButton[] buttons = new AbstractButton[] {
      new JButton(cvImagesBdl.getIcon("Copy")),
      new JButton(cvImagesBdl.getIcon("Paste")),
      null,
      new JToggleButton(cvImagesBdl.getIcon("3dFx")),
      new JToggleButton(cvImagesBdl.getIcon("RefCursor")),
    };
    // action commands associati con i bottoni della tool-bar.
    String[]   buttonsActCmd = new String[] {
      XI5250CrtCtrl.COPY_CMD,
      XI5250CrtCtrl.PASTE_CMD,
      null,
      XI5250CrtCtrl.SWITCH_3DFX_CMD,
      XI5250CrtCtrl.REFERENCE_CURSOR_CMD,
    };
    // Hint associati ad i vari bottoni.
    String[] buttonHints = new String[] {
      cvRes.getString("TXT_Copy"),
      cvRes.getString("TXT_Paste"),
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


