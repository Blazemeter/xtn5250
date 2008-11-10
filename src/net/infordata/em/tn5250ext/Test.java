package net.infordata.em.tn5250ext;

import java.awt.SystemColor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
    XI5250PanelsDispatcher disp = new XI5250PanelsDispatcher(em);
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

    public TestHandler(XI5250PanelsDispatcher disp) {
      super(disp, "");
    }

    protected boolean detailedTest() {
      return true;
    }

    protected void start() {
      for (Iterator<XI5250Field> e = getFields().iterator(); e.hasNext(); ) {
        XI5250Field field = e.next();
        setFieldHint(field, new XIHint(field.toString()));

        JPopupMenu pm = new JPopupMenu();
        pm.add(new JMenuItem(field.toString()));
        setFieldPopupMenu(field, pm);

        JButton btn = new JButton();
        new XI5250FieldConnection(this, field, btn);

        new XI5250PanelConnection(this,
                                  new JButton("+-"),
                                  15, 15, 10, 10);
      }
    }

    protected void stop() {
    }
  }
}
