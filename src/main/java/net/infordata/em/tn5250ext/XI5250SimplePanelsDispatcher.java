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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.infordata.em.tn5250.XI5250Emulator;

/**
 * Maintains a set of XI5250PanelHandler and activates them when the related 5250 screen (or panel)
 * is received.
 *
 * @author Valentino Proietti - Infordata S.p.A.
 */
public class XI5250SimplePanelsDispatcher extends XI5250PanelsDispatcher {

  private static final Logger LOGGER = Logger
      .getLogger(XI5250SimplePanelsDispatcher.class.getName());

  transient private static String DELIMITERS;

  /**
   * Contains relations between XI5250PanelHandler and a tokenized version of the related 5250
   * screen first line. It really contains Vectors of XI5250PanelHandler (one to many relation)
   */
  transient private HashMap<String, ArrayList<XI5250PanelHandler>> ivPanels;

  static {
    DELIMITERS = "";
    for (int i = 0; i <= 32; i++) {
      DELIMITERS += (char) i;
    }
  }

  /**
   * Creates a XI5250SimplePanelsDispatcher.
   */
  public XI5250SimplePanelsDispatcher() {
  }

  /**
   * Creates a XI5250SimplePanelsDispatcher to handle panels of the given XI5250Emulator instance.
   *
   * @param aEmulator emulator to set on the panel dispatcher
   */
  public XI5250SimplePanelsDispatcher(XI5250EmulatorExt aEmulator) {
    super(aEmulator);
  }

  private static String calcKey(String str, int excludeTokens) {
    String res = "";
    StringTokenizer st = new StringTokenizer(str, DELIMITERS);
    int n = Math.max(0, st.countTokens() - excludeTokens);

    for (int i = n - 1; i >= 0; i--) {
      res += " " + st.nextToken();
    }

    return res;
  }

  /**
   * Adds the given XI5250PanelHandler instance to the list of panel handlers waiting to be
   * activated.
   *
   * @param aPanel panel to add
   */
  @Override
  public synchronized void addPanelHandler(XI5250PanelHandler aPanel) {
    if (ivPanels == null) {
      ivPanels = new HashMap<>();
    }

    String key = calcKey(aPanel.getTitle(), 0);

    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.finer("addPanelHandler: [" + key + "] " + aPanel);
    }

    ArrayList<XI5250PanelHandler> vt = ivPanels
        .computeIfAbsent(key, k -> new ArrayList<>(10));

    if (!vt.contains(aPanel)) {
      vt.add(aPanel);
    }
  }

  /**
   * Removes the panel handler.
   *
   * @param aPanel panel to remove
   */
  @Override
  public synchronized void removePanelHandler(XI5250PanelHandler aPanel) {
    if (ivPanels == null) {
      return;
    }

    String key = calcKey(aPanel.getTitle(), 0);

    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.finer("removePanelHandler: [" +
          key + "] " + aPanel);
    }

    ArrayList<XI5250PanelHandler> vt = ivPanels.get(key);

    if (vt == null) {
      return;
    }

    vt.remove(aPanel);

    if (vt.size() <= 0) {
      ivPanels.remove(key);
    }
  }


  /**
   * Searches the XI5250PanelHandler instance related to the current 5250 panel. Uses a two step
   * search. First step is based on the contents of the first line present on screen then the
   * detailedTest() method provided by XI5250PanelHandler is used.
   *
   * @return panel associated to the current panel.
   * @see XI5250PanelHandler#detailedTest
   */
  @Override
  protected synchronized XI5250PanelHandler getCurrentPanelHandler() {
    final XI5250Emulator em = getEmulator();
    String title = em.getString().substring(0, em.getCrtSize().width);

    int j = 0;
    String key;
    ArrayList<XI5250PanelHandler> vt;

    // first step
    // find the XI5250PanelHandler vector using as key the tokenized title
    // minus j ending tokens
    do {
      key = calcKey(title, j++);
      vt = ivPanels.get(key);

      if (LOGGER.isLoggable(Level.FINER)) {
        LOGGER.finer("try [" + key + "] " +
            ((vt != null) ? "found" : "next"));
      }
    }
    while (vt == null && key != null && key.length() > 0);

    if (vt == null) {
      return null;
    }

    XI5250PanelHandler panelHndl;

    // second step
    for (int i = vt.size() - 1; i >= 0; i--) {
      panelHndl = vt.get(i);
      if (panelHndl.detailedTest()) {
        // increase priority
        vt.remove(panelHndl);
        vt.add(panelHndl);

        return panelHndl;
      }
    }

    return null;
  }

}
