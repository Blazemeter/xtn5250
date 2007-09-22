/*
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.tn5250ext;


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.infordata.em.util.*;



/**
 */
public class XIHintWindow extends JWindow {

  private XIHint     ivHint;
  private Component  ivComponent;

  private WinAdapter   ivWinAdapter  = new WinAdapter();


  /**
   */
  public XIHintWindow(XIHint aHint, Component aComponent) {

    super(XIUtil.getFrame(aComponent));

    if (aHint == null || aComponent == null) {
      throw new IllegalArgumentException();
    }

    addComponentListener(new CompAdapter());

    Point vLocation;

    ivComponent = aComponent;

    vLocation     = ivComponent.getLocationOnScreen();
    vLocation.y  += ivComponent.getSize().height + 4;

    setLocation(vLocation);

    getContentPane().setBackground(new Color(255, 250 , 180));

    getContentPane().add(aHint);
    setSize(aHint.getPreferredSize());
  }


  /**
   * Visibile solo se la frame associata è attiva
   */
  public void setVisible(boolean b) {
    //!!V TODO dovrebbe essere visibile solo se la frame alla quale è associato
    // è attiva
    super.setVisible(b);
  }


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  class CompAdapter extends ComponentAdapter {

    public void componentResized(ComponentEvent aEvent) {
      if (aEvent.getSource() == getParent()) {
        setVisible(false);
      }
    }

    public void componentMoved(ComponentEvent aEvent) {
      if (aEvent.getSource() == getParent()) {
        setVisible(false);
      }
    }

    public void componentShown(ComponentEvent aEvent) {
      if (aEvent.getSource() == XIHintWindow.this) {
        Frame frm = (Frame)getParent();
        frm.addComponentListener(this);
        frm.addWindowListener(ivWinAdapter);
      }
    }

    public void componentHidden(ComponentEvent aEvent) {
      if (aEvent.getSource() == XIHintWindow.this) {
        Frame frm = (Frame)getParent();
        frm.removeWindowListener(ivWinAdapter);
        frm.removeComponentListener(this);
      }
    }
  }


  //////////////////////////////////////////////////////////////////////////////

	/**
	 */
  class WinAdapter extends WindowAdapter {
    public void windowDeactivated(WindowEvent aEvent) {
      setVisible(false);
    }
  }
}
