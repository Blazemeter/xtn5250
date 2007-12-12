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
!!V 27/05/97 rel. 1.00 - first release.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.crt5250;


import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;




///////////////////////////////////////////////////////////////////////////////

/**
 * It is used by XI5250Crt to handle the XI5250Field collection.
 *
 * @see    XI5250Crt
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250FieldsList implements XI5250BaseField, Cloneable {
  private XI5250Crt   ivCrt;
  private ArrayList<XI5250Field> ivFields = new ArrayList<XI5250Field>(40);
  private List<XI5250Field> ivROFields = Collections.unmodifiableList(ivFields);

  /**
   */
  public XI5250FieldsList(XI5250Crt aCrt) {
    ivCrt = aCrt;
  }


  /**
   * Returns a cloned XI5250FieldList
   */
  @SuppressWarnings("unchecked")
  public Object clone() {
    try {
      XI5250FieldsList aClone = (XI5250FieldsList)super.clone();
      // non clono singoli campi perchè vengono sempre ricreati da 0 e mai modificati
      aClone.ivFields = (ArrayList<XI5250Field>)ivFields.clone();
      aClone.ivROFields = Collections.unmodifiableList(aClone.ivFields);
      return aClone;
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }


  /**
   * Calls init for each field it owns.
   * @see    XI5250Field#init
   */
  public void init() {
    for (int i = 0; i < ivFields.size(); i++)
      ivFields.get(i).init();
  }


  /**
   * Calls removeNotify for each field it owns.
   */
  public void removeNotify() {
    for (int i = 0; i < ivFields.size(); i++)
      ivFields.get(i).removeNotify();
  }


  /**
   * Calls saveTo for each field it owns.
   * @see    XI5250Field#saveTo
   */
  public void saveTo(XI5250FieldSaver aSaver) throws IOException {
    for (int i = 0; i < ivFields.size(); i++)
      ivFields.get(i).saveTo(aSaver);
  }


  /**
   * Calls resized for each field it owns.
   * @see    XI5250Field#resized
   */
  public void resized() {
    for (int i = 0; i < ivFields.size(); i++)
      ivFields.get(i).resized();
  }


  /**
   * Lets fields paint themselves.
   */
  public void paint(Graphics g) {
    XI5250Field field = null;
    Rectangle   clip = g.getClipBounds();

    for (int i = 0; i < ivFields.size(); i++) {
      field = ivFields.get(i);
      Rectangle fr = field.getBoundingRect();

      if (clip.intersects(fr)) {
        Graphics fg = g.create(fr.x, fr.y, fr.width, fr.height);
        try {
          field.paint(fg);
        }
        finally {
          fg.dispose();
        }
      }
    }
  }


  /**
   * Ricerca campo che inizia da tale posizione, se non lo trova
   * ritorna un numero negativo che trasformandolo con la formula
   * idx = (-idx) - 1 indica la posizione nel vettore dove dovrebbe
   * essere inserito
   */
  private int searchField(int aCol, int aRow) {
    int kk  = ivCrt.toLinearPos(aCol, aRow);
    int min = 0;
    int max = ivFields.size();
    int med = (max + min) / 2;
    XI5250Field field;
    // ricerca binaria
    while ((med < ivFields.size()) && (min <= max)) {
      field = ivFields.get(med);
      if (field.getSortKey() == kk)
        return med;
      else if (field.getSortKey() > kk)
        max = med - 1;
      else
        min = med + 1;

      med = (max + min) / 2;
    }
    // non esiste quindi
    return -(min + 1);
  }


  /**
   * Adds a field to the fields collection.
   */
  public void addField(XI5250Field aField) {
    /*
    // !!V già presente in quella posizione, oppure overlapping,
    // sollevare una eccezione invece di return ??
    if (fieldFromPos(aField.ivCol, aField.ivRow) != null)
      return;
    */

    // !!V già presente in quella posizione, oppure overlapping,
    // viene sostituito il campo
    XI5250Field field = fieldFromPos(aField.getCol(), aField.getRow());
    if (field != null) {
      ivFields.set(fromFieldToIdx(field), aField);
    }
    else {
      int idx = searchField(aField.getCol(), aField.getRow());
      idx = (-idx) - 1;
      ivFields.add(idx, aField);
    }
  }


  /**
   * Campo presente in tale posizione oppure precedente
   */
  private XI5250Field prevFieldFromPosInternal(int aCol, int aRow) {
    int idx = searchField(aCol, aRow);
    if (idx >= 0)
      return ivFields.get(idx);

    idx = (-idx) - 1;

    if (idx == 0)
      return null;

    // accedo al precedente
    return ivFields.get(idx - 1);
  }


  /**
   * Ritorna indice del campo
   */
  protected int fromFieldToIdx(XI5250Field aField) {
    XI5250Field field;
    int i = 0;
    for (Iterator<XI5250Field> e = ivFields.iterator(); e.hasNext(); i++) {
      field = e.next();
      if (field == aField)
        return i;
    }
    return -1;
  }


  /**
   * Returns the field present in the given position, null if none.
   */
  public XI5250Field fieldFromPos(int aCol, int aRow) {
    // accedo al precedente
    XI5250Field field = prevFieldFromPosInternal(aCol, aRow);
    if (field == null)
      return null;

    int kk  = ivCrt.toLinearPos(aCol, aRow);
    int fk  = field.getSortKey();
    // verifico che posizione sia sul campo
    if ((kk >= fk) && (kk < (fk + field.getLength())))
      return field;
    else
      return null;
  }


  /**
   */
  public XI5250Field nextFieldFromPos(int aCol, int aRow) {
    if (ivFields.isEmpty())
      return null;
    // accedo al precedente
    XI5250Field field = prevFieldFromPosInternal(aCol, aRow);
    if (field == null || field == ivFields.get(ivFields.size() - 1))
      return ((!ivFields.isEmpty()) ? ivFields.get(0) :
                                      null);

    int idx = fromFieldToIdx(field);
    return ivFields.get(idx + 1);
  }


  /**
   */
  public XI5250Field prevFieldFromPos(int aCol, int aRow) {
    if (ivFields.isEmpty())
      return null;
    XI5250Field field = fieldFromPos(aCol, aRow);
    // caso cursore sul campo
    if (field != null) {
      if (field == ivFields.get(0))
        return ivFields.get(ivFields.size() - 1);

      int idx = fromFieldToIdx(field);
      idx = (idx == 0) ? (ivFields.size() - 1) : idx - 1;
      return ivFields.get(idx);
    }

    // caso cursore non sul campo
    // accedo al precedente
    field = prevFieldFromPosInternal(aCol, aRow);
    if (field == null)
      return ((!ivFields.isEmpty()) ? ivFields.get(ivFields.size() - 1) : null);

    return field;
  }


  /**
   */
  public List<XI5250Field> getFields() {
    return ivROFields;
  }


  /**
   * Returns the field at the given index (null if none).
   * Fields enumeration is based on their linear buffer position.
   */
  public XI5250Field getField(int idx) {
    try {
      return (XI5250Field)ivFields.get(idx);
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      return null;
    }
  }
}