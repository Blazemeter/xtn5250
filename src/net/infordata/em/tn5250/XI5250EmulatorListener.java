/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

 
package net.infordata.em.tn5250;


import java.awt.*;
import java.util.*;
import java.awt.event.*;


/**
 * Listener for XI5250EmulatorEvent.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250EmulatorListener extends EventListener {

  /**
   * @see     XI5250EmulatorEvent#CONNECTING
   */
  public void connecting(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#CONNECTED
   */
  public void connected(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#DISCONNECTED
   */
  public void disconnected(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#STATE_CHANGED
   */
  public void stateChanged(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#NEW_PANEL_RECEIVED
   */
  public void newPanelReceived(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#FIELDS_REMOVED
   */
  public void fieldsRemoved(XI5250EmulatorEvent e);


  /**
   * @see     XI5250EmulatorEvent#DATA_SENDED
   */
  public void dataSended(XI5250EmulatorEvent e);
}