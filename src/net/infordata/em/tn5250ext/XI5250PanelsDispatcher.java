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


import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
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
public class XI5250PanelsDispatcher {

  private static final Logger LOGGER = Logger.getLogger(XI5250PanelsDispatcher.class.getName());
  
  transient private static String DELIMITERS;

  transient private XI5250EmulatorExt  ivEm;
  transient private EmulatorAdapter    ivEmulatorAdapter;

  /**
   * The current XI5250PanelHandler.
   */
  transient private XI5250PanelHandler ivPanelHndl;

  /**
   * Contains relations between XI5250PanelHandler and a tokenized version of
   * the related 5250 screen first line.
   * It really contains Vectors of XI5250PanelHandler (one to many relation)
   */
  transient private Hashtable          ivPanels;

  transient private Hashtable          ivSharedData;


  /**
   */
  static {
    DELIMITERS = "";
    for (int i = 0; i <= 32; i++)
      DELIMITERS += (char)i;
  }


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
  private static String calcKey(String str, int excludeTokens) {
    String          res = "";
    StringTokenizer st  = new StringTokenizer(str, DELIMITERS);
    int             n   = Math.max(0, st.countTokens() - excludeTokens);

    for (int i = n - 1; i>= 0; i--)
      res += " " + st.nextToken();

    return res;
  }


  /**
   */
  public final XI5250EmulatorExt getEmulator() {
    return ivEm;
  }


  /**
   * Adds the given XI5250PanelHandler instance to the list of panel handlers
   * waiting to be activated.
   */
  public synchronized void addPanelHandler(XI5250PanelHandler aPanel) {
    if (ivPanels == null)
      ivPanels = new Hashtable();

    String key = calcKey(aPanel.getTitle(), 0);

    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.finer("addPanelHandler: [" + key + "] " + aPanel);

    Vector vt = (Vector)ivPanels.get(key);

    if (vt == null) {
      vt = new Vector(10, 10);
      ivPanels.put(key, vt);
    }

    if (!vt.contains(aPanel))
      vt.addElement(aPanel);
  }


  /**
   * Removes the panel handler.
   */
  public synchronized void removePanelHandler(XI5250PanelHandler aPanel) {
    if (ivPanels == null)
      return;

    String key = calcKey(aPanel.getTitle(), 0);

    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.finer("removePanelHandler: [" +
                                  key + "] " + aPanel);

    Vector vt = (Vector)ivPanels.get(key);

    if (vt == null)
      return;

    vt.removeElement(aPanel);

    if (vt.size() <= 0)
      ivPanels.remove(key);
  }


  /**
   * Searches the XI5250PanelHandler instace related to the current 5250 panel.
   * Uses a two step search.
   * First step is based on the contents of the first line present on screen
   * then the detailedTest() method provided by XI5250PanelHandler is used.
   * @see    XI5250PanelHandler#detailedTest
   */
  protected synchronized XI5250PanelHandler getCurrentPanelHandler() {
    String title = ivEm.getString().substring(0, ivEm.getCrtSize().width);

    int    j = 0;
    String key;
    Vector vt;

    // first step
    // find the XI5250PanelHandler vector using as key the tokenized title
    // minus j ending tokens
    do {
      key = calcKey(title, j++);
      vt = (Vector)ivPanels.get(key);

      if (LOGGER.isLoggable(Level.FINER))
        LOGGER.finer("try [" + key + "] " +
                                    ((vt != null) ? "found" : "next"));
    }
    while (vt == null && key != null && key.length() > 0);

    if (vt == null)
      return null;

    XI5250PanelHandler panelHndl;

    // second step
    for (int i = vt.size() - 1; i >= 0; i--) {
      panelHndl = (XI5250PanelHandler)vt.elementAt(i);
      if (panelHndl.detailedTest()) {
        // increase priority
        vt.removeElement(panelHndl);
        vt.addElement(panelHndl);

        return panelHndl;
      }
    }

    return null;
  }


  /**
   * Returns an Hashtable that can be used to store data shared by different
   * XI5250Panel instances.
   */
  public final Hashtable getSharedData() {
    if (ivSharedData == null)
      ivSharedData = new Hashtable();

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
