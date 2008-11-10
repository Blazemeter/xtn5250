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
!!V 26/03/97 rel. 0.90 - start of revisions history.
    26/03/97 rel. 0.90 - changed to speed up drawing.
    10/04/97 rel. 0.93 - some fixes to add SignedNumeric fields handling.
    27/05/97 rel. 1.00 - first release.
    13/01/98 rel. 1.05d- NT painting bug.
    14/01/98 rel. 1.06 - asynchronous paint on off-screen image.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    29/07/99 rel. 1.14 - Rework on 3d look&feel.
 */


package net.infordata.em.crt5250;


import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;

import net.infordata.em.crt.XICrtBuffer;


///////////////////////////////////////////////////////////////////////////////

/**
 * Adds capabilities required by 5250 emulation to XICrtBuffer.
 * To be used by XI5250Crt.
 *
 * @see    XI5250Crt
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250CrtBuffer extends XICrtBuffer implements Serializable {


  private static final long serialVersionUID = 1L;

  //!!0.95c
  public static final int GRAY_INTENSITY = colorAsIntensity(Color.gray);

  //!!0.95b
  private ColorWrapper ivBackColor = new ColorWrapper(Color.black);

  /**
   * to be used with dark background
   */
  private static final Color blue = new Color(128, 128, 255);

  /**
   * The character foreground color map used with dark background colors
   */
  private final Color[][] DK_FORE_COLORS_MAP = {
    {
      // normal                          reverse
      Color.green,                       ivBackColor,
      Color.white,                       ivBackColor,
      Color.green,                       ivBackColor, // underscore
      Color.white,                       // underscore
      ivBackColor,                       // invisible
      Color.red,                         ivBackColor,
      Color.red.brighter(),              ivBackColor, // blink
      Color.red,                         ivBackColor, // underscore
      Color.red.brighter(),              // blink, underscore
      ivBackColor,                       // invisible
      Color.cyan,                        ivBackColor, // column separator
      Color.yellow,                      ivBackColor, // column separator
      Color.cyan,                        ivBackColor, // column separator, underscore
      Color.yellow,                      // column separator, underscore
      ivBackColor,                       // invisible
      Color.pink,                        ivBackColor,
      blue,                              ivBackColor,
      Color.pink,                        ivBackColor, // underscore
      blue,                              // underscore
      ivBackColor                        // invisible
    }
  };
  /**
   * The character background color map used with dark background colors
   */
  private final Color[][] DK_BACK_COLORS_MAP = {
    {
      // normal                          reverse
      ivBackColor,                       Color.green,
      ivBackColor,                       Color.white,
      ivBackColor,                       Color.green, // underscore
      ivBackColor,                       // underscore
      ivBackColor,                       // invisible
      ivBackColor,                       Color.red,
      ivBackColor,                       Color.red.brighter(),   // blink
      ivBackColor,                       Color.red,   // underscore
      ivBackColor,                       // blink, underscore
      ivBackColor,                       // invisible
      ivBackColor,                       Color.cyan,  // column separator
      ivBackColor,                       Color.yellow,// column separator
      ivBackColor,                       Color.cyan,  // column separator, underscore
      ivBackColor,                       // column separator, underscore
      ivBackColor,                       // invisible
      ivBackColor,                       Color.pink,
      ivBackColor,                       blue,
      ivBackColor,                       Color.pink,  // underscore
      ivBackColor,                       // underscore
      ivBackColor                        // invisible
    }
  };

  /**
   * The character foreground color map used with bright background colors
   */
  private final Color[][] LT_FORE_COLORS_MAP = {
    {
      // normal                          reverse
      Color.green.darker().darker(),     ivBackColor,
      Color.darkGray,                    ivBackColor,
      Color.green.darker().darker(),     ivBackColor, // underscore
      Color.darkGray,                    // underscore
      ivBackColor,                       // invisible
      Color.red,                         ivBackColor,
      Color.red.brighter(),              ivBackColor, // blink
      Color.red,                         ivBackColor, // underscore
      Color.red.brighter(),              // blink, underscore
      ivBackColor,                       // invisible
      Color.blue,                        ivBackColor, // column separator
      Color.yellow.darker(),             ivBackColor, // column separator
      Color.blue,                        ivBackColor, // column separator, underscore
      Color.yellow.darker(),             // column separator, underscore
      ivBackColor,                       // invisible
      Color.pink.darker(),               ivBackColor,
      blue.darker(),                     ivBackColor,
      Color.pink.darker(),               ivBackColor, // underscore
      blue.darker(),                     // underscore
      ivBackColor                        // invisible
    },
    { //** input fields color map
      // normal                          reverse
      Color.green.darker().darker(),     Color.white,
      Color.darkGray,                    Color.white,
      Color.green.darker().darker(),     Color.white, // underscore
      Color.darkGray,                    // underscore
      Color.white,                       // invisible
      Color.red,                         Color.white,
      Color.red.brighter(),              Color.white, // blink
      Color.red,                         Color.white, // underscore
      Color.red.brighter(),              // blink, underscore
      Color.white,                       // invisible
      Color.blue,                        Color.white, // column separator
      Color.yellow.darker(),             Color.white, // column separator
      Color.blue,                        Color.white, // column separator, underscore
      Color.yellow.darker(),             // column separator, underscore
      ivBackColor,                       // invisible
      Color.pink.darker(),               Color.white,
      blue.darker(),                     Color.white,
      Color.pink.darker(),               Color.white, // underscore
      blue.darker(),                     // underscore
      ivBackColor                        // invisible
    }
  };
  /**
   * The character background color map used with bright background colors
   */
  private final Color[][] LT_BACK_COLORS_MAP = {
    {
      // normal                          reverse
      ivBackColor,                       Color.green.darker().darker(),
      ivBackColor,                       Color.darkGray,
      ivBackColor,                       Color.green.darker().darker(), // underscore
      ivBackColor,                       // underscore
      ivBackColor,                       // invisible
      ivBackColor,                       Color.red,
      ivBackColor,                       Color.red.brighter(),   // blink
      ivBackColor,                       Color.red,   // underscore
      ivBackColor,                       // blink, underscore
      ivBackColor,                       // invisible
      ivBackColor,                       Color.blue,  // column separator
      ivBackColor,                       Color.yellow.darker(),// column separator
      ivBackColor,                       Color.blue,  // column separator, underscore
      ivBackColor,                       // column separator, underscore
      ivBackColor,                       // invisible
      ivBackColor,                       Color.pink.darker(),
      ivBackColor,                       blue.darker(),
      ivBackColor,                       Color.pink.darker(),  // underscore
      ivBackColor,                       // underscore
      ivBackColor                        // invisible
    },
    { //** input fields color map
      // normal                          reverse
      Color.white,                       Color.green.darker().darker(),
      Color.white,                       Color.darkGray,
      Color.white,                       Color.green.darker().darker(), // underscore
      Color.white,                       // underscore
      Color.white,                       // invisible
      Color.white,                       Color.red,
      Color.white,                       Color.red.brighter(),   // blink
      Color.white,                       Color.red,   // underscore
      Color.white,                       // blink, underscore
      Color.white,                       // invisible
      Color.white,                       Color.blue,  // column separator
      Color.white,                       Color.yellow.darker(),// column separator
      Color.white,                       Color.blue,  // column separator, underscore
      Color.white,                       // column separator, underscore
      Color.white,                       // invisible
      Color.white,                       Color.pink.darker(),
      Color.white,                       blue.darker(),
      Color.white,                       Color.pink.darker(),  // underscore
      Color.white,                       // underscore
      Color.white                        // invisible
    }
  };


  private Color[][] ivForegroundColorsMap = DK_FORE_COLORS_MAP;
  private Color[][] ivBackgroundColorsMap = DK_BACK_COLORS_MAP;

  /**
   * 5250 Extra char attribute
   */
  static final int UNDERSCORE       = 0x01;
  /**
   * 5250 Extra char attribute
   */
  static final int COLUMN_SEPARATOR = 0x02;

  static final int[] EXTRA_ATTR_MAP = {
    // normal               reverse
    0x00,
    0x00,
    0x00,
    0x00,
    UNDERSCORE,
    UNDERSCORE,
    UNDERSCORE,
    0x00,
    0x00,
    0x00,
    0x00,
    0x00,
    UNDERSCORE,
    UNDERSCORE,
    UNDERSCORE,
    0x00,
    COLUMN_SEPARATOR,
    COLUMN_SEPARATOR,
    COLUMN_SEPARATOR,
    COLUMN_SEPARATOR,
    COLUMN_SEPARATOR | UNDERSCORE,
    COLUMN_SEPARATOR | UNDERSCORE,
    COLUMN_SEPARATOR | UNDERSCORE,
    0x00,
    0x00,
    0x00,
    0x00,
    0x00,
    UNDERSCORE,
    UNDERSCORE,
    UNDERSCORE,
    0x00
  };


  /**
   * Contructs a XI5250CrtBuffer with the given dimensions expressed in number of chars.
   */
  public XI5250CrtBuffer(int nCols, int nRows) {
    super(nCols, nRows);
    setDefAttr(0x20);
    clear();            // sets new attribute
  }


  /**
   * Creates a XI5250CrtBuffer from a portion of another one.
   */
  public XI5250CrtBuffer(XI5250CrtBuffer from, int aC, int aR, int aW, int aH) {
    super(from, aC, aR, aW, aH);
    setDefBackground(from.getDefBackground());
  }


  /**
   * Returns a cloned XICrtBuffer (the new one needs initGraphics() to be
   * displayed).
   */
  public Object clone() {
    XI5250CrtBuffer aClone = new XI5250CrtBuffer(this, 0, 0,
                                                 getCrtSize().width, getCrtSize().height);
    /* !!1.04
    XI5250CrtBuffer aClone = new XI5250CrtBuffer(getCrtSize().width, getCrtSize().height);
    aClone.setDefBackground(getDefBackground());   //!!0.95b
    aClone.copyFrom(this);
    */
    return aClone;
  }


  /**
   * Handles lines wrap.
   */
  public synchronized void drawString(String aStr, int col, int row,
                                      int aAttr) {
    int lPos = toLinearPos(col, row);
    col = toColPos(lPos);
    row = toRowPos(lPos);

    // if attribute == USE_PRESENT_ATTRIBUTE then get previous attribute
    if (aAttr == XI5250Crt.USE_PRESENT_ATTRIBUTE)
      aAttr = getAttr(col, row);

    // split line to handle wrap
    int len = aStr.length();
    int x   = col;
    int y   = row;
    int dx;
    int i   = 0;
    int maxW = getCrtSize().width;

    while (len > 0) {
      dx = Math.min(maxW - x, len);

      super.drawString(aStr.substring(i, i + dx), x, y, aAttr);

      i += dx;
      len -= dx;
      x = 0;
      ++y;
    }
  }


  /**
   * Draws attribute place-holder char.
   */
  protected void _drawAttributePlaceHolder(Graphics gr, int col, int row,
                                           int aAttr) {
    int lPos = toLinearPos(col, row);
    col = toColPos(lPos);
    row = toRowPos(lPos);

    int charW = getCharSize().width;
    int charH = getCharSize().height;

    gr.setColor(getBackground(0x20));
    gr.fillRect(col * charW, row * charH, charW, charH);

    if (XI5250Crt.DEBUG >= 2) { // to see them
      gr.setColor(getForeground(0x20));
      gr.drawString("#", col * charW, (row + 1) * charH);
    }
  }


  /**
   * Draws 5250 extra attribute (UNDERLINE and COLUMN_SEPARATOR).
   */
  protected void _drawExtraAttribute(Graphics gr, int col, int row, int len,
                                     int aAttr) {
    int extra = getExtraCharAttribute(aAttr);
    int dy = 2;          //!!0.95b

    int charW = getCharSize().width;
    int charH = getCharSize().height;

    gr.setColor(getForeground(aAttr));

    if ((extra & UNDERSCORE) != 0) {
      gr.drawLine(col * charW, (row + 1) * charH - dy - 1,
                  (col + len) * charW - 1, (row + 1) * charH - dy - 1);
    }

    if ((extra & COLUMN_SEPARATOR) != 0) {
      gr.setColor(getForeground(0x22));

      for (int i = 0; i < len; i++) {
        gr.drawLine((col + i) * charW, (row + 1) * charH - dy,
                    (col + i) * charW, (row + 1) * charH - dy);

        gr.drawLine((col + i + 1) * charW - 1, (row + 1) * charH - dy,
                    (col + i + 1) * charW - 1, (row + 1) * charH - dy);
      }
    }
  }


  /**
   * Splits string to detect attribute place-holder presence.
   */
  protected void _drawString(Graphics gr, String aStr, int col, int row,
                             int aAttr) {
    if (aStr.length() <= 0)
      return;

    int pos = -1;
    for (int i = 0; i < aStr.length(); i++) {
      if (aStr.charAt(i) == XI5250Crt.ATTRIBUTE_PLACE_HOLDER) {
        pos = i;
        break;
      }
    }

    if (pos == -1) {
      super._drawString(gr, aStr, col, row, aAttr);
      _drawExtraAttribute(gr, col, row, aStr.length(), aAttr);
    }
    else {
      // draw string portion before attribute place-holder
      if (pos > 0) {
        super._drawString(gr, aStr, col, row, aAttr);
        _drawExtraAttribute(gr, col, row, aStr.length(), aAttr);
      }
      // draw attribute place-holder
      _drawAttributePlaceHolder(gr, col + pos, row, aAttr);
      // draw string portion after attribute place-holder
      if (pos < (aStr.length() - 1))
        _drawString(gr, aStr.substring(pos + 1), col + pos + 1, row, aAttr);
    }
  }


  /**
   * Can be used to verify the presence of a string in the buffer.
   * Redefined to implement lines wrap.
   * @see    String#indexOf
   */
  public String getString(int col, int row, int nChars) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < nChars; i++) {
      int j = toLinearPos(col + i, row);
      str.append(getChar(toColPos(j), toRowPos(j)));
    }
    return new String(str);
  }


  /**
   * Attribute to color mapping.
   */
  protected Color getBackground(int aAttribute) {
    // see SA21-9247-6 pg 2-143
    int mapIdx = Math.min(ivBackgroundColorsMap.length - 1,
                          getColorMapIdx(aAttribute));
    return ivBackgroundColorsMap[mapIdx]
                                [getColorAttributeIdx(aAttribute) - 0x20];
  }


  /**
   * Attribute to color mapping.
   */
  protected Color getForeground(int aAttribute) {
    // see SA21-9247-6 pg 2-143
    int mapIdx = Math.min(ivForegroundColorsMap.length - 1,
                          getColorMapIdx(aAttribute));
    return ivForegroundColorsMap[mapIdx]
                                [getColorAttributeIdx(aAttribute) - 0x20];
  }


  /**
   * Attribute to extra char attribute mapping.
   */
  protected int getExtraCharAttribute(int aAttribute) {
    return EXTRA_ATTR_MAP[getColorAttributeIdx(aAttribute) - 0x20];
  }


  /**
   */
  protected final byte getColorMapIdx(int aAttribute) {
    return (byte)((aAttribute >> 24) & 0xFF);
  }


  /**
   */
  protected final int getColorAttributeIdx(int aAttribute) {
    return aAttribute & 0x00FFFFFF;
  }


  /**
   */
  public int getAttr(int col, int row) {
    int attr = super.getAttr(col, row);
    XI5250Crt crt = (XI5250Crt)getCrt();
    if (crt != null) {
      //!!1.14 change color table
      XI5250Field field = crt.getFieldFromPos(col, row);
      if (field != null && !field.isOrgBypassField())
        attr |= (1 << 24);
    }
    return attr;
  }


  /**
   * Converts x-y coord to buffer linear position.
   */
  public final int toLinearPos(int aCol, int aRow) {
    return (aRow * getCrtSize().width) + aCol;
  }


  /**
   * Converts buffer linear position to x-y coord.
   */
  public final int toColPos(int aPos) {
    return aPos % getCrtSize().width;
  }


  /**
   * Converts buffer linear position to x-y coord.
   */
  public final int toRowPos(int aPos) {
    return aPos / getCrtSize().width;
  }


  /**
   * Converts color to an intensity value (0 to 1000)
   * @see    #setDefBackground
   */
  public static final int colorAsIntensity(Color aColor) {
    float[] hsb = Color.RGBtoHSB(aColor.getRed(), aColor.getGreen(),
                                 aColor.getBlue(), null);
    return (int)(hsb[2] * 1000);
  }


  /**
   * Changes the default background color.
   * The new color intensity is used to choose which colors table must be used.
   */
  public void setDefBackground(Color aColor) {
    if (ivBackColor.equals(aColor))
      return;

    // check if background color intensity (referred to GRAY_INTENSITY)
    // has changed
    if ((colorAsIntensity(ivBackColor) >= GRAY_INTENSITY) !=
        (colorAsIntensity(aColor) >= GRAY_INTENSITY)) {
      boolean dark = (colorAsIntensity(aColor) < GRAY_INTENSITY);

      ivBackgroundColorsMap = (dark) ? DK_BACK_COLORS_MAP : LT_BACK_COLORS_MAP;
      ivForegroundColorsMap = (dark) ? DK_FORE_COLORS_MAP : LT_FORE_COLORS_MAP;
    }

    ivBackColor.setColor(aColor);
  }


  /**
   */
  public Color getDefBackground() {
    return ivBackColor.getColor();
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


  //////////////////////////////////////////////////////////////////////////////

  /**
   * Simply routes all method calls to the contained color methods
   */
  private static class ColorWrapper extends Color implements Serializable {

    private static final long serialVersionUID = 1L;

    private Color ivColor;


    /**
     */
    public ColorWrapper(Color aColor) {
      super(0);
      ivColor = aColor;
    }


    /**
     */
    public void setColor(Color aColor) {
      ivColor = aColor;
    }


    /**
     */
    public Color getColor() {
      return ivColor;
    }


    /**
     */
    public int getRed() {
      return ivColor.getRed();
    }

    public int getGreen() {
      return ivColor.getGreen();
    }

    public int getBlue() {
      return ivColor.getBlue();
    }

    public int getRGB() {
      return ivColor.getRGB();
    }

    public Color brighter() {
      return ivColor.brighter();
    }

    public Color darker() {
      return ivColor.darker();
    }

    public int hashCode() {
      return ivColor.hashCode();
    }

    public boolean equals(Object obj) {
      return ivColor.equals(obj);
    }

    public String toString() {
      return ivColor.toString();
    }
  }
}

