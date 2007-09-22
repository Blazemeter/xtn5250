/*	
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    07/07/98 rel. 1.07 - Uses XIUtil.createImage(), the old approach doesn' t
             work at design time.
    15/06/99 rel. 1.13 - Introduced crt5250 bundle.
 */


package net.infordata.em.tn5250;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.net.URL;

import javax.swing.*;

import net.infordata.em.util.*;



/**
 * The image bundle.
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIImagesBdl extends ListResourceBundle {

  private static XIImagesBdl      cvImagesBdl;

  private static Object[][] cvContents = null;

  static {
    try {
      Object[][] contents = new Object[][] {
        {"TemporaryLock",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/TemporaryLockState.gif")},
        {"NormalLock",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/NormalLockState.gif")},
        {"Help",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/HelpState.gif")},
        {"Message",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/Message.gif")},
        {"Flash",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/Flash.gif")},
        //
        {"Connect",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/Connect.gif")},
        {"Disconnect",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/Disconnect.gif")},
        {"InFrame",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/InFrame.gif")},
        {"SnapShot",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/SnapShot.gif")},
      };

      Object[][] superContents =
          net.infordata.em.crt5250.XIImagesBdl.getImagesBdl().getContents();
      cvContents = new Object[superContents.length + contents.length][2];
      System.arraycopy(superContents, 0, cvContents, 0, superContents.length);
      System.arraycopy(contents, 0, cvContents, superContents.length, contents.length);
    }
    catch (RuntimeException ex) {
      ex.printStackTrace(Diagnostic.getOut());
      throw ex;
    }
  }


  private XIImagesBdl() {
  }


  public static XIImagesBdl getImagesBdl() {
    if (cvImagesBdl == null) {
      cvImagesBdl = new XIImagesBdl();
    }

    return cvImagesBdl;
  }


  public Object[][] getContents() {
    return cvContents;
  }


  /**
   */
  public final Image getImage(String anImageName) {
    return ((ImageIcon)getIcon(anImageName)).getImage();
  }


  private Hashtable ivIcons = new Hashtable();
  /**
   */
  public final Icon getIcon(String anImageName) {
    Icon icon = (Icon)ivIcons.get(anImageName);
    if (icon == null) {
      icon = new ImageIcon((Image)getObject(anImageName));
      ivIcons.put(anImageName, icon);
    }
    return icon;
  }
}
