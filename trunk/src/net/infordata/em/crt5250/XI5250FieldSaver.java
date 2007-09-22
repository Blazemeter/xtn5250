/*
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.crt5250;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import net.infordata.em.tnprot.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * Interface that must be implemented to save XI5250 field status.
 * 
 * @see    XI5250Field#saveTo
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250FieldSaver {
  /**
   *
   */
  public void write(XI5250Field aField, String aStr) throws IOException;
}