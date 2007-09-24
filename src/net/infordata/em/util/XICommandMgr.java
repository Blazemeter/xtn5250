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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;


/**
 */
public class XICommandMgr {

  // Debug level 0 = none, 1 = , 2 = detailed
  static final int DEBUG = 0;

  // relazione action-command -> XACommand
  private Hashtable       ivActCmd2Cmd = new Hashtable();

  // action-commands disabilitati
  private Vector          ivDisabledActCmd = new Vector();

  // relazione oggetto -> action-command
  private Hashtable       ivObj2ActCmd = new Hashtable();

  // action-command -> boolean value
  private Hashtable       ivActCmd2Value = new Hashtable();

  // lock per gestione action commands
  private Object          ivActCmdLock = new Object();

  //
  private ActionListener  ivActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      XICommandMgr.this.actionPerformed(e);
    }
  };

  // usato per sopperire ad un buco delle AWT - CheckboxMenuItem non inviano
  // action command
  private ItemListener    ivItemListener;


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  public XICommandMgr() {

    ivItemListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        XICommandMgr.this.actionPerformed(new ActionEvent(e.getSource(),
                                          ActionEvent.ACTION_PERFORMED,
                                          null));
      }
    };
  }


  /**
   * L' ActionListener utilizzato per la gestione degli action command.
   */
  public ActionListener getActionListener() {
    return ivActionListener;
  }


  /**
   */
  private void actionPerformed(ActionEvent e) {
  
    if (DEBUG >= 2)
      Diagnostic.getOut().println(e);

    String actCmd = (String)ivObj2ActCmd.get(e.getSource());
    
    // se l' oggetto sorgente dell' evento non ha un action command associato lo  prelevo
    // dall' evento stesso
    if (actCmd == null)
      actCmd = e.getActionCommand();
    
    if (actCmd != null)
      dispatchCommand(e.getSource(), actCmd);
  }
  
  
  /**
   */
  public void dispatchCommand(String cmd) {
    dispatchCommand(null, cmd);
  }
  
  
  /**
   */
  protected void dispatchCommand(Object cmp, String cmd) {
    
    if (DEBUG >= 1)
      Diagnostic.getOut().println("dispatchCommand " + cmp + " " + cmd);

    boolean flag;
    
    synchronized (ivActCmdLock) {
    
      // stato associato al comando
      boolean flagCmd = getCommandState(cmd);
  
      // stato associato al componente
      boolean flagCmp = !flagCmd;
      if (cmp instanceof CheckboxMenuItem)
        flagCmp = ((CheckboxMenuItem)cmp).getState();
      //!!V NON SERVE else if (cmp instanceof AbstractButton)
      //!!V NON SERVE   flagCmp = ((AbstractButton)cmp).isSelected();

      boolean enabled = isCommandEnabled(cmd);
    
      flag = (enabled) ? flagCmp : flagCmd;
    }  
      
    setCommandState(cmd, flag);
  }
      

  /**
   */
  public void setCommandState(String cmd, boolean value) {
    
    if (DEBUG >= 1)
      Diagnostic.getOut().println("setCommandState " + cmd + " " + value);

    boolean oldCmdState;
    
    synchronized (ivActCmdLock) {

      oldCmdState = getCommandState(cmd); 
    
      ivActCmd2Value.put(cmd, new Boolean(value));
      
      // sincronizzo lo stato di tutti gli oggetti associati al comando
      Object obj;
      for (Enumeration e = ivObj2ActCmd.keys(); e.hasMoreElements(); ) {
        obj = e.nextElement();
      
        if (cmd.equals(ivObj2ActCmd.get(obj))) {
      
          if (obj instanceof AbstractButton &&
              ((AbstractButton)obj).isSelected() != value) {
            ((AbstractButton)obj).setSelected(value);
          }
          else if (obj instanceof CheckboxMenuItem &&
              ((CheckboxMenuItem)obj).getState() != value) {
            ((CheckboxMenuItem)obj).setState(value);
          }  
        }
      }    
    }
    
    if (value != oldCmdState)
      processCommand(cmd);
  }
   
   
  /**
   * Ritorna lo stato associato al comando
   */
  public boolean getCommandState(String cmd) {
    Boolean app = (Boolean)ivActCmd2Value.get(cmd);
    
    return (app != null) ? app.booleanValue() : false;
  }
   
      
  /**
   * Ricerca l' XACommand associato con l' action command cmd e, se presente, lo esegue
   * chiamandone il metodo execute.
   *   
   * @see    MenuItem#setActionCommand
   * @see    Button#setActionCommand
   * @see    XIBaseButton#setActionCommand
   */
  protected void processCommand(String anActCmd) {
  
    if (DEBUG >= 1)
      Diagnostic.getOut().println("processActionCommand " + anActCmd);

    XICommand command = getCommand(anActCmd);
    if (command != null)
      command.execute();
  }
  
  
  /**
   * Richiamata dagli altri metodi handleCommand.
   */
  private void handleActionCommand(Object obj, String anActionCommand) {
    if (obj == null)
      throw new IllegalArgumentException("anObj can't be null");
    
    if (anActionCommand != null) {
      ivObj2ActCmd.put(obj, anActionCommand);  
      // reinizializzo stato comando
      setCommandState(anActionCommand, getCommandState(anActionCommand));
    }  
    else
      ivObj2ActCmd.remove(obj);  
  }

   
  /**
   * Associa un ActionCommand ad un MenuItem e lo inserisce nella gestione di XACommandManager,
   * se anActionCommand è null allora elimina l' oggetto da tale gestione.
   */
  public void handleCommand(MenuItem aMenuItem, String anActionCommand) {
  
    synchronized (ivActCmdLock) {
      handleActionCommand((Object)aMenuItem, anActionCommand);
    
      aMenuItem.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aMenuItem.addActionListener(ivActionListener);  
        aMenuItem.setEnabled(isCommandEnabled(anActionCommand));
      }  
      else 
        aMenuItem.removeActionListener(ivActionListener);  
    }    
  }
  
 
  /**
   * Associa un ActionCommand ad un CheckboxMenuItem e lo inserisce nella gestione di XACommandManager,
   * se anActionCommand è null allora elimina l' oggetto da tale gestione.
   */
  public void handleCommand(CheckboxMenuItem aMenuItem, String anActionCommand) {
  
    synchronized (ivActCmdLock) {
      handleActionCommand((Object)aMenuItem, anActionCommand);
    
      aMenuItem.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aMenuItem.addItemListener(ivItemListener);  
        aMenuItem.setEnabled(isCommandEnabled(anActionCommand));
      }  
      else 
        aMenuItem.removeItemListener(ivItemListener);  
    }    
  }
  
 
  /**
   * Associa un ActionCommand ad un Button.
   */
  public void handleCommand(Button aButton, String anActionCommand) {
  
    synchronized (ivActCmdLock) {
      handleActionCommand((Object)aButton, anActionCommand);
    
      aButton.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aButton.addActionListener(ivActionListener);  
        aButton.setEnabled(isCommandEnabled(anActionCommand));
      }  
      else 
        aButton.removeActionListener(ivActionListener);  
    }    
  }
  
 
  /**
   * Associa un ActionCommand ad un AbstractButton.
   */
  public void handleCommand(AbstractButton aButton, String anActionCommand) {

    synchronized (ivActCmdLock) {
      handleActionCommand((Object)aButton, anActionCommand);

      aButton.setActionCommand(anActionCommand);
      if (anActionCommand != null) {
        aButton.addActionListener(ivActionListener);
        aButton.setEnabled(isCommandEnabled(anActionCommand));
      }
      else
        aButton.removeActionListener(ivActionListener);
    }
  }


  /**
   * Abilita o disabilita un action-command e tutti i controlli ad esso associati.
   */
  public void enableCommand(String anActionCommand, boolean toBeEnabled) {
  
    if (anActionCommand == null)
      throw new IllegalArgumentException("The ActionCommand can't be null.");
      
    synchronized (ivActCmdLock) {
      // verifico se action-command è già nello stato voluto 
      if (toBeEnabled == isCommandEnabled(anActionCommand))   
        return;

      if (DEBUG >= 1)
        Diagnostic.getOut().println(
            anActionCommand + " " + (toBeEnabled ? "enabled" :
                                                   "disabled"));

      if (toBeEnabled)
        ivDisabledActCmd.removeElement(anActionCommand);
      else
        ivDisabledActCmd.addElement(anActionCommand);
    
      // abilito/disabilito tutti gli oggetti associati ad un determinato action-command
      Object obj;
      for (Enumeration e = ivObj2ActCmd.keys(); e.hasMoreElements(); ) {
        obj = e.nextElement();
      
        if (anActionCommand.equals(ivObj2ActCmd.get(obj))) {
      
          if (obj instanceof MenuItem)
            ((MenuItem)obj).setEnabled(toBeEnabled);
          else if (obj instanceof Button)
            ((Button)obj).setEnabled(toBeEnabled);
          else if (obj instanceof AbstractButton)
            ((AbstractButton)obj).setEnabled(toBeEnabled);
          else
            throw new IllegalStateException("An invalid class type was found.");  
        }
      }    
    }  
  }
  
  
  /**
   */
  public final boolean isCommandEnabled(String anActionCommand) {
    return !ivDisabledActCmd.contains(anActionCommand);
  }
   
 
  /**
   * Utilizzato per associare una instanza di una classe che implementa l' 
   * interfaccia XACommand ad un action command (sia esso generato da un menù o da un bottone)
   * <pre>
   * es.  
   * // NB: in questo esempio viene utilizzata una inner-class, normalmente è consigliabile
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
   */
  public void setCommand(String anActionCommand, XICommand aCommand) {
  
    if (anActionCommand == null)
      throw new IllegalArgumentException("The ActionCommand can't be null.");

    if (DEBUG >= 1)
      Diagnostic.getOut().println(
         aCommand + " -> " + anActionCommand);
    
    ivActCmd2Cmd.put(anActionCommand, aCommand);
  }
  
  
  /**
   * Ritorna l' instanza di XACommand associata con l' action command (null se nessuno).
   */
  public XICommand getCommand(String anActionCommand) {
  
    return (XICommand)ivActCmd2Cmd.get(anActionCommand);
  }
}
