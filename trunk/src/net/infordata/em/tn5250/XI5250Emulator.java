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
!!V 20/03/97 rel. 0.90 - start of revisions history.
    28/03/97 rel. 0.91 - keyboard events can be queued (setKeyboardQueue(true))
    08/04/97 rel. 0.92a- system request handled like IBM personal-communications.
    10/04/97 rel. 0.93 - some fix to add SignedNumeric fields handling.
             Error row is saved when the emulator enters PRE_HELP state, it is
             restored on exit.
    16/04/97 rel. 0.94 - userError method added.
             rel. 0.95... work in progress
    27/05/97 rel. 1.00 - first release.
    04/06/97 rel. 1.00b- create5250Field method added.
    05/06/97 rel. 1.00c- reference cursor.
    19/06/97 rel. 1.01 - uses XITelnet version 1.01 with telnet proxy support.
    27/07/97 rel. 1.01b- when a packet is received and the emulator is in
             ST_SYSTEM_REQUEST state then it switches-back to the previous
             state.
             The CTRL key (restore) clears the keyboard queue.
    08/07/97 rel. 1.01c- reference cursor.
             clearKeyboardQueue() method added.
    14/07/97 rel. 1.02 - added support for 27x132 terminal type (IBM-3477-FC),
             to use this feature call the method setTerminalType().
    15/07/97 rel. 1.02c- XIDataOrd includes 0x1F char.
    16/07/97 rel. 1.02d- XI5250Emulator doesn't implement the XITelnetEmulator
             interface publically.
    17/07/97 rel. 1.02e- .
    23/07/97 rel. 1.03 - .
    25/07/97 rel. 1.03a- a bug in ...Multicaster.
    30/07/97 rel. 1.03b- bugs.
    06/08/97 rel. 1.03c- bug fix.
             Double-buffering in XICrt class.
    08/08/97 rel. 1.03d- translateKeyEvent() and processRawKeyEvent().
    28/08/97 rel. 1.04 - clipboard support added.
    02/09/97 rel. 1.04a- bug fix - XIReadFieldsCmd, XIReadImmediateCmd.
    24/09/97 rel. 1.05 - DNCX project.
    03/10/97 rel. 1.05a- bug fix (XISOHOrd).
    13/01/98 rel. 1.05d- NT painting bug (see crt.XICrtBuffer).
    14/01/98 rel. 1.06 - asynchronous paint on off-screen image
             (see crt.XICrtBuffer).
    15/01/98 rel. 1.06a- bug fix (XIRestoreScreenCmd, XISaveScreenCmd).
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    07/07/98 rel. 1.07 - XIImagesBdl uses XIUtil.createImage().
    09/07/98 rel. 1.10 - XI5250Applet.
    04/02/99 rel. 1.11 - Swing 1.1 and jdk 1.2 support.
    08/06/99 rel. 1.11a- Some adjustment to XI5250Applet.
    08/06/99 rel. 1.13 - Snap-shot.
    23/06/99 rel. 1.13a- See XI5250Crt.
             Mnemonics (short-cut) added to menu items.
    29/07/99 rel. 1.14 - Rework on 3d look&feel.
    02/08/99 rel. 1.15 - Rework on 3d look&feel, removed subpackage statusbar.*.
    06/09/99 rel. 1.15a- Missing action in restoreMemento().
    02/05/00 rel. 1.15b- Jdk 1.3rc2.
    23/11/00 rel. 1.15d- Jdk 1.3.
*/


package net.infordata.em.tn5250;


import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.EventListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250CrtBuffer;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.crt5250.XI5250FieldsList;
import net.infordata.em.crt5250.XIEbcdicTranslator;
import net.infordata.em.tnprot.XITelnet;
import net.infordata.em.tnprot.XITelnetEmulator;


////////////////////////////////////////////////////////////////////////////////

/**
 * THE 5250 EMULATOR.
 * <pre>
 * How to use it:
 *
 * XI5250Emulator em  = new XI5250Emulator();
 *
 * em.setKeyboardQueue(true);
 * em.setHost("AS400-HostName-Or-IpAddress");
 * em.setActive(true);
 *
 * </pre>
 *
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250Emulator extends XI5250Crt implements Serializable {

  private static final Logger LOGGER = Logger.getLogger(XI5250Emulator.class.getName());

  private static final long serialVersionUID = 1L;

  public static final String VERSION = "1.19";
  
  public static final int MAX_ROWS = 27;
  public static final int MAX_COLS = 132;

  // opcodes
  protected static final byte OPCODE_NOP              = (byte)0x00;
  protected static final byte OPCODE_INVITE_OPERATION = (byte)0x01;
  protected static final byte OPCODE_OUTPUT_ONLY      = (byte)0x02;
  protected static final byte OPCODE_PUT_GET          = (byte)0x03;
  protected static final byte OPCODE_SAVE_SCREEN      = (byte)0x04;
  protected static final byte OPCODE_RESTORE_SCREEN   = (byte)0x05;
  protected static final byte OPCODE_READ_IMM         = (byte)0x06;
  protected static final byte OPCODE_RESERVED1        = (byte)0x07;
  protected static final byte OPCODE_READ_SCREEN      = (byte)0x08;
  protected static final byte OPCODE_RESERVED2        = (byte)0x09;
  protected static final byte OPCODE_CANCEL_INVITE    = (byte)0x0A;
  protected static final byte OPCODE_TURN_ON_MSG      = (byte)0x0B;
  protected static final byte OPCODE_TURN_OFF_MSG     = (byte)0x0C;
  protected static final String[] OPCODE =
                                 {"No operation",
                                  "Invite operation",
                                  "Output only",
                                  "Put/Get Operation",
                                  "Save screen operation",
                                  "Restore screen operation",
                                  "Read immediate operation",
                                  "Reserved 1",
                                  "Read screen operation",
                                  "Reserved 2",
                                  "Cancel invite operation",
                                  "Turn ON message light",
                                  "Turn OFF message light"};
  
  // 5250 stream parsing errors
  protected static final int ERR_INVALID_COMMAND        = 10030101;
  protected static final int ERR_INVALID_CLEAR_UNIT_ALT = 10030105;
  protected static final int ERR_INVALID_SOH_LENGTH     = 10050131;
  protected static final int ERR_INVALID_ROW_COL_ADDR   = 10050122;
  protected static final int ERR_INVALID_EXT_ATTR_TYPE  = 10050132;
  protected static final int ERR_INVALID_SF_CLASS_TYPE  = 10050111;

  // flags
  protected static final byte FLAG_HLP                = (byte)0x01;
  protected static final byte FLAG_TRQ                = (byte)0x02;
  protected static final byte FLAG_SRQ                = (byte)0x04;
  protected static final byte FLAG_ATN                = (byte)0x40;
  protected static final byte FLAG_ERR                = (byte)0x80;

  protected static final byte ESC       = (byte)0x04;
  // read commands
  protected static final byte CMD_QUERY_DEVICE    = (byte)0xF3;
  protected static final byte CMD_READ_IMMEDIATE  = (byte)0x72;
  protected static final byte CMD_READ_FIELDS     = (byte)0x42;
  protected static final byte CMD_READ_MDT_FIELDS = (byte)0x52;
  protected static final byte CMD_READ_SCREEN     = (byte)0x62;
  protected static final byte CMD_SAVE_SCREEN     = (byte)0x02;
  // output commands
  protected static final byte CMD_CLEAR_FMT_TABLE = (byte)0x50;
  protected static final byte CMD_CLEAR_UNIT      = (byte)0x40;
  protected static final byte CMD_CLEAR_UNIT_ALT  = (byte)0x20;
  protected static final byte CMD_RESTORE_SCREEN  = (byte)0x12;
  protected static final byte CMD_ROLL            = (byte)0x23;
  protected static final byte CMD_WRITE_ERROR_CODE= (byte)0x21;
  protected static final byte CMD_WRITE_TO_DISPLAY= (byte)0x11;

  // orders
  // see http://publibfp.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/co2e2001/15.6.3?DT=19950629163252
  protected static final byte ORD_SBA  = (byte)0x11;  // Set buffer address 
  protected static final byte ORD_IC   = (byte)0x13;  // Insert cursor
  protected static final byte ORD_MC   = (byte)0x14;  // Move cursor 
  protected static final byte ORD_RA   = (byte)0x02;  // Repeat to address
  protected static final byte ORD_EA   = (byte)0x03;  // Erase to address 
  protected static final byte ORD_SOH  = (byte)0x01;  // Start of header
  protected static final byte ORD_TD   = (byte)0x10;  // Transparent data 
  protected static final byte ORD_WEA  = (byte)0x12;  // Write extended attributes 
  protected static final byte ORD_SF   = (byte)0x1D;  // Start of field
  protected static final byte ORD_WDSF = (byte)0x15;  // Write to Display Structured Field
  
  protected static final byte[] STRPCCMD = new byte[] {
    (byte)0x27, (byte)0x80, (byte)0xfc, (byte)0xd7, (byte)0xc3, (byte)0xd6, (byte)0x40, (byte)0x83, (byte)0x80, (byte)0xa1, (byte)0x80
  };  // Start PC Command
  protected static final byte[] ENDSTRPCCMD = new byte[] {
    (byte)0x27, (byte)0x00, (byte)0xfc, (byte)0xd7, (byte)0xc3, (byte)0xd6, (byte)0x40, (byte)0x83, (byte)0x80, (byte)0x82, (byte)0x00
  };  // Start PC Command

  // aid codes
  public static final byte AID_COMMAND   = (byte)0x31;
  public static final byte AID_FUNCTION  = (byte)0x32;
  public static final byte AID_F3        = (byte)0x33;
  public static final byte AID_F4        = (byte)0x34;
  public static final byte AID_F5        = (byte)0x35;
  public static final byte AID_F6        = (byte)0x36;
  public static final byte AID_F7        = (byte)0x37;
  public static final byte AID_F8        = (byte)0x38;
  public static final byte AID_F9        = (byte)0x39;
  public static final byte AID_F10       = (byte)0x3A;
  public static final byte AID_F11       = (byte)0x3B;
  public static final byte AID_F12       = (byte)0x3C;
  public static final byte AID_F13       = (byte)0xB1;
  public static final byte AID_F14       = (byte)0xB2;
  public static final byte AID_F15       = (byte)0xB3;
  public static final byte AID_F16       = (byte)0xB4;
  public static final byte AID_F17       = (byte)0xB5;
  public static final byte AID_F18       = (byte)0xB6;
  public static final byte AID_F19       = (byte)0xB7;
  public static final byte AID_F20       = (byte)0xB8;
  public static final byte AID_F21       = (byte)0xB9;
  public static final byte AID_F22       = (byte)0xBA;
  public static final byte AID_F23       = (byte)0xBB;
  public static final byte AID_F24       = (byte)0xBC;
  public static final byte AID_CLEAR     = (byte)0xBD;
  public static final byte AID_ENTER     = (byte)0xF1;
  public static final byte AID_HELP      = (byte)0xF3;
  public static final byte AID_ROLL_DN   = (byte)0xF4;
  public static final byte AID_ROLL_UP   = (byte)0xF5;
  public static final byte AID_PRINT     = (byte)0xF6;
  public static final byte AID_REC_BACKSP= (byte)0xF8;
  public static final byte AID_AUTO_ENTER= (byte)0x3F;

  // emulator state
  // state less than 0 are not saved in ivPrevState
  public static final int ST_NULL              = -2;
  public static final int ST_TEMPORARY_LOCK    = -1;
  public static final int ST_HARDWARE_ERROR    = 0;
  public static final int ST_NORMAL_LOCKED     = 1;
  public static final int ST_NORMAL_UNLOCKED   = 2;
  public static final int ST_POST_HELP         = 3;
  public static final int ST_POWER_ON          = 4;
  public static final int ST_PRE_HELP          = 5;
  public static final int ST_SS_MESSAGE        = 6;
  public static final int ST_SYSTEM_REQUEST    = 7;
  public static final int ST_POWERED           = 8;   //!!1.01

  private static final String[] ST_DESCRIPTION =
                           {"ST_NULL",
                            "ST_TEMPORARY_LOCK",
                            "ST_HARDWARE_ERROR",
                            "ST_NORMAL_LOCKED",
                            "ST_NORMAL_UNLOCKED",
                            "ST_POST_HELP",
  	                        "ST_POWER_ON",
                            "ST_PRE_HELP",
                            "ST_SS_MESSAGE",
                            "ST_SYSTEM_REQUEST",
                            "ST_POWERED"};

  // telnet connection
  transient private XITelnet        ivTelnet;
  transient private byte[]          ivRXBuf    = new byte[1024 * 8];
  transient private int             ivRXBufLen;

  // one bit for each function key
  transient private int             ivFunctionKeysMask;

  /**
   * The current commands list.
   */
  transient XI5250CmdList   ivCmdList;
  /**
   * The pending command (used when an aid code is pressed).
   */
  transient XI5250Cmd       ivPendingCmd;

  // current and previouos state
  transient private int         ivState = ST_NULL;
  transient private int         ivPrevState = ST_NULL;

  transient private int         ivErrorRow;

  transient private XI5250StatusBar ivStatusBar;

  //
  //!!1.06a protected Vector        ivSavedScreenList = new Vector(10, 5);
  //!!!.06a replace the vector with a circular buffer
  transient protected XI5250EmulatorMemento[] ivSavedScreens =
      new XI5250EmulatorMemento[10];
  transient protected int ivSavedScreensIdx = 0;

  //!!0.91
  transient KeyEventQueue           ivKeybEventQueue;
  transient KeyEventDispatchThread  ivKeybThread;

  //!!0.92 used when the user switches to SYSTEM_REQUEST state
  transient private XI5250EmulatorMemento  ivSysReqMemento;
  transient private XI5250Field            ivSysReqField;

  //!!0.93 used when the emulator switch to PRE_HELP state
  transient private XI5250CrtBuffer        ivPreHelpErrorLine;

  //!!0.95
  transient private XI5250EmulatorListener ivEmulatorListener;

  //!!1.02
  private String                 ivTermType;

  //!!1.02d
  transient private TelnetEmulator         ivTelnetEmulator = new TelnetEmulator();

  //!!1.03a
  /**
   * Used when switching from 24x80 to ...
   */
  transient protected Font                   ivPrevFont;

  //!!1.04c
  public static final String       ACTIVE            = "active";
  public static final String       TERMINAL_TYPE     = "terminalType";
  public static final String       ALTFKEY_REMAP     = "altFKeyRemap";

  public static final String       STRPCCMD_ENABLED  = "strPcCmd";

  //!!1.07
  private String                   ivHost;
  
  private boolean                  ivAltFKeyRemap;
  
  private boolean                  ivStrPcCmdEnabled;
  private boolean                  ivReceivedStrPcCmd;
  private boolean                  ivReceivedEndStrPcCmd;
  
  /**
   * Default contructor.
   */
  public XI5250Emulator() {
    ivStatusBar = new XI5250StatusBar();
    ivStatusBar.setVisible(false);
    add(ivStatusBar);

    enableEvents(AWTEvent.COMPONENT_EVENT_MASK);  //!!1.03

    setTerminalType("IBM-3179-2");

    setState(ST_POWER_ON);

    setErrorRow(getCrtSize().height - 1);

    setKeyboardQueue(true);
  }


  /**
   * XI5250EmulatorListener handling
   */
  public synchronized void addEmulatorListener(XI5250EmulatorListener l) {
    ivEmulatorListener = Multicaster.add(ivEmulatorListener, l);
  }


  /**
   * XI5250EmulatorListener handling
   */
  public synchronized void removeEmulatorListener(XI5250EmulatorListener l) {
    ivEmulatorListener = Multicaster.remove(ivEmulatorListener, l);
  }


  /**
   * Routes XI5250EmulatorEvent to listeners
   */
  protected void processEmulatorEvent(XI5250EmulatorEvent e) {
    if (ivEmulatorListener == null)
      return;

    switch (e.getID()) {
      //
      case XI5250EmulatorEvent.CONNECTING:
        ivEmulatorListener.connecting(e);
        break;
      //
      case XI5250EmulatorEvent.CONNECTED:
        ivEmulatorListener.connected(e);
        break;
      //
      case XI5250EmulatorEvent.DISCONNECTED:
        ivEmulatorListener.disconnected(e);
        break;
      //
      case XI5250EmulatorEvent.STATE_CHANGED:
        ivEmulatorListener.stateChanged(e);
        break;
      //
      case XI5250EmulatorEvent.NEW_PANEL_RECEIVED:
        ivEmulatorListener.newPanelReceived(e);
        break;
      //
      case XI5250EmulatorEvent.FIELDS_REMOVED:
        ivEmulatorListener.fieldsRemoved(e);
        break;
      //
      case XI5250EmulatorEvent.DATA_SENDED:
        ivEmulatorListener.dataSended(e);
        break;
    }
  }


  /**
   * Redefined to move the status bar.
  protected void processCrtEvent(XI5250CrtEvent e) {
    switch (e.getID()) {
      case XI5250CrtEvent.SIZE_CHANGED:
        if (ivStatusBar != null)
          moveStatusBar();
        break;
    }
    super.processCrtEvent(e);
  }
   */


  /**
   * Redefined to move the status bar.
   */
  @Override
  public void doLayout() {
    super.doLayout();
    moveStatusBar();
  }


  /**
   */
  private void moveStatusBar() {
    synchronized (getTreeLock()) {
      ivStatusBar.setFont(getFont());
      ivStatusBar.setBounds(0, getCrtBufferSize().height,
                            getCrtBufferSize().width,
                            getCharSize().height + 4);
      ivStatusBar.setVisible(true);
    }
  }


  /**
   * Sets the host name, a previuos open connection is closed.
   */
  public synchronized void setHost(String aHost) {
    if (aHost == ivHost ||
        (aHost != null && aHost.equals(ivHost)))
      return;

    setActive(false);
    ivHost = aHost;
  }


  /**
   * Returns the host name.
   */
  public final String getHost() {
    return ivHost;
  }


  /**
   * Activate or deactivate the connection.
   */
  public void setActive(boolean activate) {
    boolean wasActive;
    synchronized (this) {
      wasActive = isActive();
      if (activate == wasActive)
        return;

      if (activate) {
        ivTelnet = new XITelnet(ivHost);
        ivTelnet.setEmulator(ivTelnetEmulator);
        ivTelnet.connect();
      }
      else {
        ivTelnet.disconnect();
        ivTelnet.setEmulator(null);
        ivTelnet = null;
      }
    }

    firePropertyChange(ACTIVE, wasActive, isActive());
  }


  /**
   * True if the connection is active.
   */
  public boolean isActive() {
    return (ivTelnet == null) ? false : ivTelnet.isConnected();
  }


  /**
   * Sets the terminal type.
   * <pre>
   * Valid values are:
   * IBM-3179-2  : 24x80  color display (DEFAULT)
   * IBM-3477-FC : 27x132 color display
   * </pre>
   */
  public void setTerminalType(String aTermType) {
    if (aTermType != null && aTermType.equals(ivTermType))
      return;

    if (isActive())
      throw new IllegalArgumentException(
          "Cannot change the terminal type, close the connection first.");

    if (!"IBM-3179-2".equals(aTermType) && !"IBM-3477-FC".equals(aTermType))
      throw new IllegalArgumentException(
          "Wrong terminal type, valid values are IBM-3179-2 or IBM-3477-FC.");

    String oldTermType = ivTermType;
    ivTermType = aTermType;

    firePropertyChange(TERMINAL_TYPE, oldTermType, ivTermType);
  }


  /**
   */
  public String getTerminalType() {
    return ivTermType;
  }

  
  /**
   * @param value - if true FKEYS are accepted even if pressed in combination
   *   with the ALT key (usefull with platforms where FKEYS can be captured by 
   *   the OS itself, such as MAX-OSX). 
   */
  public void setAltFKeyRemap(boolean value) {
    if (value == ivAltFKeyRemap)
      return;
    boolean old = ivAltFKeyRemap;
    ivAltFKeyRemap = value;
    firePropertyChange(ALTFKEY_REMAP, old, ivAltFKeyRemap);
  }
  
  public boolean getAltFKeyRemap() {
    return ivAltFKeyRemap;
  }

  /**
   * @param value - if true the STRPCCMD order is enabled. 
   */
  public void setStrPcCmdEnabled(boolean value) {
    if (value == ivStrPcCmdEnabled)
      return;
    boolean old = ivStrPcCmdEnabled;
    ivStrPcCmdEnabled = value;
    firePropertyChange(STRPCCMD_ENABLED, old, ivStrPcCmdEnabled);
  }
  
  public boolean isStrPcCmdEnabled() {
    return ivStrPcCmdEnabled;
  }

  /**
   * Moves the cursor to the given position.
   * It updates also the status bar area with the new cursor coordinates.
   */
  @Override
  public void setCursorPos(int aCol, int aRow) {
    super.setCursorPos(aCol, aRow);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ivStatusBar.setCoordArea(getCursorCol() + 1, getCursorRow() + 1);
      }
    });
  }


  /**
   * Redefined to take care of the status-bar presence
   */
  @Override
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    size.height += getCharSize().height + 4;
    return size;
  }


  /**
   * Redefined to take care of the status-bar presence
   */
  @Override
  public Dimension getMinimumSize() {
    Dimension size = super.getMinimumSize();
    size.height += getMinCharSize().height + 4;
    return size;
  }


  /**
   * Redefined to take care of the status-bar presence
   * @see    crt.XICrt#getTestSize
   */
  @Override
  protected Dimension getTestSize(Font aFont) {
    FontMetrics fm = getFontMetrics(aFont);
    Dimension res = new Dimension(fm.charWidth('W') * getCrtSize().width,
                                  fm.getHeight() * (getCrtSize().height + 1) + 4);
    return res;
  }


  /**
   * Factory method for XI5250Field creation.
   */
  protected XI5250Field create5250Field(byte[] aFFW, byte[] aFCW,
                                        int aCol, int aRow,
                                        int aLen, int aAttr) {
    return new XI5250Field(this, aFFW, aFCW, aCol, aRow, aLen, aAttr);
  }


  /**
   * Creates a XI5250EmulatorMemento instance.
   * It is like a snapshot of the current internal state.
   */
  public synchronized XI5250EmulatorMemento createMemento() {
    return new XI5250EmulatorMemento((XI5250FieldsList)ivFields.clone(),
                                     ivFunctionKeysMask, ivPendingCmd,
                                     ivState, ivPrevState,
                                     getCursorCol(), getCursorRow(),
                                     ivErrorRow,
                                     (XI5250CrtBuffer)getCrtBuffer().clone());
  }


  /**
   * Restores the internal state to a previous saved state.
   */
  public void restoreMemento(XI5250EmulatorMemento aMemento) {
    //!!0.95 this statement avoids deadlocks with component resizing.
    synchronized (getTreeLock()) {
      synchronized (this) {
        Dimension dim = aMemento.ivCrtBuffer.getCrtSize();
        setCrtSize(dim.width, dim.height); //1.15a

        ivFields = aMemento.ivFields;
        ivFunctionKeysMask = aMemento.ivFunctionKeysMask;
        ivPendingCmd =  aMemento.ivPendingCmd;
        setState(aMemento.ivState);
        ivPrevState = aMemento.ivPrevState;
        setErrorRow(aMemento.ivErrorRow);
        setCursorPos(aMemento.ivCol, aMemento.ivRow);
        getCrtBuffer().copyFrom(aMemento.ivCrtBuffer);

        processEmulatorEvent(new XI5250EmulatorEvent(XI5250EmulatorEvent.NEW_PANEL_RECEIVED,
                                                     this));
      }
    }
    repaint();
  }


  /**
   * Works as createMemento but it is used when it switches to the System Request State.
   */
  protected synchronized XI5250EmulatorMemento createSysReqMemento() {
    return new XI5250EmulatorMemento((XI5250FieldsList)ivFields.clone(),
                                     ivFunctionKeysMask, ivPendingCmd,
                                     ivState, ivPrevState,
                                     getCursorCol(), getCursorRow(),
                                     ivErrorRow,
                                     new XI5250CrtBuffer(
                                         (XI5250CrtBuffer)getCrtBuffer(),
                                         0, ivErrorRow, getCrtSize().width, 1));
  }


  /**
   * @see    #createSysReqMemento
   */
  protected void restoreSysReqMemento(XI5250EmulatorMemento aMemento) {
    //!!0.95 this statement avoids deadlocks with component resizing
    synchronized (getTreeLock()) {
      synchronized (this) {
        ivFields = aMemento.ivFields;
        ivFunctionKeysMask = aMemento.ivFunctionKeysMask;
        ivPendingCmd =  aMemento.ivPendingCmd;
        //!! NO
        //setState(aMemento.ivState);
        //ivPrevState = aMemento.ivPrevState;
        setErrorRow(aMemento.ivErrorRow);
        setCursorPos(aMemento.ivCol, aMemento.ivRow);

        getCrtBuffer().copyFrom(0, ivErrorRow, aMemento.ivCrtBuffer, 0, 0,
                                getCrtSize().width, 1);

        processEmulatorEvent(
            new XI5250EmulatorEvent(XI5250EmulatorEvent.NEW_PANEL_RECEIVED,
                                    this));
      }
    }
    repaint();
  }


  /**
   * Called by XITelnet just before trying to connect.
   * Sets the 5250 telnet requested flags (ie. TELOPT_BINARY, TELOPT_TTYPE, TELOPT_EOR).
   * Fires the XI5250EmulatorEvent.CONNECTING event.
   */
  protected void connecting() {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("connecting()");

    // sets required telnet options
    ivTelnet.setTerminalType(ivTermType);

    ivTelnet.setLocalReqFlag(XITelnet.TELOPT_BINARY, true);
    ivTelnet.setLocalReqFlag(XITelnet.TELOPT_TTYPE, true);
    ivTelnet.setLocalReqFlag(XITelnet.TELOPT_EOR, true);

    ivTelnet.setRemoteReqFlag(XITelnet.TELOPT_BINARY, true);
    ivTelnet.setRemoteReqFlag(XITelnet.TELOPT_EOR, true);

    processEmulatorEvent(
        new XI5250EmulatorEvent(XI5250EmulatorEvent.CONNECTING, this));
  }


  /**
   * Called by XITelnet when a connection is established.
   * Fires the XI5250EmulatorEvent.CONNECTED event.
   */
  protected void connected() {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("connected()");

    processEmulatorEvent(
        new XI5250EmulatorEvent(XI5250EmulatorEvent.CONNECTED, this));
  }


  /**
   * Called by XITelnet when a connection is closed.
   * Fires the XI5250EmulatorEvent.DISCONNECTED event.
   */
  protected void disconnected() {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("disconnected()");

    { //!!1.07
      setState(ST_POWER_ON);
      setDefAttr(0x20);
      clear();
      removeFields();
      setCursorPos(0, 0);
    }

    processEmulatorEvent(
        new XI5250EmulatorEvent(XI5250EmulatorEvent.DISCONNECTED, this));
  }


  /**
   * Called by XITelnet when an IOException is catched.
   */
  protected void catchedIOException(final IOException ex) {
    if (LOGGER.isLoggable(Level.WARNING))
      LOGGER.log(Level.WARNING, "catchedIOException()", ex);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(XI5250Emulator.this, 
            ex.getClass().getName() + "\n" + 
            ex.getMessage() + 
            "\nSee the log for details ",
            "WARNING", JOptionPane.WARNING_MESSAGE);
      }
    });
  }

  /**
   * Called when 5250 stream parsing exception is catched.
   * @param ex
   */
  protected void catched5250Exception(XI5250Exception ex) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.log(Level.FINE, "catched5250Exception()", ex);
    send5250Error(ex.getErrorCode());
  }
  
  /**
   * Called when an generic exception is catched.
   * @param ex
   */
  protected void catchedException(final Throwable ex) {
    LOGGER.log(Level.SEVERE, "catchedException()", ex);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(XI5250Emulator.this, 
            ex.getMessage() + "\nSee the log for details ",
            "ERROR", JOptionPane.ERROR_MESSAGE);
      }
    });
  }

  /**
   */
  protected void receivedData(byte[] buf, int len) {
    if (getState() == ST_POWER_ON) {
      if (LOGGER.isLoggable(Level.WARNING))
        LOGGER.log(Level.WARNING, "Discarding received data len: " + len);
      return;
    }

    // eventuali eccezioni di overflow vengono generate dal linguaggio in automatico
    System.arraycopy(buf, 0, ivRXBuf, ivRXBufLen, len);
    ivRXBufLen += len;
  }


  /**
   * Processes the 5250 received stream creating a new XI5250CmdList.
   * <pre>
   * this is a simplified version of what it does:
   *
   *   ...
   *   XI5250CmdList cmdList = createCmdList(this);  // creates a new command list
   *   ...
   *   cmdList.readFrom5250Stream(dataStream);       // construct commands from the 5250 data stream
   *   ...
   *   cmdList.execute();                            // execute all commands
   *   ...
   *   initAllFields();                              // input fields inizialization
   *   ...
   *   ...                  // fires XI5250EmulatorEvent.NEW_PANEL_RECEIVED event
   *
   * </pre>
   */
  protected void receivedEOR() {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("receivedEOR()");

      if (LOGGER.isLoggable(Level.FINER)) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ivRXBufLen; i++)
          sb.append(XITelnet.toHex(ivRXBuf[i]) + "  ");
        LOGGER.finer(sb.toString());
      }
    }

    // verify packet header
    int packetLen = (XITelnet.toInt(ivRXBuf[0]) << 8) +
                    XITelnet.toInt(ivRXBuf[1]);
    if (packetLen != ivRXBufLen || ivRXBuf[2] != (byte)0x12 ||
        ivRXBuf[3] != (byte)0xA0) {
      //TODO ??
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.fine("malformed packet");

      ivRXBufLen = 0;
      return;
    }

    // variable header part
    int     varHdrLen = XITelnet.toInt(ivRXBuf[6]);
    int     dataStart = 6 + varHdrLen;                // start of data
    @SuppressWarnings("unused") 
    final boolean errFlag   = (ivRXBuf[7] & FLAG_ERR) != 0; // data stream output error
    @SuppressWarnings("unused") 
    final boolean atnFlag   = (ivRXBuf[7] & FLAG_ATN) != 0; // attention key pressed
    @SuppressWarnings("unused") 
    final boolean srqFlag   = (ivRXBuf[7] & FLAG_SRQ) != 0; // system request key pressed
    @SuppressWarnings("unused") 
    final boolean trqFlag   = (ivRXBuf[7] & FLAG_TRQ) != 0; // test request key
    @SuppressWarnings("unused") 
    final boolean hlpFlag   = (ivRXBuf[7] & FLAG_HLP) != 0; // help error state

    byte    opCode    = ivRXBuf[9];

    ByteArrayInputStream dataStream = new ByteArrayInputStream(ivRXBuf, dataStart,
                                                               ivRXBufLen - dataStart);
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("OPCODE : " + OPCODE[opCode]);

    switch (opCode) {
      //
      case OPCODE_NOP:
      case OPCODE_RESERVED1:
      case OPCODE_RESERVED2:
        break;
      //
      case OPCODE_OUTPUT_ONLY:
      case OPCODE_PUT_GET:
      case OPCODE_READ_SCREEN:
      case OPCODE_SAVE_SCREEN:
      case OPCODE_RESTORE_SCREEN:
      case OPCODE_READ_IMM:
      case OPCODE_INVITE_OPERATION:
        ivReceivedStrPcCmd = false;
        ivReceivedEndStrPcCmd = false;
        // read command list
        XI5250CmdList cmdList = createCmdList(this);
        try {
          cmdList.readFrom5250Stream(dataStream);
          ivCmdList = cmdList;
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              //!!0.95 this statement avoids deadlocks during component resizing
              synchronized (getTreeLock()) {
                synchronized (this) {
                  if (getState() == ST_SYSTEM_REQUEST)  //!!1.01b
                    setPrevState();

                  setFreeze(true);
                  try {
                    ivCmdList.execute();
                    // input fields initialization
                    initAllFields();

                    processEmulatorEvent(
                        new XI5250EmulatorEvent(
                                XI5250EmulatorEvent.NEW_PANEL_RECEIVED,
                                XI5250Emulator.this));
                  }
                  finally {
                    setFreeze(false);
                  }
                }
              }
              if (ivReceivedStrPcCmd) {
                char ctrlChar = getChar(0, 0);
                boolean wait = !(ctrlChar == 'a');
                String cmd = getString(1, 0, 132 - STRPCCMD.length);
                {
                  int k = cmd.length() - 1;
                  for ( ; k >= 0; k--) {
                    char ch = cmd.charAt(k);
                    if (!Character.isISOControl(ch) && !Character.isWhitespace(ch))
                      break;
                  }
                  cmd = cmd.substring(0, k + 1);
                }
                try {
                  if (LOGGER.isLoggable(Level.INFO)) {
                    if (wait)
                      LOGGER.info("Executing and waiting for local command: " + cmd);
                    else
                      LOGGER.info("Executing local command: " + cmd);
                  }
                  try {
                    strPcCmd(wait, cmd);
                  }
                  catch (IOException ex) {
                    catchedIOException(ex);
                  }
                  catch (InterruptedException ex) {
                    catchedException(ex);
                  }
                }
                finally {
                  // AUTOENTER
                  processRawKeyEvent(
                      new KeyEvent(XI5250Emulator.this, KeyEvent.KEY_PRESSED,
                      0, -1, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));                  
                }
              }
              else if (ivReceivedEndStrPcCmd) {
                if (LOGGER.isLoggable(Level.INFO)) {
                  LOGGER.info("Received STRPCCMD shutdown" );
                }
                // AUTOENTER
                processRawKeyEvent(
                    new KeyEvent(XI5250Emulator.this, KeyEvent.KEY_PRESSED,
                    0, -1, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));                  
              }
            }
          });
        }
        catch (IOException ex) {
          catchedException(ex);
        }
        catch (XI5250Exception ex) {
          catched5250Exception(ex);
        }
        catch (Exception ex) {
          catchedException(ex);
        }

        if (LOGGER.isLoggable(Level.FINER))
          LOGGER.finer("" + cmdList);

        break;
      //
      case OPCODE_CANCEL_INVITE:
        receivedCancelInvite();
        break;
      //
      case OPCODE_TURN_ON_MSG:
      case OPCODE_TURN_OFF_MSG:
        switchMsgLight(opCode == OPCODE_TURN_ON_MSG);
        break;
      //
      default:
        throw new RuntimeException("Illegal 5250 opcode received");
    }

    ivRXBufLen = 0;
  }


  /**
   */
  protected void localFlagsChanged(byte aIACOpt) {
  }


  /**
   */
  protected void remoteFlagsChanged(byte aIACOpt) {
    if (aIACOpt == XITelnet.TELOPT_EOR &&
        ivTelnet.isRemoteFlagON(XITelnet.TELOPT_EOR))
      setState(ST_POWERED);
  }


  /**
   */
  protected void unhandledRequest(byte aIACOpt, String aIACStr) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("unhandledRequest()");
  }


  /**
   * Turns on or off the message icon on the status bar.
   */
  protected void switchMsgLight(boolean turnOn) {
    ivStatusBar.setMessageArea((turnOn) ? XI5250StatusBar.MESSAGE_ON :
                                          XI5250StatusBar.MESSAGE_OFF);
  }


  /**
   * Answers to cancel invite request.
   */
  protected void receivedCancelInvite() {
    ivPendingCmd = null;

    send5250Packet((byte)0x00, OPCODE_CANCEL_INVITE, null);
  }


  /**
   * Enables or disables the keyboard queue. (DEFAULT enabled)
   */
  public synchronized void setKeyboardQueue(boolean flag) {
    if (flag == isKeyboardQueue())
      return;

    if (flag) {
      ivKeybEventQueue = new KeyEventQueue();
      startKeybThread();
    }
    else {
      ivKeybEventQueue = null;
      stopKeybThread();
    }
  }


  /**
   */
  public boolean isKeyboardQueue() {
    return (ivKeybEventQueue != null);
  }


  /**
   * Removes all entries in keyboard queue.
   */
  public void	clearKeyboardQueue() {
    if (ivKeybEventQueue != null)
      ivKeybEventQueue.removeAll();
  }


  /**
   */
  protected void startKeybThread() {
    if (!isKeyboardQueue() || ivKeybThread != null)
      return;

    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("startKeybThread()");

    ivKeybThread = new KeyEventDispatchThread(this);
    ivKeybThread.start();
  }


  /**
   */
  protected void stopKeybThread() {
    if (!isKeyboardQueue() || ivKeybThread == null)
      return;

    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("stopKeybThread()");

    ivKeybThread.stopDispatching();
    ivKeybThread = null;
  }


  /**
   * Changes the emulator state.
   */
  protected synchronized void setState(int aState) {
    if (aState == ivState)
      return;

    int oldState = ivState;

    // ST_TEMPORARY_LOCK must not be saved !!!
    if (ivState >= 0)
      ivPrevState = ivState;
    ivState = aState;
    ivStatusBar.setStateArea(aState);

    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("ivPrevState = " + ST_DESCRIPTION[ivPrevState - ST_NULL] + "    " +
          "ivState = " + ST_DESCRIPTION[ivState - ST_NULL]);

    // things to do after exiting from a state
    switch (oldState) {
      //
      case ST_SYSTEM_REQUEST:
        restoreSysReqMemento(ivSysReqMemento);
        ivSysReqMemento = null;
        ivSysReqField = null;
        break;
      //
      case ST_PRE_HELP:
        // restore error row
        getCrtBuffer().copyFrom(0, ivErrorRow, ivPreHelpErrorLine,
                                0, 0, getCrtSize().width, 1);
        repaint();
        break;
    }

    // things to do after entering a new state
    switch (aState) {
      //
      case ST_POWER_ON:
        ivStatusBar.setFlashArea(true);
        break;
      //
      case ST_POWERED:
        ivStatusBar.setFlashArea(false);
        break;
      //
      case ST_TEMPORARY_LOCK:
      case ST_NORMAL_LOCKED:
        stopKeybThread();
        break;
      //
      case ST_SYSTEM_REQUEST:
        ivSysReqMemento = createSysReqMemento();
        removeFields();
        ivPendingCmd = null;                      //!!0.92a
        drawString(String.valueOf(XI5250Emulator.ATTRIBUTE_PLACE_HOLDER),
                   0, ivErrorRow, 0x34);
        ivSysReqField =
            new XI5250Field(this, 1, ivErrorRow, getCrtSize().width - 2, -1);
        addField(ivSysReqField);
        initAllFields();
        ivSysReqField.clear();
        setCursorPos(1, ivErrorRow);
        startKeybThread();
        break;
      //
      case ST_PRE_HELP:
        // save error row
        ivPreHelpErrorLine = new XI5250CrtBuffer((XI5250CrtBuffer)getCrtBuffer(),
                                                 0, ivErrorRow,
                                                 getCrtSize().width, 1);
        startKeybThread();
        break;
      //
      default:
        startKeybThread();
        break;
    }

    processEmulatorEvent(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, this));
  }


  /**
   * Returns the current emulator state.
   */
  public int getState() {
    return ivState;
  }


  /**
   * Switches back to the previous state.
   */
  protected void setPrevState() {
    setState(ivPrevState);
  }


  /**
   */
  public int getPrevState() {
    return ivPrevState;
  }


  /**
   * Factory method for XI5250CmdList class.
   */
  protected XI5250CmdList createCmdList(XI5250Emulator aEm) {
    return new XI5250CmdList(aEm);
  }


  /**
   * Factory method for XI5250OrdList class
   */
  protected XI5250OrdList createOrdList(XI5250Emulator aEm) {
    return new XI5250OrdList(aEm);
  }


  /**
   * Changes the error row.
   */
  protected void setErrorRow(int aRow) {
    ivErrorRow = Math.min(aRow, getCrtSize().height - 1);
  }


  /**
   */
  protected int getErrorRow() {
    return ivErrorRow;
  }


  /**
   */
  protected void setFunctionKeysMask(int aMask) {
    ivFunctionKeysMask = aMask;
  }


  /**
   */
  protected boolean isMasterMDTSet() {
    //TODO ??
    return true;
  }


  /**
   * DO NOT USE, reserved for commands and orders
   * Sends a 5250 data stream.
   */
  public void send5250Packet(byte flags, byte opcode, byte[] buf, int aLen) {
    byte[] cBuf = {(byte)0x00, (byte)0x00,   //len
                   (byte)0x12, (byte)0xA0,
                   (byte)0x00, (byte)0x00,
                   (byte)0x04, flags,
                   (byte)0x00, opcode};
    int len = aLen + cBuf.length;

    cBuf[0] = (byte)((len & 0xFF00)>> 8);
    cBuf[1] = (byte)(len & 0x00FF);

    try {
      ivTelnet.send(cBuf);
      if (buf != null  && aLen != 0)
        ivTelnet.send(buf, aLen);
      ivTelnet.sendEOR();
    }
    catch (IOException ex) {
      catchedIOException(ex);
    }
  }


  /**
   * DO NOT USE, reserved for commands and orders
   * Sends a 5250 data stream.
   */
  public void send5250Packet(byte flags, byte opcode, byte[] buf) {
    send5250Packet(flags, opcode, buf, (buf != null) ? buf.length : 0);
  }


  /**
   */
  public void send5250UserError(int aErrorCode) {
    XIEbcdicTranslator    translator = getTranslator();
    byte[] buf = translator.toPacked(aErrorCode, 4);

    send5250Packet(FLAG_HLP, OPCODE_NOP, buf);
  }


  /**
   * DO NOT USE, reserved for commands and orders
   * Sends a 5250 error decoding request.
   */
  public void send5250Error(int aErrorCode) {
    XIEbcdicTranslator    translator = getTranslator();
    byte[] buf = translator.toPacked(aErrorCode, 8);

    send5250Packet(FLAG_ERR, OPCODE_NOP, buf);
  }


  /**
   * DO NOT USE, reserved for commands and orders
   * Sends a 5250 data-stream.
   * To store in the output stream the fields content uses an instance of XIFieldTo5250Stream.
   * @see    XIFieldTo5250Stream
   *
   * @param    anAidCode          the aid code to use AID_...
   * @param    includeFields      fields content must be sended
   * @param    includeMDTOnly     send only modified fields
   */
  public void send5250Data(int anAidCode, boolean includeFields,
                           boolean includeMDTOnly) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    byte[] cBuf = {(byte)(getCursorRow() + 1), (byte)(getCursorCol() + 1),
                   (byte)anAidCode};

    try {
      out.write(cBuf);

      if (includeFields)
        ivFields.saveTo(new XIFieldTo5250Stream(this, out, includeMDTOnly));

      byte[] buf = out.toByteArray();

      if (LOGGER.isLoggable(Level.FINER)) {
        LOGGER.finer("---->Send5250Data");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++)
          sb.append(XITelnet.toHex(buf[i]) + "  ");
        LOGGER.finer(sb.toString());
      }

      send5250Packet((byte)0x00, OPCODE_NOP, buf);

      processEmulatorEvent(new XI5250EmulatorEvent(XI5250EmulatorEvent.DATA_SENDED,
                                                   this, (byte)anAidCode));
    }
    catch (IOException ex) {
      catchedIOException(ex);
    }
  }


  /**
   * DO NOT USE, reserved for commands and orders
   */
  public void send5250SavedScreen(int aScreenNum) {
    byte[] cBuf = {ESC, CMD_RESTORE_SCREEN, (byte)aScreenNum};

    send5250Packet((byte)0x00, OPCODE_SAVE_SCREEN, cBuf);
  }


  /**
   * DO NOT USE, reserved for commands and orders
   */
  public void send5250Screen() {
    XIEbcdicTranslator    translator = getTranslator();
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

    for (int row = 0; row < getCrtSize().height; row++) {
      for (int col = 0; col < getCrtSize().width; col++) {
        char ch = getChar(col, row);
        if (ch == ATTRIBUTE_PLACE_HOLDER) {
          ch = (char)getAttr(col, row);
          out.write((byte)ch);
        }
        else
          out.write(translator.toEBCDIC(ch));
      }
    }

    byte[] buf = out.toByteArray();

    send5250Packet((byte)0x00, OPCODE_READ_SCREEN, buf);
  }


  /**
   * Updates the status-bar shift area.
   */
  protected void updateStatusBar(InputEvent e) {
    ivStatusBar.setShiftArea((e.isShiftDown()) ?
                               XI5250StatusBar.SHIFT_DOWN :
                               XI5250StatusBar.SHIFT_UP);
  }


  /**
   * DO NOT USE, reserved for commands and orders
   * Removes all fields.
   */
  @Override
  public void removeFields() {
    super.removeFields();
    processEmulatorEvent(new XI5250EmulatorEvent(XI5250EmulatorEvent.FIELDS_REMOVED, this));
  }


  /**
   * Redefined to request 5250 error decoding.
   */
  @Override
  protected void userError(int aError) {
    Toolkit.getDefaultToolkit().beep();
    setState(ST_PRE_HELP);
    send5250UserError(aError);
  }


  /**
   */
  @Override
  protected void processMouseEvent(MouseEvent e) {
    updateStatusBar(e);
    super.processMouseEvent(e);
  }


  /**
   */
  @Override
  protected void processKeyEvent(KeyEvent e) {
    updateStatusBar(e);
    super.processKeyEvent(e);
  }


  /**
   * Avoid concurrent access with the synchronized part of receivedEOR() method.
   * When we get there the translation process has already been done.
   */
  @Override
  public synchronized void processRawKeyEvent(KeyEvent e) {
    // keyboard handling that doesn' t depends on 5250 state
    switch (e.getID()) {
      //
      case KeyEvent.KEY_PRESSED:
        switch (e.getKeyCode()) {
          //
          case KeyEvent.VK_ESCAPE:
            if (ivKeybEventQueue != null)
              ivKeybEventQueue.removeAll();

            // SysReq
            if (e.getModifiers() == KeyEvent.SHIFT_MASK)
              setState(ST_SYSTEM_REQUEST);
            break;
          //
          case KeyEvent.VK_CONTROL:      //!!1.01b
            if (e.getModifiers() == KeyEvent.CTRL_MASK && ivKeybEventQueue != null)
              ivKeybEventQueue.removeAll();
            break;
        }
        break;
    }

    if (ivKeybEventQueue == null)
      doProcessKeyEvent(e);
    else
      ivKeybEventQueue.postEvent(new KeyEvent(e.getComponent(), e.getID(),
                                              e.getWhen(), e.getModifiers(),
                                              e.getKeyCode(), e.getKeyChar()));

    //!!1.13a consume anything, except ALT+... key combinations
    if ((e.getModifiers() & KeyEvent.ALT_MASK) == 0)
      e.consume();
  }


  /**
   * Here is where the real keyboard handling is done.
   * If the keyboard queue is enabled we get there when it is safe to handle the key event.
   */
  @Override
  protected synchronized void doProcessKeyEvent(KeyEvent e) {
    // pre
    switch (e.getID()) {
      //
      case KeyEvent.KEY_PRESSED:
        if (getState() == ST_PRE_HELP && !isCharKey(e))
          setPrevState();
        break;
    }

    // keyboard handling that depends on 5250 state
    switch (getState()) {
      //
      case ST_TEMPORARY_LOCK:
      case ST_NORMAL_LOCKED:
        break;
      //
      case ST_SYSTEM_REQUEST:
        processKeySystemRequest(e);
        break;
      //
      case ST_NORMAL_UNLOCKED:
        processKeyNormalUnlocked(e);
        break;
    }
  }


  /**
   * Keyboard handling during the System request state.
   */
  protected boolean processKeySystemRequest(KeyEvent e) {
    super.doProcessKeyEvent(e);
    if (e.isConsumed())
      return true;

    boolean res = false;
    switch (e.getID()) {
      //
      case KeyEvent.KEY_TYPED:
        if (isCharKey(e)) {
          // if we get there then a key has been pressed outside of an input field
          setPrevState();
          userError(5);
          res = true;
        }
        break;
      //
      case KeyEvent.KEY_PRESSED:
        switch (e.getKeyCode()) {
          case KeyEvent.VK_ESCAPE:
            if (e.getModifiers() == 0)
              setPrevState();
            break;
          //
          case KeyEvent.VK_ENTER:
            String aStr = ivSysReqField.getString();
            setPrevState();
            int i;

            // exclude trailing null chars
            for (i = aStr.length() - 1; (i >= 0) && (aStr.charAt(i) == '\u0000'); i--)
              ;

            byte[] buf = getTranslator().toText(aStr, i + 1);

            if (LOGGER.isLoggable(Level.FINER)) {
              LOGGER.finer("---->Send5250SysReq");
              StringBuilder sb = new StringBuilder();
              for (i = 0; i < buf.length; i++)
                sb.append(XITelnet.toHex(buf[i]) + "  ");
              LOGGER.finer(sb.toString());
            }

            send5250Packet(FLAG_SRQ, OPCODE_NOP, buf);
            break;
        }
        break;
    }

    return res;
  }


  /**
   * Keyboard handling during the Normal unlocked state.
   */
  protected boolean processKeyNormalUnlocked(KeyEvent e) {
    super.doProcessKeyEvent(e);
    if (e.isConsumed())
      return true;

    boolean res = false;
    switch (e.getID()) {
      //
      case KeyEvent.KEY_TYPED:
        if (isCharKey(e)) {
          // if we get there then a key was pressed outside of an input field
          userError(5);
          res = true;
        }
        break;
      //
      case KeyEvent.KEY_PRESSED:
        switch (e.getKeyCode()) {
          //
          case KeyEvent.VK_ESCAPE:
            // Attn
            if (e.getModifiers() == 0)
              send5250Packet(FLAG_ATN, OPCODE_NOP, null);
            break;
          //
          case KeyEvent.VK_ENTER:
            res = processKeyEnter(e.getModifiers());
            break;
          //
          case KeyEvent.VK_F1:
          case KeyEvent.VK_F2:
          case KeyEvent.VK_F3:
          case KeyEvent.VK_F4:
          case KeyEvent.VK_F5:
          case KeyEvent.VK_F6:
          case KeyEvent.VK_F7:
          case KeyEvent.VK_F8:
          case KeyEvent.VK_F9:
          case KeyEvent.VK_F10:
          case KeyEvent.VK_F11:
          case KeyEvent.VK_F12:
            res = processKeyFXX(e.getKeyCode(), e.getModifiers());
            break;
          //
          case KeyEvent.VK_PAGE_UP:
          case KeyEvent.VK_PAGE_DOWN:
            res = processKeyPageXX(e.getKeyCode(), e.getModifiers());
            break;
          //
          case KeyEvent.VK_PAUSE:
            res = processKeyPause(e.getModifiers());
            break;
        }
        break;
    }

    return res;
  }


  private static final int AUTO_ENTER_MODIFIERS = (new KeyEvent(new JLabel(""), 
      KeyEvent.KEY_PRESSED, 0, -1, KeyEvent.VK_ENTER, (char)KeyEvent.VK_ENTER)).getModifiers();
  
  /**
   */
  protected boolean processKeyEnter(int aModifier) {
    // Cannot detect -1 modifier directly
    if (aModifier != 0 && aModifier != AUTO_ENTER_MODIFIERS)
      return false;

    if (ivPendingCmd != null)
      ivPendingCmd.executePending(
          (aModifier != AUTO_ENTER_MODIFIERS) ? AID_ENTER : AID_AUTO_ENTER, false);

    return true;
  }


  /**
   */
  protected boolean processKeyFXX(int aKey, int aModifier) {
    int ofs = aKey - KeyEvent.VK_F1;
    int aidCode;
    if (aModifier == 0)
      aidCode = AID_COMMAND + ofs;
    else if (aModifier == KeyEvent.SHIFT_MASK)
      aidCode = AID_F13 + ofs;
    else if (aModifier == KeyEvent.ALT_MASK && aKey == KeyEvent.VK_F1)
      aidCode  = AID_HELP;
    else
      return false;

    int mask = 1 << ofs;

    if (ivPendingCmd != null)
      ivPendingCmd.executePending(aidCode, ((ivFunctionKeysMask & mask) != 0));
    return true;
  }


  /**
   */
  protected boolean processKeyPageXX(int aKey, int aModifier) {
    if (aModifier != 0)
      return false;

    if (ivPendingCmd != null)
      ivPendingCmd.executePending((aKey == KeyEvent.VK_PAGE_UP) ? AID_ROLL_DN :
                                                                  AID_ROLL_UP,
                                  false);

    return true;
  }

  /**
   */
  protected boolean processKeyPause(int aModifier) {
    if (aModifier != 0)
      return false;

    if (ivPendingCmd != null)
      ivPendingCmd.executePending(AID_CLEAR, false);

    return true;
  }

  /**
   * Exit usefull for keyboard remapping.
   */
  @SuppressWarnings("deprecation")
  @Override
  protected KeyEvent translateKeyEvent(KeyEvent e) {
    if (!getAltFKeyRemap())
      return e;
    if (e.getKeyCode() >= KeyEvent.VK_F1 && e.getKeyCode () <= KeyEvent.VK_F12) {
      // ALT+SHIFT+Fx replaces ALT+Fx 
      if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0 && 
          (e.getModifiers() & KeyEvent.SHIFT_MASK) != 0) {
        e.setModifiers(e.getModifiers() & ~KeyEvent.SHIFT_MASK); 
      }
      // ALT+Fx replaces Fx
      else if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
        e.setModifiers(e.getModifiers() & ~KeyEvent.ALT_MASK); 
      }
    }
    return e;    
  }

  /**
   */
  @Override
  protected void finalize() throws Throwable {
    setActive(false);
    super.finalize();
  }

  
  void receivedStrPcCmd() {
    if (!isStrPcCmdEnabled())
      throw new IllegalStateException();
    ivReceivedStrPcCmd = true;
  }
  void receivedEndStrPcCmd() {
    if (!isStrPcCmdEnabled())
      throw new IllegalStateException();
    ivReceivedEndStrPcCmd = true;
  }

  /**
   * @param wait
   * @param cmd
   * @return the process return code if wait is true, otherwise 0
   * @throws IOException
   * @throws InterruptedException
   */
  protected int strPcCmd(boolean wait, String cmd) throws IOException, InterruptedException {
    Process proc = Runtime.getRuntime().exec(cmd);
    return wait ? proc.waitFor() : 0;
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Multicast Listener for XI5250Emulator
   * @version
   * @author   Valentino Proietti - Infordata S.p.A.
   */
  private static class Multicaster extends AWTEventMulticaster
                                   implements XI5250EmulatorListener {

    protected Multicaster(EventListener a, EventListener b) {
      super(a, b);
    }


    //!!1.03a
    @Override
    protected EventListener remove(EventListener oldl) {
      if (oldl == a)  return b;
      if (oldl == b)  return a;
      EventListener a2 = removeInternal(a, oldl);
      EventListener b2 = removeInternal(b, oldl);
      if (a2 == a && b2 == b)
        return this;
      return add((XI5250EmulatorListener)a2, (XI5250EmulatorListener)b2);
    }


    public static XI5250EmulatorListener add(XI5250EmulatorListener a,
                                             XI5250EmulatorListener b) {
      if (a == null)  return b;
      if (b == null)  return a;
      return new Multicaster(a, b);
    }


    public static XI5250EmulatorListener remove(XI5250EmulatorListener a,
                                                XI5250EmulatorListener b) {
      return (XI5250EmulatorListener)removeInternal(a, b);
    }


    /**
     */
    public void connecting(XI5250EmulatorEvent e) {
      ((XI5250EmulatorListener)a).connecting(e);
      ((XI5250EmulatorListener)b).connecting(e);
    }


    /**
     */
    public void connected(XI5250EmulatorEvent e) {
      ((XI5250EmulatorListener)a).connected(e);
      ((XI5250EmulatorListener)b).connected(e);
    }


    /**
     */
    public void disconnected(XI5250EmulatorEvent e) {
      ((XI5250EmulatorListener)a).disconnected(e);
      ((XI5250EmulatorListener)b).disconnected(e);
    }


    /**
     */
    public void stateChanged(XI5250EmulatorEvent e) {
      ((XI5250EmulatorListener)a).stateChanged(e);
      ((XI5250EmulatorListener)b).stateChanged(e);
    }


    /**
     */
    public void newPanelReceived(XI5250EmulatorEvent e) {
      ((XI5250EmulatorListener)a).newPanelReceived(e);
      ((XI5250EmulatorListener)b).newPanelReceived(e);
    }


    /**
     */
    public void fieldsRemoved(XI5250EmulatorEvent e) {
      ((XI5250EmulatorListener)a).fieldsRemoved(e);
      ((XI5250EmulatorListener)b).fieldsRemoved(e);
    }


    /**
     */
    public void dataSended(XI5250EmulatorEvent e) {
      ((XI5250EmulatorListener)a).dataSended(e);
      ((XI5250EmulatorListener)b).dataSended(e);
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @version
   * @author   Valentino Proietti - Infordata S.p.A.
   */
  protected static class KeyEventQueueItem {
    AWTEvent          ivEvent;
    int               ivId;
    KeyEventQueueItem ivNext;


    public KeyEventQueueItem(AWTEvent evt) {
      ivEvent = evt;
      ivId = evt.getID();
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Implements a circular Queue.
   * It is used when keyboard buffering is on.
   *
   * @version
   * @author   Valentino Proietti - Infordata S.p.A.
   */
  protected static class KeyEventQueue {

    private KeyEventQueueItem ivQueue;


    public KeyEventQueue() {
    }


    public synchronized void postEvent(AWTEvent theEvent) {
      KeyEventQueueItem eqi = new KeyEventQueueItem(theEvent);
      if (ivQueue == null) {
        ivQueue = eqi;
        ivQueue.ivNext = ivQueue;
        notifyAll();
      }
      else {
        eqi.ivNext = ivQueue.ivNext;
        ivQueue.ivNext = eqi;
        ivQueue = eqi;
      }
    }


    public synchronized AWTEvent getNextEvent() throws InterruptedException {
      while (ivQueue == null)
        wait();

      KeyEventQueueItem eqi = ivQueue.ivNext;
      if (eqi == ivQueue)
        ivQueue = null;
      else
        ivQueue.ivNext = eqi.ivNext;
      return eqi.ivEvent;
    }


    public synchronized void removeAll() {
      ivQueue = null;
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * When keyboard buffering is on all keys are posted in a KeyEventQueue.
   * This thread dispatches such key events to the doProcessKeyEvent method of XI5250Emulator.
   *
   * @version
   * @author   Valentino Proietti - Infordata S.p.A.
   */
  protected static class KeyEventDispatchThread extends Thread {

    protected XI5250Emulator ivEmulator;
    protected boolean        ivStop = false;


    /**
     */
    public KeyEventDispatchThread(XI5250Emulator aEmulator) {
      super("5250 Keyboard event dispatching thread");
      ivEmulator = aEmulator;
    }


    public void stopDispatching() {
      ivStop = true;
      if (this != Thread.currentThread()) {  //!!V 06/07/98
        interrupt();
      }
    }


    /**
     */
    @Override
    public void run() {
      while (!ivStop && ivEmulator.isKeyboardQueue()) {
        try {
          final KeyEvent e = (KeyEvent)ivEmulator.ivKeybEventQueue.getNextEvent();
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              synchronized (ivEmulator.getTreeLock()) {  // just to avoid dead-locks
                ivEmulator.doProcessKeyEvent(e);
              }
            }
          });
        }
        catch (InterruptedException ex) {
          continue;
        }
        catch (ThreadDeath ex) {
          throw ex;
        }
        catch (Throwable ex) {
          System.err.println(
              "Exception occurred during 5250 keyboard event dispatching:");
          ex.printStackTrace();
        }
      }
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  class TelnetEmulator implements XITelnetEmulator {

    public final void connecting() {
      XI5250Emulator.this.connecting();
    }

    public final void connected() {
      XI5250Emulator.this.connected();
    }

    public final void disconnected() {
      XI5250Emulator.this.disconnected();
    }

    public final void catchedIOException(IOException ex) {
      XI5250Emulator.this.catchedIOException(ex);
    }

    public final void receivedData(byte[] buf, int len) {
      XI5250Emulator.this.receivedData(buf, len);
    }

    public final void receivedEOR() {
      XI5250Emulator.this.receivedEOR();
    }

    public final void unhandledRequest(byte aIACOpt, String aIACStr) {
      XI5250Emulator.this.unhandledRequest(aIACOpt, aIACStr);
    }

    public final void localFlagsChanged(byte aIACOpt) {
      XI5250Emulator.this.localFlagsChanged(aIACOpt);
    }

    public final void remoteFlagsChanged(byte aIACOpt) {
      XI5250Emulator.this.remoteFlagsChanged(aIACOpt);
    }
  }
}
