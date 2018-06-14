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
    10/04/97 rel. 0.93 - chars less than space are not drawed but are stored in ivCharBuffer.
    28/04/97 rel. _.__ - ivCharBuffer, ivAttrBuffer array from [cols][rows] to [rows][cols].
    14/05/97 rel. 1.00 - first release.
    30/07/97 rel. 1.03b- bugs.
    13/01/98 rel. 1.05d- NT painting bug.
    14/01/98 rel. 1.06 - asynchronous paint on off-screen image.
    03/03/98 rel. _.___- SWING and reorganization.
 ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    29/07/99 rel. 1.14 - Rework on 3d look&feel.
 */

package net.infordata.em.crt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implements an off-screen image buffer.
 * To be used by XICrt.
 *
 * @see    XICrt
 *
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XICrtBuffer implements Serializable {

  private static final long serialVersionUID = 1L;

  private int      ivNCols;
  private int      ivNRows;

  transient private Graphics ivGr;
  transient private int      ivCharW;
  transient private int      ivCharH;
  transient private int      ivCharD;
  transient private int      ivGrW;
  transient private int      ivGrH;

  private int      ivDefAttr;     // default char attribute

  // [rows][cols]
  private char[][] ivCharBuffer;
  private int[][]  ivAttrBuffer;

  transient private List<Rectangle> ivDirtyAreas = new ArrayList<>(20);

  transient private XICrt  ivCrt;

  /**
   * Creates a XICrtBuffer with the given dimensions expressed in number of
   * chars.
   *
   * @param nCols number of columns for the buffer.
   * @param nRows number of rows for the buffer.
   */
  public XICrtBuffer(int nCols, int nRows) {
    ivNCols = nCols;
    ivNRows = nRows;
    ivCharBuffer = new char[ivNRows][ivNCols];
    ivAttrBuffer = new int[ivNRows][ivNCols];

    clear();
  }

  /**
   * Creates a XICrtBuffer filling it with a portion of another one.
   *
   * @param from buffer from where to create the new buffer
   * @param aC column from where to extract from the given buffer.
   * @param aR row from where to extract from the given buffer.
   * @param aW number of columns to extract from the given buffer.
   * @param aH number of rows to extract from the given buffer.
   */
  public XICrtBuffer(XICrtBuffer from, int aC, int aR, int aW, int aH) {
    this(aW, aH);
    copyFrom(0, 0, from, aC, aR, aW, aH);
    setDefAttr(from.getDefAttr());
  }

  /**
   * Returns a cloned XICrtBuffer (the new one needs initGraphics() to be
   * displayed).
   *
   * @return cloned XICrtBuffer.
   */
  @Override
  public Object clone() {
    return new XICrtBuffer(this, 0, 0, ivNCols, ivNRows);
  }

  final void setCrt(XICrt crt) {
    ivCrt = crt;
  }

  public final XICrt getCrt() {
    return ivCrt;
  }

  /**
   * Initializes the graphics area.
   *
   * @param gr graphics to use.
   */
  public synchronized void setGraphics(Graphics gr) {
    ivGr = gr;
    if (ivGr != null) {
      FontMetrics fontMetrics = ivGr.getFontMetrics();
      ivCharW = fontMetrics.charWidth('W');
      ivCharH = fontMetrics.getHeight();
      ivCharD = fontMetrics.getDescent();
      ivGrW = ivNCols * ivCharW;
      ivGrH = ivNRows * ivCharH;

      copyFrom(this);   // refresh required
    }
    else {
      ivCharW = 1;    // to avoid possible division by 0 in pending events
      ivCharH = 1;
      ivCharD = 1;
      ivGrW = ivNCols * ivCharW;
      ivGrH = ivNRows * ivCharH;
    }
  }

  /**
   * Dumps the buffer on print stream.
   * Useful for debugging.
   *
   * @param out stream where to dump the buffer
   */
  public void dumpBuffer(PrintStream out) {
    out.println("BUFFER DUMP");
    for (int r = 0; r < ivNRows; r++) {
      for (int c = 0; c < ivNCols; c++)
        out.print(ivCharBuffer[r][c]);
      out.println();
    }
    for (int r = 0; r < ivNRows; r++) {
      for (int c = 0; c < ivNCols; c++)
        out.print(Integer.toHexString(ivAttrBuffer[r][c]) + " ");
      out.println();
    }
    out.println("END BUFFER DUMP");
  }

  /**
   * Copy contents from another XICrtBuffer (can be itself).
   *
   * @param from buffer to copy content from.
   */
  public void copyFrom(XICrtBuffer from) {
    copyFrom(0, 0, from, 0, 0, from.ivNCols, from.ivNRows);
  }

  /**
   * Copy contents from another XICrtBuffer (can be itself).
   * @param    col     the destination column pos.
   * @param    row     the destination row pos.
   * @param    from    the source XICrtBuffer.
   * @param    aC      the source column pos.
   * @param    aR      the source row pos.
   * @param    aW      the source dimension.
   * @param    aH      the source dimension.
   */
  public synchronized void copyFrom(int col, int row,
      XICrtBuffer from,
      int aC, int aR, int aW, int aH) {
    aW = Math.min(aW, from.ivNCols - aC);
    aH = Math.min(aH, from.ivNRows - aR);
    int          nCols = Math.min(ivNCols - col, aW);
    int          nRows = Math.min(ivNRows - row, aH);
    int          lastAttr;
    int          lastCol;
    int          lastRow;
    StringBuilder str;

    for (int r = 0; r < nRows; r++) {
      for (int c = 0; c < nCols; ) {
        lastCol = col + c;
        lastRow = row + r;
        lastAttr = from.getAttrInternal(aC + c, aR + r);
        str = new StringBuilder();

        // group chars with the same attribute
        for ( ; c < nCols && lastAttr == from.getAttrInternal(aC + c, aR + r); c++)
          str.append(from.getChar(aC + c, aR + r));

        drawString(new String(str), lastCol, lastRow, lastAttr);
      }
    }
  }

  public synchronized void invalidateAll() {
    addDirtyArea(new Rectangle(0, 0, ivNCols, ivNRows));
  }

  /**
   * Clears the screen buffer.
   */
  public synchronized void clear() {
    for (int c = 0; c < ivNCols; c++)
      for (int r = 0; r < ivNRows; r++) {
        ivCharBuffer[r][c] = '\u0000';
        ivAttrBuffer[r][c] = ivDefAttr;
      }

    ivDirtyAreas.clear();

    if (ivGr != null) {
      Graphics gr = ivGr.create();
      try {
        gr.setColor(getBackground(ivDefAttr));
        gr.fillRect(0, 0, ivGrW, ivGrH);
      }
      finally {
        gr.dispose();
      }
    }
  }

  public synchronized void scrollDown(int r1, int r2, int nRows) {
    if ((r1 >= r2) || (nRows == 0))
      throw new IllegalArgumentException("ScrollDown()");

    sync();

    nRows = Math.max(nRows, r2 - r1);

    for (int r = r2; r > (r1 + nRows); r--)
      for (int c = 0; c < ivNCols; c++) {
        ivCharBuffer[r][c] = ivCharBuffer[r - nRows][c];
        ivAttrBuffer[r][c] = ivAttrBuffer[r - nRows][c];
      }

    for (int r = r1; r <= (r1 + nRows); r++)
      for (int c = 0; c < ivNCols; c++) {
        ivCharBuffer[r][c] = '\u0000';
        ivAttrBuffer[r][c] = ivDefAttr;
      }

    if (ivGr != null) {
      Graphics gr = ivGr.create();
      try {
        gr.copyArea(0, r1 * ivCharH, ivNCols * ivCharW, (r2 - r1) * ivCharH,
            0, ivCharH * nRows);
        gr.setColor(getBackground(ivDefAttr));
        gr.fillRect(0, r1 * ivCharH, ivNCols * ivCharW, ivCharH * nRows);
      }
      finally {
        gr.dispose();
      }
    }
  }

  public synchronized void scrollUp(int r1, int r2, int nRows) {
    if ((r1 >= r2) || (nRows == 0))
      throw new IllegalArgumentException("ScrollUp()");

    sync();

    nRows = Math.max(nRows, r2 - r1);

    for (int r = r1; r < (r2 - nRows); r++)
      for (int c = 0; c < ivNCols; c++) {
        ivCharBuffer[r][c] = ivCharBuffer[r + nRows][c];
        ivAttrBuffer[r][c] = ivAttrBuffer[r + nRows][c];
      }

    for (int r = r2; r >= (r2 - nRows); r--)
      for (int c = 0; c < ivNCols; c++) {
        ivCharBuffer[r][c] = '\u0000';
        ivAttrBuffer[r][c] = ivDefAttr;
      }

    if (ivGr != null) {
      Graphics gr = ivGr.create();
      try {
        gr.copyArea(0, (r1 + 1) * ivCharH, ivNCols * ivCharW, (r2 - r1) * ivCharH,
            0, -ivCharH * nRows);
        gr.setColor(getBackground(ivDefAttr));
        gr.fillRect(0, (r2 - nRows + 1) * ivCharH, ivNCols * ivCharW, ivCharH * nRows);
      }
      finally {
        gr.dispose();
      }
    }
  }

  /**
   * Returns the dimensions in chars.
   *
   * @return dimensions in chars.
   */
  public Dimension getCrtSize() {
    return new Dimension(ivNCols, ivNRows);
  }

  /**
   * Returns the dimensions in pixels.
   *
   * @return dimensions in pixels.
   */
  public Dimension getSize() {
    return new Dimension(ivGrW, ivGrH);
  }

  public Dimension getCharSize() {
    return new Dimension(ivCharW, ivCharH);
  }

  /**
   * Converts characters coordinates in pixels coordinates.
   *
   * @param col column of the position to convert to pixel coordinates.
   * @param row row of the position to convert to pixel coordinates.
   * @return pixel coordinates for the given position.
   */
  public Point toPoint(int col, int row) {
    return new Point(col * ivCharW, (row + 1) * ivCharH);
  }

  /**
   * Uses the default attribute.
   *
   * @param str string to print
   * @param col column where to print the string
   * @param row row where to print the string
   */
  public void drawString(String str, int col, int row) {
    drawString(str, col, row, ivDefAttr);
  }

  /**
   * Uses the given attribute.
   *
   * @param aStr string to print
   * @param col column where to print the string
   * @param row row where to print the string
   * @param aAttr attribute of the string
   */
  public synchronized void drawString(String aStr, int col, int row,
      int aAttr) {
    col = Math.max(0, Math.min(ivNCols - 1, col));
    row = Math.max(0, Math.min(ivNRows - 1, row));
    int len = Math.min(aStr.length(), ivNCols - col);

    if (len <= 0)
      return;

    for (int i = 0; i < len; i++) {
      ivCharBuffer[row][col + i] = aStr.charAt(i);
      ivAttrBuffer[row][col + i] = aAttr;
    }

    addDirtyArea(new Rectangle(col, row, len, 1));
  }

  private void addDirtyArea(Rectangle newRt) {
    Rectangle rt;
    Rectangle rtG;
    Rectangle res = new Rectangle(newRt);
    int       count = 0;
    for (Iterator<Rectangle> e = ivDirtyAreas.iterator(); e.hasNext(); ) {
      rt = e.next();
      // used to see if rectangles are adiacent (x coord)
      rtG = new Rectangle(rt);
      rtG.grow(1, 0);
      if (rtG.intersects(newRt)) {
        e.remove();
        res = res.union(rt);
        // no more than two rects can be joined (x coord)
        if (++count >= 2)
          break;
      }
    }
    ivDirtyAreas.add(res);
  }

  /**
   * To be called just before painting the offscreen image.
   */
  public synchronized void sync() {
    if (ivGr == null || ivDirtyAreas.isEmpty())
      return;

    Graphics gr = ivGr.create();
    try {
      for (Rectangle rt : ivDirtyAreas) {
        int          lastAttr;
        int          lastCol;
        int          lastRow;
        StringBuilder str;

        for (int r = 0; r < rt.height; r++) {
          for (int c = 0; c < rt.width; ) {
            lastCol = rt.x + c;
            lastRow = rt.y + r;
            lastAttr = getAttr(rt.x + c, rt.y + r);
            str = new StringBuilder();

            // group char with the same attribute to speed up drawing
            for ( ; c < rt.width && lastAttr == getAttr(rt.x + c, rt.y + r); c++)
              str.append(getChar(rt.x + c, rt.y + r));

            _drawString(gr, new String(str), lastCol, lastRow, lastAttr);
          }
        }

        if (XICrt.DEBUG >= 2) {
          gr.setColor(Color.yellow);
          gr.drawRect(rt.x * ivCharW + 1, rt.y * ivCharH + 1,
              rt.width * ivCharW - 3, rt.height * ivCharH - 3);
        }
      }

      ivDirtyAreas.clear();
    }
    finally {
      gr.dispose();
    }
  }

  /**
   * Draws the given string on the graphics context (called by sync()).
   *
   * @param gr graphics where to draw the string
   * @param aStr string to print
   * @param col column where to print the string
   * @param row row where to print the string
   * @param aAttr attribute of the string
   */
  protected void _drawString(Graphics gr, String aStr, int col, int row,
      int aAttr) {
    int len = aStr.length();

    gr.setColor(getBackground(aAttr));
    gr.fillRect(col * ivCharW, row * ivCharH, ivCharW * len, ivCharH);

    // do not draw char less than space
    StringBuilder strBuf = new StringBuilder(aStr);
    for (int i = 0; i < strBuf.length(); i++) {
      if (strBuf.charAt(i) < ' ')
        strBuf.setCharAt(i, ' ');
    }
    String str = new String(strBuf);

    gr.setColor(getForeground(aAttr));
    gr.drawString(str.substring(0, len), col * ivCharW,
        (row + 1) * ivCharH - ivCharD);
  }

  public String getString(int col, int row, int nChars) {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < nChars; i++) {
      str.append(getChar(col + i, row));
    }
    return new String(str);
  }

  public String getString() {
    char[] buf = new char[ivNRows * ivNCols];

    for (int i = 0; i < ivNRows; i++)
      System.arraycopy(ivCharBuffer[i], 0, buf, i * ivNCols, ivNCols);

    return new String(buf);
  }

  /**
   * Background attribute to color mapping.
   *
   * @param aAttribute attribute from where to get the color.
   * @return background color.
   */
  protected Color getBackground(int aAttribute){
    return Color.black;
  }

  /**
   * Foreground attribute to color mapping.
   *
   * @param aAttribute attribute from where to get the color.
   * @return foreground color.
   */
  protected Color getForeground(int aAttribute) {
    return Color.green;
  }

  public final int getAttrInternal(int col, int row) {
    col = Math.max(0, Math.min(ivNCols - 1, col));
    row = Math.max(0, Math.min(ivNRows - 1, row));

    return ivAttrBuffer[row][col];
  }

  public int getAttr(int col, int row) {
    return getAttrInternal(col, row);
  }

  public final char getChar(int col, int row) {
    col = Math.max(0, Math.min(ivNCols - 1, col));
    row = Math.max(0, Math.min(ivNRows - 1, row));

    return ivCharBuffer[row][col];
  }

  /**
   * Sets the default attribute.
   *
   * @param aAttr default attribute to set
   */
  public void setDefAttr(int aAttr) {
    ivDefAttr = aAttr;
  }

  /**
   * Returns the default attribute.
   *
   * @return default attribute.
   */
  public final int getDefAttr() {
    return ivDefAttr;
  }

  public Point toPoints(int aCol, int aRow) {
    return new Point(aCol * ivCharW, aRow * ivCharH);
  }

  public Rectangle toPoints(int aCol, int aRow, int aNCols, int aNRows) {
    return new Rectangle(aCol * ivCharW, aRow * ivCharH,
        aNCols * ivCharW, aNRows * ivCharH);
  }

  void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }

  void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
  }

}
