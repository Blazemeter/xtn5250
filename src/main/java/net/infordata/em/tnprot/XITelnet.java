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
!!V 07/04/97 rel. 1.00 - start of revisions history.
    14/05/97 rel. 1.00a- ...
    19/05/97 rel. 1.01 - added support for telnet proxy.
    15/07/07 rel. 1.02c- removed throws UnknownHostException form connect method.
             finalize() method added.
    24/09/97 rel. 1.05 - DNCX project.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.tnprot;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;

///////////////////////////////////////////////////////////////////////////////

/**
 * Handles a telnet protocol connection. A telnet emulator must implement XITelnetEmulator interface
 * to receive notification about the telnet connection. Supports RFC885 End of record.
 *
 * @author Valentino Proietti - Infordata S.p.A.
 * @see XITelnetEmulator
 */
public class XITelnet {

  private static final Logger LOGGER = Logger.getLogger(XITelnet.class.getName());

  /**
   * Telnet options or flags.
   */
  public static final byte TELOPT_BINARY = 0;
  public static final byte TELOPT_ECHO = 1;
  public static final byte TELOPT_RCP = 2;
  public static final byte TELOPT_SGA = 3;
  public static final byte TELOPT_NAMS = 4;
  public static final byte TELOPT_STATUS = 5;
  public static final byte TELOPT_TM = 6;
  public static final byte TELOPT_RCTE = 7;
  public static final byte TELOPT_NAOL = 8;
  public static final byte TELOPT_NAOP = 9;
  public static final byte TELOPT_NAOCRD = 10;
  public static final byte TELOPT_NAOHTS = 11;
  public static final byte TELOPT_NAOHTD = 12;
  public static final byte TELOPT_NAOFFD = 13;
  public static final byte TELOPT_NAOVTS = 14;
  public static final byte TELOPT_NAOVTD = 15;
  public static final byte TELOPT_NAOLFD = 16;
  public static final byte TELOPT_XASCII = 17;
  public static final byte TELOPT_LOGOUT = 18;
  public static final byte TELOPT_BM = 19;
  public static final byte TELOPT_DET = 20;
  public static final byte TELOPT_SUPDUP = 21;
  public static final byte TELOPT_SUPDUPOUTPUT = 22;
  public static final byte TELOPT_SNDLOC = 23;
  public static final byte TELOPT_TTYPE = 24;
  public static final byte TELOPT_EOR = 25;
  public static final byte TELOPT_TUID = 26;
  public static final byte TELOPT_OUTMRK = 27;
  public static final byte TELOPT_TTYLOC = 28;
  public static final byte TELOPT_3270REGIME = 29;
  public static final byte TELOPT_X3PAD = 30;
  public static final byte TELOPT_NAWS = 31;
  public static final byte TELOPT_TSPEED = 32;
  public static final byte TELOPT_LFLOW = 33;
  public static final byte TELOPT_LINEMODE = 34;
  public static final byte TELOPT_XDISPLOC = 35;
  public static final byte TELOPT_OLD_ENVIRON = 36;
  public static final byte TELOPT_AUTHENTICATION = 37;
  public static final byte TELOPT_ENCRYPT = 38;
  public static final byte TELOPT_NEW_ENVIRON = 39;
  public static final String[] TELOPT =
      {"BINARY", "ECHO", "RCP", "SUPPRESS GO AHEAD", "NAME",
          "STATUS", "TIMING MARK", "RCTE", "NAOL", "NAOP", "NAOCRD",
          "NAOHTS", "NAOHTD", "NAOFFD", "NAOVTS", "NAOVTD", "NAOLFD",
          "EXTEND ASCII", "LOGOUT", "BYTE MACRO",
          "DATA ENTRY TERMINAL", "SUPDUP", "SUPDUP OUTPUT",
          "SEND LOCATION", "TERMINAL TYPE", "END OF RECORD",
          "TACACS UID", "OUTPUT MARKING", "TTYLOC", "3270 REGIME",
          "X.3 PAD", "NAWS", "TSPEED", "LFLOW", "LINEMODE",
          "XDISPLOC", "OLD-ENVIRON", "AUTHENTICATION", "ENCRYPT",
          "NEW-ENVIRON", "<UNKNOWN>"};

  /**
   * Telnet escapes
   */
  static final byte IAC = (byte) 0xFF;
  static final byte DONT = (byte) 0xFE;
  static final byte DO = (byte) 0xFD;
  static final byte WONT = (byte) 0xFC;
  static final byte WILL = (byte) 0xFB;
  static final byte SB = (byte) 0xFA;
  static final byte SE = (byte) 0xF0;
  static final byte EOR = (byte) 0xEF;
  static final String[] TELCMD = {
      "IAC", "DONT", "DO", "WONT",
      "WILL", "SB", "GA", "EL",
      "EC", "AYT", "AO", "IP",
      "BRK", "DATA MARK", "NOP", "SE",
      "EOR"};

  static final byte SEND = (byte) 0x01;
  static final byte IS = (byte) 0x00;

  // parser states for IAC sequences
  static final int SIAC_START = 0;
  static final int SIAC_WCMD = 1;
  static final int SIAC_WOPT = 2;
  static final int SIAC_WSTR = 3;

  private String ivHost;
  private int ivPort;
  private int connectionTimeoutMillis;
  private SocketFactory socketFactory = SocketFactory.getDefault();
  private boolean disconnectOnException = true;

  /**
   * if null then the connection is closed
   */
  transient private Socket ivSocket;
  transient private InputStream ivIn;
  transient private BufferedOutputStream ivOut;
  transient private RxThread ivReadTh;

  transient private byte ivIACCmd;
  transient private byte ivIACOpt;
  transient private String ivIACStr;

  transient private boolean[] ivLocalFlags = new boolean[128];
  transient private boolean[] ivRemoteFlags = new boolean[128];

  private boolean[] ivLocalReqFlags = new boolean[128];
  private boolean[] ivRemoteReqFlags = new boolean[128];

  private String ivTermType;
  private String ivEnvironment;

  transient private int ivIACParserStatus = SIAC_START;

  private XITelnetEmulator ivEmulator;

  transient private String ivFirstHost;
  transient private String ivSecondHost;

  transient private boolean ivUsed = false;

  public static int toInt(byte bb) {
    return ((int) bb & 0xff);
  }

  public static String toHex(byte bb) {
    String hex = Integer.toString(toInt(bb), 16);
    return "00".substring(hex.length()) + hex;
  }

  public static String toHex(byte[] buf, int len) {
    StringBuilder sb = new StringBuilder(len * 4);
    for (int i = 0; i < len; i++) {
      sb.append(toHex(buf[i])).append(' ');
    }
    return sb.toString();
  }

  public static String toHex(byte[] buf) {
    return toHex(buf, buf.length);
  }

  /**
   * Uses telnet default port for socket connection.
   *
   * @param aHost host to connect to.
   */
  public XITelnet(String aHost) {
    this(aHost, 23);
  }

  /**
   * Uses the given port for socket connection.
   *
   * @param aHost host to connect to.
   * @param aPort port to connect to.
   */
  public XITelnet(String aHost, int aPort) {
    if (aHost == null) {
      throw new IllegalArgumentException("Host cannot be null");
    }

    ivHost = aHost;
    ivPort = aPort;

    try {
      StringTokenizer st = new StringTokenizer(ivHost, "#");
      ivFirstHost = st.nextToken();
      ivSecondHost = st.nextToken();
    } catch (NoSuchElementException ex) {
    }

    String[] parts = ivFirstHost.split(":");
    if (parts.length == 2) {
      ivFirstHost = parts[0];
      ivPort = Integer.parseInt(parts[1]);
    }
  }

  /**
   * Returns the host-name or ip address.
   *
   * @return the host name of the server to connect to.
   */
  public String getHost() {
    return ivHost;
  }

  /**
   * Returns the telnet port.
   *
   * @return the port to connect to.
   */
  public int getPort() {
    return ivPort;
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public void setSocketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  public void setDisconnectOnException(boolean disconnectOnException) {
    this.disconnectOnException = disconnectOnException;
  }

  /**
   * Sets the receiving notifications XITelnetEmulator instance.
   *
   * @param aEmulator the emulator to send notifications to.
   */
  public void setEmulator(XITelnetEmulator aEmulator) {
    ivEmulator = aEmulator;
  }

  public boolean isConnected() {
    return (ivSocket != null);
  }

  /**
   * Sets the telnet terminal type option. Must be used before that a connection is established.
   *
   * @param aTerminalType terminal type to use.
   */
  public void setTerminalType(String aTerminalType) {
    if (isConnected()) {
      throw new IllegalArgumentException("Telnet already connected");
    }
    setLocalReqFlag(TELOPT_TTYPE, true);
    ivTermType = aTerminalType;
  }

  public String getTerminalType() {
    return ivTermType;
  }

  /**
   * Sets the telnet environment option. Must be used before that a connection is established.
   *
   * @param aEnv environment to use.
   */
  public void setEnvironment(String aEnv) {
    if (isConnected()) {
      throw new IllegalArgumentException("Telnet already connected");
    }
    setLocalReqFlag(TELOPT_NEW_ENVIRON, true);
    ivEnvironment = aEnv;
  }

  public String getEnvironment() {
    return ivEnvironment;
  }

  /**
   * Sets the local requested flags. Must be used before that a connection is established.
   *
   * @param flag use a TELOPT_ constant.
   * @param b to be requested ?
   */
  public void setLocalReqFlag(byte flag, boolean b) {
    if (isConnected()) {
      throw new IllegalArgumentException("Telnet already connected");
    }
    ivLocalReqFlags[flag] = b;
  }

  /**
   * Sets the remote requested flags. Must be used before that a connection is established.
   *
   * @param flag use a TELOPT_ constant.
   * @param b to be requested ?
   */
  public void setRemoteReqFlag(byte flag, boolean b) {
    if (isConnected()) {
      throw new IllegalArgumentException("Telnet already connected");
    }
    ivRemoteReqFlags[flag] = b;
  }

  /**
   * Can be used to query a local flag status.
   *
   * @param flag to query if is set
   * @return true if the flag is on, false otherwise.
   */
  public boolean isLocalFlagON(byte flag) {
    return ivLocalFlags[flag];
  }

  /**
   * Can be used to query a remote flag status.
   *
   * @param flag to query if is set
   * @return true if the flag is on, false otherwise.
   */
  public boolean isRemoteFlagON(byte flag) {
    return ivRemoteFlags[flag];
  }

  /**
   * Tries to establish a telnet connection. If a connection is already established then a call to
   * disconnect() is made.
   */
  public synchronized void connect() {
    if (ivUsed) {
      throw new IllegalArgumentException("XITelnet cannot be recycled");
    }

    disconnect();
    connecting();

    try {
      ivSocket = socketFactory.createSocket();
      ivSocket.connect(new InetSocketAddress(ivFirstHost, ivPort), connectionTimeoutMillis);

      ivIn = ivSocket.getInputStream();
      /*
         we use a BufferedOutputStream to avoid sending many small packets and ease tracing with
         Wireshark by keeping packets unaltered
          */
      ivOut = new BufferedOutputStream(ivSocket.getOutputStream());

      ivReadTh = new RxThread();
      ivReadTh.start();

      ivUsed = true;

      connected();
    } catch (IOException ex) {
      caughtIOException(ex);
    }
  }

  private void closeSocket(boolean remote) {
    if (ivSocket != null) {
      try {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("closing...");
        }
        ivSocket.close();
      } catch (IOException ex) {
        // non richiamare caughtIOException();
      }
      ivSocket = null;

      disconnected(remote);
    }
  }

  /**
   * Closes the telnet connection.
   */
  public synchronized void disconnect() {
    disconnect(false);
  }

  private synchronized void disconnect(boolean remote) {
    if (ivReadTh != null) {
      ivReadTh.terminate();
      ivReadTh = null;
    }
    closeSocket(remote);
  }

  /**
   * Telnet IAC parser.
   *
   * @param bb byte containing the IAC.
   * @return the number of bytes to move processing in the read buffer for this IAC.
   */
  protected int processIAC(byte bb) {
    int res = 1;

    switch (ivIACParserStatus) {
      case SIAC_START:
        switch (bb) {
          case IAC:
            if (LOGGER.isLoggable(Level.FINE)) {
              LOGGER.fine("IAC");
            }

            ivIACParserStatus = SIAC_WCMD;
            res = 0;
            break;
        }
        break;
      // CMD
      case SIAC_WCMD:
        if (LOGGER.isLoggable(Level.FINE)) {
          StringBuilder sb = new StringBuilder();
          sb.append(" r " + bb + " ");
          try {
            sb.append(TELCMD[-(bb + 1)] + " ");
          } catch (Exception ex) {
          }
          LOGGER.fine(sb.toString());
        }

        switch (bb) {
          case IAC:
            ivIACParserStatus = SIAC_START;
            break;
          case EOR:
            ivIACParserStatus = SIAC_START;
            res = 0;
            if (ivLocalFlags[TELOPT_EOR]) {
              receivedEOR();
            }
            break;
          case WILL:
          case WONT:
          case DO:
          case DONT:
            ivIACCmd = bb;
            ivIACParserStatus = SIAC_WOPT;
            res = 0;
            break;
          case SB:
            ivIACStr = "";
            ivIACCmd = bb;
            ivIACParserStatus = SIAC_WOPT;
            res = 0;
            break;
          case SE:
            ivIACParserStatus = SIAC_START;

            if (LOGGER.isLoggable(Level.FINE)) {
              LOGGER.fine("SE " + TELOPT[ivIACOpt]);
            }

            res = 0;
            if (ivLocalFlags[ivIACOpt]) {
              switch (ivIACOpt) {
                case TELOPT_TTYPE:
                  sendIACStr(SB, TELOPT_TTYPE, true, ivTermType);
                  break;
                case TELOPT_NEW_ENVIRON:
                  sendIACStr(SB, TELOPT_NEW_ENVIRON, true, ivEnvironment);
                  break;
                default:
                  unhandledRequest(ivIACOpt, ivIACStr);
                  break;
              }
            }
            break;
          default:
            ivIACParserStatus = SIAC_START;
            res = 0;
            break;
        }
        break;
      // OPT
      case SIAC_WOPT:
        ivIACOpt = bb;

        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(TELOPT[ivIACOpt]);
        }

        res = 0;
        switch (ivIACCmd) {
          case SB:
            break;
          case DONT:
            if (ivLocalFlags[ivIACOpt]) {
              ivLocalFlags[ivIACOpt] = false;
              sendIACCmd(WONT, ivIACOpt);

              localFlagsChanged(ivIACOpt);
            }
            break;
          case DO:
            // opzione locale accettabile
            if (ivLocalReqFlags[ivIACOpt]) {
              if (!ivLocalFlags[ivIACOpt]) {
                ivLocalFlags[ivIACOpt] = true;
                sendIACCmd(WILL, ivIACOpt);

                localFlagsChanged(ivIACOpt);
              }
            } else {
              sendIACCmd(WONT, ivIACOpt);
            }
            break;
          case WONT:
            if (ivRemoteFlags[ivIACOpt]) {
              ivRemoteFlags[ivIACOpt] = false;
              sendIACCmd(DONT, ivIACOpt);

              remoteFlagsChanged(ivIACOpt);
            }
            break;
          case WILL:
            // opzione remota accettabile
            if (ivRemoteReqFlags[ivIACOpt]) {
              if (!ivRemoteFlags[ivIACOpt]) {
                ivRemoteFlags[ivIACOpt] = true;
                sendIACCmd(DO, ivIACOpt);

                remoteFlagsChanged(ivIACOpt);
              }
            } else {
              sendIACCmd(DONT, ivIACOpt);
            }
            break;
        }

        if (ivIACCmd != SB) {
          ivIACParserStatus = SIAC_START;
        } else {
          ivIACParserStatus = SIAC_WSTR;
        }
        break;
      case SIAC_WSTR:
        res = 0;
        switch (bb) {
          case IAC:
            ivIACParserStatus = SIAC_WCMD;
            break;
          default:
            ivIACStr += (char) bb;
            break;
        }
        break;
    }

    return res;
  }

  /**
   * Sends an telnet EOR sequence.
   */
  public synchronized void sendEOR() {
    byte[] buf = {IAC, EOR};

    try {
      ivOut.write(buf);
      ivOut.flush();
    } catch (IOException ex) {
      caughtIOException(ex);
    }
  }

  /**
   * Sends a telnet IAC sequence.
   *
   * @param aCmd command to send
   * @param aOpt option to send
   */
  public synchronized void sendIACCmd(byte aCmd, byte aOpt) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(" t " + aCmd + " " + TELCMD[-(aCmd + 1)] + " " +
          TELOPT[aOpt]);
    }

    byte[] buf = {IAC, aCmd, aOpt};

    try {
      ivOut.write(buf);
      ivOut.flush();
    } catch (IOException ex) {
      caughtIOException(ex);
    }
  }

  /**
   * Sends a telnet IAC sequence with a string argument.
   *
   * @param aCmd command to send
   * @param aOpt option to send
   * @param sendIS whether to send IS sub-command or not.
   * @param aString the command body
   */
  public synchronized void sendIACStr(byte aCmd, byte aOpt, boolean sendIS, String aString) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("t " + aCmd + " " + TELCMD[-(aCmd + 1)] + " " +
          TELOPT[aOpt] + " " + aString);
    }

    byte[] endBuf = {IAC, SE};
    byte[] startBuf =
        sendIS ? new byte[]{IAC, aCmd, aOpt, IS} : new byte[]{IAC, aCmd, aOpt};
    try {
      ivOut.write(startBuf);
      ivOut.write(aString.getBytes());
      ivOut.write(endBuf);
      ivOut.flush();
    } catch (IOException ex) {
      caughtIOException(ex);
    }
  }

  /**
   * Sends a data buffer (IAC bytes are doubled).
   *
   * @param aBuf the buffer containing the bytes to send.
   * @param aLen number of bytes to send from the buffer.
   */
  public synchronized void send(byte[] aBuf, int aLen) {
    try {
      for (int i = 0; i < aLen; i++) {
        ivOut.write(aBuf[i]);
        if (aBuf[i] == IAC) {
          ivOut.write(IAC);
        }
      }
      /*
       this is the only method from XITelnet that does not invoke flush, and since we are now using
       a BufferedOutputStream we need all methods to flush.
        */
      ivOut.flush();
    } catch (IOException ex) {
      caughtIOException(ex);
    }
  }

  /**
   * Sends a data buffer (IAC bytes are doubled).
   *
   * @param aBuf bytes to send to the terminal server.
   */
  public void send(byte[] aBuf) {
    send(aBuf, aBuf.length);
  }

  /**
   * Flushes output buffer.
   */
  public synchronized void flush() {
    try {
      ivOut.flush();
    } catch (IOException ex) {
      caughtIOException(ex);
    }
  }

  /**
   * Called just before trying to connect.
   */
  protected void connecting() {
    if (ivEmulator != null) {
      ivEmulator.connecting();
    }
  }

  /**
   * Called after that a connection is established.
   */
  protected void connected() {
    if (ivSecondHost != null && !ivSecondHost.equals("")) {
      send((ivSecondHost + "\n").getBytes());
    }

    if (ivEmulator != null) {
      ivEmulator.connected();
    }
  }

  /**
   * Called after that the connection is closed.
   *
   * @param remote whether the disconnection was caused by a local disconnect, or by the terminal
   * server.
   */
  protected void disconnected(boolean remote) {
    if (ivEmulator != null) {
      ivEmulator.disconnected(remote);
    }
  }

  /**
   * Called when an IOException is caught.
   *
   * @param ex exception throw when communicating with terminal server.
   */
  protected synchronized void caughtIOException(IOException ex) {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.log(Level.FINE, "", ex);
    }

    try {
      if (ivEmulator != null) {
        ivEmulator.caughtIOException(ex);
      }
    } finally {
      if (disconnectOnException) {
        disconnect();
      }
    }
  }

  /**
   * Called when an unhandled IAC request is received.
   *
   * @param aIACOpt option of the IAC
   * @param aIACStr body of the IAC
   */
  protected void unhandledRequest(byte aIACOpt, String aIACStr) {
    if (ivEmulator != null) {
      ivEmulator.unhandledRequest(aIACOpt, aIACStr);
    }
  }

  /**
   * Called when a local flags has been changed.
   *
   * @param aIACOpt IAC option that changed
   */
  protected void localFlagsChanged(byte aIACOpt) {
    if (ivEmulator != null) {
      ivEmulator.localFlagsChanged(aIACOpt);
    }
  }

  /**
   * Called when a remote flags has been changed.
   *
   * @param aIACOpt IAC option that changed
   */
  protected void remoteFlagsChanged(byte aIACOpt) {
    if (ivEmulator != null) {
      ivEmulator.remoteFlagsChanged(aIACOpt);
    }
  }

  /**
   * Called when data are received. Data are already cleared from IAC sequence. NOTE: receivedData
   * is always called in the receiving thread.
   *
   * @param buf bytes received from the terminal server.
   * @param len number of bytes received.
   */
  protected void receivedData(byte[] buf, int len) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest(toHex(buf, len));
    }

    if (ivEmulator != null) {
      ivEmulator.receivedData(buf, len);
    }
  }

  /**
   * Called when a telnet EOR sequence is received. NOTE: receivedEOR is always called in the
   * receiving thread.
   */
  protected void receivedEOR() {
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("EOR");
    }

    if (ivEmulator != null) {
      ivEmulator.receivedEOR();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    disconnect();
    super.finalize();
  }

  /*
   * Used only for test purposes.
   */
  public static void main(String[] argv) {
    XITelnet tn = new XITelnet("192.168.0.1#192.168.0.4");
    tn.setTerminalType("IBM-3477-FC");

    tn.setLocalReqFlag(TELOPT_BINARY, true);
    tn.setLocalReqFlag(TELOPT_TTYPE, true);
    tn.setLocalReqFlag(TELOPT_EOR, true);

    tn.setRemoteReqFlag(TELOPT_BINARY, true);
    tn.setRemoteReqFlag(TELOPT_EOR, true);

    try {
      tn.connect();

      Thread.sleep(10000);

      tn.disconnect();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  class RxThread extends Thread {

    private boolean ivTerminate = false;

    public RxThread() {
      super("XITelnet rx thread");
    }

    public void terminate() {
      ivTerminate = true;
      if (this != Thread.currentThread()) {  //!!V 03/03/98
        interrupt();
      }
    }

    /**
     * The receiving thread.
     */
    @Override
    public void run() {
      byte[] buf = new byte[1024];
      byte[] rBuf = new byte[1024];
      int len = 0;
      int i, j;

      try {
        while (!ivTerminate) {
          len = ivIn.read(buf);
          if (len < 0) {
            disconnect(true);
            return;
          }

          // process all IAC commands
          for (i = 0, j = 0; i < len; i++) {
            rBuf[j] = buf[i];
            if ((ivIACParserStatus != SIAC_START) || (buf[i] == IAC)) {
              // if a IAC is received then split rx buffer
              if ((ivIACParserStatus == SIAC_START) && (buf[i] == IAC)) {
                if (j > 0) {
                  receivedData(rBuf, j);
                }
                j = 0;
              }
              j += processIAC(buf[i]);
            } else {
              ++j;
            }
          }

          if (j > 0) {
            receivedData(rBuf, j);
          }
        }
      } catch (IOException ex) {
        if (!ivTerminate) {
          caughtIOException(ex);
        }
      }
    }

  }

}
