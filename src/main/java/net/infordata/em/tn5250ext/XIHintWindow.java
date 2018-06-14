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
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.tn5250ext;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.infordata.em.util.*;

public class XIHintWindow extends JWindow {

  private static final long serialVersionUID = 1L;

  private Component ivComponent;

  private WinAdapter ivWinAdapter = new WinAdapter();

  public XIHintWindow(XIHint aHint, Component aComponent) {

    super(XIUtil.getFrame(aComponent));

    if (aHint == null || aComponent == null) {
      throw new IllegalArgumentException();
    }

    addComponentListener(new CompAdapter());

    Point vLocation;

    ivComponent = aComponent;

    vLocation = ivComponent.getLocationOnScreen();
    vLocation.y += ivComponent.getSize().height + 4;

    setLocation(vLocation);

    getContentPane().setBackground(new Color(255, 250, 180));

    getContentPane().add(aHint);
    setSize(aHint.getPreferredSize());
  }

  /**
   * Visibile solo se la frame associata attiva
   *
   * @param b true to set visible, false to set invisible.
   */
  @Override
  public void setVisible(boolean b) {
    //!!V TODO dovrebbe essere visibile solo se la frame alla quale � associato
    super.setVisible(b);
  }

  class CompAdapter extends ComponentAdapter {

    @Override
    public void componentResized(ComponentEvent aEvent) {
      if (aEvent.getSource() == getParent()) {
        setVisible(false);
      }
    }

    @Override
    public void componentMoved(ComponentEvent aEvent) {
      if (aEvent.getSource() == getParent()) {
        setVisible(false);
      }
    }

    @Override
    public void componentShown(ComponentEvent aEvent) {
      if (aEvent.getSource() == XIHintWindow.this) {
        Frame frm = (Frame) getParent();
        frm.addComponentListener(this);
        frm.addWindowListener(ivWinAdapter);
      }
    }

    @Override
    public void componentHidden(ComponentEvent aEvent) {
      if (aEvent.getSource() == XIHintWindow.this) {
        Frame frm = (Frame) getParent();
        frm.removeWindowListener(ivWinAdapter);
        frm.removeComponentListener(this);
      }
    }
  }

  class WinAdapter extends WindowAdapter {

    @Override
    public void windowDeactivated(WindowEvent aEvent) {
      setVisible(false);
    }

  }

}
