package net.infordata.em;

import com.google.common.annotations.VisibleForTesting;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.net.SocketFactory;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerminalClient {

  private static final Logger LOG = LoggerFactory.getLogger(TerminalClient.class);

  private TerminalClientEmulator emulator = new TerminalClientEmulator();

  /**
   * Sets the type of terminal to emulate.
   *
   * @param terminalType the type of terminal to emulate. Currently known values are IBM-3179-2 and
   * IBM-3477-FC. If none is set, then IBM-3179-2 is used.
   */
  public void setTerminalType(String terminalType) {
    emulator.setTerminalType(terminalType);
  }

  /**
   * Sets a class to handle general exception handler.
   *
   * @param exceptionHandler a class to handle exceptions. If none is provided then exceptions stack
   * trace will be printed to error output.
   */
  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    emulator.setExceptionHandler(exceptionHandler);
  }

  /**
   * Allows setting the {@link SocketFactory} to be used to create sockets which allows using SSL
   * sockets.
   *
   * @param socketFactory the {@link SocketFactory} to use. If non is specified {@link
   * SocketFactory#getDefault()} will be used.
   */
  public void setSocketFactory(SocketFactory socketFactory) {
    emulator.setSocketFactory(socketFactory);
  }

  /**
   * Sets the timeout for the socket connection.
   *
   * @param connectionTimeoutMillis Number of millis to wait for a connection to be established
   * before it fails. If not specified no timeout (same as 0 value) will be applied.
   */
  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    emulator.setConnectionTimeoutMillis(connectionTimeoutMillis);
  }
  
  /**
   * Connect to a terminal server.
   *
   * @param host host name of the terminal server.
   * @param port port where the terminal server is listening for connections.
   */
  public void connect(String host, int port) {
    emulator.setHost(host);
    emulator.setPort(port);
    emulator.setActive(true);
  }

  /**
   * Set the text of a field in the screen.
   *
   * @param row row number where to set the field text. First row is 1.
   * @param column column number where to set the field text. First column is 1.
   * @param text the text to set on the field.
   */
  public void setFieldTextByCoord(int row, int column, String text) {
    XI5250Field field = emulator.getFieldFromPos(column - 1, row - 1);
    if (field == null) {
      throw new IllegalArgumentException("Invalid field position " + row + "," + column);
    }
    field.setString(text);
    updateCursorPosition(text, column - 1, row - 1);
  }

  @VisibleForTesting
  public void updateCursorPosition(String text, int col, int row) {
    emulator.setCursorPos((col + text.length()) % emulator.getCrtSize().width,
        row + (col + text.length()) / emulator.getCrtSize().width);
  }

  public void setFieldTextByLabel(String label, String text) {
    XI5250Field field = emulator.getFieldNextTo(label);
    if (field == null) {
      throw new IllegalArgumentException("Invalid label" + label);
    }
    field.setString(text);
    updateCursorPosition(text, field.getCol(), field.getRow());
  }

  public void setFieldTextByTabulator(int tabs, String text) {
    int row = emulator.getCursorRow();
    int col = emulator.getCursorCol();
    XI5250Field field = emulator.getFieldFromPos(col, row);
    for (int i = tabs; i > 0; i--) {
      field = emulator.getNextFieldFromPos(col, row);
      row = field.getRow();
      col = field.getCol();
    }
    if (field == null) {
      throw new NoSuchElementException("No field found ");
    }

    String str = field.getTrimmedString() + text;
    field.setString(str);
    updateCursorPosition(text, field.getCol(), field.getRow());
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
    emulator.processRawKeyEvent(
        new KeyEvent(emulator, KeyEvent.KEY_PRESSED, 0, modifiers, keyCode,
            KeyEvent.CHAR_UNDEFINED));
  }

  /**
   * Gets the screen text.
   *
   * @return The screen text with newlines separating each row.
   */
  public String getScreenText() {
    int height = emulator.getCrtSize().height;
    int width = emulator.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(emulator.getString(0, i, width).replaceAll("[\\x00-\\x19]", " "));
      screen.append("\n");
    }
    return screen.toString();
  }

  /**
   * Gets the fields present on the current screen.
   *
   * @return fields of the screen. Take into consideration that this only returns fields that hold
   * some information to be sent to the server. The screen is composed by its textual representation
   * plus these fields.
   */
  public List<XI5250Field> getFields() {
    return emulator.getFields();
  }

  /**
   * Get the screen dimensions of the terminal emulator screen.
   *
   * @return Allows getting the number of rows and columns used by the terminal emulator.
   */
  public Dimension getScreenDimensions() {
    return emulator.getCrtSize();
  }

  /**
   * Allows checking if keyboard has been locked (no input can be sent) by the terminal server.
   *
   * @return True if the keyboard is currently locked, false otherwise.
   */
  public boolean isKeyboardLocked() {
    int state = emulator.getState();
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
    return emulator.isCursorVisible() ? Optional
        .of(new Point(emulator.getCursorCol() + 1, emulator.getCursorRow() + 1)) : Optional.empty();
  }

  public void setCursorPosition(int row, int column) {
    emulator.setCursorPos(column, row);
  }
  
  /**
   * Gets the status of the alarm.
   *
   * Prefer using resetAlarm so it is properly reset when checking value. Use this operation only if
   * you are implementing some tracing or debugging and don't want to change the alarm flag status.
   */
  public boolean isAlarmOn() {
    return emulator.isAlarmOn();
  }

  /**
   * Allows resetting and getting the status of the alarm triggered by the terminal server.
   *
   * @return True if the alarm has sounded, false otherwise.
   */
  public boolean resetAlarm() {
    return emulator.resetAlarm();
  }

  /**
   * Add a {@link XI5250EmulatorListener} to the terminal emulator.
   *
   * @param listener listener to be notified on any event (state change, new screen, etc) is
   * triggered by the terminal emulator.
   */
  public void addEmulatorListener(XI5250EmulatorListener listener) {
    emulator.addEmulatorListener(listener);
  }

  /**
   * Remove a {@link XI5250EmulatorListener} from the terminal emulator.
   *
   * @param listener listener to be remove from notificaitons.
   */
  public void removeEmulatorListener(XI5250EmulatorListener listener) {
    emulator.removeEmulatorListener(listener);
  }

  /**
   * Disconnect the terminal emulator from the server.
   */
  public void disconnect() {
    emulator.setActive(false);
  }

  private static class TerminalClientEmulator extends XI5250Emulator {

    private ExceptionHandler exceptionHandler;
    private boolean alarmSounded;

    private TerminalClientEmulator() {
      setDisconnectOnSocketException(false);
    }

    private void setExceptionHandler(ExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
    }

    @Override
    protected void caughtIOException(IOException ex) {
      if (exceptionHandler != null) {
        exceptionHandler.onException(ex);
      }
    }

    @Override
    protected void caughtException(Throwable ex) {
      if (exceptionHandler != null) {
        exceptionHandler.onException(ex);
      }
    }

    @Override
    protected void disconnected(boolean remote) {
      if (exceptionHandler != null && remote) {
        exceptionHandler.onConnectionClosed();
      }
      super.disconnected(remote);
    }

    @Override
    public void soundAlarm() {
      alarmSounded = true;
    }

    private boolean isAlarmOn() {
      return alarmSounded;
    }

    private boolean resetAlarm() {
      boolean ret = alarmSounded;
      alarmSounded = false;
      return ret;
    }

  }

}
