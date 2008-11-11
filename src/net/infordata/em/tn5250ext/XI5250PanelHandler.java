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
!!V 14/05/97 rel. 0.96d- uses VALIDATE_PENDING.
    30/05/97 rel. 1.00 - .
    24/09/97 rel. 1.05 - DNCX project (PopupMenu).
    06/10/97 rel. 1.05b- Hint window is forced to stay into the screen
             boundaries.
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.tn5250ext;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JPopupMenu;

import net.infordata.em.crt5250.XI5250CrtAdapter;
import net.infordata.em.crt5250.XI5250CrtEvent;
import net.infordata.em.crt5250.XI5250Field;


/**
 * Abstract base class for all classes created to control 5250 panels.
 *
 * <pre>
 * example:
 *
 *  //
 *  class XARegistrazioniIVA extends XI5250PanelHandler {
 *    XI5250Field ivCtv;
 *    XI5250Field ivCambio;
 *    XI5250Field ivDivisa;
 *
 *    //
 *    class FieldAdapter extends XI5250FieldAdapter {
 *      public void valueChanged(XI5250FieldEvent e) {
 *        // Has the divisa field value changed ??
 *        if (e.getField() == ivDivisa)
 *          handleDivisa();
 *      }
 *    }
 *
 *    //
 *    public XARegistrazioniIVA(XI5250PanelsDispatcher aPanelsDisp,
 *                              String aKey) {
 *      super(aPanelsDisp, aKey);
 *    }
 *
 *    //
 *    public boolean detailedTest() {
 *      return checkField(getFieldNextTo("Causale"), 3) &&
 *             checkField(getFieldNextTo("Cliente"), 6);
 *    }
 *
 *    //
 *    public void start() {
 *      // Dt reg
 *      XI5250Field fld = getFieldNextTo("Dt reg");
 *      if (checkField(fld, 8)) {
 *        XIButton btn = new XICalendarButton(fld, "dd/MM/yy");
 *        new XI5250FieldConnection(this, fld, btn);
 *
 *        setFieldHint(fld, new XIHint("The posting date"));
 *      }
 *
 *      // cliente
 *      fld = getFieldNextTo("Cliente");
 *      if (checkField(fld, 6)) {
 *        XIButton btn = new XIButton();
 *        new XI5250FieldConnection(this, fld, btn);
 *      }
 *
 *      // Data doc.
 *      fld = getFieldNextTo("Data doc.");
 *      if (checkField(fld, 6)) {
 *        XIButton btn = new XICalendarButton(fld, "ddMMyy");
 *        new XI5250FieldConnection(this, fld, btn);
 *      }
 *
 *      // Divisa
 *      fld = getFieldNextTo("Divisa");
 *      if (checkField(fld, 4)) {
 *        ivDivisa = fld;
 *        ivDivisa.addFieldListener(new FieldAdapter());
 *      }
 *
 *      // Cambio
 *      fld = getFieldNextTo("Cambio");
 *      if (checkField(fld, 8)) {
 *        ivCambio = fld;
 *      }
 *
 *      // Ctv
 *      fld = getFieldNextTo("Ctv");
 *      if (checkField(fld, 11)) {
 *        ivCtv = fld;
 *      }
 *
 *      //
 *      if (ivDivisa != null)
 *        handleDivisa();
 *    }
 *
 *    //
 *    public void stop() {
 *      ivCtv = null;
 *      ivCambio = null;
 *      ivDivisa = null;
 *    }
 *
 *    //
 *    protected void handleDivisa() {
 *      String  str = ivDivisa.getTrimmedString();
 *      boolean enable = !(str.equals("") || str.equals("*"));
 *
 *      if (ivCambio != null)
 *        ivCambio.setEnabled(enable);
 *
 *      if (ivCtv != null)
 *        ivCtv.setEnabled(enable);
 *    }
 *  }
 *
 * </pre>
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public abstract class XI5250PanelHandler {

  private String                 ivTitle;

  transient private XI5250PanelsDispatcher ivDispatcher;

  // Hash table used to mantain relations between XI5250Fields and
  // XI5250FieldConnections
  transient private HashMap<XI5250Field, XI5250FieldConnection> ivConnections;

  // Hash table used to mantain relations between Components and
  // XI5250PanelConnections
  transient private HashMap<Component, XI5250PanelConnection>   ivPanelConnections;

  transient private int                    ivInvalidateCount;

  transient private CrtAdapter             ivCrtAdapter;

  //!!1.00
  transient private HashMap<XI5250Field, XIHint> ivHints;
  transient private XIHintWindow       ivHintWindow;
  transient private javax.swing.Timer  ivHintTimer;
  transient private int                ivHintDelay = 1000;

  //!!1.04d
  transient private XIHint             ivLastHint;
  transient private HintThread         ivHintThread;

  //!!V-23/09/97
  transient private HashMap<XI5250Field, JPopupMenu> ivPopupMenus;
  transient private ArrayList<JPopupMenu>            ivPopupList;
  transient private MouseListener      ivMouseListener;


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  transient private ComponentListener  ivHintListener = new ComponentAdapter() {
    @Override
    public void componentHidden(ComponentEvent aEvent) {
      // ivHintWindow potrebbe essere null
      ((XIHintWindow)aEvent.getSource()).
          removeComponentListener(ivHintListener);
      XI5250PanelHandler.this.hideHint();
    }
  };


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  class EmulatorMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      XI5250PanelHandler.this.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      XI5250PanelHandler.this.mouseReleased(e);
    }
  };


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Registers itself in the provided XI5250PanelsDispatcher.
   * @param aPanelTitle    is the string that is used by XI5250PanelsDispatcher
   *                       in the first step of the panel handler search.
   *
   * @see    XI5250PanelsDispatcher#addPanelHandler
   * @see    XI5250PanelsDispatcher#getCurrentPanelHandler
   */
  public XI5250PanelHandler(XI5250PanelsDispatcher aPanelDisp,
                            String aPanelTitle) {
    ivDispatcher = aPanelDisp;
    ivTitle = aPanelTitle;

    ivDispatcher.addPanelHandler(this);
  }


  /**
   * Returns the related XI5250EmulatorExt.
   */
  public final XI5250EmulatorExt getEmulator() {
    return ivDispatcher.getEmulator();
  }


  /**
   * Returns a Map that can be used to store data shared by different
   * XI5250Panel instances.
   * @see    XI5250PanelsDispatcher#getSharedData
   */
  public Map<Object, Object> getSharedData() {
    return ivDispatcher.getSharedData();
  }


  /**
   */
  public String getTitle() {
    return ivTitle;
  }


  /**
   * Returns the Window containing the given component.
   */
  public static Window getWindow(Component aComp) {
    Component comp = aComp;
    while (comp != null && !(comp instanceof Window))
      comp = comp.getParent();
    return (Window)comp;
  }


  /**
   * Check if this is really the panel we are waiting for.
   * Normally it' s enough to check the presence and length of a couple of
   * fields.
   * <pre>
   *    //
   *    public boolean detailedTest() {
   *      return checkField(getFieldNextTo("Causale"), 3) &&
   *             checkField(getFieldNextTo("Cliente"), 6);
   *    }
   * </pre>
   */
  protected abstract boolean detailedTest();


  /**
   * Begin handling the 5250 panel.
   */
  protected abstract void start();


  /**
   * Stop handling the 5250 panel.
   * All references to XI5250Fields must be initialized to null.
   */
  protected abstract void stop();


  /**
   * Called just after that the 5250 panel contents has been sended to host.
   * @param anAidCode    the aid code (ie XI5250Emulator.AID_F3, ...)
   */
  protected void dataSended(byte anAidCode) {
  }


  /**
   */
  final void startInternal() {
    XI5250EmulatorExt em = getEmulator();

    ivCrtAdapter = new CrtAdapter();
    em.addCrtListener(ivCrtAdapter);

    em.addMouseListener(ivMouseListener = new EmulatorMouseListener());

    start();

    validate();
  }


  /**
   * calls stop() and then removes all added components and XI5250Connections
   */
  final void stopInternal() {
    try {
      stop();
    }
    finally {
      //!!1.04d
      if (ivHintThread != null) {
        ivHintThread.interrupt();
        ivHintThread = null;
      }

      hideHint();
      ivHints = null;

      XI5250EmulatorExt em = getEmulator();

      em.removeMouseListener(ivMouseListener);

      em.removeCrtListener(ivCrtAdapter);

      // remove all XI5250PanelConnections
      if (ivPanelConnections != null) {
        XI5250PanelConnection c;
        for (Iterator<XI5250PanelConnection> e = ivPanelConnections.values().iterator();
             e.hasNext(); ) {
          c = e.next();
          em.remove(c.getComponent());
        }
        ivPanelConnections = null;
      }

      // remove all XI5250FieldConnections
      if (ivConnections != null) {
        XI5250FieldConnection c;
        for (Iterator<XI5250FieldConnection> e = ivConnections.values().iterator(); 
             e.hasNext(); ) {
          c = e.next();
          em.remove(c.getComponent());
        }
        ivConnections = null;
      }

      // remove all PopupMenu
      if (ivPopupList != null) {
        JPopupMenu c;
        for (Iterator<JPopupMenu> e = ivPopupList.iterator(); e.hasNext(); ) {
          c = e.next();
          em.remove(c);
        }
        ivPopupMenus = null;
        ivPopupList = null;
      }
    }
  }


  /**
   * It is used by XI5250FieldConnection to register itself to be called when a
   * layout validate is required
   */
  void connect(XI5250Field aField, XI5250FieldConnection aConnection) {
    XI5250EmulatorExt em = getEmulator();

    if (ivConnections == null)
      ivConnections = new HashMap<XI5250Field, XI5250FieldConnection>();

    XI5250FieldConnection c =
        ivConnections.put(aField, aConnection);

    if (c != null)
      em.remove(c.getComponent());

    em.add(aConnection.getComponent());

    invalidate();
  }


  /**
   * It is used by XI5250PanelConnection to register itself to be called when a
   * layout validate is required
   */
  void connect(XI5250PanelConnection aConnection) {
    XI5250EmulatorExt em = getEmulator();

    if (ivPanelConnections == null)
      ivPanelConnections = new HashMap<Component, XI5250PanelConnection>();

    XI5250PanelConnection c = (XI5250PanelConnection)
                              ivPanelConnections.put(aConnection.getComponent(),
                                                     aConnection);

    if (c == null)
      em.add(aConnection.getComponent());

    invalidate();
  }


  /**
   * @exception    Throwable .
   */
  @Override
  public void finalize() throws Throwable {
    stopInternal();
    super.finalize();
  }


  /**
   * @see    #validate
   */
  public void invalidate() {
    ++ivInvalidateCount;
  }


  /**
   * Implements a mechanism like the one present in AWT Container classes, but
   * instead of using a LayoutManager it uses XI5250PanelConnection and
   * XI5250FieldConnection to place components on the panel.
   */
  public void validate() {
    if (ivInvalidateCount <= 0)
      return;

    ivInvalidateCount = 0;

    XI5250EmulatorExt em = getEmulator();

    synchronized (em.getTreeLock()) {
      // validate all panel-connections
      if (ivPanelConnections != null) {
        XI5250PanelConnection conn;
        for (Iterator<XI5250PanelConnection> en = ivPanelConnections.values().iterator();
             en.hasNext(); ) {
          conn = en.next();
          conn.recalcBounds(em);
        }
      }

      // validate all field-connections
      if (ivConnections != null) {
        XI5250Field           fld;
        XI5250FieldConnection conn;
        for (Iterator<XI5250Field> en = ivConnections.keySet().iterator(); en.hasNext(); ) {
          fld = en.next();
          conn = ivConnections.get(fld);
          conn.recalcBounds(em, fld);
        }
      }
    }
  }


  /**
   * Called when the input cursor enters a field area.
   */
  protected void fieldActivated(XI5250Field aField) {
    /*!!1.04d
    if (getEmulator().isHintOnActiveField())
      setCurrentHint(aField);
    */
  }


  /**
   * Called when the cursor exits from field area.
   */
  protected void fieldDeactivated(XI5250Field aField) {
    /*!!1.04d
    if (getEmulator().isHintOnActiveField())
      setCurrentHint(null);
    */
  }


  /**
   * Called when the font size of the emulator changes.
   * Forces a validate.
   */
  protected void sizeChanged() {
    invalidate();
    validate();

    hideHint();
  }


  /**
   * Called just before processing a key event.
   */
  protected void keyEvent(KeyEvent e) {
  }


  /**
   * Called to activate an hint over a field.
   */
  /*!!1.04d
  protected void setCurrentHint(XI5250Field aField)
  {
    XIHint hint = (aField == null || ivHints == null) ? null :
                                                        (XIHint)ivHints.get(aField);

    if (hint != null)
      showHint(hint, aField);
    else
      hideHint();
  }
  */


  /**
   */
  protected void refreshHint() {
    if (!getEmulator().hasFocus())
      hideHint();

    // campo attuale
    XI5250Field field = ((!getEmulator().getShowHints()) ?
                           null :
                           getEmulator().isHintOnActiveField() ?
                             getEmulator().getCurrentField() :
                             getEmulator().getFieldUnderMouse());

    // hint attuale
    XIHint hint = (field == null || ivHints == null) ?
                    null :
                    (XIHint)ivHints.get(field);

    if (hint == ivLastHint)
      return;

    if (hint != null)
      showHint(hint, field);
    else
      hideHint();

    ivLastHint = hint;
  }


  /**
   */
  private void showHint(XIHint aHint, XI5250Field aField) {
    synchronized (ivDispatcher.getTreeLock()) {
      hideHint();

      if (!getEmulator().getShowHints())
        return;

      ivHintWindow = new XIHintWindow(aHint, getEmulator());
    }
    int delay = ivHintDelay;
    if (getEmulator().isHintOnActiveField())
      delay /= 2;

    ivHintTimer = new javax.swing.Timer(delay, new HintTimer(aField));
    ivHintTimer.setRepeats(false);
    ivHintTimer.start();
  }


  /**
   */
  private void hideHint() {
    synchronized (ivDispatcher.getTreeLock()) {
      ivLastHint = null;  //!!1.04d

      if (ivHintTimer != null) {
        ivHintTimer.stop();
        ivHintTimer = null;
      }

      if (ivHintWindow != null) {
        ivHintWindow.setVisible(false);
        ivHintWindow = null;
      }
    }
  }


  /**
   * Called when the mouse cursor enters the field area.
   */
  protected void mouseEntersField(XI5250Field aField) {
    /*!!1.04d
    if (!getEmulator().isHintOnActiveField())
      setCurrentHint(aField);
    */
  }


  /**
   * Called when the mouse cursor exits from field area.
   */
  protected void mouseExitsField(XI5250Field aField) {
    /*!!1.04d
    if (!getEmulator().isHintOnActiveField())
      setCurrentHint(null);
    */
  }


  /**
   * Sets the field relative XIHint (null to remove).
   * <pre>
   * ex:
   *   setFieldHint(getFieldNextTo("Cust."),
   *                new XIHint("The new customer"));
   * </pre>
   */
  public void setFieldHint(XI5250Field aField, XIHint aHint) {
    if (aField == null)
      return;

    if (ivHints == null) {
      ivHints = new HashMap<XI5250Field, XIHint>();
    }

    if (aHint == null)
      ivHints.remove(aField);
    else
      ivHints.put(aField, aHint);

    if (!ivHints.isEmpty()) {
      if (ivHintThread == null) {
        //!!1.04d
        ivHintThread = new HintThread();
        ivHintThread.setPriority(Thread.NORM_PRIORITY - 1);
        ivHintThread.start();
      }
    }
    else {
      if (ivHintThread != null) {
        ivHintThread.interrupt();
        ivHintThread = null;
      }
    }
  }


  /**
   * Sets the field relative JPopupMenu (null to remove).
   */
  public void setFieldPopupMenu(XI5250Field aField, JPopupMenu aPopupMenu) {
    if (aField == null)
      return;

    if (ivPopupMenus == null)
      ivPopupMenus = new HashMap<XI5250Field, JPopupMenu>();

    if (ivPopupList == null)
      ivPopupList = new ArrayList<JPopupMenu>(10);

    if (aPopupMenu == null)
      ivPopupMenus.remove(aField);
    else
      ivPopupMenus.put(aField, aPopupMenu);

    if (!ivPopupList.contains(aPopupMenu)) {
      ivPopupList.add(aPopupMenu);
      getEmulator().add(aPopupMenu);
    }
  }


  /**
   */
  protected void mousePressed(MouseEvent e) {
  }


  /**
   */
  protected void mouseReleased(MouseEvent e) {
    XI5250Field fld = getEmulator().getFieldUnderMouse();
    if (fld != null && ivPopupMenus != null &&
        e.isPopupTrigger()) {
      JPopupMenu pop = (JPopupMenu)ivPopupMenus.get(fld);
      if (pop != null)
        pop.show(getEmulator(), e.getX(), e.getY());
    }
  }


  /**
   */
  public String getString(int col, int row, int nChars) {
    return getEmulator().getString(col, row, nChars);
  }


  /**
   * Returns an Enumeration of all the fields present on the panel.
   */
  public final List<XI5250Field> getFields() {
    return getEmulator().getFields();
  }


  /**
   * Returns the field present at the given position (null if none).
   */
  public final XI5250Field getFieldFromPos(int aCol, int aRow) {
    return getEmulator().getFieldFromPos(aCol, aRow);
  }


  /**
   * Returns the field next to the given label (null if none).
   */
  public final XI5250Field getFieldNextTo(String aLabel) {
    return getEmulator().getFieldNextTo(aLabel);
  }


  /**
   * Verifies the presence of the given label.
   */
  public final boolean isLabelPresent(String aLabel) {
    return (getEmulator().getLabelLinearPos(aLabel) >= 0);
  }


  /**
   * Checks that: the field is different from null, it isn' t a bypass field
   * and it has the required length.
   */
  public boolean checkField(XI5250Field aField, int aLen) {
    return (aField != null && !aField.isOrgBypassField() &&
            aField.getLength() == aLen);
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  class CrtAdapter extends XI5250CrtAdapter {

    @Override
    public void fieldActivated(XI5250CrtEvent e) {
      XI5250PanelHandler.this.fieldActivated(e.getField());
    }

    @Override
    public void fieldDeactivated(XI5250CrtEvent e) {
      XI5250PanelHandler.this.fieldDeactivated(e.getField());
    }

    @Override
    public void sizeChanged(XI5250CrtEvent e) {
      XI5250PanelHandler.this.sizeChanged();
    }

    @Override
    public void keyEvent(XI5250CrtEvent e) {
      XI5250PanelHandler.this.keyEvent(e.getKeyEvent());
    }

    @Override
    public void mouseEntersField(XI5250CrtEvent e) {
      XI5250PanelHandler.this.mouseEntersField(e.getField());
    }

    @Override
    public void mouseExitsField(XI5250CrtEvent e) {
      XI5250PanelHandler.this.mouseExitsField(e.getField());
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  class HintTimer implements ActionListener {

    private XI5250Field ivField;

    public HintTimer(XI5250Field aField) {
      ivField = aField;
    }

    public void actionPerformed(ActionEvent anEvent) {
      synchronized (ivDispatcher.getTreeLock()) {
        if (ivHintWindow != null && getEmulator().hasFocus()) {
          {
            Point       pt = getEmulator().getLocationOnScreen();
            Rectangle[] rcts = ivField.getRowsRects();
            Rectangle   rct  = rcts[rcts.length - 1];

            pt.translate(rct.x, rct.y + rct.height);

            pt.translate(-4, 4);

            Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();

            //!!1.05b
            pt.x = Math.max(0, Math.min(ss.width - ivHintWindow.getSize().width, pt.x));
            pt.y = Math.max(0, Math.min(ss.height - ivHintWindow.getSize().height, pt.y));

            ivHintWindow.setLocation(pt.x, pt.y);
          }

          ivHintWindow.setVisible(true);
          // potrebbe non essere visibile (vedi XIHintWindow)
          if (!ivHintWindow.isVisible())
            hideHint();
          else
            ivHintWindow.addComponentListener(ivHintListener);
        }
      }
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  class HintThread extends Thread {
    @Override
    public void run() {
      while (true) {
        try {
          Thread.sleep(250);
        }
        catch (InterruptedException ex) {
          break;
        }

        refreshHint();
      }
    }
  }
}
