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

package net.infordata.em.tn5250ext;

import net.infordata.em.tn5250.XI5250Applet;


public class XI5250AppletExt extends XI5250Applet {

  private static final long serialVersionUID = 1L;

  @Override
  protected XI5250EmulatorExt createEmulator() {
    return new XI5250EmulatorExt(); 
  }
  
  @Override
  public void init() {
    super.init();
    final boolean pPSHBTNCHC = "true".equalsIgnoreCase(getParameter("PSHBTNCHC"));
    if (pPSHBTNCHC) {
      XI5250EmulatorExt emext = getEmulatorExt();
      PanelsDispatcher disp = new PanelsDispatcher();
      disp.setEmulator(emext);
      new PSHBTNCHCHandler(disp);
    }
  }
  
  /**
   */
  public final XI5250EmulatorExt getEmulatorExt() {
    return (XI5250EmulatorExt)super.getEmulator();
  }
  
  //////
  
  private static class PanelsDispatcher extends XI5250PanelsDispatcher {

    private XI5250PanelHandler ivHandler;
    
    @Override
    public synchronized void addPanelHandler(XI5250PanelHandler panel) {
      if (ivHandler != null)
        throw new IllegalArgumentException("Handler already setted");
      ivHandler = panel;
    }

    @Override
    protected synchronized XI5250PanelHandler getCurrentPanelHandler() {
      return ivHandler;
    }

    @Override
    public synchronized void removePanelHandler(XI5250PanelHandler panel) {
      if (ivHandler != panel)
        throw new IllegalArgumentException("Not the registered handler " + panel);
      ivHandler = null;
    }
  }
}
