/*
!!V 14/05/97 rel. 0.96d- removed SIZE_CHANGED.
    27/05/97 rel. 1.00 - first release.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.crt5250;


import java.awt.*;
import java.util.*;
import java.awt.event.*;


/**
 * Listener for XI5250FieldEvent.
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250FieldListener extends EventListener {
  /**
   * Input cursor enters in the field area
   */
  public void activated(XI5250FieldEvent e);


  /**
   * Input cursor exits from the input area
   */
  public void deactivated(XI5250FieldEvent e);


  /**
   * Field value is changed
   */
  public void valueChanged(XI5250FieldEvent e);
  
  
  /**
   * Field enabled state is changed using setEnabled method
   * @see    XI5250Field#setEnabled
   * @see    XI5250Field#isEnabled
   */
  public void enabledStateChanged(XI5250FieldEvent e);
  
  
  /**
   */
  public void keyEvent(XI5250FieldEvent e);
}