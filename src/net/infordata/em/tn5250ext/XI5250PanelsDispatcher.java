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
!!V 06/08/97 rel. 1.00a- bug fix.
    06/10/97 rel. 1.05b- newPanelReceived event sended also in the PRE_HELP state.
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 

package net.infordata.em.tn5250ext;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.infordata.em.tn5250.XI5250EmulatorAdapter;
import net.infordata.em.tn5250.XI5250EmulatorEvent;


/**
 * Mantains a set of XI5250PanelHandler and activates them when the related 5250
 * screen (or panel) is received.
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public abstract class XI5250PanelsDispatcher {

  protected static final Logger LOGGER = Logger.getLogger(XI5250PanelsDispatcher.class.getName());
  
  transient private XI5250EmulatorExt  ivEm;
  transient private EmulatorAdapter    ivEmulatorAdapter;

  /**
   * The current XI5250PanelHandler.
   */
  transient private XI5250PanelHandler ivPanelHndl;

  transient private HashMap<Object, Object> ivSharedData;


  /**
   * Creates a XI5250PanelsDispatcher.
   */
  public XI5250PanelsDispatcher() {
  }


  /**
   * Creates a XI5250PanelsDispatcher to handle panels of the given
   * XI5250Emulator instance.
   */
  public XI5250PanelsDispatcher(XI5250EmulatorExt aEmulator) {
    this();
    setEmulator(aEmulator);
  }


  /**
   */
  public synchronized void setEmulator(XI5250EmulatorExt aEmulator) {
    if (ivEm == aEmulator)
      return;

    if (ivEm != null) {
      setPanelHndl(null);  //!!1.00a
      ivEm.removeEmulatorListener(ivEmulatorAdapter);
      ivEm.removeDispatcher(this);
    }

    ivEmulatorAdapter = null;
    ivEm = aEmulator;

    if (ivEm != null) {
      ivEm.addDispatcher(this);
      ivEm.addEmulatorListener((ivEmulatorAdapter = new EmulatorAdapter()));
    }
  }

  /**
   */
  public final XI5250EmulatorExt getEmulator() {
    return ivEm;
  }

  public final Object getTreeLock() {
    return ivEm.getTreeLock();
  }

  /**
   * Adds the given XI5250PanelHandler instance to the list of panel handlers
   * waiting to be activated.
   */
  public abstract void addPanelHandler(XI5250PanelHandler aPanel);

  /**
   * Removes the panel handler.
   */
  public abstract void removePanelHandler(XI5250PanelHandler aPanel);

  /**
   * Searches the XI5250PanelHandler instace related to the current 5250 panel.<br>
   * @see    XI5250PanelHandler#detailedTest
   */
  protected abstract XI5250PanelHandler getCurrentPanelHandler();

  /**
   * Returns a Map that can be used to store data shared by different
   * XI5250Panel instances.
   */
  public final Map<Object, Object> getSharedData() {
    if (ivSharedData == null)
      ivSharedData = new HashMap<Object, Object>();

    return ivSharedData;
  }


  /**
   * A new panel has been received so try to find the relative
   * XI5250PanelHandler using getCurrentPanelHandler method.
   *
   * @see    #getCurrentPanelHandler
   */
  protected synchronized void newPanelReceived() {
    if (ivEm.getState() == XI5250EmulatorExt.ST_NORMAL_UNLOCKED ||
        ivEm.getState() == XI5250EmulatorExt.ST_PRE_HELP) {         //!!1.05b
      XI5250PanelHandler panelHndl = getCurrentPanelHandler();

      if (panelHndl != null)
        setPanelHndl(panelHndl);
    }
  }


  /**
   */
  private void fieldsRemoved() {
    // destroy old panel handler
    setPanelHndl(null);
  }


  /**
   */
  private void dataSended(byte anAidCode) {
    if (ivPanelHndl != null)
      ivPanelHndl.dataSended(anAidCode);
  }


  /**
   * Used to set the current XI5250PanelHandler and to stop the previous one.
   */
  protected void setPanelHndl(XI5250PanelHandler aPanelHndl) {
    if (aPanelHndl == ivPanelHndl)
      return;

    if (ivPanelHndl != null)
      ivPanelHndl.stopInternal();

    ivPanelHndl = aPanelHndl;

    if (ivPanelHndl != null)
      ivPanelHndl.startInternal();
  }


  /**
   * Returns the current active panel handler (none if null)
   */
  protected XI5250PanelHandler getCurrentPanelHndl() {
    return ivPanelHndl;
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  class EmulatorAdapter extends XI5250EmulatorAdapter {

    public void stateChanged(XI5250EmulatorEvent e) {
    }

    public void disconnected(XI5250EmulatorEvent e) {
      XI5250PanelsDispatcher.this.fieldsRemoved();
    }

    public void newPanelReceived(XI5250EmulatorEvent e) {
      XI5250PanelsDispatcher.this.newPanelReceived();
    }

    public void fieldsRemoved(XI5250EmulatorEvent e) {
      XI5250PanelsDispatcher.this.fieldsRemoved();
    }

    public void dataSended(XI5250EmulatorEvent e) {
      XI5250PanelsDispatcher.this.dataSended(e.getAidCode());
    }
  }
}
