/*
!!V 15/07/97 rel. 1.02c- XIDataOrd includes 0x1F char.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.tn5250;


import java.io.*;
import java.util.*;

import net.infordata.em.tnprot.*;
import net.infordata.em.util.*;



///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 Orders list (Works like a macro order).
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250OrdList extends XI5250Ord {

  private static Class[] cv5250OrdClasses = new Class[0x1F + 1];

  protected Vector       ivOrdVect;

  protected boolean[]    ivOrdPresent = new boolean[0x1F + 1];


  static {
    cv5250OrdClasses[XI5250Emulator.ORD_IC] = XIICOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_RA] = XIRAOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_SBA] = XISBAOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_SF] = XISFOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_SOH] = XISOHOrd.class;
  }


  /**
   */
  protected XI5250OrdList(XI5250Emulator aEmulator) {
    init(aEmulator);
  }


  /**
   */
  public boolean isOrderPresent(byte aOrder) {
    return ivOrdPresent[aOrder];
  }


  /**
   * @exception    XI5250Exception    raised if order parameters are wrong.
   */
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {

    int       bb;
    XI5250Ord ord;
    ivOrdVect = new Vector(100, 20);

    if (XI5250Emulator.DEBUG >= 2)
      Diagnostic.getOut().println("  START OF ORDERS LIST");

    for (int i = 0; ; i++) {
      inStream.mark(1);
      if ((bb = inStream.read()) == -1)
        break;

      if ((byte)bb == XI5250Emulator.ESC) {
        inStream.reset();
        break;
      }

      if (bb == 0x00 || bb == 0x1C || bb >= 0x1F)
        inStream.reset();          // need it (it is also the color attribute)
      else
        ivOrdPresent[bb] = true;  // remember orders present

      try {
        ord = createOrdInstance(bb);
      }
      catch (Exception ex) {
        throw new RuntimeException("Error during instance creation");
      }

      if (ord != null) {
        ord.init(ivEmulator);
        ord.readFrom5250Stream(inStream);

        if (XI5250Emulator.DEBUG >= 2)
          Diagnostic.getOut().println("  " + ord);

        ivOrdVect.addElement(ord);
      }
      else {
        throw new XI5250Exception("Order not supported : 0x" + XITelnet.toHex((byte)bb));
      }
    }
  }


  /**
   */
  protected void execute() {
    for (int i = 0; i < ivOrdVect.size(); i++)
      ((XI5250Ord)ivOrdVect.elementAt(i)).execute();
  }


  /**
   * Creates the 5250 order instance related to the given 5250 order id.
   * @exception    IllegalAccessException .
   * @exception    InstantiationException .
   */
  public XI5250Ord createOrdInstance(int aOrd)
      throws IllegalAccessException, InstantiationException {

    Class     cls;

    if (aOrd == 0x00 || aOrd == 0x1C || aOrd >= 0x1F)
      cls = XIDataOrd.class;
    else
      cls = cv5250OrdClasses[aOrd];

    if (cls != null)
      return (XI5250Ord)cls.newInstance();
    else
      return null;
  }


  /**
   */
  public String toString() {
    String ss = super.toString() + ivOrdVect.toString();
    return ss;
  }
}