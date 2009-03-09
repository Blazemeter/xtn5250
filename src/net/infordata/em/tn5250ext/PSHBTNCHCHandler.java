package net.infordata.em.tn5250ext;

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;

import net.infordata.em.crt.XICrt;
import net.infordata.em.crt5250.XI5250Field;

/**
 */
public class PSHBTNCHCHandler extends XI5250PanelHandler {
  
  private PSHBTNCHCHandler.FontsCache    ivFontsCache;
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

  @Override
  protected void stop() {
    ivButtons.clear();
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