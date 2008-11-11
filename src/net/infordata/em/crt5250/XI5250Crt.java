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
!!V 24/03/97 rel. 0.90 - added XI5250CrtEventListener.
    16/04/97 rel. 0.94 - userError method added.
    27/05/97 rel. 1.00 - first release.
    30/05/97 rel. 1.00a- home key handled.
             MOUSE_ENTERS_FIELD and MOUSE_EXITS_FIELD added.
    05/06/97 rel. 1.00c- reference cursor.         
    23/07/97 rel. 1.03 - .
    25/07/97 rel. 1.03a- a bug in ...Multicaster.
    08/08/97 rel. 1.03d- translateKeyEvent() and processRawKeyEvent().
    27/08/97 rel. 1.04 - clipboard support.
    24/09/97 rel. 1.05 - DNCX project.
    22/12/97 rel. 1.05b- .
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    04/02/99 rel. 1.11 - Swing 1.1 and jdk 1.2 support.
    11/06/99 rel. 1.12a- CursorShape interface has been introduced, some rework
             on cursor handling.
    29/07/99 rel. 1.14 - Rework on 3d look.
 */
 
 
package net.infordata.em.crt5250;


import java.awt.AWTEvent;
import java.awt.AWTEventMulticaster;
import java.awt.AWTKeyStroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.UIManager;

import net.infordata.em.crt.XICrt;
import net.infordata.em.crt.XICrtBuffer;

///////////////////////////////////////////////////////////////////////////////

/**
 * Adds capabilities required by 5250 emulation to XICrt.
 * 5250 requires word-wrap and linear access to the buffer.
 * Introduces also the 5250 input fields handling.
 * It uses as off-screen buffer XI5250CrtBuffer.
 *
 * @see    XI5250CrtBuffer
 * @see    XI5250Field
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250Crt extends XICrt implements Serializable {

  private static final long serialVersionUID = 1L;

  // Debug level 0 = none, 1 = , 2 = detailed
  static final int DEBUG = 0;

  /**
   * Character used as place holder for 5250 attributes.
   */
  public static final char ATTRIBUTE_PLACE_HOLDER = '';

  /**
   * If used as attribute it does' t change the attribute already present.
   * @see #drawString()
   */
  public static final int  USE_PRESENT_ATTRIBUTE = 0;


  // 5250 buffer address position
  transient private int               ivSBA;

  //!!2.00 transient private boolean           ivHasFocus;

  /**
   * The fields list.
   */
  transient protected XI5250FieldsList  ivFields = new XI5250FieldsList(this);
  transient private   XI5250Field       ivCurrentField;

  private   boolean   ivInsertState;

  transient private   XI5250CrtListener ivCrtListener;

  // used by XI5250Field to jump next KEY_TYPED event
  transient boolean   ivDropKeyChar;

  //!!0.95b
  private int ivDefFieldsBorderStyle = XI5250Field.NO_BORDER;

  //!!1.00a
  transient private   XI5250Field       ivFieldUnderMouse;

  //!!1.00c
  private   boolean   ivRefCursor;

  //!!1.04
  transient private   boolean           ivDragging;
  transient private   boolean           ivMousePressed;
  transient private   Point             ivStartDragging;
  transient private   Rectangle         ivSelectedArea;

  //!!1.04c
  transient private   XI5250Field       ivHighLightedField;

  //!!1.04c properties
  public static final String INSERT_STATE            = "insertState";
  public static final String REFERENCE_CURSOR        = "referenceCursor";
  public static final String SELECTED_AREA           = "selectedArea";
  public static final String DEF_FIELDS_BORDER_STYLE = "defFieldsBorderStyle";

  //!!1.12a
  private static final CursorShape cvInsertCursorShape = new InsertCursorShape();
  private static final CursorShape cvNormalCursorShape = new NormalCursorShape();

  transient private final CursorShape ivFixedCursorShape = new FixedCursorShape();

  public static final String CODE_PAGE = "codePage";
  
  public static final String DEFAULT_CODE_PAGE = "CP1144";
  private  String ivCodePage = DEFAULT_CODE_PAGE;
  transient private XIEbcdicTranslator ivTranslator = 
    XIEbcdicTranslator.getTranslator(DEFAULT_CODE_PAGE);

  /**
   * Default constructor.
   */
  public XI5250Crt() {
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
    enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    enableEvents(AWTEvent.FOCUS_EVENT_MASK);

    //0.95b
    setBackground(getDefBackground());
    
    // jdk 1.4 tab-key and shift+tab-key should be delivered to the component
    { 
      Set<AWTKeyStroke> fks = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
      Set<AWTKeyStroke> newFks = new HashSet<AWTKeyStroke>();
      for (AWTKeyStroke keyStroke : fks) {
        if (keyStroke.getKeyCode() == KeyEvent.VK_TAB && 
            keyStroke.getModifiers() == 0)
          continue;
        newFks.add(keyStroke);
      }
      setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newFks);
    }
    {
      Set<AWTKeyStroke> fks = getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
      Set<AWTKeyStroke> newFks = new HashSet<AWTKeyStroke>();
      for (AWTKeyStroke keyStroke : fks) {
        if (keyStroke.getKeyCode() == KeyEvent.VK_TAB && 
            (keyStroke.getModifiers() | KeyEvent.SHIFT_MASK) != 0)
          continue;
        newFks.add(keyStroke);
      }
      setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, newFks);
    }
  }


  /**
   * Usefull to get a static picture of the screen contents.
   * Fields are not copied.
   */
  public XI5250Crt getStaticClone() {
    return getStaticClone(0, 0, getCrtSize().width, getCrtSize().height);
  }


  /**
   * Usefull to get a static picture of the screen contents.
   * Fields are not copied.
   */
  public synchronized XI5250Crt getStaticClone(int col, int row,
                                               int width, int height) {
    XI5250Crt crt = new XI5250Crt();
    crt.setCrtBuffer(new XI5250CrtBuffer((XI5250CrtBuffer)getCrtBuffer(),
                                         col, row, width, height));
    crt.setFont(getFont());
    crt.setBackground(getDefBackground());
    return crt;
  }


  /**
   * Redefined to use XI5250CrtBuffer.
   */
  @Override
  protected XICrtBuffer createCrtBuffer(int nCols, int nRows) {
    return new XI5250CrtBuffer(nCols, nRows);
  }


  /**
   */
  @Override
  public synchronized void setCrtSize(int nCols, int nRows) {
    XI5250CrtBuffer oldCrt = (XI5250CrtBuffer)getCrtBuffer();

    super.setCrtSize(nCols, nRows);

    XI5250CrtBuffer newCrt = (XI5250CrtBuffer)getCrtBuffer();
    if (oldCrt != newCrt) {
      newCrt.setDefBackground(oldCrt.getDefBackground());
    }
  }


  /**
   */
  @Override
  protected CursorShape getCursorShape() {
    return (ivInsertState) ? cvInsertCursorShape : cvNormalCursorShape;
  }


  /**
   */
  @Override
  protected CursorShape getFixedCursorShape() {
    return (ivRefCursor) ? ivFixedCursorShape : null;
  }


  /**
   * @see    XI5250CrtListener
   */
  public synchronized void addCrtListener(XI5250CrtListener l) {
    ivCrtListener = Multicaster.add(ivCrtListener, l);
  }


  /**
   * @see    XI5250CrtListener
   */
  public synchronized void removeCrtListener(XI5250CrtListener l) {
    ivCrtListener = Multicaster.remove(ivCrtListener, l);
  }


  /**
   * Routes XI5250CrtEvent to listeners.
   */
  protected void processCrtEvent(XI5250CrtEvent e) {
    if (ivCrtListener == null)
      return;

    switch (e.getID()) {
      case XI5250CrtEvent.FIELD_ACTIVATED:
        ivCrtListener.fieldActivated(e);
        break;
      case XI5250CrtEvent.FIELD_DEACTIVATED:
        ivCrtListener.fieldDeactivated(e);
        break;
      case XI5250CrtEvent.SIZE_CHANGED:
        ivCrtListener.sizeChanged(e);
        break;
      case XI5250CrtEvent.KEY_EVENT:
        ivCrtListener.keyEvent(e);
        break;
      case XI5250CrtEvent.MOUSE_ENTERS_FIELD:
        ivCrtListener.mouseEntersField(e);
        break;
      case XI5250CrtEvent.MOUSE_EXITS_FIELD:
        ivCrtListener.mouseExitsField(e);
        break;
    }
  }


  /**
   * Calls the init method for all fields.
   * @see    XI5250FieldsList#init
   */
  public void initAllFields() {
    ivFields.init();
    setCursorPos(getCursorCol(), getCursorRow());  // to refresh the current-field
    // NO repaint() needed
  }


  /**
   * Adds an XI5250Field.
   * @see    XI5250FieldsList#addField
   */
  public void addField(XI5250Field aField) {
    ivFields.addField(aField);
    // NO repaint() needed
  }


  /**
   * Removes all fields.
   */
  public void removeFields() {
    try {
      setCurrentField(null);      //!!1.04c
      setFieldUnderMouse(null);   //!!1.04c
      setHighLightedField(null);  //!!1.04c
      ivFields.removeNotify();    //!!V-23/06/97
    }
    finally {
      ivFields = new XI5250FieldsList(this);
    }
    // NO repaint() needed
  }


  /**
   */
  public List<XI5250Field> getFields() {
    return ivFields.getFields();
  }


  /**
   * Returns the field at the given index (null if none).
   * Fields enumeration is based on their linear buffer position.
   */
  public XI5250Field getField(int idx) {
    return ivFields.getField(idx);
  }


  /**
   * Returns the field present at the given position (null if none).
   */
  public XI5250Field getFieldFromPos(int aCol, int aRow) {
    return ivFields.fieldFromPos(aCol, aRow);
  }


  /**
   * Gets the next field or the first one.
   */
  public XI5250Field getNextFieldFromPos(int aCol, int aRow) {
    return ivFields.nextFieldFromPos(aCol, aRow);
  }


  /**
   * Gets the previous field or the last one.
   */
  public XI5250Field getPrevFieldFromPos(int aCol, int aRow) {
    return ivFields.prevFieldFromPos(aCol, aRow);
  }


  /**
   * Returns the linear position of a string jumping fields contents
   * (-1 if not found).
   * @see    #toColPos
   * @see    #toRowPos
   */
  public int getLabelLinearPos(String aLabel) {
    String str = getString();
    for (int pos = str.indexOf(aLabel); pos >= 0;
         pos = str.indexOf(aLabel, pos + 1)) {
      // if it is not present in a field
      if (ivFields.fieldFromPos(toColPos(pos), toRowPos(pos)) == null)
        return pos;
    }
    return -1;
  }


  /**
   * Returns the field that follows a label (null if none).
   * @see    getLabelLinearPos
   * @see    getNextFieldFromPos
   */
  public XI5250Field getFieldNextTo(String aLabel) {
    int pos = getLabelLinearPos(aLabel);
    if (pos < 0)
      return null;

    return getNextFieldFromPos(toColPos(pos), toRowPos(pos));
  }


  /**
   * Called for example when the user tries to write outside of a field.
   */
  protected void userError(int aError) {
    Toolkit.getDefaultToolkit().beep();
  }


  /**
   */
  public void setSBA(int col, int row) {
    setSBA(toLinearPos(col, row));
  }


  /**
   */
  public void setSBA(int aLPos) {
    ivSBA = aLPos;
  }


  /**
   */
  public int getSBA() {
    return ivSBA;
  }


  /**
   */
  public int getSBACol() {
    return toColPos(ivSBA);
  }


  /**
   */
  public int getSBARow() {
    return toRowPos(ivSBA);
  }
  
  
  public void setCodePage(String cp) {
    if (cp == null)
      cp = DEFAULT_CODE_PAGE;
    if (cp.equals(ivCodePage))
      return;
    String old = ivCodePage;
    ivTranslator = XIEbcdicTranslator.getTranslator(cp);
    ivCodePage = cp;
    
    firePropertyChange(CODE_PAGE, old, ivCodePage);
  }
  
  
  public String getCodePage() {
    return ivCodePage;
  }
  
  
  public final XIEbcdicTranslator getTranslator() {
    if (ivTranslator != null)
      return ivTranslator;
    ivTranslator = XIEbcdicTranslator.getTranslator(ivCodePage);
    return ivTranslator;
  }


  /**
   * Changes the insert state.
   */
  public void setInsertState(boolean aInsertState) {
    if (aInsertState == ivInsertState)
      return;
    boolean wasCursorVisible = isCursorVisible();
    setCursorVisible(false);
    boolean oldInsertState = ivInsertState;
    ivInsertState = aInsertState;
    setCursorVisible(wasCursorVisible);

    firePropertyChange(INSERT_STATE, oldInsertState, ivInsertState);
  }


  /**
   */
  public boolean isInsertState() {
    return ivInsertState;
  }


  /**
   * Enables, disables reference cursor.
   */
  public void setReferenceCursor(boolean aFlag) {
    if (aFlag == ivRefCursor)
      return;
    boolean wasCursorVisible = isCursorVisible();
    setCursorVisible(false);
    boolean oldRefCursor = ivRefCursor;
    ivRefCursor = aFlag;
    setCursorVisible(wasCursorVisible);

    firePropertyChange(REFERENCE_CURSOR, oldRefCursor, ivRefCursor);
  }


  /**
   */
  public boolean isReferenceCursor() {
    return ivRefCursor;
  }


  /**
   * Changes the cursor position.
   */
  @Override
  public void setCursorPos(int aCol, int aRow) {
    super.setCursorPos(aCol, aRow);

    XI5250Field field = ivFields.fieldFromPos(getCursorCol(), getCursorRow());
    setCurrentField(field);
  }


  /**
   * Moves the cursor relative to the current position.
   */
  public void moveCursor(int col, int row) {
    col += getCursorCol();
    row += getCursorRow();

    // indirizzo assoluto nel buffer lineare
    int xx = toLinearPos(col, row);
    if (xx == -1)
      xx = toLinearPos(getCrtSize().width - 1, getCrtSize().height - 1);
    else
      if (xx < 0)
        xx = toLinearPos(col, getCrtSize().height - 1);

    col = toColPos(xx);
    row = toRowPos(xx);

    if (col < 0) col = getCrtSize().width - 1;
    if (row < 0) row = getCrtSize().height - 1;

    setCursorPos(col % getCrtSize().width, row % getCrtSize().height);
  }


  /**
   * Moves the cursor on the next field.
   */
  public void cursorOnNextField() {
    XI5250Field field = ivFields.nextFieldFromPos(getCursorCol(), getCursorRow());
    XI5250Field startField = field;
    if (field != null) {
      while (field.isBypassField()) {
        field = ivFields.nextFieldFromPos(field.getCol(), field.getRow());
        if (field == startField)
          break;
      }
    }

    if (field != null && !field.isBypassField())
      setCursorPos(field.getCol(), field.getRow());
  }


  /**
   * Moves the cursor on the previous field.
   */
  public void cursorOnPrevField() {
    XI5250Field field = ivFields.prevFieldFromPos(getCursorCol(), getCursorRow());
    XI5250Field startField = field;
    if (field != null) {
      while (field.isBypassField()) {
        field = ivFields.prevFieldFromPos(field.getCol(), field.getRow());
        if (field == startField)
          break;
      }
    }

    if (field != null && !field.isBypassField())
      setCursorPos(field.getCol(), field.getRow());
  }


  /**
   * Moves the cursor on the first field.
   */
  public void cursorOnFirstField() {
    XI5250Field field = ivFields.getField(0);
    XI5250Field startField = field;
    if (field != null) {
      while (field.isBypassField()) {
        field = ivFields.nextFieldFromPos(field.getCol(), field.getRow());
        if (field == startField)
          break;
      }
    }

    if (field != null && !field.isBypassField())
      setCursorPos(field.getCol(), field.getRow());
  }


  /**
   */
  protected void setCurrentField(XI5250Field field) {  //!!1.04c
    if (field == ivCurrentField)
      return;

    if (ivCurrentField != null) {
      ivCurrentField.activated(false);
      processCrtEvent(new XI5250CrtEvent(XI5250CrtEvent.FIELD_DEACTIVATED, this,
                                         ivCurrentField));
    }

    ivCurrentField = field;

    if (ivCurrentField != null) {
      ivCurrentField.activated(true);
      processCrtEvent(new XI5250CrtEvent(XI5250CrtEvent.FIELD_ACTIVATED, this,
                                         ivCurrentField));
    }
  }


  /**
   * Returns the field under the input cursor (null if none).
   */
  public XI5250Field getCurrentField() {               //!!1.04c
    return ivCurrentField;
  }


  /**
   */
  public void setHighLightedField(XI5250Field field) {  //!!1.04c
    if (field == ivHighLightedField)
      return;

    if (field != null &&
        (ivFields == null || (ivFields.fromFieldToIdx(field) < 0)))
      throw new IllegalArgumentException("The given field isn' t actually present");

    Rectangle rt;

    if (ivHighLightedField != null) {
      rt = ivHighLightedField.getBoundingRect();
      rt.grow(4, 4);
      repaint(rt.x, rt.y, rt.width, rt.height);
    }

    ivHighLightedField = field;

    if (ivHighLightedField != null) {
      rt = ivHighLightedField.getBoundingRect();
      rt.grow(4, 4);
      repaint(rt.x, rt.y, rt.width, rt.height);
    }
  }


  /**
   */
  public XI5250Field getHighLightedField() {               //!!1.04c
    return ivHighLightedField;
  }


  /**
   * Used internally to change the field under the mouse.
   * It also fires the MOUSE_EXITS_FIELD and MOUSE_ENTERS_FIELD
   */
  protected void setFieldUnderMouse(XI5250Field aField) {
    if (aField == ivFieldUnderMouse)
      return;

    if (ivFieldUnderMouse != null) {
      processCrtEvent(new XI5250CrtEvent(XI5250CrtEvent.MOUSE_EXITS_FIELD, this,
                                         ivFieldUnderMouse));
    }

    ivFieldUnderMouse = aField;

    if (ivFieldUnderMouse != null) {
      processCrtEvent(new XI5250CrtEvent(XI5250CrtEvent.MOUSE_ENTERS_FIELD, this,
                                         ivFieldUnderMouse));
    }
  }


  /**
   * Returns the field under the mouse cursor (null if none).
   */
  public XI5250Field getFieldUnderMouse() {
    return ivFieldUnderMouse;
  }


  /**
   */
  private void checkFieldUnderMouse(MouseEvent e) {
    switch (e.getID()) {
      //
      case MouseEvent.MOUSE_EXITED:
        setFieldUnderMouse(null);
        break;
      //
      default:
        int col = e.getX() / getCharSize().width;
        int row = e.getY() / getCharSize().height;

        XI5250Field fld = getFieldFromPos(col, row);

        setFieldUnderMouse(fld);
        break;
    }
  }


  /**
   */
  @Override
  protected void processMouseEvent(MouseEvent e) {
    switch (e.getID()) {
      //
      case MouseEvent.MOUSE_PRESSED:
        requestFocus();
        if (ivMousePressed || (e.getModifiers() != MouseEvent.BUTTON1_MASK))
          break;
        ivMousePressed = true;
        // sets the start dragging row and col
        ivStartDragging = new Point(assureColIn(e.getX() / getCharSize().width),
                                    assureRowIn(e.getY() / getCharSize().height));
        break;
      //
      case MouseEvent.MOUSE_RELEASED:
        if (!ivMousePressed)
          break;
        ivMousePressed = false;

        if (!ivDragging) {
          setSelectedArea(null);
          if ((new Rectangle(getCrtBufferSize())).contains(e.getPoint()))
            setCursorPos(e.getX() / getCharSize().width, e.getY() / getCharSize().height);
        }
        else {
          setSelectedArea(ivStartDragging,
                          new Point(assureColIn(e.getX() / getCharSize().width),
                                    assureRowIn(e.getY() / getCharSize().height)));
        }
        ivDragging = false;
        break;
    }
    super.processMouseEvent(e);
    checkFieldUnderMouse(e);
  }


  /**
   */
  @Override
  protected void processMouseMotionEvent(MouseEvent e) {
    switch (e.getID()) {
      //
      case MouseEvent.MOUSE_DRAGGED:
        if (!ivMousePressed)
          break;

        ivDragging = true;
        setSelectedArea(ivStartDragging,
                        new Point(assureColIn(e.getX() / getCharSize().width),
                                  assureRowIn(e.getY() / getCharSize().height)));
        break;
    }
    super.processMouseMotionEvent(e);
    checkFieldUnderMouse(e);
  }


  /**
   */
  private void setSelectedArea(Point p1, Point p2) {
    setSelectedArea(new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y),
                                  Math.abs(p1.x - p2.x) + 1, Math.abs(p1.y - p2.y) + 1));
  }


  /**
   */
  public void setSelectedArea(Rectangle ivRect) {
    Rectangle oldSelectedArea;

    synchronized (this) {
      if (ivSelectedArea != null && ivSelectedArea.equals(ivRect))
        return;

      if (ivSelectedArea != null)
        repaint(ivSelectedArea.x * getCharSize().width,
                ivSelectedArea.y * getCharSize().height,
                ivSelectedArea.width * getCharSize().width,
                ivSelectedArea.height * getCharSize().height);

      oldSelectedArea = ivSelectedArea;
      ivSelectedArea = (ivRect == null) ? null : new Rectangle(ivRect);

      if (ivSelectedArea != null)
        repaint(ivSelectedArea.x * getCharSize().width,
                ivSelectedArea.y * getCharSize().height,
                ivSelectedArea.width * getCharSize().width,
                ivSelectedArea.height * getCharSize().height);
    }

    firePropertyChange(SELECTED_AREA, oldSelectedArea, ivSelectedArea);
  }


  /**
   * Returns the selected area (null if none).
   */
  public Rectangle getSelectedArea() {
    return (ivSelectedArea == null) ? null : new Rectangle(ivSelectedArea);
  }


  /**
   */
  private static void drawHorzLine(int inc, Graphics gc, int x, int y, int dx) {
    Graphics2D g2 = (Graphics2D)gc;
    float dash[] = { 6f };
    BasicStroke b = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
    g2.setStroke(b);
    g2.drawLine(x, y, x + dx, y);
    
//    boolean draw = true;
//
//    for (int i = 0; i < dx; i += inc)	{
//      if (draw)
//        gc.drawLine(x + i, y, x + i + inc, y);
//
//      draw = !draw;
//    }
  }

  /**
   */
  private static void drawVertLine(int inc, Graphics gc, int x, int y, int dy) {
    Graphics2D g2 = (Graphics2D)gc;
    float dash[] = { 6f };
    BasicStroke b = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, 
                        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
    g2.setStroke(b);
    g2.drawLine(x, y, x, y + dy);
    
//    boolean draw = true;
//
//    for (int i = 0; i < dy; i += inc)	{
//      if (draw)
//        gc.drawLine(x, y + i, x, y + i + inc);
//
//      draw = !draw;
//    }
  }


  /**
   */
  private void drawSelectedArea(Graphics aGc) {
    Rectangle rt = new Rectangle(ivSelectedArea.x * getCharSize().width,
                                 ivSelectedArea.y * getCharSize().height,
                                 ivSelectedArea.width * getCharSize().width,
                                 ivSelectedArea.height * getCharSize().height);

    Graphics   gg  = aGc.create();

    gg.setColor(Color.yellow);
    gg.setXORMode(Color.black);

    gg.clipRect(rt.x, rt.y, rt.width, rt.height);

    //drawVertLine(5, gg, rt.x, rt.y, rt.height);
    //drawHorzLine(5, gg, rt.x, rt.y, rt.width);

    //drawVertLine(5, gg, rt.x + rt.width - 1, rt.y, rt.height);
    //drawHorzLine(5, gg, rt.x, rt.y + rt.height - 1, rt.width);

    drawVertLine(5, gg, rt.x + 1, rt.y + 1, rt.height);
    drawHorzLine(5, gg, rt.x + 1, rt.y + 1, rt.width);

    drawVertLine(5, gg, rt.x + rt.width - 1, rt.y + 1, rt.height);
    drawHorzLine(5, gg, rt.x + 1, rt.y + rt.height - 1, rt.width);

    gg.dispose();
  }


  /**
   */
  public static boolean isCharKey(KeyEvent e) {
    boolean res =
           (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
            e.getKeyChar() >= ' ' &&
            e.getKeyChar() != '\uFFFF' &&            // jdk 1.2
            e.getKeyCode() != KeyEvent.VK_DELETE &&
            e.getKeyChar() != KeyEvent.VK_DELETE &&  //!!1.15c linux jdk 1.2.2
            e.getKeyCode() != KeyEvent.VK_ADD);
    return res;
  }


  /**
   */
  @Override
  protected synchronized void processKeyEvent(KeyEvent e) {
    processRawKeyEvent(translateKeyEvent(e));
    if (!e.isConsumed())
      super.processKeyEvent(e);
  }


  /**
   * Exit usefull for keyboard remapping.
   */
  protected KeyEvent translateKeyEvent(KeyEvent e) {
    return e;
  }


  /**
   * Call this method if you want to submit a raw key event
   * (ie. jump the translation process).
   */
  public synchronized void processRawKeyEvent(KeyEvent e) {
    doProcessKeyEvent(e);
  }


  /**
   * Must be used instead of dispatchEvent to generate automatic key code (auto-enter ...).
   * If cursor is over a field it calls XI5250Field.processKeyEvent.
   */
  protected synchronized void doProcessKeyEvent(KeyEvent e) {
    XI5250Field field = ivFields.fieldFromPos(getCursorCol(), getCursorRow());
    if (field != null && !field.isBypassField())
      field.processKeyEvent(e);

    if (e.isConsumed())
      return;

    processCrtEvent(new XI5250CrtEvent(XI5250CrtEvent.KEY_EVENT, this,
                                       ivCurrentField, e));

    if (e.isConsumed())
      return;

    boolean res = false;

    switch (e.getID()) {
      //
      case KeyEvent.KEY_PRESSED:
        switch (e.getKeyCode()) {
          //
          case KeyEvent.VK_UP:
            res = processKeyUp(e.getModifiers());
            break;
          //
          case KeyEvent.VK_DOWN:
            res = processKeyDown(e.getModifiers());
            break;
          //
          case KeyEvent.VK_BACK_SPACE:
          case KeyEvent.VK_LEFT:
            res = processKeyLeft(e.getModifiers());
            break;
          //
          case KeyEvent.VK_RIGHT:
            res = processKeyRight(e.getModifiers());
            break;
          //
          case KeyEvent.VK_TAB:
            res = processKeyTab(e.getModifiers());
            break;
          //
          case KeyEvent.VK_INSERT:
            res = processKeyIns(e.getModifiers());
            break;
          //
          case KeyEvent.VK_HOME:
            res = processKeyHome(e.getModifiers());
            break;
        }
        if (res)
          e.consume();
        break;
    }
  }


  /**
   */
  protected boolean processKeyUp(int aModifier) {
    switch (aModifier) {
      case 0:
        moveCursor(0, -1);
        return true;
    }
    return false;
  }


  /**
   */
  protected boolean processKeyDown(int aModifier) {
    switch (aModifier) {
      case 0:
        moveCursor(0, 1);
        return true;
    }
    return false;
  }


  /**
   */
  protected boolean processKeyLeft(int aModifier) {
    switch (aModifier) {
      case 0:
        moveCursor(-1, 0);
        return true;
    }
    return false;
  }


  /**
   */
  protected boolean processKeyRight(int aModifier) {
    switch (aModifier) {
      case 0:
        moveCursor(1, 0);
        return true;
    }
    return false;
  }


  /**
   */
  protected boolean processKeyTab(int aModifier) {
    switch (aModifier) {
      case 0:
        cursorOnNextField();
        return true;
      case KeyEvent.SHIFT_MASK:
        cursorOnPrevField();
        return true;
    }
    return false;
  }


  /**
   */
  protected boolean processKeyIns(int aModifier) {
    switch (aModifier) {
      case 0:
        setInsertState(!ivInsertState);
        return true;
      case KeyEvent.CTRL_MASK:
        doCopy();
        return true;
      case KeyEvent.SHIFT_MASK:
        doPaste();
        return true;
    }
    return false;
  }


  /**
   */
  protected boolean processKeyHome(int aModifier) {
    switch (aModifier) {
      case 0:
        cursorOnFirstField();
        return true;
      case KeyEvent.SHIFT_MASK:   //!!1.00c
        setReferenceCursor(!isReferenceCursor());
        return true;
      case KeyEvent.CTRL_MASK:
        if (DEBUG >= 1) {
          getCrtBuffer().dumpBuffer(System.out);
          return true;
        }
        break;
      case KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK:
        if (DEBUG >= 1) {
          repaint();
          return true;
        }
        break;
    }
    return false;
  }


  /**
   */
  @Override
  protected void processFocusEvent(FocusEvent e) {
    switch (e.getID()) {
      //
      case FocusEvent.FOCUS_GAINED:
        setCursorVisible(true);
        break;
      //
      case FocusEvent.FOCUS_LOST:
        setCursorVisible(false);
        break;
    }
    super.processFocusEvent(e);
  }


  /**
   * Returns true.
   */
  public boolean isFocusTraverseable() {
    return true;
  }


  /**
   * Redefined because 5250 works with line wrap.
   */
  @Override
  public void drawString(String str, int col, int row, int aAttr) {
    int lines = ((col + str.length()) / getCrtSize().width + 1);

    if (lines <= 1)
      super.drawString(str, col, row, aAttr);
    else {
      XICrtBuffer crtBuffer = getCrtBuffer();

      crtBuffer.drawString(str, col, row, aAttr);
      repaint(0 * crtBuffer.getCharSize().width,
              row * crtBuffer.getCharSize().height,
              crtBuffer.getSize().width,
              lines * crtBuffer.getCharSize().height);
    }
  }


  /**
   * Converts x-y coord to buffer linear position.
   */
  public final int toLinearPos(int aCol, int aRow) {
    return ((XI5250CrtBuffer)getCrtBuffer()).toLinearPos(aCol, aRow);
  }


  /**
   * Converts buffer linear position to x-y coord.
   */
  public final int toColPos(int aPos) {
    return ((XI5250CrtBuffer)getCrtBuffer()).toColPos(aPos);
  }


  /**
   * Converts buffer linear position to x-y coord.
   */
  public final int toRowPos(int aPos) {
    return ((XI5250CrtBuffer)getCrtBuffer()).toRowPos(aPos);
  }


  /**
   * If the font is really changed then a SIZE_CHANGED event is fired.
   */
  @Override
  public void setFont(Font aFont) {
    Font oldFont = getFont();
    super.setFont(aFont);
    // check if font is changed
    if (oldFont != getFont()) {
      processCrtEvent(new XI5250CrtEvent(XI5250CrtEvent.SIZE_CHANGED, this,
                                         ivCurrentField));
      if (ivFields != null)
        ivFields.resized();
    }
  }


  /**
   * Usefull to add paint over the normal 5250 screen.
   */
  @Override
  protected void foregroundPaint(Graphics g) {
    ivFields.paint(g);

    super.foregroundPaint(g);

    if (ivHighLightedField != null) { //!!1.04c
      Rectangle  rt = ivHighLightedField.getBoundingRect();
      g.setColor(Color.red.brighter().brighter());
      rt.grow(1, 1);
      g.drawRoundRect(rt.x, rt.y, rt.width, rt.height, 4, 4);
      rt.grow(1, 1);
      g.drawRoundRect(rt.x, rt.y, rt.width, rt.height, 4, 4);
    }

    if (ivSelectedArea != null)
      drawSelectedArea(g);
  }


  /**
   * Changes the default fields border style.
   * @see    XI5250Field#getUsedBorderStyle
   */
  public void setDefFieldsBorderStyle(int aStyle) {
    if (aStyle == ivDefFieldsBorderStyle)
      return;

    if (aStyle <= XI5250Field.DEFAULT_BORDER ||
        aStyle > XI5250Field.LOWERED_BORDER)
      throw new IllegalArgumentException("Wrong border style argument");

    int oldDefFieldsBorderStyle = ivDefFieldsBorderStyle;
    ivDefFieldsBorderStyle = aStyle;
    repaint();

    firePropertyChange(DEF_FIELDS_BORDER_STYLE, oldDefFieldsBorderStyle,
                       ivDefFieldsBorderStyle);
  }


  /**
   * Returns the default fields border style.
   */
  public int getDefFieldsBorderStyle() {
    return ivDefFieldsBorderStyle;
  }


  /**
   * Changes the default background.
   * To add 3D Fx :
   * <pre>
   *   crt.setDefBackground(SystemColor.control);
   *   crt.setDefFieldsBorderStyle(XI5250Field.LOWERED_BORDER);
   * </pre>
   * @see    XI5250CrtBuffer#setDefBackground
   */
  public synchronized void setDefBackground(Color aColor) {
    XI5250CrtBuffer ivBuf = (XI5250CrtBuffer)getCrtBuffer();

    if (ivBuf.getDefBackground().equals(aColor))
      return;

    ivBuf.setDefBackground(aColor);
    setBackground(ivBuf.getDefBackground());
    ivBuf.copyFrom(ivBuf);
    repaint();
  }


  /**
   * Retruns the default background color.
   */
  public Color getDefBackground() {
    return ((XI5250CrtBuffer)getCrtBuffer()).getDefBackground();
  }


  /**
   * Copies the selected area into the clipboard.
   */
  protected synchronized void doCopy() {
    if (ivSelectedArea == null)
      return;

    Clipboard clipboard = getToolkit().getSystemClipboard();

    /*!!V This approach will be used when the jdk bug Id4066902 disappear.
    XI5250Crt crt = getStaticClone(ivSelectedArea.x, ivSelectedArea.y,
                                   ivSelectedArea.width, ivSelectedArea.height);

    clipboard.setContents(crt, crt);
    */

    //!!V To be replaced
    StringBuilder strBuf = new StringBuilder();
    for (int r = ivSelectedArea.y;
         r < (ivSelectedArea.y + ivSelectedArea.height); r++) {
      strBuf.append(getString(ivSelectedArea.x, r, ivSelectedArea.width));

      if (r < (ivSelectedArea.y + ivSelectedArea.height - 1))
        strBuf.append("\n");
    }

    for (int i = 0; i < strBuf.length(); i++)
      if (strBuf.charAt(i) < ' ' && strBuf.charAt(i) != '\n')
        strBuf.setCharAt(i, ' ');

    String str = new String(strBuf);

    StringSelection contents = new StringSelection(str);
    clipboard.setContents(contents, contents);

    setSelectedArea(null);
  }


  /**
   */
  public boolean isPasteable() {
    Clipboard clipboard = getToolkit().getSystemClipboard();

    Transferable content = clipboard.getContents(this);

    return (content != null && content.isDataFlavorSupported(DataFlavor.stringFlavor));
  }


  /**
   * Inserts the clipboard contents.
   */
  protected synchronized void doPaste() {
    if (!isPasteable())
      return;

    Clipboard clipboard = getToolkit().getSystemClipboard();

    Transferable content = clipboard.getContents(this);

    try {
      String str = (String)content.getTransferData(DataFlavor.stringFlavor);

      int         col = getCursorCol();
      int         row = getCursorRow();
      boolean     error = false;
      XI5250Field field;

      for (int i = 0; i < str.length(); i++) {
        char ch = str.charAt(i);
        if (ch == '\n') {
          col = getCursorCol();
          row++;
        }
        else if (ch < ' ') {
          error = true;
          col++;
        }
        else {
          if ((col >= 0) && (col < getCrtSize().width) &&
              (row >= 0) && (row < getCrtSize().height) &&
              (field = getFieldFromPos(col, row)) != null) {
            if (field.insertChar(ch, col, row, false, true) != 0)
              error = true;
          }
          else
            error = true;
          col++;
        }
      }

      if (error)
        Toolkit.getDefaultToolkit().beep();  //!!V richiamare userError ??
    }
    catch (Exception ex) {
    }
  }


//  /**
//   */
//  void writeObject(ObjectOutputStream oos) throws IOException {
//    oos.defaultWriteObject();
//  }
//
//  void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
//    ois.defaultReadObject();
//  }


  /**
   * Used only for test purposes.
   */
  public static void main(String[] argv) {

    XI5250Crt   crt = new XI5250Crt();

    XI5250CrtFrame frm = new XI5250CrtFrame("TEST", crt);
    frm.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        System.out.println("END.");
        System.exit(0);
      }
    });

    crt.setCursorPos(1, 2);

    crt.drawString("eccoci", 1, 2);
    crt.addField(new XI5250Field(crt, new byte[2], new byte[2], 1, 2, 6, -1));

    crt.drawString("green",  2, 0, 0x20);
    crt.drawString("green", 10, 0, 0x21);

    crt.drawString("redred", 20, 0, 0x28);
    crt.drawString("redred", 30, 0, 0x29);

    crt.drawString("redred", 40, 0, 0x2A);
    crt.drawString("redred", 50, 0, 0x2B);

    crt.drawString("blue",  0, 1, 0x3A);
    crt.drawString("blue", 10, 1, 0x3B);

    crt.drawString("turquoise", 20, 1, 0x30);
    crt.drawString("turquoise", 30, 1, 0x31);
    
    crt.drawString("yellow", 40, 1, 0x36);
    crt.drawString("yellow", 50, 1, 0x33);
    
    crt.drawString("pink", 60, 1, 0x38);
    crt.drawString("pink", 70, 1, 0x39);
    
    crt.drawString("XYZ", 79, 22, 0x34);
    crt.drawString("X", 79, 23);

    System.out.println(crt.getString(79, 22, 3));

    crt.addField(new XI5250Field(crt, new byte[2], new byte[2], 1, 6, 10, -1));
    crt.drawString("eccociABCD", 1, 6);
    
    {
      XI5250Field fld = new XI5250Field(crt, new byte[2], new byte[2], 21, 3, 170, -1); 
      fld.setBorderStyle(XI5250Field.LOWERED_BORDER);
      crt.addField(fld);

      crt.setHighLightedField(fld);

      crt.drawString(ATTRIBUTE_PLACE_HOLDER + "eccoci 0 1 2 3 4 5 6 7 8 9 0", 20, 3, 0x34);
      crt.drawString("FINE", 1, 5);
    }

    {
      // signed numeric
      byte[] FFW = {0x07, 0x00};
      XI5250Field fld = new XI5250Field(crt, FFW, new byte[2], 1, 7, 6, -1);
      crt.addField(fld);
      crt.drawString(ATTRIBUTE_PLACE_HOLDER + "", 0, 7, 0x34);
    }
      
    {
      // numeric only
      byte[] FFW = {0x03, 0x06};
      crt.addField(new XI5250Field(crt, FFW, new byte[2], 1, 8, 6, -1));
      crt.drawString(ATTRIBUTE_PLACE_HOLDER + "", 0, 8, 0x34);
    }
    
    crt.initAllFields();

    crt.setDefFieldsBorderStyle(XI5250Field.LOWERED_BORDER);
    //!!1.14 crt.setDefBackground(SystemColor.control);
    crt.setDefBackground(UIManager.getColor("control"));

    frm.setBounds(0, 0, 728, 512);
    frm.centerOnScreen();
    frm.setVisible(true);

    crt.setReferenceCursor(true);

//    int i = 0;
//    XI5250Crt sCrt = null;

    crt.setBlinkingCursor(true);

    while (true) {
      try {
        Thread.sleep(10000);
        //crt.setFont(new Font("Monospaced", Font.BOLD, ((i++ % 2) == 0) ? 10 : 14));
        //crt.setDefBackground(((i++ % 2) == 0) ? SystemColor.control : SystemColor.black);

        /*
        if ((i % 2) != 0)
          crt.setCrtSize(132, 27);
        else
          crt.setCrtSize(80, 24);
        */

        /*
        if (sCrt != null)
          frm.remove(sCrt);
        sCrt = crt.getStaticClone();
        frm.add("West", sCrt);
        frm.validate();
        */
        //crt.setBlinkingCursor(!crt.isBlinkingCursor());
      }
      catch (InterruptedException ex) {
        break;
      }
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Multicast Listener for XI5250Crt
   * @version
   * @author   Valentino Proietti - Infordata S.p.A.
   */
  private static class Multicaster extends AWTEventMulticaster
                                   implements XI5250CrtListener {

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
      return add((XI5250CrtListener)a2, (XI5250CrtListener)b2);
    }


    public static XI5250CrtListener add(XI5250CrtListener a,
                                        XI5250CrtListener b) {
      if (a == null)  return b;
      if (b == null)  return a;
      return new Multicaster(a, b);
    }


    public static XI5250CrtListener remove(XI5250CrtListener a,
                                           XI5250CrtListener b) {
      return (XI5250CrtListener)removeInternal(a, b);
    }


    /**
     */
    public void fieldActivated(XI5250CrtEvent e) {
      ((XI5250CrtListener)a).fieldActivated(e);
      ((XI5250CrtListener)b).fieldActivated(e);
    }


    public void fieldDeactivated(XI5250CrtEvent e) {
      ((XI5250CrtListener)a).fieldDeactivated(e);
      ((XI5250CrtListener)b).fieldDeactivated(e);
    }


    public void sizeChanged(XI5250CrtEvent e) {
      ((XI5250CrtListener)a).sizeChanged(e);
      ((XI5250CrtListener)b).sizeChanged(e);
    }


    public void keyEvent(XI5250CrtEvent e) {
      ((XI5250CrtListener)a).keyEvent(e);
      ((XI5250CrtListener)b).keyEvent(e);
    }


    public void mouseEntersField(XI5250CrtEvent e) {
      ((XI5250CrtListener)a).mouseEntersField(e);
      ((XI5250CrtListener)b).mouseEntersField(e);
    }


    public void mouseExitsField(XI5250CrtEvent e) {
      ((XI5250CrtListener)a).mouseExitsField(e);
      ((XI5250CrtListener)b).mouseExitsField(e);
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Draws fixed cursor shapes for normal and insert state.
   */
  private class FixedCursorShape implements CursorShape {

    /**
     */
    public void drawCursorShape(Graphics gc, Rectangle aRt) {
      Dimension  dim = getCrtBufferSize();
      Graphics gg = gc.create(0, 0, dim.width, dim.height);
      try {
        Rectangle rt = new Rectangle(aRt);   //!!0.95b
        rt.grow(-1, -1);

        gg.setColor(Color.white);
        gg.setXORMode(Color.black);

        drawVertLine(9, gg, rt.x, 0, dim.height);
        drawHorzLine(9, gg, 0, rt.y + rt.height - 1, dim.width);
      }
      finally {
        gg.dispose();
      }
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Draws blinking cursor shapes for normal state.
   */
  private static class NormalCursorShape implements CursorShape {

    /**
     */
    public void drawCursorShape(Graphics gc, Rectangle aRt) {
      Rectangle rt = new Rectangle(aRt);   //!!0.95b
      rt.grow(-1, -1);

      gc.setColor(Color.white);
      gc.setXORMode(Color.black);

      //!!1.05b
      int dy = (rt.height / 10);
      gc.fillRect(rt.x, rt.y + rt.height - dy * 3,
                  rt.width, dy * 3);

      gc.setPaintMode();
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Draws blinking cursor shapes for Insert state.
   */
  private static class InsertCursorShape implements CursorShape {

    /**
     */
    public void drawCursorShape(Graphics gc, Rectangle aRt) {
      Rectangle rt = new Rectangle(aRt);   //!!0.95b
      rt.grow(-1, -1);

      gc.setColor(Color.white);
      gc.setXORMode(Color.black);

      //!!1.05b
      int dy = (rt.height / 10);
      /*!!1.15b
      gc.fillPolygon(new int[] {rt.x,
                                rt.x,
                                rt.x + rt.width,
                                rt.x + rt.width},
                     new int[] {rt.y + rt.height,
                                rt.y + rt.height - dy * 6,
                                rt.y + rt.height - dy * 3,
                                rt.y + rt.height},
                     4);
      */
      gc.fillRect(rt.x, rt.y + rt.height - dy * 5,
                  rt.width, dy * 5);

      gc.setPaintMode();
    }
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  public static class SupportPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private XI5250Crt ivCrt;

    public SupportPanel(XI5250Crt crt) {
      super(null);
      if (crt == null)
        throw new IllegalArgumentException();
      ivCrt = crt;
      add(crt);
    }

    @Override
     protected void addImpl(Component comp, Object constraints, int index) {
       if (getComponentCount() > 0)
         throw new IllegalStateException("This panel doesn't support components");
       super.addImpl(comp, constraints, index);
     }

     int count = 0;  //!!P
     @Override
     public void doLayout() {
       //!!PSystem.out.println("!!P doLayout() " + count++);
       //!!P(new Exception()).printStackTrace();
       synchronized (getTreeLock()) {
         Insets insets = getInsets();
         Dimension size = getSize();
         size.width -= insets.left + insets.right;
         size.height -= insets.top + insets.bottom;
         ivCrt.setSize(size);
         ivCrt.invalidate();
         Dimension pSize = ivCrt.getPreferredSize();
         ivCrt.setBounds(Math.max(insets.left, (size.width - pSize.width) / 2),
                         Math.max(insets.top, (size.height - pSize.height) / 2),
                         pSize.width,
                         pSize.height);
       }
     }

     @Override
     public Dimension getPreferredSize() {
       Insets insets = getInsets();
       Dimension pSize = ivCrt.getPreferredSize();
       pSize.width += insets.left + insets.right;
       pSize.height += insets.top + insets.bottom;
       return pSize;
     }

     /*!!NO
     private static Border cvBorder = BorderFactory.createEtchedBorder();

     protected void paintBorder(Graphics gr) {
       super.paintBorder(gr);
       Rectangle rt = ivCrt.getBounds();
       rt.grow(1, 1);
       //gr.setColor(ivCrt.getBackground().darker());
       //gr.drawRect(rt.x, rt.y, rt.width, rt.height);
       cvBorder.paintBorder(this, gr, rt.x, rt.y, rt.width, rt.height);
     }
     */
  }
}
