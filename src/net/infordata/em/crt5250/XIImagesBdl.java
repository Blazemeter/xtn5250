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
!!V 15/06/99 rel. 1.13 - creation.
 */


package net.infordata.em.crt5250;

import java.awt.*;
import java.util.*;

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
      cvContents = new Object[][] {
        {"ShiftDown",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/ShiftDown.gif")},
        {"CapsLock",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/CapsLock.gif")},
        //
        {"3dFx",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/3dFx.gif")},
        {"Copy",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/Copy.gif")},
        {"Paste",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/Paste.gif")},
        {"RefCursor",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/RefCursor.gif")},
        //
        {"Logo",
         XIUtil.createImage(
             XIImagesBdl.class, "resources/Logo.gif")},
      };
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
