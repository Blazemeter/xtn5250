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
 */

package net.infordata.em.tn5250;

import net.infordata.em.crt5250.XI5250CrtBuffer;
import net.infordata.em.crt5250.XI5250FieldsList;

/**
 * Used to store, without exposing it, the internal state of XI5250Emulator.
 *
 * @see    XI5250Emulator#createMemento
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