/*
!!V 03/03/98 rel. _.___- SWING and reorganization.
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
 * Common base interface for XI5250FieldsList and XI5250Field.
 * Only for possible future implementations.
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public interface XI5250BaseField {
  /**
   */
  public void init();


  /**
   */
  public void saveTo(XI5250FieldSaver aSaver) throws IOException;
}