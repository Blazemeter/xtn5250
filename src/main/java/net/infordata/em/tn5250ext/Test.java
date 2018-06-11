package net.infordata.em.tn5250ext;

import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.infordata.em.crt.XICrt;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250Frame;


public class Test {

  private Test() { }

  /**
   * @param args
   */
  public static void main(String[] argv) {
    XI5250EmulatorExt em  = new XI5250EmulatorExt();
    em.setTerminalType("IBM-3477-FC");
    em.setKeyboardQueue(true);

    em.setHintOnActiveField(true);
    XI5250PanelsDispatcher disp = new XI5250SimplePanelsDispatcher(em);
    new TestHandler(disp);

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

    frm.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        System.exit(0);
      }
    });
    frm.setBounds(0, 0, 570, 510);
    frm.centerOnScreen();
    frm.setVisible(true);
  }
  
  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  private static class TestHandler extends XI5250PanelHandler {
    
    private FontsCache    ivFontsCache;
    private List<JButton> ivButtons = new ArrayList<JButton>();

    public TestHandler(XI5250PanelsDispatcher disp) {
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

    @Override
    protected boolean detailedTest() {
      return true;
    }
    
    @Override
    protected void start() {
      ivButtons.clear();
      final XI5250EmulatorExt em = getEmulator(); 
      final int crtWidth = em.getCrtSize().width;
      for (Iterator<XI5250Field> e = getFields().iterator(); e.hasNext(); ) {
        final XI5250Field field = e.next();
        boolean isButton = false;
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
              isButton = true;
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
        
        if (!isButton) {
          setFieldHint(field, new XIHint(field.toString()));

          JPopupMenu pm = new JPopupMenu();
          pm.add(new JMenuItem(field.toString()));
          setFieldPopupMenu(field, pm);

          JButton btn = new JButton();
          new XI5250FieldConnection(this, field, btn);
        }

        new XI5250PanelConnection(this,
                                  new JButton("+-"),
                                  15, 15, 10, 6);
      }
    }

    @Override
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
