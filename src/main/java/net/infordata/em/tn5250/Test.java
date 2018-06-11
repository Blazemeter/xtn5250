package net.infordata.em.tn5250;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.crt5250.XIEbcdicTranslator;


public class Test {

  private Test() { }
  
  private static void usageError(String msg) {
    System.err.println(msg);
    System.err.println("Usage: [-3dFX] [-altFKeyRemap] [-cp codepage] host-name");
    System.err.println("Supported code pages:");
    for (String cp : XIEbcdicTranslator.getRegisteredTranslators().keySet()) {
      System.err.println("  " + cp + 
          (XI5250Emulator.DEFAULT_CODE_PAGE.equalsIgnoreCase(cp)? " default" : ""));
    }
    System.exit(1);
  }
  
  /**
   * Used only for test purposes.
   */
  public static void main(String[] args) {

    /*!!1.12
    if (System.getProperty("java.version").compareTo("1.1.1") < 0 ||
        System.getProperty("java.version").compareTo("1.1_Final") == 0) {
      System.err.println("!!! Use JDK 1.1.1 or newer !!!");
    }
    */
//    checkJDK();

    boolean pUse3dFX = false;
    boolean pAltFKeyRemap = false;

    String arg;
    String pHost = null;
    boolean expectCP = false;
    String cp = null;
    for (int i = 0; i < args.length; i++) {
      arg = args[i];
      if (arg.startsWith("-")) {
        if ("-3dfx".equalsIgnoreCase(arg))
          pUse3dFX = true;
        else if ("-altFKeyRemap".equalsIgnoreCase(arg))
          pAltFKeyRemap = true;
        else if ("-cp".equalsIgnoreCase(arg))
          expectCP = true;
        else
          usageError("Wrong option: " + arg);
      }
      else if (expectCP) {
        expectCP = false;
        if (XIEbcdicTranslator.getTranslator(arg) == null)
          usageError("Unknown codepage: " + arg);
        cp = arg;
      }
      else {
        if (pHost == null)
          pHost = arg;
        else
          usageError("Too many host names.");
      }
    }
    if (expectCP)
      usageError("A code page is expected");
    
    final boolean altFKeyRemap = pAltFKeyRemap;
    final boolean use3dFX = pUse3dFX;
    final String host = pHost;
    final String codePage = cp;
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          XI5250Emulator em  = new XI5250Emulator();
          em.setTerminalType("IBM-3477-FC");
          em.setKeyboardQueue(true);

          em.setAltFKeyRemap(altFKeyRemap);
          em.setCodePage(codePage);
          
          if (host != null) {
            em.setHost(host);
            em.setActive(true);
          }

          XI5250Frame frm = new XI5250Frame("tn5250" + " " +
                                            XI5250Emulator.VERSION, em);
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

}
