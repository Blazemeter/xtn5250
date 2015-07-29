package net.infordata.em;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.crt5250.XIEbcdicTranslator;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250Frame;
import net.infordata.em.tn5250ext.PSHBTNCHCHandler;
import net.infordata.em.tn5250ext.XI5250EmulatorExt;
import net.infordata.em.tn5250ext.XI5250PanelHandler;
import net.infordata.em.tn5250ext.XI5250PanelsDispatcher;


/**
 * Command line startup utility. 
 * @author valentino.proietti
 */
public class Main {
  
  private Main() {}

  private static void usageError(String msg) {
    System.err.println(msg);
    System.err.println("Usage: [-3dFX] [-PSHBTNCHC] [-STRPCCMD] [-altFKeyRemap]" +
    		" [-maximized] [-cp codepage] [-devName name]" +
    		" [-autoLogon <fieldsCount>;<usrFieldLabel>;<pwdFieldLabel>;<user>;<passwd>]" +
    		" [-hideToolBar] [-hideMenuBar]" +
    		" host-name");
    System.err.println("Supported code pages:");
    for (String cp : XIEbcdicTranslator.getRegisteredTranslators().keySet()) {
      System.err.println("  " + cp + 
          (XI5250Emulator.DEFAULT_CODE_PAGE.equalsIgnoreCase(cp)? " default" : ""));
    }
    System.exit(1);
  }
  
  /**
   */
  public static void main(String[] args) {

    boolean pUse3dFX = false;
    boolean pAltFKeyRemap = false;
    boolean pPSHBTNCHC = false;
    boolean pSTRPCCMD = false;
    boolean pMaximized = false;
    boolean wkToolBar = true;
    boolean wkMenuBar = true;

    String arg;
    String pHost = null;
    boolean expectCP = false;
    boolean expectDevName = false;
    boolean expectLogonInfo = false;
    String cp = null;
    String devName = null;
    LogonInfo logonInfo = null;
    for (int i = 0; i < args.length; i++) {
      arg = args[i];
      if (arg.startsWith("-")) {
        if ("-3dfx".equalsIgnoreCase(arg))
          pUse3dFX = true;
        else if ("-PSHBTNCHC".equalsIgnoreCase(arg))
          pPSHBTNCHC = true;
        else if ("-STRPCCMD".equalsIgnoreCase(arg))
          pSTRPCCMD = true;
        else if ("-maximized".equalsIgnoreCase(arg))
          pMaximized = true;
        else if ("-altFKeyRemap".equalsIgnoreCase(arg))
          pAltFKeyRemap = true;
        else if ("-cp".equalsIgnoreCase(arg))
          expectCP = true;
        else if ("-devName".equalsIgnoreCase(arg))
          expectDevName = true;
        else if ("-autoLogon".equalsIgnoreCase(arg))
          expectLogonInfo = true;
        else if ("-hideToolBar".equalsIgnoreCase(arg))
          wkToolBar = false;
        else if ("-hideMenuBar".equalsIgnoreCase(arg))
          wkMenuBar = false;
        else
          usageError("Wrong option: " + arg);
      }
      else if (expectCP) {
        expectCP = false;
        if (XIEbcdicTranslator.getTranslator(arg) == null)
          usageError("Unknown codepage: " + arg);
        cp = arg;
      }
      else if (expectDevName) {
        expectDevName = false;
        devName = arg;
      }
      else if (expectLogonInfo) {
        expectLogonInfo = false;
        try {
          logonInfo = new LogonInfo(arg);
        }
        catch (IllegalArgumentException ex) {
          usageError(ex.getMessage());
        }
      }
      else {
        if (pHost == null)
          pHost = arg;
        else
          usageError("Too many host names.");
      }
    }
    final boolean dspToolBar = wkToolBar;
    final boolean dspMenuBar = wkMenuBar;
    if (expectCP)
      usageError("A code page is expected");
    
    final boolean altFKeyRemap = pAltFKeyRemap;
    final boolean use3dFX = pUse3dFX;
    final boolean enablePSHBTNCHC = pPSHBTNCHC;
    final boolean enableSTRPCCMD = pSTRPCCMD;
    final boolean maximized = pMaximized;
    final String host = pHost;
    final String codePage = cp;
    final String deviceName = devName;
    final LogonInfo autoLogonInfo = logonInfo;
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          XI5250Emulator em;
          if (enablePSHBTNCHC) {
            XI5250EmulatorExt emext = new XI5250EmulatorExt();
            PanelsDispatcher disp = new PanelsDispatcher();
            disp.setEmulator(emext);
            new PSHBTNCHCHandler(disp);
            if (autoLogonInfo != null)
              new AutoLogonHandler(disp, autoLogonInfo);
            em = emext;
          }
          else if (autoLogonInfo != null) {
            XI5250EmulatorExt emext = new XI5250EmulatorExt();
            PanelsDispatcher disp = new PanelsDispatcher();
            disp.setEmulator(emext);
            new AutoLogonHandler(disp, autoLogonInfo);
            em = emext;
          }
          else {
            em = new XI5250Emulator();
          }
          em.setTerminalType("IBM-3477-FC");
          em.setKeyboardQueue(true);

          em.setStrPcCmdEnabled(enableSTRPCCMD);
          em.setAltFKeyRemap(altFKeyRemap);
          em.setCodePage(codePage);
          
          if (deviceName != null)
            em.setTelnetEnv("\u0003DEVNAME\u0001" + deviceName);
          
          if (host != null) {
            em.setHost(host);
            em.setActive(true);
          }

          XI5250Frame frm = new XI5250Frame("tn5250" + " " +
                                            XI5250Emulator.VERSION, em, dspToolBar, dspMenuBar);
          frm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
              System.exit(0);
            }
          });

          //3D FX
          if (use3dFX) {
            em.setDefFieldsBorderStyle(XI5250Field.LOWERED_BORDER);
            em.setDefBackground(UIManager.getColor("control"));
          }

          //frm.setBounds(0, 0, 570, 510);
          frm.centerOnScreen(70);
          if (maximized) {
            frm.doNotPackOnStartup();
            frm.setExtendedState(Frame.MAXIMIZED_BOTH);
          }
          frm.setVisible(true);
        }
      });
    }
    catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    catch (InvocationTargetException ex) {
      ex.printStackTrace();
    }
  }
  
  //////
  
  private static class PanelsDispatcher extends XI5250PanelsDispatcher {

    private AutoLogonHandler   ivAutoLogonHandler;
    private XI5250PanelHandler ivHandler;
    
    @Override
    public synchronized void addPanelHandler(XI5250PanelHandler panel) {
      if (panel instanceof AutoLogonHandler) {
        if (ivAutoLogonHandler != null)
          throw new IllegalArgumentException("Handler already setted");
        ivAutoLogonHandler = (AutoLogonHandler)panel;
        return;
      }
      if (ivHandler != null)
        throw new IllegalArgumentException("Handler already setted");
      ivHandler = panel;
    }

    @Override
    protected synchronized XI5250PanelHandler getCurrentPanelHandler() {
      return (ivAutoLogonHandler != null && ivAutoLogonHandler.detailedTest()) ? ivAutoLogonHandler : ivHandler;
    }

    @Override
    public synchronized void removePanelHandler(XI5250PanelHandler panel) {
      if (ivHandler != panel)
        throw new IllegalArgumentException("Not the registered handler " + panel);
      ivHandler = null;
    }
  }
  
  //////
  
  private static class LogonInfo {
    
    final int fieldsCount;
    final String userLabel;
    final String passwdLabel;
    final String user;
    final String passwd;
    
    LogonInfo(String info) {
      String[] ss = info.split(";", 5);
      if (ss.length < 5)
        throw new IllegalArgumentException("Invalid autoLogon argument");
      try {
        fieldsCount = Integer.parseInt(ss[0]);
      }
      catch (NumberFormatException ex) {
        throw new IllegalArgumentException("Invalid autoLogon argument: " + ex.getMessage());
      }
      userLabel = ss[1];
      passwdLabel = ss[2];
      user = ss[3];
      passwd = ss[4];
    }
  }
  
  //////
  
  private static class AutoLogonHandler extends XI5250PanelHandler {
    
    private final LogonInfo ivLogonInfo;
    private boolean ivLoggedOn;

    public AutoLogonHandler(XI5250PanelsDispatcher aPanelDisp, LogonInfo info) {
      super(aPanelDisp);
      ivLogonInfo = info;
    }

    @Override
    protected boolean detailedTest() {
      if (ivLoggedOn) 
        return false;
      // I'm expecting xx fields in the logon panel
      if (getFields().size() != ivLogonInfo.fieldsCount)
        return false;

      // Is there the user id field ?
      if (!checkField(getFieldNextTo(ivLogonInfo.userLabel), 10))
        return false;
      // Is there the password field ?
      if (!checkField(getFieldNextTo(ivLogonInfo.passwdLabel), 10))
        return false;
      return true;
    }

    @Override
    protected void start() {
      ivLoggedOn = true;
      // Start logon panel processing
      XI5250Field userField = getFieldNextTo(ivLogonInfo.userLabel);
      XI5250Field passwdField = getFieldNextTo(ivLogonInfo.passwdLabel);
      userField.setString(ivLogonInfo.user);    // Your user id
      passwdField.setString(ivLogonInfo.passwd);  // Your password
      // Simulate the user ENTER key pressed
      getEmulator().processRawKeyEvent(
          new KeyEvent(getEmulator(), KeyEvent.KEY_PRESSED,
              0, 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
    }

    @Override
    protected void stop() {
    }
  }
}
