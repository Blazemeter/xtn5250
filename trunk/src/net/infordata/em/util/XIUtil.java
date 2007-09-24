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
!!V 07/07/98 rel. 1.07 - creation.
    02/05/00 rel. 1.15b- Jdk 1.3rc2.
 */


package net.infordata.em.util;


import java.awt.*;
import java.applet.*;
import java.io.*;
import java.lang.reflect.*;


/**
 */
public class XIUtil {


  public static final boolean is1dot2 = is1dot2();
  public static final boolean is1dot3 = is1dot3();

  private static boolean is1dot2() {
    try {
      // Test if method introduced in 1.2 is available.
      Method m = Class.class.getMethod("getProtectionDomain", null);
      return (m != null);
    }
    catch (NoSuchMethodException e) {
      return false;
    }
  }

  private static boolean is1dot3() {
    try {
      // Test if method introduced in 1.3 is available.
      Method m = Runtime.class.getMethod("addShutdownHook",
                                         new Class[] {Thread.class});
      return (m != null);
    }
    catch (NoSuchMethodException e) {
      return false;
    }
  }


  /**
   */
  private XIUtil() {
  }


  /**
   */
  public static final Frame getFrame(Component aComponent) {
    Component comp = aComponent;
    while (comp != null && !(comp instanceof Frame))
      comp = comp.getParent();
    return (Frame)comp;
  }


  /**
   */
  public static final Window getWindow(Component aComponent) {
    Component comp = aComponent;
    while (comp != null && !(comp instanceof Window))
      comp = comp.getParent();
    return (Window)comp;
  }


  /**
   */
  public static final Applet getApplet(Component aComponent) {
    Component comp = aComponent;
    while (comp != null && !(comp instanceof Applet))
      comp = comp.getParent();
    return (Applet)comp;
  }


  /**
   */
  public static Image createImage(final Class baseClass,
                                  final String gifFile) {
    byte[] buffer = null;
    try {
      /* Copy resource into a byte array.  This is
       * necessary because several browsers consider
       * Class.getResource a security risk because it
       * can be used to load additional classes.
       * Class.getResourceAsStream just returns raw
       * bytes, which we can convert to an image.
       */
      InputStream resource = baseClass.getResourceAsStream(gifFile);
      if (resource == null) {
        throw new IllegalArgumentException(baseClass.getName() + "/" +
                                           gifFile + " not found.");
      }
      BufferedInputStream in = new BufferedInputStream(resource);
      ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
      buffer = new byte[1024];
      int n;
      while ((n = in.read(buffer)) > 0) {
        out.write(buffer, 0, n);
      }
      in.close();
      out.flush();

      buffer = out.toByteArray();
      if (buffer.length == 0) {
        throw new IllegalStateException("warning: " + gifFile +
                                        " is zero-length");
      }
    }
    catch (IOException ex) {
      throw new IllegalStateException(ex.toString());
    }

    return Toolkit.getDefaultToolkit().createImage(buffer);
  }


  /**
   */
  public static char getMnemonic(String str) {
    char ch;
    int state = 0;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      switch (state) {
        case 0:
          if (ch == '&')
            state = 1;
          break;
        case 1:
          if (ch != '&')
            return ch;
          state = 0;
          break;
      }
    }
    return '\u0000';
  }


  /**
   */
  public static String removeMnemonics(String str) {
    StringBuffer sb = new StringBuffer(str.length());
    char ch;
    int state = 0;
    for (int i = 0; i < str.length(); i++) {
      ch = str.charAt(i);
      switch (state) {
        case 0:
          if (ch == '&')
            state = 1;
          else
            sb.append(ch);
          break;
        case 1:
          sb.append(ch);
          state = 0;
          break;
      }
    }
    return sb.toString();
  }
}