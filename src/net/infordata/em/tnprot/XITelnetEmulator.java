/*
!!V 14/05/97 rel. 1.00a- ...
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.tnprot;


import java.io.*;


/**
 * Interface that must be implemented by an emulator to receive notification from XITelnet.
 *
 * @see    XITelnet
 *
 * @version  1.00a
 * @author   Valentino Proietti - Infordada S.p.A.
 */
public interface XITelnetEmulator {
  /**
   * @see    XITelnet#connecting
   */
  public void connecting();

  /**
   * @see    XITelnet#connected
   */
  public void connected();

  /**
   * @see    XITelnet#disconnected
   */
  public void disconnected();

  /**
   * @see    XITelnet#catchedIOException
   */
  public void catchedIOException(IOException ex);

  /**
   * @see    XITelnet#receivedData
   */
  public void receivedData(byte[] buf, int len);

  /**
   * @see    XITelnet#receivedEOR
   */
  public void receivedEOR();

  /**
   * @see    XITelnet#unhandledRequest
   */
  public void unhandledRequest(byte aIACOpt, String aIACStr);


  /**
   * @see    XITelnet#localFlagsChanged
   */
  public void localFlagsChanged(byte aIACOpt);


  /**
   * @see    XITelnet#remoteFlagsChanged
   */
  public void remoteFlagsChanged(byte aIACOpt);
}