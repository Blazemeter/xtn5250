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
!!V 10/04/97 rel. 0.93 - some fixes to add SignedNumeric fields handling.
    16/04/97 rel. 0.94 - userError method added.
    08/05/97 rel. 0.95b-
    27/05/97 rel. 1.00 - first release.
    25/07/97 rel. 1.03a- a bug in ...Multicaster.
    06/08/97 rel. 1.03c- bug fix.
    27/08/97 rel. 1.04 - clipboard support.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    29/07/99 rel. 1.14 - Rework on 3d look&feel.
 */

package net.infordata.em.crt5250;

import java.awt.AWTEventMulticaster;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.EventListener;

import net.infordata.em.tnprot.XITelnet;

/**
 * Implements behaviour of 5250 fields with some extensions.
 *
 * @author Valentino Proietti - Infordata S.p.A.
 */
public class XI5250Field implements XI5250BaseField {

  //border styles
  /**
   * Uses default border definition.
   *
   * @see #setBorderStyle
   * @see XI5250Crt#setDefFieldsBorderStyle
   */
  public static final int DEFAULT_BORDER = 0;
  /**
   * No border.
   *
   * @see #setBorderStyle
   * @see XI5250Crt#setDefFieldsBorderStyle
   */
  public static final int NO_BORDER = 1;
  /**
   * Raised border.
   *
   * @see #setBorderStyle
   * @see XI5250Crt#setDefFieldsBorderStyle
   */
  public static final int RAISED_BORDER = 2;
  /**
   * Lowered border.
   *
   * @see #setBorderStyle
   * @see XI5250Crt#setDefFieldsBorderStyle
   */
  public static final int LOWERED_BORDER = 3;

  private XI5250Crt ivCrt;
  private byte[] ivFFW;
  private byte[] ivFCW;
  private int ivCol;
  private int ivRow;

  //ivInputLen rapresent the length of the input area
  private int ivInputLen;
  //this is the real field length
  private int ivLength;

  private int ivAttr;

  // valid only after updateStr() call
  private String ivStr;
  private int ivFldPos;
  private int ivCurPos;
  private int ivPos;

  private XI5250FieldListener ivFieldListener;
  private XI5250FieldPaintListener ivFieldPaintListener;

  private boolean ivEnabled = true;

  private int ivBorderStyle = DEFAULT_BORDER;

  /**
   * @param aCrt the containing XI5250Crt
   * @param aCol the field position
   * @param aRow the field position
   * @param aLen the field length
   * @param aAttr the field color attribute
   */
  public XI5250Field(XI5250Crt aCrt, int aCol, int aRow, int aLen, int aAttr) {
    this(aCrt, new byte[2], new byte[2], aCol, aRow, aLen, aAttr);
  }

  /**
   * @param aCrt the containing XI5250Crt
   * @param aFFW see IBM 5250 function reference manual page 2-68
   * @param aFCW see IBM 5250 function reference manual page 2-65
   * @param aCol the field position
   * @param aRow the field position
   * @param aLen the field length
   * @param aAttr the field color attribute
   */
  public XI5250Field(XI5250Crt aCrt, byte[] aFFW, byte[] aFCW,
      int aCol, int aRow, int aLen, int aAttr) {
    ivCrt = aCrt;
    ivFFW = aFFW;
    ivFCW = aFCW;
    ivCol = aCol;
    ivRow = aRow;
    ivInputLen = aLen;
    ivLength = aLen;
    ivAttr = aAttr;

    if (isSignedNumeric()) {
      --ivInputLen;
    }
  }

  protected void removeNotify() {
  }

  /**
   * XI5250FieldListener handling
   *
   * @param l listener to add to the field
   */
  public synchronized void addFieldListener(XI5250FieldListener l) {
    ivFieldListener = (XI5250FieldListener) Multicaster.add(ivFieldListener, l);
  }

  /**
   * XI5250FieldListener handling
   *
   * @param l listener to remove from the field
   */
  public synchronized void removeFieldListener(XI5250FieldListener l) {
    ivFieldListener = (XI5250FieldListener) Multicaster.
        remove(ivFieldListener, l);
  }

  /**
   * Routes XI5250FieldEvent to listeners
   *
   * @param e event to process for the field.
   */
  protected void processFieldEvent(XI5250FieldEvent e) {
    if (ivFieldListener == null) {
      return;
    }

    switch (e.getID()) {
      case XI5250FieldEvent.ACTIVATED:
        ivFieldListener.activated(e);
        break;
      case XI5250FieldEvent.DEACTIVATED:
        ivFieldListener.deactivated(e);
        break;
      case XI5250FieldEvent.VALUE_CHANGED:
        ivFieldListener.valueChanged(e);
        break;
      case XI5250FieldEvent.ENABLED_STATE_CHANGED:
        ivFieldListener.enabledStateChanged(e);
        break;
      case XI5250FieldEvent.KEY_EVENT:
        ivFieldListener.keyEvent(e);
        break;
    }
  }

  /**
   * XI5250FieldPaintListener handling
   *
   * @param l listener to add to the field
   */
  public synchronized void addFieldPaintListener(XI5250FieldPaintListener l) {
    ivFieldPaintListener =
        (XI5250FieldPaintListener) Multicaster.
            add(ivFieldPaintListener, l);
  }

  /**
   * XI5250FieldPaintListener handling
   *
   * @param l listener to remove from the field
   */
  public synchronized void removeFieldPaintListener(XI5250FieldPaintListener l) {
    ivFieldPaintListener =
        (XI5250FieldPaintListener) Multicaster.
            remove(ivFieldPaintListener, l);
  }

  /**
   * routes XI5250FieldPaintEvent to listeners
   *
   * @param e event to process
   */
  protected void processFieldPaintEvent(XI5250FieldPaintEvent e) {
    if (ivFieldPaintListener == null) {
      return;
    }

    switch (e.getID()) {
      //
      case XI5250FieldPaintEvent.FIELD_PAINT:
        ivFieldPaintListener.fieldPaint(e);
        break;
      //
      case XI5250FieldPaintEvent.ROW_PAINT:
        ivFieldPaintListener.rowPaint(e);
        break;
    }
  }

  public final XI5250Crt getCrt() {
    return ivCrt;
  }

  /**
   * Initializes the field. It is called after that the 5250 panel construction has ended to let the
   * field refresh its color attribute.
   */
  public void init() {
    updateStr();

    // Is color attribute changed ?
    int newAttr = ivCrt.getAttr(ivCrt.toColPos(ivFldPos - 1), ivCrt.toRowPos(ivFldPos - 1));
    if (newAttr != ivAttr) {
      ivAttr = newAttr;
      String str = getString();
      int from = 0;
      while (true) {
        int idx = str.indexOf(XI5250Crt.ATTRIBUTE_PLACE_HOLDER, from);
        if (idx < 0) {
          ivCrt.drawString(str.substring(from),
              ivCrt.toColPos(ivFldPos + from), ivCrt.toRowPos(ivFldPos + from), newAttr);
          break;
        }
        ivCrt.drawString(str.substring(from, idx),
            ivCrt.toColPos(ivFldPos + from), ivCrt.toRowPos(ivFldPos + from), newAttr);
        newAttr = ivCrt.getAttr(ivCrt.toColPos(ivFldPos + idx), ivCrt.toRowPos(ivFldPos + idx));
        ivCrt.drawString(String.valueOf(XI5250Crt.ATTRIBUTE_PLACE_HOLDER),
            ivCrt.toColPos(ivFldPos + idx), ivCrt.toRowPos(ivFldPos + idx), newAttr);
        from = idx + 1;
      }
    }
  }

  /**
   * Used for example to write field contents to the 5250 stream. If needed some internal attribute
   * can be exposed.
   *
   * @param aSaver saver to field the field to
   */
  public void saveTo(XI5250FieldSaver aSaver) throws IOException {
    aSaver.write(this, getString());
  }

  /**
   * Clear field to nulls.
   */
  public void clear() {
    char[] chs = new char[ivLength];
    for (int i = 0; i < chs.length; i++) {
      chs[i] = '\u0000';
    }
    ivCrt.drawString(new String(chs), ivCol, ivRow, XI5250Crt.USE_PRESENT_ATTRIBUTE);
  }

  /**
   * Changes the field contents.
   *
   * @param aStr string to set on the field
   */
  public void setString(String aStr) {
    if (aStr.equals(getTrimmedString())) {
      return;
    }

    int s = ivCrt.toLinearPos(ivCol, ivRow);
    char ch;
    int col, row;
    for (int i = 0; i < aStr.length() && i < ivInputLen; i++) {
      ch = aStr.charAt(i);
      col = ivCrt.toColPos(s + i);
      row = ivCrt.toRowPos(s + i);
      insertChar(ch, col, row, false, false);
    }
  }

  /**
   * Returns value of the field as String. NOTE: signed numeric are followed by sign char
   *
   * @return value of the field as String
   */
  public String getString() {
    return ivCrt.getString(ivCol, ivRow, ivLength);
  }

  /**
   * Returns value of the field as String, all trailing nulls and blanks are cut off. NOTE: signed
   * numeric are followed by sign char
   *
   * @return Returns value of the field as String, all trailing nulls and blanks are cut off.
   */
  public String getTrimmedString() {
    String str = getString();
    int i;

    // exclude trailing null and blank chars
    for (i = str.length() - 1; (i >= 0) && ((str.charAt(i) == '\u0000') ||
        (str.charAt(i) == ' ')); i--) {
      ;
    }

    return (i < 0) ? "" : str.substring(0, i + 1);
  }

  /**
   * Returns the Field Format Word.
   *
   * @param i the byte index to get from the field format word.
   * @return the Field Format Word.
   */
  public byte getFFW(int i) {
    return ivFFW[i];
  }

  /**
   * Returns the Field Control Word.
   *
   * @param i the byte index to get from the field control word.
   * @return the Field Control Word.
   */
  public byte getFCW(int i) {
    return ivFCW[i];
  }

  /**
   * The field column position.
   *
   * @return the field column position.
   */
  public int getCol() {
    return ivCol;
  }

  /**
   * The field row position.
   *
   * @return the field row position.
   */
  public int getRow() {
    return ivRow;
  }

  /**
   * The field length.
   *
   * @return the field length.
   */
  public int getLength() {
    return ivLength;
  }

  /**
   * Called every time the field contents changes. It fires the XI5250FieldEvent.VALUE_CHANGED
   * event.
   */
  protected void setMDTOn() {
    ivFFW[0] |= 0x08;
    processFieldEvent(new XI5250FieldEvent(XI5250FieldEvent.VALUE_CHANGED, this));
  }

  public void resetMDT() {
    ivFFW[0] &= ~0x08;
  }

  /**
   * True if it was created as bypass field
   *
   * @return true if it was created as bypass field
   */
  public boolean isOrgBypassField() {
    return ((ivFFW[0] & 0x20) != 0);
  }

  /**
   * True if it was created or it became a bypass field return (isOrgBypassField() ||
   * (!isEnabled());
   *
   * @return True if it was created or it became a bypass field return
   * @see #isEnabled()
   */
  public boolean isBypassField() {
    return (isOrgBypassField() || !isEnabled());
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if dup is enabled, false otherwise.
   */
  public boolean isDupEnabled() {
    return ((ivFFW[0] & 0x10) != 0);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if mdt is on, false otherwise.
   */
  public boolean isMDTOn() {
    return ((ivFFW[0] & 0x08) != 0);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is alphabetic shift, false otherwise.
   */
  public boolean isAlphabeticShift() {
    return ((ivFFW[0] & 0x07) == 0);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is alphabetic only, false otherwise.
   */
  public boolean isAlphabeticOnly() {
    return ((ivFFW[0] & 0x07) == 0x01);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is numeric shift, false otherwise.
   */
  public boolean isNumericShift() {
    return ((ivFFW[0] & 0x07) == 0x02);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is numeric only, false otherwise.
   */
  public boolean isNumericOnly() {
    return ((ivFFW[0] & 0x07) == 0x03);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is digits only, false otherwise.
   */
  public boolean isDigitsOnly() {
    return ((ivFFW[0] & 0x07) == 0x05);
  }

  /**
   * Used to query Field Format Word flags.<br> Magnetic stripe reader, selector light pen, ...
   *
   * @return true if is IO only, false otherwise.
   */
  public boolean isIOOnly() {
    return ((ivFFW[0] & 0x07) == 0x06);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is signed numeric, false otherwise.
   */
  public boolean isSignedNumeric() {
    return ((ivFFW[0] & 0x07) == 0x07);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is auto enter, false otherwise.
   */
  public boolean isAutoEnter() {
    return ((ivFFW[1] & 0x80) != 0);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is exit required, false otherwise.
   */
  public boolean isExitRequired() {
    return ((ivFFW[1] & 0x40) != 0);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is mono case, false otherwise.
   */
  public boolean isMonocase() {
    return ((ivFFW[1] & 0x20) != 0);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is mandatory enter, false otherwise.
   */
  public boolean isMandatoryEnter() {
    return ((ivFFW[1] & 0x08) != 0);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if has to right adjust with zero fill, false otherwise.
   */
  public boolean isRightAdjustZeroFill() {
    return ((ivFFW[1] & 0x07) == 0x05);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if has to right adjust with blank fill, false otherwise.
   */
  public boolean isRightAdjustBlankFill() {
    return ((ivFFW[1] & 0x07) == 0x06);
  }

  /**
   * Used to query Field Format Word flags.
   *
   * @return true if is mandatory fill, false otherwise.
   */
  public boolean isMandatoryFill() {
    return ((ivFFW[1] & 0x07) == 0x07);
  }

  /**
   * Key used to order fields in XI5250FieldsList. Simply returns the field linear position in the
   * 5250 buffer.
   *
   * @return the field linear position in the 5250 buffer
   */
  protected int getSortKey() {
    return ivCrt.toLinearPos(ivCol, ivRow);
  }

  private void updateStr() {
    ivStr = ivCrt.getString(ivCol, ivRow, ivInputLen);
    //!!V se lunghezza stringa = 0 generare eccezione
    ivFldPos = ivCrt.toLinearPos(ivCol, ivRow);
    ivCurPos = ivCrt.toLinearPos(ivCrt.getCursorCol(), ivCrt.getCursorRow());
    ivPos = ivCurPos - ivFldPos;
  }

  protected void processKeyEvent(KeyEvent e) {
    processFieldEvent(new XI5250FieldEvent(XI5250FieldEvent.KEY_EVENT,
        this, e));

    if (e.isConsumed()) {
      return;
    }

    switch (e.getID()) {
      case KeyEvent.KEY_TYPED:
        if (XI5250Crt.isCharKey(e)) {
          if (processKeyChar(e.getKeyChar())) {
            e.consume();
          }
        }
        break;
      case KeyEvent.KEY_PRESSED:
        if (processOtherKey(e)) {
          e.consume();
        }
        break;
      case KeyEvent.KEY_RELEASED:
        ivCrt.ivDropKeyChar = false;
        break;
    }
  }

  protected int insertChar(char aCh, int col, int row,
      boolean insert, boolean fromKeyboard) {
    if (isIOOnly() && fromKeyboard) {
      return 4;  // Do not accept keyboard input 
    }

    if ((isDigitsOnly() && !(aCh >= '0' && aCh <= '9'))) {
      // 10 = only chars between 0 and 9
      return 10;
    }

    if ((isSignedNumeric() && !(aCh >= '0' && aCh <= '9')) ||
        (isNumericOnly() && !((aCh >= '0' && aCh <= '9') ||
            aCh == ' ' || aCh == '+' || aCh == '-' ||
            aCh == '.' || aCh == ','))
        ) {
      // 9 = numeric field
      return 9;
    }

    if (isMonocase()) {
      aCh = Character.toUpperCase(aCh);
    }

    updateStr();

    ivCurPos = ivCrt.toLinearPos(col, row);
    ivPos = ivCurPos - ivFldPos;

    if (ivPos < 0 || ivPos >= ivLength) {
      throw new IllegalStateException("");
    }

    StringBuilder strBuf = new StringBuilder(ivStr);

    if (!insert) {
      strBuf.setCharAt(ivPos, aCh);

      ivCrt.drawString(new String(strBuf).substring(ivPos, ivPos + 1),
          col, row, ivAttr);
    } else {
      if (strBuf.charAt(ivInputLen - 1) != '\u0000' &&
          strBuf.charAt(ivInputLen - 1) != ' ') {
        // 12 = field is full
        return 12;
      } else {
        strBuf.insert(ivPos, aCh);
        ivCrt.drawString(new String(strBuf).substring(ivPos, ivInputLen),
            col, row, ivAttr);
      }
    }

    setMDTOn();   // field modified
    return 0;
  }

  protected boolean processKeyChar(char aCh) {
    if (ivCrt.ivDropKeyChar) {
      ivCrt.ivDropKeyChar = false;
      return true;
    }

    int error = insertChar(aCh, ivCrt.getCursorCol(), ivCrt.getCursorRow(),
        ivCrt.isInsertState(), true);
    if (error > 0) {
      ivCrt.userError(error);
      return true;
    }

    // is cursor on last char ??
    if (ivPos < (ivInputLen - 1)) {
      ivCrt.moveCursor(1, 0);
    } else {
      // send auto enter (-1 as modifier) see XI5250Emulator.processKeyEnter()
      if (isAutoEnter()) {
        ivCrt.doProcessKeyEvent(new KeyEvent(ivCrt, KeyEvent.KEY_PRESSED, 0,
            -1, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER));
      }
      // send auto tab
      else if (!isExitRequired()) {
        ivCrt.doProcessKeyEvent(new KeyEvent(ivCrt, KeyEvent.KEY_PRESSED, 0, 0,
            KeyEvent.VK_TAB, (char) KeyEvent.VK_TAB));
      }
    }

    return true;
  }

  protected boolean processOtherKey(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_BACK_SPACE:
        return processBackSpace(e.getModifiers());
      case KeyEvent.VK_DELETE:
        return processDelete(e.getModifiers());
      case KeyEvent.VK_ADD:
        ivCrt.ivDropKeyChar = true;
        doFieldExit(false);
        return true;
      case KeyEvent.VK_SUBTRACT:
        ivCrt.ivDropKeyChar = true;
        if (isNumericOnly() || isSignedNumeric()) {
          doFieldExit(true);
          return true;
        }
        // 16 = field- not valid
        ivCrt.userError(16);
        return true;
      case KeyEvent.VK_ENTER:
        return processEnter(e.getModifiers());
      case KeyEvent.VK_END:
        return processEnd(e.getModifiers());
    }
    return false;
  }

  protected boolean processBackSpace(int aModifier) {
    switch (aModifier) {
      case 0:
        updateStr();
        if (ivPos == 0) {
          ivCrt.doProcessKeyEvent(new KeyEvent(ivCrt, KeyEvent.KEY_PRESSED, 0, KeyEvent.SHIFT_MASK,
              KeyEvent.VK_TAB, (char) KeyEvent.VK_TAB));
        } else {
          ivCrt.moveCursor(-1, 0);
        }
        return true;
      case KeyEvent.SHIFT_MASK:
        updateStr();
        if (ivPos == 0) {
        } else {
          ivCrt.moveCursor(-1, 0);
          updateStr();
          StringBuilder strBuf = new StringBuilder(ivStr);
          for (int i = ivPos + 1; i < ivStr.length(); i++) {
            strBuf.setCharAt(i - 1, strBuf.charAt(i));
          }
          strBuf.setCharAt(ivStr.length() - 1, '\u0000');

          ivCrt.drawString(new String(strBuf).substring(ivPos),
              ivCrt.getCursorCol(), ivCrt.getCursorRow(), ivAttr);

          setMDTOn();   // field modified
        }
        return true;
    }
    return false;
  }

  protected boolean processDelete(int aModifier) {
    switch (aModifier) {
      case 0:
        updateStr();
        StringBuilder strBuf = new StringBuilder(ivStr);
        for (int i = ivPos + 1; i < ivStr.length(); i++) {
          strBuf.setCharAt(i - 1, strBuf.charAt(i));
        }
        strBuf.setCharAt(ivStr.length() - 1, '\u0000');

        ivCrt.drawString(new String(strBuf).substring(ivPos),
            ivCrt.getCursorCol(), ivCrt.getCursorRow(), ivAttr);

        setMDTOn();   // field modified
        return true;
    }
    return false;
  }

  protected boolean processEnter(int aModifier) {
    switch (aModifier) {
      case KeyEvent.SHIFT_MASK:
        doFieldExit(false);
        return true;
    }
    return false;
  }

  private void doFieldExit(boolean isMinus) {
    XIEbcdicTranslator translator = getCrt().getTranslator();

    updateStr();
    StringBuilder strBuf = new StringBuilder(ivStr);

    for (int i = ivPos; i < ivStr.length(); i++) {
      strBuf.setCharAt(i, '\u0000');
    }

    for (; ivPos > 0 && strBuf.charAt(ivPos - 1) == '\u0000'; ivPos--) {
    }

    // see IBM 5250 function reference manual page 2-70
    if (isNumericOnly()) {
      if (isMinus && ivPos > 0) {
        char ch = strBuf.charAt(ivPos - 1);
        if (ch != '+' && ch != '-' && ch != '.' && ch != ',' && ch != ' ') {
          byte xx = translator.toEBCDIC(strBuf.charAt(ivPos - 1));
          xx &= 0x0F;
          xx |= 0xD0;
          strBuf.setCharAt(ivPos - 1, translator.toChar(xx));
        } else {
          ivCrt.userError(26);
          return;
        }
      }
    }

    if (isRightAdjustZeroFill() || isRightAdjustBlankFill() ||
        isSignedNumeric()) {
      int i;

      // right allign
      for (i = 0; i < ivPos; i++) {
        strBuf.setCharAt(ivStr.length() - i - 1, strBuf.charAt(ivPos - i - 1));
      }

      for (i = ivStr.length() - ivPos - 1; i >= 0; i--) {
        strBuf.setCharAt(i, (isRightAdjustZeroFill()) ? '0' : ' ');
      }

      String str = new String(strBuf);

      // see IBM 5250 function reference manual page 2-70
      if (isSignedNumeric()) {
        str += (isMinus) ? '-' : '\u0000';
      }

      ivCrt.drawString(str, ivCol, ivRow, ivAttr);

    } else {
      ivCrt.drawString(new String(strBuf).substring(ivPos),
          ivCrt.getCursorCol(), ivCrt.getCursorRow(), ivAttr);
    }

    setMDTOn();   // field modified

    // send tab
    ivCrt.doProcessKeyEvent(new KeyEvent(ivCrt, KeyEvent.KEY_PRESSED, 0, 0,
        KeyEvent.VK_TAB, (char) KeyEvent.VK_TAB));
  }

  protected boolean processEnd(int aModifier) {
    switch (aModifier) {
      case 0:
        int i;
        updateStr();
        for (i = ivStr.length() - 1; (i >= 0) && (ivStr.charAt(i) <= ' '); i--) {
        }
        int xx = ivCrt.toLinearPos(ivCol, ivRow) + Math.min(i + 1, ivInputLen - 1);
        ivCrt.setCursorPos(ivCrt.toColPos(xx), ivCrt.toRowPos(xx));
        return true;
      case KeyEvent.SHIFT_MASK:
        updateStr();
        StringBuilder strBuf = new StringBuilder(ivStr);
        for (int j = ivPos; j < ivStr.length(); j++) {
          strBuf.setCharAt(j, '\u0000');
        }

        ivCrt.drawString(new String(strBuf).substring(ivPos),
            ivCrt.getCursorCol(), ivCrt.getCursorRow(), ivAttr);

        setMDTOn();   // field modified
        return true;
    }
    return false;
  }

  /**
   * Returns the number of rows occupied by this field NOTE: a field can be wrapped on more than one
   * line
   *
   * @return the number of rows occupied by this field
   */
  public int getNRows() {
    return ((ivCol + ivLength - 1) / ivCrt.getCrtSize().width + 1);
  }

  /**
   * Returns the bounding rectangle (in pixel).
   *
   * @return the bounding rectangle (in pixel)
   */
  public Rectangle getBoundingRect() {
    Dimension charSize = ivCrt.getCharSize();
    int rows = getNRows();

    if (rows <= 1) {
      return new Rectangle(ivCol * charSize.width,
          ivRow * charSize.height,
          ivLength * charSize.width,
          rows * charSize.height);
    } else {
      return new Rectangle(0 * charSize.width,
          ivRow * charSize.height,
          ivCrt.getCrtBufferSize().width,
          rows * charSize.height);
    }
  }

  /**
   * Returns all the rows occupied by the fields. NOTE: coordinates are expressed in chars.
   *
   * @return all the rows occupied by the fields
   */
  public Rectangle[] getRows() {
    int rows = getNRows();
    //Dimension   charSize = ivCrt.getCharSize();
    Rectangle[] rcts = new Rectangle[rows];

    // split line to handle wrap
    int len = ivLength;
    int x = ivCol;
    int y = ivRow;
    int dx;
    int i = 0;

    while (len > 0) {
      dx = Math.min(ivCrt.getCrtSize().width - x, len);

      rcts[i++] = new Rectangle(x, y, dx, 1);

      len -= dx;
      x = 0;
      ++y;
    }

    return rcts;
  }

  /**
   * Returns all the rectangles that the field is composed of. NOTE: coordinates are expressed in
   * pixel and are relative to XI5250Crt panel.
   *
   * @return all the rectangles that the field is composed of
   */
  public Rectangle[] getRowsRects() {
    Rectangle[] rcts = getRows();

    for (int i = 0; i < rcts.length; i++) {
      rcts[i] = ivCrt.toPoints(rcts[i].x, rcts[i].y, rcts[i].width, rcts[i].height);
    }

    return rcts;
  }

  /**
   * Repaint the field.
   */
  public void repaint() {
    Rectangle rt = getBoundingRect();
    ivCrt.repaint(rt.x, rt.y, rt.width, rt.height);
  }

  /**
   * Coordinates are relative to the field bounding rectangle
   *
   * @param g graphics where to pain the field
   */
  protected void paint(Graphics g) {
    processFieldPaintEvent(
        new XI5250FieldPaintEvent(XI5250FieldPaintEvent.ROW_PAINT,
            this, g));

    Rectangle clip = g.getClipBounds();
    if (clip == null) {
      return;
    }

    Rectangle[] rowsRects = getRowsRects();

    // request a paint for each row
    for (int j = 0; j < rowsRects.length; j++) {
      Rectangle fr = new Rectangle(rowsRects[j]);
      // coord relative to field bounding rect position
      fr.translate(-getBoundingRect().x, -getBoundingRect().y);

      if (clip.intersects(fr)) {
        Graphics fg = g.create();
        // set new clipping rectangle, but do not translate origin
        fg.clipRect(fr.x, fr.y, fr.width, fr.height);
        try {
          rowPaint(fg);
        } finally {
          fg.dispose();
        }
      }
    }

    if (!isOrgBypassField()) {
      drawBorder(g);
    }
  }

  /**
   * Draws the field borders (a field can be splitted on more than one crt line).
   *
   * @param g graphics where to draw the border of the field.
   */
  protected void drawBorder(Graphics g) {
    int borderStyle = getUsedBorderStyle();
    if (borderStyle <= NO_BORDER) {
      return;
    }

    Rectangle[] rowsRects = getRowsRects();

    for (int i = 0; i < rowsRects.length; i++) {
      rowsRects[i].translate(-getBoundingRect().x, -getBoundingRect().y);
    }

    Color bg = getCrt().getDefBackground();
    Color cl1, cl2;
    if (isCurrentField()) {
      cl1 = (borderStyle == RAISED_BORDER) ? bg.darker().darker().darker() :
          bg.darker();
      cl2 = (borderStyle == RAISED_BORDER) ? bg.darker() :
          bg.darker().darker().darker();
    } else {
      cl1 = (borderStyle == RAISED_BORDER) ? bg.darker() :
          bg;
      cl2 = (borderStyle == RAISED_BORDER) ? bg :
          bg.darker();
    }

    switch (rowsRects.length) {
      case 0:
        break;
      case 1:
        g.setColor(cl1);
        g.drawLine(rowsRects[0].x,
            rowsRects[0].y,
            rowsRects[0].x,
            rowsRects[0].y + rowsRects[0].height - 1);
        g.drawLine(rowsRects[0].x,
            rowsRects[0].y,
            rowsRects[0].x + rowsRects[0].width - 1,
            rowsRects[0].y);

        g.setColor(cl2);
        g.drawLine(rowsRects[0].x + rowsRects[0].width - 1,
            rowsRects[0].y,
            rowsRects[0].x + rowsRects[0].width - 1,
            rowsRects[0].y + rowsRects[0].height - 1);
        g.drawLine(rowsRects[0].x,
            rowsRects[0].y + rowsRects[0].height - 1,
            rowsRects[0].x + rowsRects[0].width - 1,
            rowsRects[0].y + rowsRects[0].height - 1);
        break;
      case 2:
        int dx = Math.max(0, (rowsRects[1].x + rowsRects[1].width - 1) - rowsRects[0].x);

        g.setColor(cl1);
        g.drawLine(rowsRects[0].x,
            rowsRects[0].y,
            rowsRects[0].x,
            rowsRects[0].y + rowsRects[0].height - 1);
        g.drawLine(rowsRects[0].x,
            rowsRects[0].y,
            rowsRects[0].x + rowsRects[0].width - 1,
            rowsRects[0].y);

        g.drawLine(rowsRects[1].x,
            rowsRects[1].y,
            rowsRects[1].x + rowsRects[1].width - 1 - dx,
            rowsRects[1].y);

        g.setColor(cl2);
        g.drawLine(rowsRects[1].x + rowsRects[1].width - 1,
            rowsRects[1].y,
            rowsRects[1].x + rowsRects[1].width - 1,
            rowsRects[1].y + rowsRects[1].height - 1);
        g.drawLine(rowsRects[1].x,
            rowsRects[1].y + rowsRects[1].height - 1,
            rowsRects[1].x + rowsRects[1].width - 1,
            rowsRects[1].y + rowsRects[1].height - 1);

        g.drawLine(rowsRects[0].x + dx,
            rowsRects[0].y + rowsRects[0].height - 1,
            rowsRects[0].x + rowsRects[0].width - 1,
            rowsRects[0].y + rowsRects[0].height - 1);
        break;
      default:
        int l = rowsRects.length - 1;

        g.setColor(cl1);
        g.drawLine(rowsRects[0].x,
            rowsRects[0].y,
            rowsRects[0].x,
            rowsRects[0].y + rowsRects[0].height - 1);
        g.drawLine(rowsRects[0].x,
            rowsRects[0].y,
            rowsRects[0].x + rowsRects[0].width - 1,
            rowsRects[0].y);

        g.drawLine(rowsRects[1].x,
            rowsRects[0].y + rowsRects[0].height - 1,
            rowsRects[0].x,
            rowsRects[0].y + rowsRects[0].height - 1);

        g.setColor(cl2);
        g.drawLine(rowsRects[l].x + rowsRects[l].width - 1,
            rowsRects[l].y,
            rowsRects[l].x + rowsRects[l].width - 1,
            rowsRects[l].y + rowsRects[l].height - 1);
        g.drawLine(rowsRects[l].x,
            rowsRects[l].y + rowsRects[l].height - 1,
            rowsRects[l].x + rowsRects[l].width - 1,
            rowsRects[l].y + rowsRects[l].height - 1);

        g.drawLine(rowsRects[l].x + rowsRects[l].width - 1,
            rowsRects[l - 1].y + rowsRects[l - 1].height - 1,
            rowsRects[l - 1].x + rowsRects[l - 1].width - 1,
            rowsRects[l - 1].y + rowsRects[l - 1].height - 1);
        break;
    }
  }


  /**
   * Called for each rectangle returned by getRowsRects. Coordinates are relative to the field
   * bounding rectangle.
   *
   * @param g graphics where to paint the row.
   * @see #getRowsRects
   */
  protected void rowPaint(Graphics g) {
    processFieldPaintEvent(new XI5250FieldPaintEvent(XI5250FieldPaintEvent.ROW_PAINT,
        this, g));
    if (!ivEnabled) {
      Rectangle rt = getBoundingRect();
      g.setColor(Color.white);

      int hh = rt.height;
      for (int x = -hh; x < rt.width; x += 8) {
        g.drawLine(x, hh, x + hh, 0);
      }
    }
  }

  /**
   * Field has changed its dimensions.
   */
  protected void resized() {
  }

  /**
   * Field has changed its activated (focus) state.
   *
   * @param activated true if the field has been activated, false if it has been deactivated.
   */
  protected void activated(boolean activated) {
    repaint();
    processFieldEvent(new XI5250FieldEvent((activated) ? XI5250FieldEvent.ACTIVATED :
        XI5250FieldEvent.DEACTIVATED,
        this));
  }

  public final boolean isCurrentField() {
    return ivCrt.getCurrentField() == this;
  }

  /**
   * Enable or disable the field.
   *
   * @param flag true to enable the field, false to disable it.
   */
  public void setEnabled(boolean flag) {
    if (flag == ivEnabled) {
      return;
    }
    ivEnabled = flag;
    repaint();
    processFieldEvent(
        new XI5250FieldEvent(XI5250FieldEvent.ENABLED_STATE_CHANGED, this));
  }

  public boolean isEnabled() {
    return ivEnabled;
  }

  /**
   * Changes the field border style.
   *
   * @param aStyle style to set on field border
   */
  public void setBorderStyle(int aStyle) {
    if (aStyle == ivBorderStyle) {
      return;
    }

    if (aStyle < DEFAULT_BORDER || aStyle > LOWERED_BORDER) {
      throw new IllegalArgumentException("Wrong border style argument");
    }

    ivBorderStyle = aStyle;
    repaint();
  }

  /**
   * @return the border style for the field.
   * @see #getUsedBorderStyle
   */
  public int getBorderStyle() {
    return ivBorderStyle;
  }


  /**
   * Returns the real used border style. It takes care of the default border style defined with
   * XI5250Crt.setDefFieldsBorderStyle
   *
   * @return Returns the real used border style. It takes care of the default border style defined
   * with XI5250Crt.setDefFieldsBorderStyle
   * @see XI5250Crt#setDefFieldsBorderStyle
   */
  public int getUsedBorderStyle() {
    int style = getBorderStyle();
    return (style != DEFAULT_BORDER) ? style : ivCrt.getDefFieldsBorderStyle();
  }

  @Override
  public String toString() {
    return super.toString() + " [FFW=[" + XITelnet.toHex(ivFFW[0]) + "," +
        XITelnet.toHex(ivFFW[1]) + "]," +
        "FCW=[" + XITelnet.toHex(ivFCW[0]) + "," +
        XITelnet.toHex(ivFCW[1]) + "]," +
        "Attr=" + XITelnet.toHex((byte) ivAttr) + "," +
        "(" + ivCol + "," + ivRow + ")" +
        "Len=" + ivLength + "]";
  }

  /**
   * Multicast Listener for XI5250Field
   *
   * @author Valentino Proietti - Infordata S.p.A.
   */
  private static class Multicaster extends AWTEventMulticaster
      implements XI5250FieldListener,
      XI5250FieldPaintListener {

    protected Multicaster(EventListener a, EventListener b) {
      super(a, b);
    }

    @Override
    protected EventListener remove(EventListener oldl) {
      if (oldl == a) {
        return b;
      }
      if (oldl == b) {
        return a;
      }
      EventListener a2 = removeInternal(a, oldl);
      EventListener b2 = removeInternal(b, oldl);
      if (a2 == a && b2 == b) {
        return this;
      }
      return add(a2, b2);
    }

    public static EventListener add(EventListener a, EventListener b) {
      if (a == null) {
        return b;
      }
      if (b == null) {
        return a;
      }
      return new Multicaster(a, b);
    }

    public static EventListener remove(EventListener a, EventListener b) {
      return removeInternal(a, b);
    }

    public void activated(XI5250FieldEvent e) {
      ((XI5250FieldListener) a).activated(e);
      ((XI5250FieldListener) b).activated(e);
    }

    public void deactivated(XI5250FieldEvent e) {
      ((XI5250FieldListener) a).deactivated(e);
      ((XI5250FieldListener) b).deactivated(e);
    }

    public void valueChanged(XI5250FieldEvent e) {
      ((XI5250FieldListener) a).valueChanged(e);
      ((XI5250FieldListener) b).valueChanged(e);
    }

    public void enabledStateChanged(XI5250FieldEvent e) {
      ((XI5250FieldListener) a).enabledStateChanged(e);
      ((XI5250FieldListener) b).enabledStateChanged(e);
    }

    public void keyEvent(XI5250FieldEvent e) {
      ((XI5250FieldListener) a).keyEvent(e);
      ((XI5250FieldListener) b).keyEvent(e);
    }

    public void fieldPaint(XI5250FieldPaintEvent e) {
      ((XI5250FieldPaintListener) a).fieldPaint(e);
      ((XI5250FieldPaintListener) b).fieldPaint(e);
    }

    public void rowPaint(XI5250FieldPaintEvent e) {
      ((XI5250FieldPaintListener) a).rowPaint(e);
      ((XI5250FieldPaintListener) b).rowPaint(e);
    }

  }

}
