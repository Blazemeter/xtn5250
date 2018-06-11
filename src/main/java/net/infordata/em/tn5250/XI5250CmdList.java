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


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.infordata.em.tnprot.XITelnet;



///////////////////////////////////////////////////////////////////////////////

/**
 * 5250 Commands list (Works like a macro command).
 *
 * @version  
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XI5250CmdList extends XI5250Cmd {

  private static final Logger LOGGER = Logger.getLogger(XI5250CmdList.class.getName());
  
  private static Class<?>[] cv5250CmdClasses = new Class[256];;

  protected List<XI5250Cmd> ivCmdVect;

  // flags used across command execution
  protected boolean ivICOrderExecuted;


  static {
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_IMMEDIATE)] =
        XIReadImmediateCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_FIELDS)] =
        XIReadFieldsCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_MDT_FIELDS)] =
        XIReadMdtFieldsCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_READ_SCREEN)] =
        XIReadScreenCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_SAVE_SCREEN)] =
        XISaveScreenCmd.class;

    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_CLEAR_FMT_TABLE)] =
        XIClearFmtTableCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_CLEAR_UNIT)] =
        XIClearUnitCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_CLEAR_UNIT_ALT)] =
        XIClearUnitAltCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_RESTORE_SCREEN)] =
        XIRestoreScreenCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_ROLL)] =
        XIRollCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_WRITE_ERROR_CODE)] =
        XIWriteErrorCodeCmd.class;
    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_WRITE_TO_DISPLAY)] =
        XIWriteToDisplayCmd.class;

    cv5250CmdClasses[XITelnet.toInt(XI5250Emulator.CMD_QUERY_DEVICE)] =
        XIQueryCmd.class;
  }


  /**
   */
  protected XI5250CmdList(XI5250Emulator aEmulator) {
    init(aEmulator);
  }


  /**
   * @exception    XI5250Exception    raised if command parameters are wrong.
   */
  @Override
  protected void readFrom5250Stream(InputStream inStream)
      throws IOException, XI5250Exception {
    int       bb;
    XI5250Cmd cmd;
    ivCmdVect = new ArrayList<XI5250Cmd>(100);

    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.finer("START OF COMMANDS LIST");

    // jump until ESC is found
    while ((bb = inStream.read()) != -1 && (byte)bb != XI5250Emulator.ESC)
      ;

    // start from 1 because ESC is already read
    for (int i = 1; (bb = inStream.read()) != -1; i++) {
      switch (i % 2) {
        case 0:
          if ((byte)bb != XI5250Emulator.ESC)
            throw new XI5250Exception("Malformed 5250 packet", XI5250Emulator.ERR_INVALID_COMMAND);
          break;
        case 1:
          try {
            cmd = createCmdInstance(XITelnet.toInt((byte)bb));
          }
          catch (Exception ex) {
            throw new RuntimeException(ex);
          }

          if (cmd != null) {
            cmd.init(ivEmulator);
            cmd.readFrom5250Stream(inStream);

            if (LOGGER.isLoggable(Level.FINER))
              LOGGER.finer("" + cmd);

            ivCmdVect.add(cmd);
          }
          else {
            throw new XI5250Exception("Command not supported : 0x" +
                                      XITelnet.toHex((byte)bb), 
                                      XI5250Emulator.ERR_INVALID_COMMAND);
          }
          break;
      }
    }
  }


  /**
   */
  @Override
  protected void execute() {
    for (int i = 0; i < ivCmdVect.size(); i++)
      ivCmdVect.get(i).execute();
  }


  /**
   * Creates the 5250 command related to the given 5250 command id.
   * @exception    IllegalAccessException .
   * @exception    InstantiationException .
   */
  protected XI5250Cmd createCmdInstance(int aCmd)
      throws IllegalAccessException, InstantiationException {
    Class<?>     cls;

    cls = cv5250CmdClasses[aCmd];
    if (cls != null)
      return (XI5250Cmd)cls.newInstance();
    else
      return null;
  }


  /**
   */
  @Override
  public String toString() {
    String ss = super.toString() + ivCmdVect.toString();
    return ss;
  }
}