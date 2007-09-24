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
