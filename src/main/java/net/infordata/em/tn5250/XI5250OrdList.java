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
!!V 15/07/97 rel. 1.02c- XIDataOrd includes 0x1F char.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.tn5250;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.infordata.em.tnprot.XITelnet;

/**
 * 5250 Orders list.
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250OrdList extends XI5250Ord {

  private static final Logger LOGGER = Logger.getLogger(XI5250OrdList.class.getName());
  
  private static Class<?>[] cv5250OrdClasses = new Class<?>[256];

  protected List<XI5250Ord> ivOrdVect;

  protected boolean[]    ivOrdPresent = new boolean[256];

  static {
    cv5250OrdClasses[XI5250Emulator.ORD_IC] = XIICOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_RA] = XIRAOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_SBA] = XISBAOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_SF] = XISFOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_SOH] = XISOHOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_MC] = XIMCOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_EA] = XIEAOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_TD] = XITDOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_WEA] = XIWEAOrd.class;
    cv5250OrdClasses[XI5250Emulator.ORD_WDSF] = XIWdsfOrd.class;
  }

  protected XI5250OrdList(XI5250Emulator aEmulator) {
    init(aEmulator);
  }

  public boolean isOrderPresent(byte aOrder) {
    return ivOrdPresent[aOrder];
  }

  /**
   * @param inStream the stream from where to read the order list from.
   * @throws XI5250Exception raised if order parameters are wrong.
   * @throws IOException raised when there is an input/output problem.
   */
  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {

    int       bb;
    XI5250Ord ord;
    ivOrdVect = new ArrayList<>(100);

    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.finer("  START OF ORDERS LIST");
    
    while(true) {
      inStream.mark(1);
      if ((bb = inStream.read()) == -1)
        break;

      if ((byte)bb == XI5250Emulator.ESC) {
        inStream.reset();
        break;
      }

      if (XIDataOrd.isDataCharacter(bb)) {
        inStream.reset();          // need it (it is also the color attribute)
        if (ivEmulator.isStrPcCmdEnabled()) {  
          inStream.mark(XI5250Emulator.STRPCCMD.length);
          byte[] lhbb = new byte[XI5250Emulator.STRPCCMD.length];
          int sz = inStream.read(lhbb);
          if (sz == XI5250Emulator.STRPCCMD.length) {
            if (Arrays.equals(lhbb, XI5250Emulator.STRPCCMD)) {
              ivEmulator.receivedStrPcCmd();
            }
            else if (Arrays.equals(lhbb, XI5250Emulator.ENDSTRPCCMD)) {
              ivEmulator.receivedEndStrPcCmd();
            }
            else {
              inStream.reset();
            }
          }
          else {
            inStream.reset();
          }
        }
      }
      else
        ivOrdPresent[bb] = true;  // remember orders present

      try {
        ord = createOrdInstance(bb);
      }
      catch (Exception ex) {
        throw new RuntimeException(ex);
      }

      if (ord != null) {
        ord.init(ivEmulator);
        ord.readFrom5250Stream(inStream);

        if (LOGGER.isLoggable(Level.FINER))
          LOGGER.finer("  " + ord);

        ivOrdVect.add(ord);
      }
      else {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("Order not supported : 0x" + XITelnet.toHex((byte)bb));
          for (int ii = 0; ii < ivOrdVect.size(); ii++) {
            LOGGER.fine("Prev. order[" + ii + "]: " + ivOrdVect.get(ii));
          }
          byte[] buf = new byte[10]; 
          int count = inStream.read(buf);
          LOGGER.fine("Next " + count + " bytes: " + XITelnet.toHex(buf, count));
        }
        throw new XI5250Exception("Order not supported : 0x" + XITelnet.toHex((byte)bb),
            XI5250Emulator.ERR_INVALID_COMMAND);
      }
    }
  }

  @Override
  protected void execute() {
    for (XI5250Ord anIvOrdVect : ivOrdVect) {
      anIvOrdVect.execute();
    }
  }

  /**
   * Creates the 5250 order instance related to the given 5250 order id.
   * @param aOrd order id to create
   * @return created order
   * @throws    IllegalAccessException .
   * @throws    InstantiationException .
   */
  public XI5250Ord createOrdInstance(int aOrd)
      throws IllegalAccessException, InstantiationException {

    Class<?>     cls;

    if (XIDataOrd.isDataCharacter(aOrd))
      cls = XIDataOrd.class;
    else
      cls = cv5250OrdClasses[aOrd];

    if (cls != null)
      return (XI5250Ord)cls.newInstance();
    else
      return null;
  }

  @Override
  public String toString() {
    return super.toString() + ivOrdVect.toString();
  }

}
