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
!!V 09/07/98 rel. 1.10 - creation.
    05/02/99 rel. 1.11 - Swing 1.1 bug work-around.
    08/06/99 rel. 1.11a- The emulator is centered.
 */


package net.infordata.em.tn5250;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.util.XICommand;
import net.infordata.em.util.XICommandMgr;
import net.infordata.em.util.XIUtil;


/**
 */
public class XI5250Applet extends JApplet {

  private static final long serialVersionUID = 1L;

  private static final int DEBUG = 0;

  // if true the emulation frame survives to the applet dead,
  // !!VERIFY: a stopped applet causes event dispatching problems
  private static final boolean UNDEAD_FRAME = false;

  // images
  private static XIImagesBdl cvImagesBdl = XIImagesBdl.getImagesBdl();

  private static ResourceBundle cvRes =
      ResourceBundle.getBundle("net.infordata.em.tn5250.resources.Res");

  private XI5250EmulatorCtrl ivEmulatorCtrl;
  private EmulatorFrame      ivFrame;
  private boolean            ivFirstTime = true;

  private boolean            ivDestroyed = false;

  private PropertyChangeListener ivPropertyChangeListener =
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          emulatorPropertyChanged(evt);
        }
      };

  public static final String INFRAME_CMD = "INFRAME_CMD";


  /**
   */
  public void init() {
    if (DEBUG >= 1)
      System.out.println("init()");

    /*!!1.12
    if (System.getProperty("java.version").compareTo("1.1.1") < 0 ||
        System.getProperty("java.version").compareTo("1.1_Final") == 0) {
      System.err.println("!!! Use JDK 1.1.1 or newer !!!");
    }
    */
//    XI5250Emulator.checkJDK();

    String  host = null;
    host = getParameter("host");

    /*
    if (host == null) {
      System.err.println("Host name or ip address is required.");
      System.exit(-1);
    }
    */

    boolean inplace = true;
    {
      String ss = getParameter("inplace");
      if (ss != null && "false".equals(ss.toLowerCase()))
        inplace = false;
    }
    
    boolean altFKeyRemap = "true".equalsIgnoreCase(getParameter("altFKeyRemap"));
    String codePage = getParameter("codePage");

    ivEmulatorCtrl = new XI5250EmulatorCtrl(new XI5250Emulator());
    getEmulator().setTerminalType("IBM-3477-FC");
    getEmulator().setHost(host);
    getEmulator().setAltFKeyRemap(altFKeyRemap);
    getEmulator().setCodePage(codePage);

    //!! 1.11a execution moved to the EventQueueThread
    if (inplace)
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          emulatorInPlace();
        }
      });
    else
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          emulatorInFrame();
        }
      });

    // InFrame command
    getCommandMgr().enableCommand(
        INFRAME_CMD, ivFrame == null);
    getCommandMgr().setCommand(INFRAME_CMD,  new XICommand() {
      public void execute() {
        processInFrameCmd();
      }
    });
  }


  /**
   */
  public void start() {
    if (DEBUG >= 1)
      System.out.println("start()");

    if (getEmulator().getHost() != null)
      getEmulator().setActive(true);
  }


  /**
   */
  public void stop() {
    if (DEBUG >= 1)
      System.out.println("stop()");
  }


  /**
   */
  public void destroy() {
    if (DEBUG >= 1)
      System.out.println("destroy()");

    ivDestroyed = true;
    if (!UNDEAD_FRAME || ivFrame == null) {
      emulatorInPlace();
      getEmulator().setActive(false);
    }
  }


  /**
   */
  public final XI5250Emulator getEmulator() {
    return ivEmulatorCtrl.getEmulator();
  }


  /**
   */
  public final XICommandMgr getCommandMgr() {
    return ivEmulatorCtrl.getCommandMgr();
  }


  /**
   */
  protected void processInFrameCmd() {
    emulatorInFrame();
  }


  /**
   */
  public void emulatorInPlace() {
    if (!ivFirstTime && ivFrame == null)
      return;

    ivFirstTime = false;

    if (DEBUG >= 1)
      System.out.println("inplace");

    if (ivFrame != null) {
      ivFrame.setJMenuBar(null);  // work around for a Swing 1.1 bug
      
      ivFrame.dispose();
      ivFrame = null;
    }

    getCommandMgr().enableCommand(INFRAME_CMD, true);

    JToolBar toolBar = createToolBar();
    JMenuBar menuBar = createMenuBar();

    setJMenuBar(menuBar);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.NORTH);

    JPanel panel = new XI5250Emulator.SupportPanel(getEmulator());
    panel.setBackground(getEmulator().getBackground());
    getContentPane().add(panel, BorderLayout.CENTER);

    getEmulator().addPropertyChangeListener(ivPropertyChangeListener);
  }


  /**
   */
  public void emulatorInFrame() {
    if (ivFrame != null)
      return;

    if (DEBUG >= 1)
      System.out.println("inframe");

    getCommandMgr().enableCommand(INFRAME_CMD, false);

    getEmulator().removePropertyChangeListener(ivPropertyChangeListener);
    setJMenuBar(null);
    getContentPane().removeAll();
    validate();
    repaint();

    ivFrame = new EmulatorFrame("tn5250" + " " +
                                XI5250Emulator.VERSION,
                                getEmulator());
    ivFrame.setBounds(0, 0, 648, 506);
    ivFrame.centerOnScreen();
    ivFrame.setVisible(true);
  }


  /**
   */
  private void frameClosed() {
    if (DEBUG >= 1)
      System.out.println("frameClosed()");

    if (UNDEAD_FRAME && ivDestroyed)
      getEmulator().setActive(false);
    else
      emulatorInPlace();
  }


  /**
   */
  private void emulatorPropertyChanged(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();
    if ("background".equals(propertyName))
      getEmulator().getParent().setBackground(getEmulator().getBackground());
    else if ("font".equals(propertyName) ||
             XI5250Crt.CRT_SIZE.equals(propertyName)) 
      getEmulator().revalidate();
  }


  /**
   */
  private JMenuBar createMenuBar() {
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

      commMenu.add(connItem);
      commMenu.add(disconnItem);
      commMenu.addSeparator();
      commMenu.add(aboutItem);

      getCommandMgr().handleCommand(connItem,
                                    XI5250EmulatorCtrl.CONNECT_CMD);
      getCommandMgr().handleCommand(disconnItem,
                                    XI5250EmulatorCtrl.DISCONNECT_CMD);
      getCommandMgr().handleCommand(aboutItem,
                                    XI5250EmulatorCtrl.ABOUT_CMD);
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
      JMenuItem inFrameItem =
          new JMenuItem(cvRes.getString("TXT_InFrame"));
      JCheckBoxMenuItem switch3DfxItem =
          new JCheckBoxMenuItem(cvRes.getString("TXT_3dFx"));
      JCheckBoxMenuItem referenceCursorItem =
          new JCheckBoxMenuItem(cvRes.getString("TXT_RefCursor"));

      optionsMenu.add(inFrameItem);
      optionsMenu.addSeparator();
      optionsMenu.add(switch3DfxItem);
      optionsMenu.add(referenceCursorItem);

      getCommandMgr().handleCommand(inFrameItem,
                                    INFRAME_CMD);
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
  private JToolBar createToolBar() {
    // bottoni della tool-bar
    AbstractButton[] buttons = new AbstractButton[] {
      new JButton(
          new ImageIcon((Image)cvImagesBdl.getObject("Connect"))),
      new JButton(
          new ImageIcon((Image)cvImagesBdl.getObject("Disconnect"))),
      null,
      new JButton(
          new ImageIcon((Image)cvImagesBdl.getObject("Copy"))),
      new JButton(
          new ImageIcon((Image)cvImagesBdl.getObject("Paste"))),
      null,
      new JButton(
          new ImageIcon((Image)cvImagesBdl.getObject("SnapShot"))),
      null,
      new JButton(
          new ImageIcon((Image)cvImagesBdl.getObject("InFrame"))),
      null,
      new JToggleButton(
          new ImageIcon((Image)cvImagesBdl.getObject("3dFx"))),
      new JToggleButton(
          new ImageIcon((Image)cvImagesBdl.getObject("RefCursor"))),
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
      INFRAME_CMD,
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
      cvRes.getString("TXT_InFrame"),
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


  /**
   */
  public void paint(Graphics g) {
    super.paint(g);
    if (ivFrame != null) {
      Rectangle rt = getBounds();
      g.setColor(Color.darkGray);

      int hh = rt.height;
      for (int x = -hh; x < rt.width; x += 16)
        g.drawLine(x, hh, x + hh, 0);
    }
  }

  
  ///////////////////////////////////////////////////////////////////////////////

  /**
   */
  private class EmulatorFrame extends XI5250Frame {

    private static final long serialVersionUID = 1L;

    public EmulatorFrame(String aTitle, XI5250Emulator aCrt) {
      super(aTitle, aCrt);
    }

    protected void processExitCmd() {
      if (UNDEAD_FRAME && ivDestroyed)
        super.processExitCmd();
      else
        dispose();
    }

    protected void processWindowEvent(WindowEvent e) {
      switch(e.getID()) {
        case WindowEvent.WINDOW_CLOSED:
          XI5250Applet.this.frameClosed();
          return;
      }
      super.processWindowEvent(e);
    }
  }
}

