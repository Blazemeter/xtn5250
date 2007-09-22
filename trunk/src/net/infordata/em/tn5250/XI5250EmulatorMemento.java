/*
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.tn5250;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import net.infordata.em.crt5250.*;
import net.infordata.em.tnprot.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * Used to store, without exposing it, the internal state of XI5250Emulator.
 *
 * @see    XI5250Emulator#createMemento
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250EmulatorMemento {

  protected XI5250FieldsList ivFields;

  protected int             ivFunctionKeysMask;

  protected XI5250Cmd       ivPendingCmd;

  protected int             ivState;
  protected int             ivPrevState;

  protected int             ivCol;
  protected int             ivRow;

  protected int             ivErrorRow;

  // from super classes
  protected XI5250CrtBuffer ivCrtBuffer;


  public XI5250EmulatorMemento(XI5250FieldsList aFieldList,
                               int aFunctionKeysMask, XI5250Cmd aPendingCmd,
                               int aState, int aPrevState,
                               int aCol, int aRow,
                               int aErrorRow,
                               XI5250CrtBuffer aCrtBuffer) {
    ivFields = aFieldList;

    ivFunctionKeysMask = aFunctionKeysMask;

    ivPendingCmd = aPendingCmd;

    ivState = aState;
    ivPrevState = aPrevState;

    ivCol = aCol;
    ivRow = aRow;

    ivErrorRow = aErrorRow;

    ivCrtBuffer = aCrtBuffer;
  }
}