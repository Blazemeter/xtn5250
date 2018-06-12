package net.infordata.em;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Optional;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalClient {

  private static final Logger LOG = LoggerFactory.getLogger(TerminalClient.class);

  private XI5250Emulator em;

  private String terminalType = "IBM-3179-2";

  /**
   * Sets the type of terminal to emulate.
   *
   * @param terminalType the type of terminal to emulate. Currently known values are IBM-3179-2 and
   * IBM-3477-FC. If none is set, then IBM-3179-2 is used.
   */
  public void setTerminalType(String terminalType) {
    this.terminalType = terminalType;
  }

  /**
   * Connect to a terminal server.
   *
   * @param host host name of the terminal server.
   * @param port port where the terminal server is listening for connections.
   */
  public void connect(String host, int port) {
    em = new XI5250Emulator();
    em.setHost(host);
    em.setPort(port);
    em.setTerminalType(terminalType);
    em.setActive(true);
  }

  /**
   * Set the text of a field in the screen.
   *
   * @param row row number where to set the field text. First row is 1.
   * @param column column number where to set the field text. First column is 1.
   * @param text the text to set on the field.
   */
  public void setFieldText(int row, int column, String text) {
    XI5250Field field = em.getFieldFromPos(column - 1, row - 1);
    if (field == null) {
      throw new IllegalArgumentException("Invalid field position " + row + "," + column);
    }
    field.setString(text);
    em.setCursorPos((column - 1 + text.length()) % em.getCrtSize().width,
        row - 1 + (column - 1 + text.length()) / em.getCrtSize().width);
  }

  /**
   * Send a key event to terminal server.
   *
   * This method is usually used to send Enter after setting text fields, or to send some other keys
   * (like F1).
   *
   * @param keyCode Key code to send. For example {@link java.awt.event.KeyEvent#VK_ENTER}.
   * @param modifiers modifiers to apply to the key code. For example {@link
   * java.awt.event.KeyEvent#SHIFT_MASK}.
   */
  public void sendKeyEvent(int keyCode, int modifiers) {
    em.processRawKeyEvent(
        new KeyEvent(em, KeyEvent.KEY_PRESSED, 0, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED));
  }

  /**
   * Gets the screen text.
   *
   * @return The screen text with newlines separating each row.
   */
  public String getScreenText() {
    int height = em.getCrtSize().height;
    int width = em.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(em.getString(0, i, width).replaceAll("[\\x00-\\x19]", " "));
      screen.append("\n");
    }
    return screen.toString();
  }

  /**
   * Get the screen dimensions of the terminal emulator screen.
   *
   * @return Allows getting the number of rows and columns used by the terminal emulator.
   */
  public Dimension getScreenDimensions() {
    return em.getCrtSize();
  }

  /**
   * Allows checking if keyboard has been locked (no input can be sent) by the terminal server.
   *
   * @return True if the keyboard is currently locked, false otherwise.
   */
  public boolean isKeyboardLocked() {
    int state = em.getState();
    switch (state) {
      case XI5250Emulator.ST_NULL:
      case XI5250Emulator.ST_TEMPORARY_LOCK:
      case XI5250Emulator.ST_NORMAL_LOCKED:
      case XI5250Emulator.ST_POWER_ON:
      case XI5250Emulator.ST_POWERED:
        return true;
      case XI5250Emulator.ST_HARDWARE_ERROR:
      case XI5250Emulator.ST_POST_HELP:
      case XI5250Emulator.ST_PRE_HELP:
      case XI5250Emulator.ST_SS_MESSAGE:
      case XI5250Emulator.ST_SYSTEM_REQUEST:
      case XI5250Emulator.ST_NORMAL_UNLOCKED:
        return false;
      default:
        LOG.warn("Unexpected state: {}", state);
        return false;
    }
  }

  /**
   * Get the position of the cursor in the screen.
   *
   * @return The position of the cursor in the screen (x contains the column and y the row). If the
   * cursor is not visible then empty value is returned.
   */
  public Optional<Point> getCursorPosition() {
    return em.isCursorVisible() ? Optional
        .of(new Point(em.getCursorCol() + 1, em.getCursorRow() + 1)) : Optional.empty();
  }

  /**
   * Add a {@link XI5250EmulatorListener} to the terminal emulator.
   *
   * @param listener listener to be notified on any event (state change, new screen, etc) is
   * triggered by the terminal emulator.
   */
  public void addEmulatorListener(XI5250EmulatorListener listener) {
    em.addEmulatorListener(listener);
  }

  /**
   * Remove a {@link XI5250EmulatorListener} from the terminal emulator.
   *
   * @param listener listener to be remove from notificaitons.
   */
  public void removeEmulatorListener(XI5250EmulatorListener listener) {
    em.removeEmulatorListener(listener);
  }

  /**
   * Disconnect the terminal emulator from the server.
   *
   * @throws InterruptedException thrown when the disconnect is interrupted.
   */
  protected void disconnect() {
    em.setActive(false);
  }

}
