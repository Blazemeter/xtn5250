/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.io.*;
import java.util.*;

import net.infordata.em.crt5250.*;
import net.infordata.em.tnprot.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 Save screen command
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XISaveScreenCmd extends XI5250Cmd {

  protected void readFrom5250Stream(InputStream inStream) throws IOException {
  }


  protected void execute() {
  	/*!!1.06a
    int pos = ivEmulator.ivSavedScreenList.size();
    ivEmulator.ivSavedScreenList.addElement(ivEmulator.createMemento());
    */
    //!!1.06a
    int pos = (ivEmulator.ivSavedScreensIdx++) % ivEmulator.ivSavedScreens.length;
    ivEmulator.ivSavedScreens[pos] = ivEmulator.createMemento();

    ivEmulator.send5250SavedScreen(pos);
  }
}