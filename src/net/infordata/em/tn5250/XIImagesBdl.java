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
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
    07/07/98 rel. 1.07 - Uses XIUtil.createImage(), the old approach doesn' t
             work at design time.
    15/06/99 rel. 1.13 - Introduced crt5250 bundle.
 */


package net.infordata.em.tn5250;

import java.awt.Image;
import java.util.HashMap;
import java.util.ListResourceBundle;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.infordata.em.util.XIUtil;


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


  @Override
  public Object[][] getContents() {
    return cvContents;
  }


  /**
   */
  public final Image getImage(String anImageName) {
    return ((ImageIcon)getIcon(anImageName)).getImage();
  }


  private Map<String, Icon> ivIcons = new HashMap<String, Icon>();

  /**
   */
  public synchronized final Icon getIcon(String anImageName) {
    Icon icon = (Icon)ivIcons.get(anImageName);
    if (icon == null) {
      icon = new ImageIcon((Image)getObject(anImageName));
      ivIcons.put(anImageName, icon);
    }
    return icon;
  }
}
