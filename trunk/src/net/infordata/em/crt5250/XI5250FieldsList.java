/*
!!V 27/05/97 rel. 1.00 - first release.
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
 * It is used by XI5250Crt to handle the XI5250Field collection.
 *
 * @see    XI5250Crt
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250FieldsList implements XI5250BaseField, Cloneable {
  private XI5250Crt   ivCrt;
  private Vector      ivFields = new Vector(40, 20);


  /**
   */
  public XI5250FieldsList(XI5250Crt aCrt) {
    ivCrt = aCrt;
  }


  /**
   * Returns a cloned XI5250FieldList
   */
  public Object clone() {
    try {
      XI5250FieldsList aClone = (XI5250FieldsList)super.clone();
      // non clono singoli campi perchè vengono sempre ricreati da 0 e mai modificati
      aClone.ivFields = (Vector)ivFields.clone();
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
      ((XI5250Field)ivFields.elementAt(i)).init();
  }


  /**
   * Calls removeNotify for each field it owns.
   */
  public void removeNotify() {
    for (int i = 0; i < ivFields.size(); i++)
      ((XI5250Field)ivFields.elementAt(i)).removeNotify();
  }


  /**
   * Calls saveTo for each field it owns.
   * @see    XI5250Field#saveTo
   */
  public void saveTo(XI5250FieldSaver aSaver) throws IOException {
    for (int i = 0; i < ivFields.size(); i++)
      ((XI5250Field)ivFields.elementAt(i)).saveTo(aSaver);
  }


  /**
   * Calls resized for each field it owns.
   * @see    XI5250Field#resized
   */
  public void resized() {
    for (int i = 0; i < ivFields.size(); i++)
      ((XI5250Field)ivFields.elementAt(i)).resized();
  }


  /**
   * Lets fields paint themselves.
   */
  public void paint(Graphics g) {
    XI5250Field field = null;
    Rectangle   clip = g.getClipBounds();

    for (int i = 0; i < ivFields.size(); i++) {
      field = (XI5250Field)ivFields.elementAt(i);
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
      field = (XI5250Field)ivFields.elementAt(med);
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
      ivFields.setElementAt(aField, fromFieldToIdx(field));
    }
    else {
      int idx = searchField(aField.getCol(), aField.getRow());
      idx = (-idx) - 1;
      ivFields.insertElementAt(aField, idx);
    }
  }


  /**
   * Campo presente in tale posizione oppure precedente
   */
  private XI5250Field prevFieldFromPosInternal(int aCol, int aRow) {
    int idx = searchField(aCol, aRow);
    if (idx >= 0)
      return (XI5250Field)ivFields.elementAt(idx);

    idx = (-idx) - 1;

    if (idx == 0)
      return null;

    // accedo al precedente
    return (XI5250Field)ivFields.elementAt(idx - 1);
  }


  /**
   * Ritorna indice del campo
   */
  protected int fromFieldToIdx(XI5250Field aField) {
    XI5250Field field;
    int i = 0;
    for (Enumeration e = ivFields.elements(); e.hasMoreElements(); i++) {
      field = (XI5250Field)e.nextElement();
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
    // accedo al precedente
    XI5250Field field = prevFieldFromPosInternal(aCol, aRow);
    if (field == null || field == ivFields.lastElement())
      return ((!ivFields.isEmpty()) ? (XI5250Field)ivFields.firstElement() :
                                      null);

    int idx = fromFieldToIdx(field);
    return (XI5250Field)ivFields.elementAt(idx + 1);
  }


  /**
   */
  public XI5250Field prevFieldFromPos(int aCol, int aRow) {
    XI5250Field field = fieldFromPos(aCol, aRow);
    // caso cursore sul campo
    if (field != null) {
      if (field == ivFields.firstElement())
        return (XI5250Field)ivFields.lastElement();

      int idx = fromFieldToIdx(field);
      idx = (idx == 0) ? (ivFields.size() - 1) : idx - 1;
      return (XI5250Field)ivFields.elementAt(idx);
    }

    // caso cursore non sul campo
    // accedo al precedente
    field = prevFieldFromPosInternal(aCol, aRow);
    if (field == null)
      return ((!ivFields.isEmpty()) ? (XI5250Field)ivFields.lastElement() : null);

    return field;
  }


  /**
   * Returns the fields enumeration.
   * Fields enumeration is based on their linear buffer position.
   */
  public Enumeration getFields() {
    return ivFields.elements();
  }


  /**
   * Returns the field at the given index (null if none).
   * Fields enumeration is based on their linear buffer position.
   */
  public XI5250Field getField(int idx) {
    try {
      return (XI5250Field)ivFields.elementAt(idx);
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      return null;
    }
  }
}