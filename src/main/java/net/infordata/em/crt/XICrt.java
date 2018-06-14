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
!!V 08/04/97 rel. 0.92a- new update method that avoid screen flashing.
    18/04/97 rel. 0.92b- setFreeze() method changed.
    14/05/97 rel. 1.00 - first release.
    23/05/97 rel. 1.00a- improved recalcFontSize() method.
    05/06/97 rel. 1.00c- reference cursor.
    08/07/97 rel. 1.01c- if the crt is freezed then the cursor is never drawed.
    14/07/97 rel. 1.02 - setCrtSize() method added.
    15/07/97 rel. 1.02c- finalize() method added.
    17/07/97 rel. 1.02e- revisited setFont().
    25/07/97 rel. 1.03a- revisited setFont().
    30/07/97 rel. 1.03b- bugs.
    06/07/97 rel. 1.03c- double-buffering.
    28/08/97 rel. 1.04 - bug in setFreeze().
    24/09/97 rel. 1.05 - DNCX project.
    14/01/98 rel. 1.06 - asynchronous paint on off-screen image.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    04/02/99 rel. 1.11 - Swing 1.1, bug in recalcFontSize() and jdk 1.2 support.
    11/06/99 rel. 1.12a- CursorShape interface has been introduced, some rework
             on cursor handling.
    29/07/99 rel. 1.14 - Rework on 3d look&feel.
    27/02/01 rel. _.__ - Rework on cursor handling.
 */

package net.infordata.em.crt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import net.infordata.em.util.XIUtil;

/**
 * Implements a generic monospaced-character panel. It uses an out off screen image buffer to
 * speed-up painting operations. Character coordinate are zero based.
 *
 * @author Valentino Proietti - Infordata S.p.A.
 * @see XICrtBuffer
 */
public class XICrt extends JComponent implements Serializable {

  private static final long serialVersionUID = 1L;

  // Debug level 0 = none, 1 = , 2 = detailed
  static final int DEBUG = 0;

  /**
   * Minimum accepted font size.
   *
   * @see #setFont
   */
  public static final int MIN_FONT_SIZE = 1;
  public static final int MAX_FONT_SIZE = 30;

  transient private VolatileImage ivImage;

  // The offscreen buffer.
  private XICrtBuffer ivCrtBuffer;

  // repaint is freezed
  transient private boolean ivFreeze = false;
  transient private int ivRepaintCount;
  transient private Rectangle ivSumRect;

  // cursor blinking thread
  transient private CursorBlinkingThread ivBlinkThread;

  // used to calculate minimum-size
  transient private int ivMinCharW;
  transient private int ivMinCharH;

  transient private boolean ivInitialized;

  private Font ivFont;

  transient private FontsCache ivFontsCache;

  public static final String CRT_SIZE = "crtSize";

  private transient Cursor ivCursor = new Cursor();

  private static final CursorShape cvDefaultCursorShape =
      new DefaultCursorShape();
  private static final CursorShape cvVoidCursorShape =
      new VoidCursorShape();

  public XICrt() {
    setFreeze(true);
    setOpaque(true);

    setFont(new Font("Monospaced", Font.PLAIN, MIN_FONT_SIZE));
    setLayout(null);
    setCrtBuffer(createCrtBuffer(80, 24));
    setBackground(Color.black);
  }

  /**
   * Factory method for XICrtBuffer creation.
   *
   * @param nCols number of rows for the buffer
   * @param nRows number of columns for the buffer
   * @return created buffer.
   * @see XICrtBuffer
   */
  protected XICrtBuffer createCrtBuffer(int nCols, int nRows) {
    return new XICrtBuffer(nCols, nRows);
  }

  /**
   * Changes the panel font. Only monospaced fonts are accepted.
   *
   * @param aFont font to set.
   */
  @Override
  public void setFont(Font aFont) {
    FontMetrics fontMetrics = getFontMetrics(aFont);
    if (fontMetrics.charWidth('W') != fontMetrics.charWidth('i')) {
      throw new IllegalArgumentException("Accept only monospaced font");
    }

    if (aFont.getSize() < MIN_FONT_SIZE) {
      throw new IllegalArgumentException("Font too small");
    }
    if (aFont.getSize() > MAX_FONT_SIZE) {
      throw new IllegalArgumentException("Font too great");
    }

    if (aFont.equals(ivFont)) {
      return;
    }

    if (ivFont == null ||
        !aFont.getName().equals(ivFont.getName()) ||
        aFont.getStyle() != ivFont.getStyle()) {
      ivFontsCache = new FontsCache(aFont);
    }

    ivFont = aFont;
    super.setFont(ivFont);

    if (ivInitialized) {
      initializeCrtBuffer();
    }
  }

  @Override
  public Font getFont() {
    return ivFont;
  }

  private Graphics initializeVolatileImage() {
    Font font = getFont();
    FontMetrics fontMetrics = getFontMetrics(font);

    int ww = fontMetrics.charWidth('W');
    int hh = fontMetrics.getHeight();
    ivImage = createVolatileImage(ivCrtBuffer.getCrtSize().width * ww,
        ivCrtBuffer.getCrtSize().height * hh);
    Graphics gr = ivImage.getGraphics();
    gr.setFont(font);

    fontMetrics = getFontMetrics(new Font(font.getName(), font.getStyle(), MIN_FONT_SIZE));
    ivMinCharW = fontMetrics.charWidth('W');
    ivMinCharH = fontMetrics.getHeight();
    return gr;
  }

  private void initializeCrtBuffer() {
    synchronized (this) {
      ivCrtBuffer.setGraphics(initializeVolatileImage());
      repaint();
    }
    // request parent layout recalc.
    super.invalidate();
  }

  /**
   * If initialized calls recalcFontSize().
   *
   * @see #recalcFontSize
   */
  @Override
  public void invalidate() {
    if (ivInitialized) {
      recalcFontSize();
    }

    super.invalidate();
  }

  /**
   * Redefines Panel.addNotify() to do some initializations when called for the first time.
   */
  @Override
  public void addNotify() {
    super.addNotify();

    if (!ivInitialized) {
      ivInitialized = true;
      initializeCrtBuffer();
      setFreeze(false);
    }
  }

  @Override
  public void removeNotify() {
    ivInitialized = false;
    setFreeze(true);
    super.removeNotify();
    ivCrtBuffer.setGraphics(null);
    setFont(new Font("Monospaced", Font.PLAIN, MIN_FONT_SIZE));
  }

  /**
   * Returns the preferred size based on the current font dimension.
   *
   * @return preferred size based on the current font dimension/
   */
  @Override
  public Dimension getPreferredSize() {
    return ivCrtBuffer.getSize();
  }

  /**
   * Returns the minimum size based on minimum size of the current font.
   *
   * @return minimum size based on minimum size of the current font.
   */
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(ivMinCharW * ivCrtBuffer.getCrtSize().width,
        ivMinCharH * ivCrtBuffer.getCrtSize().height);
  }

  /**
   * Redefined to handle the freeze attribute.
   *
   * @param tm dummy parameter
   * @param x the x value of the dirty region
   * @param y the y value of the dirty region
   * @param width the width of the dirty region
   * @param height the height of the dirty region
   * @see #setFreeze
   */
  @Override
  public void repaint(long tm, int x, int y, int width, int height) {
    if (!ivFreeze) {
      super.repaint(tm, x, y, width, height);
    } else {
      ++ivRepaintCount;
      ivSumRect = ivSumRect.union(new Rectangle(x, y, width, height));
    }
  }

  /**
   * Freezes the screen, can be used to group more than one repaint. Do not forget to restore freeze
   * to false when done.
   * <pre>
   * setFreeze(true);
   * try {
   *   ...      // repaints to be grouped
   * }
   * finally {
   *   setFreeze(false);
   * }
   * </pre>
   *
   * @param bb true to freez the screen, false to unfreezing the screen.
   */
  public synchronized void setFreeze(boolean bb) {
    if (bb == ivFreeze) {
      return;
    }

    ivFreeze = bb;

    if (!ivFreeze) {
      if (ivRepaintCount > 0) {
        repaint(ivSumRect.x, ivSumRect.y, ivSumRect.width, ivSumRect.height);
      }
      ivSumRect = null;
      ivRepaintCount = 0;

      ivCursor.resync();
    } else {
      ivSumRect = new Rectangle();
    }
  }

  public final boolean isFreeze() {
    return ivFreeze;
  }

  @Override
  public final void paint(Graphics g) {
    // remove cursor from screen
    // cannot check if the cursor intersects the clipping area, because the
    // cursor shape can exceed the cursor bounding rectangle (5250 reference
    // cursor
    ivCursor.beforePaint(g);
    try {
      super.paint(g);
    } finally {
      ivCursor.afterPaint(g);
    }
  }

  @Override
  protected void paintBorder(Graphics g) {
  }

  /**
   * Subclasses must redefine foregroundPaint to add painting.
   *
   * @param g graphic where to paint the component.
   * @see #foregroundPaint
   */
  @Override
  public synchronized void paintComponent(Graphics g) {
    if (ivImage != null) {
      GraphicsConfiguration gconf = getGraphicsConfiguration(); // acquires a tree-lock
      synchronized (ivCrtBuffer) {
        do {
          int returnCode = ivImage.validate(gconf);
          if (returnCode == VolatileImage.IMAGE_RESTORED) {
            // Contents need to be restored
            ivCrtBuffer.invalidateAll();
          } else if (returnCode == VolatileImage.IMAGE_INCOMPATIBLE) {
            // old vImg doesn't work with new GraphicsConfig; re-create it
            ivCrtBuffer.setGraphics(initializeVolatileImage());
            ivCrtBuffer.invalidateAll();
          }
          ivCrtBuffer.sync();
          g.drawImage(ivImage, 0, 0, null);
          if (true) {
            break;
          }
        } while (ivImage.contentsLost());
      }
    }

    if (isOpaque()) {
      Dimension crtBuf = getCrtBufferSize();
      Dimension crt = getSize();
      g.setColor(getBackground());
      g.fillRect(crtBuf.width, 0, crt.width - crtBuf.width, crt.height);
      g.fillRect(0, crtBuf.height, crt.width, crt.height - crtBuf.height);
    }

    foregroundPaint(g);

    if (DEBUG >= 2) {
      Rectangle rt = g.getClipBounds();
      g.setColor(Color.red);
      g.drawRect(rt.x, rt.y, rt.width - 1, rt.height - 1);
    }
  }

  /**
   * Subclasses must redefine this method to add paintings instead of paint().
   *
   * @param g graphics where to paint the foreground.
   */
  protected void foregroundPaint(Graphics g) {
    super.paintComponent(g);
  }

  /**
   * Clears the panel.
   */
  public void clear() {
    ivCrtBuffer.clear();
    repaint();
  }

  /**
   * Scrolls a portion of the panel.
   *
   * @param down true when scrolling down, false when scrolling up.
   * @param row1 starting row from where to scroll
   * @param row2 ending row from where to scroll
   * @param nLines number of lines to scroll
   */
  public void scroll(boolean down, int row1, int row2, int nLines) {
    if (down) {
      ivCrtBuffer.scrollDown(row1, row2, nLines);
    } else {
      ivCrtBuffer.scrollUp(row1, row2, nLines);
    }

    repaint(0, row1 * ivCrtBuffer.getCharSize().height,
        ivCrtBuffer.getSize().width,
        (row2 - row1) * ivCrtBuffer.getCharSize().height);
  }

  /**
   * Draws a string using the default attribute.
   *
   * @param str string to draw.
   * @param col column where to draw the string.
   * @param row row where to draw the string.
   */
  public void drawString(String str, int col, int row) {
    drawString(str, col, row, ivCrtBuffer.getDefAttr());
  }

  /**
   * Draws a string with the given attribute.
   *
   * @param str string to draw.
   * @param col column where to draw the string.
   * @param row row where to draw the string.
   * @param aAttr attribute of the string.
   */
  public void drawString(String str, int col, int row, int aAttr) {
    ivCrtBuffer.drawString(str, col, row, aAttr);
    repaint(col * ivCrtBuffer.getCharSize().width, row * ivCrtBuffer.getCharSize().height,
        str.length() * ivCrtBuffer.getCharSize().width, ivCrtBuffer.getCharSize().height);
  }

  /**
   * Can be used to verify the presence of a string in the buffer.
   *
   * @param col column from where to get the string
   * @param row  row from where to get the string
   * @param nChars number of chars to get from the given position
   * @return string with the given length from the given position
   *
   * @see String#indexOf
   */
  public String getString(int col, int row, int nChars) {
    return ivCrtBuffer.getString(col, row, nChars);
  }

  /**
   * Can be used to verify the presence of a string in the buffer.
   * @return gets the entire screen text.
   *
   * @see String#indexOf
   */
  public String getString() {
    return ivCrtBuffer.getString();
  }

  /**
   * Returns the attribute at the given position.
   * @param col column of the position to get the attribute from
   * @param row  row of the position to get the attribute from
   * @return attribute for the given position
   */
  public int getAttr(int col, int row) {
    return ivCrtBuffer.getAttr(col, row);
  }

  /**
   * Returns the character present at the given position.
   * @param col column of the position to get the character from
   * @param row  row of the position to get the character from
   * @return character at the given position
   */
  public char getChar(int col, int row) {
    return ivCrtBuffer.getChar(col, row);
  }

  /**
   * Sets the default attribute.
   *
   * @param aAttr default attribute to set
   */
  public void setDefAttr(int aAttr) {
    ivCrtBuffer.setDefAttr(aAttr);
  }

  /**
   * Forces the column coord in crt bounds
   *
   * @param aCol column position to force to bounds
   * @return the position within the crt bounds for the given column
   */
  protected final int assureColIn(int aCol) {
    return Math.max(0, Math.min(ivCrtBuffer.getCrtSize().width - 1, aCol));
  }

  /**
   * Forces the row coord in crt bounds
   * @param aRow row position to force to bounds
   * @return the position within the crt bounds for the given row
   */
  protected final int assureRowIn(int aRow) {
    return Math.max(0, Math.min(ivCrtBuffer.getCrtSize().height - 1, aRow));
  }

  /**
   * Moves the cursor at the give position.
   * @param aCol column to move the cursor to
   * @param aRow row to move the cursor to
   */
  public void setCursorPos(int aCol, int aRow) {
    ivCursor.setPosition(assureColIn(aCol), assureRowIn(aRow));
  }

  /**
   * Returns the cursor column position.
   * @return cursor column position.
   */
  public int getCursorCol() {
    return ivCursor.getCol();
  }

  /**
   * Returns the cursor row position.
   * @return cursor row position.
   */
  public int getCursorRow() {
    return ivCursor.getRow();
  }

  /**
   * Sets the dimensions in chars. The screen is cleared and the cursor is moved to (0, 0).
   *
   * @param nCols number of columns for the screen
   * @param nRows number of rows for the screen
   */
  public void setCrtSize(int nCols, int nRows) {
    synchronized (this) {
      Dimension dim = ivCrtBuffer.getCrtSize();

      if (dim.width == nCols && dim.height == nRows) {
        return;
      }

      XICrtBuffer newCrtBuffer = createCrtBuffer(nCols, nRows);
      setCrtBuffer(newCrtBuffer);

      if (ivInitialized) {
        ivCrtBuffer.setGraphics(ivImage.getGraphics());
      }
    }

    setCursorPos(0, 0);
    repaint();
    invalidate();

    firePropertyChange(CRT_SIZE, null, null);
  }

  /**
   * Returns the dimensions in chars.
   * @return dimensions in chars
   */
  public Dimension getCrtSize() {
    return ivCrtBuffer.getCrtSize();
  }

  /**
   * Sets the off-screen buffer.
   * @param aCrt off-screen buffer to set.
   */
  protected final void setCrtBuffer(XICrtBuffer aCrt) {
    if (aCrt == ivCrtBuffer) {
      return;
    }

    if (aCrt.getCrt() != null) {
      throw new IllegalArgumentException("Buffer already associated with a crt");
    }

    if (ivCrtBuffer != null) {
      ivCrtBuffer.setCrt(null);
    }
    ivCrtBuffer = aCrt;
    if (ivCrtBuffer != null) {
      ivCrtBuffer.setCrt(this);
    }
  }

  protected final XICrtBuffer getCrtBuffer() {
    return ivCrtBuffer;
  }

  /**
   * Returns the dimension in pixels of the off-screen buffer.
   * @return dimension in pixels of the off-screen buffer
   */
  public Dimension getCrtBufferSize() {
    return ivCrtBuffer.getSize();
  }

  /**
   * Returns the current char size in pixels.
   * @return current char size in pixels.
   */
  public Dimension getCharSize() {
    return ivCrtBuffer.getCharSize();
  }

  /**
   * Returns the minimum char size in pixels.
   * @return minimum char size in pixels.
   */
  public Dimension getMinCharSize() {
    return new Dimension(ivMinCharW, ivMinCharH);
  }

  /**
   * Returns the cursor bounding rectangle.
   * @return cursor bounding rectangle.
   */
  protected Rectangle getCursorRect() {
    return ivCursor.getBoundingRect();
  }

  public void setCursorVisible(boolean aFlag) {
    ivCursor.setVisible(aFlag);
  }

  public final boolean isCursorVisible() {
    return ivCursor.isVisible();
  }

  public synchronized void setBlinkingCursor(boolean flag) {
    if (flag == (ivBlinkThread != null)) {
      return;
    }

    if (flag) {
      ivBlinkThread = new CursorBlinkingThread();
      ivBlinkThread.setPriority(Thread.NORM_PRIORITY - 1);
      ivBlinkThread.start();
    } else {
      ivBlinkThread.terminate();
      ivBlinkThread = null;
    }

    ivCursor.resync();
  }

  public boolean isBlinkingCursor() {
    return ivBlinkThread != null;
  }

  /**
   * Used by recalcFontSize. Can be changed by subclasses to take care, for example, of the
   * status-bar presence.
   *
   * @param aFont font used to get size of characters
   * @return size of screen.
   * @see #recalcFontSize
   */
  protected Dimension getTestSize(Font aFont) {
    FontMetrics fm = getFontMetrics(aFont);
    return new Dimension(fm.charWidth('W') * getCrtSize().width,
        fm.getHeight() * getCrtSize().height);
  }

  /**
   * Found a font able to cover as much as possible of the off-screen buffer surface.
   */
  private void recalcFontSize() {
    Font font = getFont();
    Dimension size = getSize();

    int i;
    Font xFont = font;
    Font yFont = font;
    Font ft;

    int startIdx = 1;
    if (size.width < ivCrtBuffer.getSize().width) {
      for (i = xFont.getSize(); i >= MIN_FONT_SIZE; i -= 4) {
        ft = ivFontsCache.getFont(i);
        if (getTestSize(ft).width <= size.width) {
          xFont = ft;
          break;
        } else {
          xFont = ft;
        }
      }
      startIdx = 0;
    }
    // found max font (x dimension)
    // to speed up the search uses a two step algorythm
    for (int j = startIdx; j >= 0; j--) {
      for (i = xFont.getSize(); i <= 30; i += (1 + j * 3)) {
        ft = ivFontsCache.getFont(i);
        if (getTestSize(ft).width <= size.width) {
          xFont = ft;
        } else {
          break;
        }
      }
    }

    // found max font (y dimension)
    startIdx = 1;
    if (size.height < ivCrtBuffer.getSize().height) {
      for (i = yFont.getSize(); i >= MIN_FONT_SIZE; i -= 4) {
        ft = ivFontsCache.getFont(i);
        if (getTestSize(ft).height <= size.height) {
          yFont = ft;
          break;
        } else {
          yFont = ft;
        }
      }
      startIdx = 0;
    }
    for (int j = startIdx; j >= 0; j--) {
      for (i = yFont.getSize(); i <= 30; i += (1 + j * 3)) {
        ft = ivFontsCache.getFont(i);
        if (getTestSize(ft).height <= size.height) {
          yFont = ft;
        } else {
          break;
        }
      }
    }

    // try to use the smaller one
    setFont(ivFontsCache.getFont(Math.min(xFont.getSize(), yFont.getSize())));
  }

  /**
   * From char coords to point coords.
   *
   * @param aCol column to get the point from
   * @param aRow row to get the point from
   * @return point for the given position
   */
  public final Point toPoints(int aCol, int aRow) {
    return ivCrtBuffer.toPoints(aCol, aRow);
  }

  /**
   * From char coords to point coords.
   * @param aCol column to get the rectangle from
   * @param aRow row to get the rectangle from
   * @param aNCols number of columns for the rectangle
   * @param aNRows number of rows for the rectangle
   * @return rectangle for the given position, rows and columns
   */
  public final Rectangle toPoints(int aCol, int aRow, int aNCols, int aNRows) {
    return ivCrtBuffer.toPoints(aCol, aRow, aNCols, aNRows);
  }

  @Override
  protected void finalize() throws Throwable {
    setBlinkingCursor(false);
    super.finalize();
  }

  void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }

  void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
  }

  protected CursorShape getCursorShape() {
    return cvDefaultCursorShape;
  }

  protected CursorShape getFixedCursorShape() {
    return null;
  }

  /*
   * Used only for test purposes.
   */
  public static void main(String[] argv) {
    JFrame frm = new JFrame();
    XICrt crt = new XICrt();

    crt.drawString("eccome" + '!', 0, 1);

    crt.setBackground(Color.yellow);

    frm.getContentPane().add(crt);
    frm.setBounds(0, 0, 600, 500);

    frm.setVisible(true);

    crt.setCursorVisible(true);
    crt.drawString("CIAO", 0, 0);
    crt.drawString("X", 79, 23);

    System.out.println(crt.ivCrtBuffer.getChar(79, 23));

    try {
      Thread.sleep(5000);
    } catch (Exception ex) {
    }
    crt.drawString("ARICIAO", 20, 0);

    System.out.println("end.");
  }

  private class FontsCache {

    private Font[] ivFonts = new Font[MAX_FONT_SIZE - MIN_FONT_SIZE + 1];
    private Font ivFont;


    public FontsCache(Font font) {
      ivFont = font;
    }


    public Font getFont(int size) {
      if (ivFonts[size - MIN_FONT_SIZE] == null) {
        ivFonts[size - MIN_FONT_SIZE] = new Font(ivFont.getName(),
            ivFont.getStyle(),
            size);
      }
      return ivFonts[size - MIN_FONT_SIZE];
    }
  }

  private class CursorBlinkingThread extends Thread {

    private volatile boolean ivTerminate = false;

    public CursorBlinkingThread() {
      super("XICrt cursor blinking thread");
    }


    public void terminate() {
      ivTerminate = true;
      interrupt();
    }

    @Override
    public void run() {
      while (!ivTerminate) {
        if (isCursorVisible()) {
          ivCursor.blink();
        }

        try {
          Thread.sleep(700);
        } catch (Exception ex) {
        }
      }
    }
  }

  /**
   * A cursor shape cannot change its shape, but you can switch to a different cursor shape.
   */
  public interface CursorShape {

    void drawCursorShape(Graphics gc, Rectangle rt);

  }

  public static class VoidCursorShape implements CursorShape {

    public void drawCursorShape(Graphics gc, Rectangle rt) {
    }

  }

  public static class DefaultCursorShape implements CursorShape {

    public void drawCursorShape(Graphics gc, Rectangle rt) {
      gc.setColor(Color.white);
      gc.setXORMode(Color.black);
      gc.fillRect(rt.x, rt.y, rt.width, rt.height);
      gc.setPaintMode();
    }

  }

  private class Cursor implements Serializable {

    private static final long serialVersionUID = 1L;

    transient private List<CursorPlaceHolder> ivCursorsPH = new ArrayList<>(10);

    private CursorPlaceHolder ivCurrentCursorPH = new CursorPlaceHolder(0, 0);

    private boolean ivVisible = true;

    transient private int ivPending = 0;
    transient private int ivPendingBlink = 0;

    transient private Runnable ivPendingEvent = () -> {
      ivPending = 0;
      sync(false, null, true);
    };

    transient private Runnable ivPendingBlinkEvent = new Runnable() {
      private boolean flag;

      public void run() {
        ivPendingBlink = 0;
        flag = !flag;
        sync(true, null, flag);
      }
    };

    public void resync() {
      if (SwingUtilities.isEventDispatchThread()) {
        ivPendingEvent.run();
      } else if ((ivPending++) == 0) {
        SwingUtilities.invokeLater(ivPendingEvent);
      }
    }

    public void blink() {
      if (SwingUtilities.isEventDispatchThread()) {
        ivPendingBlinkEvent.run();
      } else if ((ivPendingBlink++) == 0) {
        SwingUtilities.invokeLater(ivPendingBlinkEvent);
      }
    }

    public void setPosition(int col, int row) {
      synchronized (ivCursorsPH) {
        if (col == getCol() && row == getRow()) {
          return;
        }
        ivCursorsPH.add(ivCurrentCursorPH);
        ivCurrentCursorPH = new CursorPlaceHolder(col, row);
      }
      resync();
    }

    public void setVisible(boolean flag) {
      synchronized (ivCursorsPH) {
        if (flag == ivVisible) {
          return;
        }
        ivVisible = flag;
        ivCursorsPH.add(ivCurrentCursorPH);
        ivCurrentCursorPH = new CursorPlaceHolder(getCol(), getRow());
      }
      resync();
    }

    public final int getCol() {
      return ivCurrentCursorPH.getCol();
    }

    public final int getRow() {
      return ivCurrentCursorPH.getRow();
    }

    public final boolean isVisible() {
      return ivVisible;
    }

    public Rectangle getBoundingRect() {
      return ivCurrentCursorPH.getBoundingRect();
    }

    private void sync(boolean blinkingShapeOnly, Graphics aGc, boolean showIt) {
      if (DEBUG >= 1) {
        if (!SwingUtilities.isEventDispatchThread()) {
          throw new IllegalStateException();
        }
      }
      synchronized (ivCursorsPH) {
        Graphics gc = (aGc != null) ? aGc :
            (XIUtil.is1dot2 && !XIUtil.is1dot3) ? null : getGraphics();
        try {
          // remove old place-holders
          for (Iterator<CursorPlaceHolder> e = ivCursorsPH.iterator(); e.hasNext(); ) {
            e.next().drawShapes(false, gc, false);
          }
          ivCursorsPH.clear();
          // draw (or hide) the current one
          ivCurrentCursorPH.drawShapes(blinkingShapeOnly, gc, showIt);
        } finally {
          if (aGc == null && gc != null) {
            gc.dispose();
          }
        }
      }
    }

    protected void beforePaint(Graphics g) {
      // do nothing, cursor place-holders are removed in painting areas
    }

    protected void afterPaint(Graphics g) {
      // restore cursor place-holders in painting areas, if they were visible
      if (DEBUG >= 1) {
        if (!SwingUtilities.isEventDispatchThread()) {
          throw new IllegalStateException();
        }
      }
      // remove old place-holders
      for (Iterator<CursorPlaceHolder> e = ivCursorsPH.iterator(); e.hasNext(); ) {
        e.next().syncShapesAfterPaint(g);
      }
      ivCurrentCursorPH.syncShapesAfterPaint(g);
    }
  }

  private class CursorPlaceHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private int ivCol;
    private int ivRow;

    transient private CursorShape ivCursorShape;
    transient private CursorShape ivFixedCursorShape;

    transient private boolean ivFixedCursorDrawed;
    transient private boolean ivCursorDrawed;

    public CursorPlaceHolder(int col, int row) {
      ivCol = col;
      ivRow = row;
    }

    public final int getCol() {
      return ivCol;
    }

    public final int getRow() {
      return ivRow;
    }

    public Rectangle getBoundingRect() {
      Dimension sz = ivCrtBuffer.getCharSize();
      Point pt = ivCrtBuffer.toPoint(ivCol, ivRow);
      return new Rectangle(pt.x, pt.y - sz.height, sz.width, sz.height);
    }

    private void retrieveShapes() {
      // a cursorPlaceHolder is bounded always to the same shape
      if (ivCursorShape == null) {
        ivCursorShape = getCursorShape();
        if (ivCursorShape == null) {
          ivCursorShape = cvVoidCursorShape;
        }
      }
      if (ivFixedCursorShape == null) {
        ivFixedCursorShape = getFixedCursorShape();
        if (ivFixedCursorShape == null) {
          ivFixedCursorShape = cvVoidCursorShape;
        }
      }
    }

    /**
     * Draws the cursor calling drawCursorShape and drawFixedCursorShape.
     *
     * @param blinkingShapeOnly true to only draw the blinking shape, false to draw entire cursor.
     * @param aGc graphics where to draw
     * @param showIt true to show the cursor, false to hide it.
     */
    public void drawShapes(boolean blinkingShapeOnly, Graphics aGc,
        boolean showIt) {
      showIt = showIt && isCursorVisible();
      if (showIt == ivCursorDrawed && showIt == ivFixedCursorDrawed) {
        return;
      }

      // a cursorPlaceHolder is bounded always to the same shape
      retrieveShapes();

      Graphics gc = (aGc != null) ? aGc : getGraphics();
      if (gc == null) {
        return;
      }
      try {
        if (ivCursorDrawed != showIt) {
          ivCursorShape.drawCursorShape(gc, getBoundingRect());
          ivCursorDrawed = showIt;
        }

        if (ivFixedCursorDrawed != showIt && !blinkingShapeOnly) {
          // workaround since there are problems with jdk 1.2.1, lines drawn outside the paint
          // method are translated of 1 pixel respect lines drawed inside the
          // paint method !!??
          if (XIUtil.is1dot2 && !XIUtil.is1dot3 && aGc == null) {
            gc.translate(-1, -1);
          }
          ivFixedCursorShape.drawCursorShape(gc, getBoundingRect());
          ivFixedCursorDrawed = showIt;
        }
      } finally {
        if (aGc == null) {
          gc.dispose();
        }
      }
    }

    /**
     * Must be called always in the paint event.
     *
     * @param gc graphics where to draw cursor shape.
     */
    public void syncShapesAfterPaint(Graphics gc) {
      if (gc == null) {
        throw new IllegalArgumentException();
      }

      // a cursorPlaceHolder is bounded always to the same shape
      retrieveShapes();

      if (ivCursorDrawed) {
        ivCursorShape.drawCursorShape(gc, getBoundingRect());
      }

      if (ivFixedCursorDrawed) {
        ivFixedCursorShape.drawCursorShape(gc, getBoundingRect());
      }
    }

  }

}
