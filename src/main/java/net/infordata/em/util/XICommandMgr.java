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
!!V 07/07/98 rel. 1.07 - creation.
 */

package net.infordata.em.util;

import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;

public class XICommandMgr {

  private static final Logger LOGGER = Logger.getLogger(XICommandMgr.class.getName());

  // relazione action-command -> XICommand
  private Map<String, XICommand> ivActCmd2Cmd = new HashMap<>();

  // action-commands disabilitati
  private List<String> ivDisabledActCmd = new ArrayList<>();

  // relazione oggetto -> action-command
  private Map<Object, String> ivObj2ActCmd = new HashMap<>();

  // action-command -> boolean value
  private Map<String, Boolean> ivActCmd2Value = new HashMap<>();

  // lock per gestione action commands
  private Object ivActCmdLock = new Object();

  //
  private ActionListener ivActionListener = XICommandMgr.this::actionPerformed;

  // usato per sopperire ad un buco delle AWT - CheckboxMenuItem non inviano
  // action command
  private ItemListener ivItemListener;

  public XICommandMgr() {
    ivItemListener = e -> XICommandMgr.this.actionPerformed(new ActionEvent(e.getSource(),
        ActionEvent.ACTION_PERFORMED,
        null));
  }

  /**
   * L' ActionListener utilizzato per la gestione degli action command.
   *
   * @return the action listener.
   */
  public ActionListener getActionListener() {
    return ivActionListener;
  }

  private void actionPerformed(ActionEvent e) {

    if (LOGGER.isLoggable(Level.FINER)) {
      LOGGER.finer("" + e);
    }

    String actCmd = ivObj2ActCmd.get(e.getSource());

    // se l' oggetto sorgente dell' evento non ha un action command associato lo  prelevo
    // dall' evento stesso
    if (actCmd == null) {
      actCmd = e.getActionCommand();
    }

    if (actCmd != null) {
      dispatchCommand(e.getSource(), actCmd);
    }
  }

  public void dispatchCommand(String cmd) {
    dispatchCommand(null, cmd);
  }

  protected void dispatchCommand(Object cmp, String cmd) {

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("dispatchCommand " + cmp + " " + cmd);
    }

    boolean flag;

    synchronized (ivActCmdLock) {

      // stato associato al comando
      boolean flagCmd = getCommandState(cmd);

      // stato associato al componente
      boolean flagCmp = !flagCmd;
      if (cmp instanceof CheckboxMenuItem) {
        flagCmp = ((CheckboxMenuItem) cmp).getState();
      }

      boolean enabled = isCommandEnabled(cmd);

      flag = (enabled) ? flagCmp : flagCmd;
    }

    setCommandState(cmd, flag);
  }

  public void setCommandState(String cmd, boolean value) {

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("setCommandState " + cmd + " " + value);
    }

    boolean oldCmdState;

    synchronized (ivActCmdLock) {

      oldCmdState = getCommandState(cmd);

      ivActCmd2Value.put(cmd, new Boolean(value));

      // sincronizzo lo stato di tutti gli oggetti associati al comando
      Object obj;
      for (Iterator<Object> e = ivObj2ActCmd.keySet().iterator(); e.hasNext(); ) {
        obj = e.next();

        if (cmd.equals(ivObj2ActCmd.get(obj))) {

          if (obj instanceof AbstractButton &&
              ((AbstractButton) obj).isSelected() != value) {
            ((AbstractButton) obj).setSelected(value);
          } else if (obj instanceof CheckboxMenuItem &&
              ((CheckboxMenuItem) obj).getState() != value) {
            ((CheckboxMenuItem) obj).setState(value);
          }
        }
      }
    }

    if (value != oldCmdState) {
      processCommand(cmd);
    }
  }

  /**
   * Ritorna lo stato associato al comando
   *
   * @param cmd command to get the state from
   * @return state of the command
   */
  public boolean getCommandState(String cmd) {
    Boolean app = ivActCmd2Value.get(cmd);
    return (app != null) ? app : false;
  }

  /**
   * Ricerca l' XACommand associato con l' action command cmd e, se presente, lo esegue chiamandone
   * il metodo execute.
   *
   * @param anActCmd command to process.
   * @see MenuItem#setActionCommand
   * @see Button#setActionCommand
   */
  protected void processCommand(String anActCmd) {

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("processActionCommand " + anActCmd);
    }

    XICommand command = getCommand(anActCmd);
    if (command != null) {
      command.execute();
    }
  }

  /*
   * Richiamata dagli altri metodi handleCommand.
   */
  private void handleActionCommand(Object obj, String anActionCommand) {
    if (obj == null) {
      throw new IllegalArgumentException("anObj can't be null");
    }

    if (anActionCommand != null) {
      ivObj2ActCmd.put(obj, anActionCommand);
      // reinizializzo stato comando
      setCommandState(anActionCommand, getCommandState(anActionCommand));
    } else {
      ivObj2ActCmd.remove(obj);
    }
  }

  /**
   * Associa un ActionCommand ad un MenuItem e lo inserisce nella gestione di XACommandManager, se
   * anActionCommand null allora elimina l' oggetto da tale gestione.
   *
   * @param aMenuItem menu item to associated the command to
   * @param anActionCommand command to associate to the menu item.
   */
  public void handleCommand(MenuItem aMenuItem, String anActionCommand) {

    synchronized (ivActCmdLock) {
      handleActionCommand(aMenuItem, anActionCommand);

      aMenuItem.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aMenuItem.addActionListener(ivActionListener);
        aMenuItem.setEnabled(isCommandEnabled(anActionCommand));
      } else {
        aMenuItem.removeActionListener(ivActionListener);
      }
    }
  }

  /**
   * Associa un ActionCommand ad un CheckboxMenuItem e lo inserisce nella gestione di
   * XACommandManager, se anActionCommand null allora elimina l' oggetto da tale gestione.
   *
   * @param aMenuItem menu item to associated the command to
   * @param anActionCommand command to associate to the menu item.
   */
  public void handleCommand(CheckboxMenuItem aMenuItem, String anActionCommand) {

    synchronized (ivActCmdLock) {
      handleActionCommand(aMenuItem, anActionCommand);

      aMenuItem.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aMenuItem.addItemListener(ivItemListener);
        aMenuItem.setEnabled(isCommandEnabled(anActionCommand));
      } else {
        aMenuItem.removeItemListener(ivItemListener);
      }
    }
  }

  /**
   * Associa un ActionCommand ad un Button.
   *
   * @param aButton button to associated the command to
   * @param anActionCommand command to associate to the button.
   */
  public void handleCommand(Button aButton, String anActionCommand) {

    synchronized (ivActCmdLock) {
      handleActionCommand(aButton, anActionCommand);

      aButton.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aButton.addActionListener(ivActionListener);
        aButton.setEnabled(isCommandEnabled(anActionCommand));
      } else {
        aButton.removeActionListener(ivActionListener);
      }
    }
  }

  /**
   * Associa un ActionCommand ad un AbstractButton.
   *
   * @param aButton button to associated the command to
   * @param anActionCommand command to associate to the button.
   */
  public void handleCommand(AbstractButton aButton, String anActionCommand) {

    synchronized (ivActCmdLock) {
      handleActionCommand(aButton, anActionCommand);

      aButton.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aButton.addActionListener(ivActionListener);
        aButton.setEnabled(isCommandEnabled(anActionCommand));
      } else {
        aButton.removeActionListener(ivActionListener);
      }
    }
  }

  /**
   * Abilita o disabilita un action-command e tutti i controlli ad esso associati.
   *
   * @param anActionCommand command to enable/disable
   * @param toBeEnabled true to enable the command, false to disable it.
   */
  public void enableCommand(String anActionCommand, boolean toBeEnabled) {

    if (anActionCommand == null) {
      throw new IllegalArgumentException("The ActionCommand can't be null.");
    }

    synchronized (ivActCmdLock) {
      // verifico se action-command gi nello stato voluto
      if (toBeEnabled == isCommandEnabled(anActionCommand)) {
        return;
      }

      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine(anActionCommand + " " + (toBeEnabled ? "enabled" : "disabled"));
      }

      if (toBeEnabled) {
        ivDisabledActCmd.remove(anActionCommand);
      } else {
        ivDisabledActCmd.add(anActionCommand);
      }

      // abilito/disabilito tutti gli oggetti associati ad un determinato action-command
      Object obj;
      for (Iterator<Object> e = ivObj2ActCmd.keySet().iterator(); e.hasNext(); ) {
        obj = e.next();

        if (anActionCommand.equals(ivObj2ActCmd.get(obj))) {

          if (obj instanceof MenuItem) {
            ((MenuItem) obj).setEnabled(toBeEnabled);
          } else if (obj instanceof Button) {
            ((Button) obj).setEnabled(toBeEnabled);
          } else if (obj instanceof AbstractButton) {
            ((AbstractButton) obj).setEnabled(toBeEnabled);
          } else {
            throw new IllegalStateException("An invalid class type was found.");
          }
        }
      }
    }
  }

  public final boolean isCommandEnabled(String anActionCommand) {
    return !ivDisabledActCmd.contains(anActionCommand);
  }

  /**
   * Utilizzato per associare una instanza di una classe che implementa l' interfaccia XACommand ad
   * un action command (sia esso generato da un men o da un bottone)
   * <pre>
   * es.
   * // NB: in questo esempio viene utilizzata una inner-class, normalmente consigliabile
   * // utilizzare una classe normale o una nested class.
   *
   * XAMainFrame frm = new XAMainFrame();
   * frm.setCommand(XAMainFrame.NEW_CMD,
   *                new XACommand()
   *                {
   *                  public void execute()
   *                  {
   *                    System.out.println("Comando eseguito");
   *                  }
   *                });
   * frm.show();
   * </pre>
   *
   * @param anActionCommand the action command to associate the command to
   * @param aCommand the command to associate to the action command
   */
  public void setCommand(String anActionCommand, XICommand aCommand) {

    if (anActionCommand == null) {
      throw new IllegalArgumentException("The ActionCommand can't be null.");
    }

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(aCommand + " -> " + anActionCommand);
    }

    ivActCmd2Cmd.put(anActionCommand, aCommand);
  }

  /**
   * Ritorna l' instanza di XACommand associata con l' action command (null se nessuno).
   *
   * @param anActionCommand action command to get the associated command from
   * @return the command associated to the given action command.
   */
  public XICommand getCommand(String anActionCommand) {
    return ivActCmd2Cmd.get(anActionCommand);
  }

}
