/*
    ***
!!V 09/07/98 rel. 1.10 - creation.
 */


package net.infordata.em.tn5250;


import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import net.infordata.em.crt5250.*;
import net.infordata.em.util.*;

import java.util.*;


/**
 * Handles common commands shared by XI5250Frame and XI5250Applet.
 */
public class XI5250EmulatorCtrl extends XI5250CrtCtrl {

  // images
  private static XIImagesBdl cvImagesBdl =
      net.infordata.em.tn5250.XIImagesBdl.getImagesBdl();

  private static ResourceBundle cvRes =
      ResourceBundle.getBundle("net.infordata.em.tn5250.resources.Res");

  public static final String CONNECT_CMD          = "CONNECT_CMD";
  public static final String DISCONNECT_CMD       = "DISCONNECT_CMD";

  public static final String ABOUT_CMD            = "ABOUT_CMD";

  public static final String SNAPSHOT_CMD         = "SNAPSHOT_CMD";

  private int ivSnapShotCount = 0;


  /**
   */
  public XI5250EmulatorCtrl(XI5250Emulator aCrt) {
    super(aCrt);

    getEmulator().addEmulatorListener(new EmulatorListener());

    // Connect command
    getCommandMgr().enableCommand(
        CONNECT_CMD, !getEmulator().isActive());
    getCommandMgr().setCommand(CONNECT_CMD,  new XICommand() {
      public void execute() {
        processConnectCmd();
      }
    });

    // Disconnect command
    getCommandMgr().enableCommand(
        DISCONNECT_CMD, getEmulator().isActive());
    getCommandMgr().setCommand(DISCONNECT_CMD,  new XICommand() {
      public void execute() {
        processDisconnectCmd();
      }
    });

    // About command
    getCommandMgr().setCommand(ABOUT_CMD, new XICommand() {
      public void execute() {
        processAboutCmd();
      }
    });

    // Snapshot command
    getCommandMgr().setCommand(SNAPSHOT_CMD, new XICommand() {
      public void execute() {
        processSnapShotCmd();
      }
    });
  }


  /**
   */
  public final XI5250Emulator getEmulator() {
    return (XI5250Emulator)getCrt();
  }


  /**
   */
  protected void processConnectCmd() {
    if (!getEmulator().isActive()) {
      Object ret = JOptionPane.showInputDialog(
                       getEmulator(),
                       cvRes.getString("TXT_HostNameInput"),
                       "", JOptionPane.QUESTION_MESSAGE, null,
                       null, getEmulator().getHost());
      if (ret == null)
        return;
      getEmulator().setHost((String)ret);
    }
    getEmulator().setActive(true);
  }


  /**
   */
  protected void processDisconnectCmd() {
    if (getEmulator().isActive()) {
      int ret = JOptionPane.showConfirmDialog(
                   getEmulator(),
                   cvRes.getString("TXT_ConfirmDisconnect"),
                   "", JOptionPane.YES_NO_OPTION);
      if (ret == JOptionPane.NO_OPTION)
        return;
    }
    getEmulator().setActive(false);
  }


  /**
   */
  protected void processAboutCmd() {
    JOptionPane.showMessageDialog(getEmulator(),
                                  "Version " + getEmulator().VERSION + "\n" +
                                  "\n" +
                                  "Infordata S.p.A.\n" +
                                  "v.proietti@infordata.net",
                                  "About",
                                  JOptionPane.INFORMATION_MESSAGE,
                                  cvImagesBdl.getIcon("Logo"));
  }


  /**
   */
  protected void processSnapShotCmd() {
    XI5250Crt clone = getEmulator().getStaticClone();
    String title = "Snap-shot " + getEmulator().getHost() + " #" +
                   (++ivSnapShotCount);
    XI5250CrtFrame frm = new XI5250CrtFrame(title, clone);
    frm.setBounds(0, 0, 728, 512);
    frm.centerOnScreen();
    frm.show();
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Usata per sincronizzare i comandi con lo stato dell' emulator.
   */
  class EmulatorListener extends XI5250EmulatorAdapter {

    protected void enableCmd() {
      getCommandMgr().enableCommand(
          CONNECT_CMD, !getEmulator().isActive());
      getCommandMgr().enableCommand(
          DISCONNECT_CMD, getEmulator().isActive());
    }

    public void connecting(XI5250EmulatorEvent e) {
      enableCmd();
    }

    public void connected(XI5250EmulatorEvent e) {
      enableCmd();
    }

    public void disconnected(XI5250EmulatorEvent e) {
      enableCmd();
    }

    public void stateChanged(XI5250EmulatorEvent e) {
    }
  }
}


