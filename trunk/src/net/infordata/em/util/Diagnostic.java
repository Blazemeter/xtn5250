/*
    ***
!!V 07/07/98 rel. 1.07 - creation.
 */


package net.infordata.em.util;


import java.io.*;


/**
 */
public class Diagnostic {


  private static PrintWriter cvOut;


  /**
   */
  private Diagnostic() {
  }


  /**
   */
  public static PrintWriter getOut() {
    if (cvOut != null)
      return cvOut;

    if (java.beans.Beans.isDesignTime())
      try {
        cvOut = new PrintWriter(new FileOutputStream("net.infordata.em.log"),
                                true);
        cvOut.println("--" + new java.util.Date());
      }
      catch (IOException ex) {
      }

    if (cvOut == null)
      cvOut = new PrintWriter(System.out, true);
    return cvOut;
  }
}
