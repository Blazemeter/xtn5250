package net.infordata.em;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.infordata.em.crt.XICrt;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.crt5250.XIEbcdicTranslator;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250Frame;
import net.infordata.em.tn5250ext.XI5250EmulatorExt;
import net.infordata.em.tn5250ext.XI5250PanelConnection;
import net.infordata.em.tn5250ext.XI5250PanelHandler;
import net.infordata.em.tn5250ext.XI5250PanelsDispatcher;


/**
 * Command line startup. 
 * @author valentino.proietti
 */
public class Main {
  
  private Main() {}

  private static void usageError(String msg) {
    System.err.println(msg);
    System.err.println("Usage: [-3dFX] [-PSHBTNCHC] [-altFKeyRemap] [-cp codepage] host-name");
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

    String arg;
    String pHost = null;
    boolean expectCP = false;
    String cp = null;
    for (int i = 0; i < args.length; i++) {
      arg = args[i];
      if (arg.startsWith("-")) {
        if ("-3dfx".equalsIgnoreCase(arg))
          pUse3dFX = true;
        else if ("-PSHBTNCHC".equalsIgnoreCase(arg))
          pPSHBTNCHC = true;
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
    final boolean enablePSHBTNCHC = pPSHBTNCHC;
    final String host = pHost;
    final String codePage = cp;
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          XI5250Emulator em;
          if (enablePSHBTNCHC) {
            XI5250EmulatorExt emext = new XI5250EmulatorExt();
            PanelsDispatcher disp = new PanelsDispatcher();
            disp.setEmulator(emext);
            new PSHBTNCHCHandler(disp);
            em = emext;
          }
          else {
            em = new XI5250Emulator();
          }
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
  
  //////
  
  private static class PanelsDispatcher extends XI5250PanelsDispatcher {

    private XI5250PanelHandler ivHandler;
    
    @Override
    public synchronized void addPanelHandler(XI5250PanelHandler panel) {
      if (ivHandler != null)
        throw new IllegalArgumentException("Handler already setted");
      ivHandler = panel;
    }

    @Override
    protected synchronized XI5250PanelHandler getCurrentPanelHandler() {
      return ivHandler;
    }

    @Override
    public synchronized void removePanelHandler(XI5250PanelHandler panel) {
      if (ivHandler != panel)
        throw new IllegalArgumentException("Not the registered handler " + panel);
      ivHandler = null;
    }
  }
  
  ///////

  /**
   */
  private static class PSHBTNCHCHandler extends XI5250PanelHandler {
    
    private FontsCache    ivFontsCache;
    private List<JButton> ivButtons = new ArrayList<JButton>();

    public PSHBTNCHCHandler(XI5250PanelsDispatcher disp) {
      super(disp, "");
    }

    @Override
    protected void sizeChanged() {
      super.sizeChanged();
      final XI5250EmulatorExt em = getEmulator(); 
      for (JButton btn : ivButtons) {
        btn.setFont(ivFontsCache.getFont(
            Math.max(1, em.getFont().getSize() - 2)));
      }
    }

    protected boolean detailedTest() {
      return true;
    }
    
    protected void start() {
      ivButtons.clear();
      final XI5250EmulatorExt em = getEmulator(); 
      final int crtWidth = em.getCrtSize().width;
      for (Iterator<XI5250Field> e = getFields().iterator(); e.hasNext(); ) {
        final XI5250Field field = e.next();
        if (field.isIOOnly()) {
          final int col = field.getCol() - 1;
          final int row = field.getRow();
          if (col >= 0 && 
              "<".equals(em.getString(col, row, 1))) {
            final int len;
            {
              final int fieldEnd = col + field.getLength();
              String str = em.getString(fieldEnd + 1, row, crtWidth - fieldEnd);
              int idx = str.indexOf(">");
              len = (idx < 0) ? -1 : idx + 2 + 1;
            }
            if (len > 0) {
              final String txt = em.getString(col + 1, row, len - 2);
              JButton btn = new JButton(txt);
              if (ivFontsCache == null) 
                ivFontsCache = new FontsCache(btn.getFont());
              btn.setFont(ivFontsCache.getFont(Math.max(1, em.getFont().getSize() - 2)));
              btn.setMargin(new Insets(2, 2, 2, 2));
              btn.setFocusable(false);
              btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                  em.setCursorPos(field.getCol(), field.getRow());
                  em.requestFocusInWindow();
                  em.processRawKeyEvent(new KeyEvent(em, KeyEvent.KEY_PRESSED, 0,
                      0, KeyEvent.VK_ENTER, (char)KeyEvent.VK_ENTER));
                }
              });
              ivButtons.add(btn);
              new XI5250PanelConnection(this,
                  btn, col, row, len, 1);
            }
          }
        }
      }
    }

    protected void stop() {
      ivButtons.clear();
    }
  }

  ///////
  
  private static class FontsCache {

    private Font[] ivFonts = new Font[XICrt.MAX_FONT_SIZE - XICrt.MIN_FONT_SIZE + 1];
    private Font   ivFont;


    public FontsCache(Font font) {
      ivFont = font;
    }


    public Font getFont(int size) {
      if (ivFonts[size - XICrt.MIN_FONT_SIZE] == null) {
        ivFonts[size - XICrt.MIN_FONT_SIZE] = 
          new Font(ivFont.getName(),
                   ivFont.getStyle(),
                   size);
      }
      return ivFonts[size - XICrt.MIN_FONT_SIZE];
    }
  }
}
