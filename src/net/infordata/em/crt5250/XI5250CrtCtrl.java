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
!!V 15/06/99 rel. 1.13 - creation.
 */


package net.infordata.em.crt5250;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.infordata.em.util.XICommand;
import net.infordata.em.util.XICommandMgr;


/**
 * Handles common commands.
 */
public class XI5250CrtCtrl {

  private static final Logger LOGGER = Logger.getLogger(XI5250CrtCtrl.class.getName());

  private XI5250Crt    ivCrt;

  private XICommandMgr ivCommandMgr = new XICommandMgr();


  public static final String SWITCH_3DFX_CMD      = "SWITCH_3DFX_CMD";
  public static final String REFERENCE_CURSOR_CMD = "REFERENCE_CURSOR_CMD";

  public static final String COPY_CMD             = "COPY_CMD";
  public static final String PASTE_CMD            = "PASTE_CMD";

  public static final String PRINT_CMD            = "PRINT_CMD";

  /**
   */
  public XI5250CrtCtrl(XI5250Crt aCrt) {
    if (aCrt == null)
      throw new IllegalArgumentException("An XI5250Crt instance is required.");

    ivCrt = aCrt;

    ivCrt.addPropertyChangeListener(new PropertyListener());

    // Copy command
    getCommandMgr().enableCommand(
        COPY_CMD, ivCrt.getSelectedArea() != null);
    getCommandMgr().setCommand(COPY_CMD, new XICommand() {
      public void execute() {
        processCopyCmd();
      }
    });

    // Paste command
    getCommandMgr().setCommand(PASTE_CMD, new XICommand() {
      public void execute() {
        processPasteCmd();
      }
    });

    // 3Dfx command
    getCommandMgr().setCommandState(SWITCH_3DFX_CMD, is3DFX());
    getCommandMgr().setCommand(SWITCH_3DFX_CMD,  new XICommand() {
      public void execute() {
        processSwitch3dFxCmd();
      }
    });

    // Reference cursor command
    getCommandMgr().setCommandState(
        REFERENCE_CURSOR_CMD, ivCrt.isReferenceCursor());
    getCommandMgr().setCommand(REFERENCE_CURSOR_CMD,  new XICommand() {
      public void execute() {
        processReferenceCursorCmd();
      }
    });

    // Print command
    getCommandMgr().setCommand(PRINT_CMD, new XICommand() {
      public void execute() {
        processPrintCmd();
      }
    });
  }


  /**
   */
  public final XI5250Crt getCrt() {
    return ivCrt;
  }


  /**
   */
  public final XICommandMgr getCommandMgr() {
    return ivCommandMgr;
  }


  /**
   */
  public final boolean is3DFX() {
    return
        (getCrt().getDefFieldsBorderStyle() == XI5250Field.LOWERED_BORDER);
  }


  /**
   */
  protected void processCopyCmd() {
    getCrt().processRawKeyEvent(
        new KeyEvent(getCrt(), KeyEvent.KEY_PRESSED,
                     0, KeyEvent.CTRL_MASK, KeyEvent.VK_INSERT, (char)KeyEvent.VK_INSERT));
  }


  /**
   */
  protected void processPasteCmd() {
    getCrt().processRawKeyEvent(
        new KeyEvent(getCrt(), KeyEvent.KEY_PRESSED,
                     0, KeyEvent.SHIFT_MASK, KeyEvent.VK_INSERT, (char)KeyEvent.VK_INSERT));
  }


  /**
   */
  protected void processSwitch3dFxCmd() {
    boolean flag = getCommandMgr().getCommandState(SWITCH_3DFX_CMD);

    if (flag) {
      getCrt().setDefFieldsBorderStyle(XI5250Field.LOWERED_BORDER);
      getCrt().setDefBackground(UIManager.getColor("control"));
    }
    else {
      getCrt().setDefFieldsBorderStyle(XI5250Field.NO_BORDER);
      getCrt().setDefBackground(SystemColor.black);
    }
  }


  /**
   */
  protected void processReferenceCursorCmd() {
    boolean flag = getCommandMgr().getCommandState(REFERENCE_CURSOR_CMD);

    getCrt().setReferenceCursor(flag);
  }


  /**
   */
  protected void emulatorPropertyChanged(PropertyChangeEvent e) {
    String propertyName = e.getPropertyName();

    if (propertyName == XI5250Crt.SELECTED_AREA) {
      getCommandMgr().enableCommand(
          COPY_CMD, getCrt().getSelectedArea() != null);
    }
    else if (propertyName == XI5250Crt.DEF_FIELDS_BORDER_STYLE) {
      getCommandMgr().setCommandState(SWITCH_3DFX_CMD, is3DFX());
    }
    else if (propertyName == XI5250Crt.REFERENCE_CURSOR) {
      getCommandMgr().setCommandState(
          REFERENCE_CURSOR_CMD, getCrt().isReferenceCursor());
    }
  }


  /**
   */
  protected void processPrintCmd() {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(new Printable() {
      public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
          throws PrinterException {
        if (pageIndex > 0) { 
          return NO_SUCH_PAGE;
        }
        final int imgWidth = (int)pageFormat.getImageableWidth();
        final int imgHeight = (int)pageFormat.getImageableHeight();
        final XI5250Crt crt = getCrt();
        Graphics2D g2d = (Graphics2D)graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        double scale;
        {
          int w = crt.getSize().width;
          int h = crt.getSize().height;
          scale = Math.min(((double)imgWidth) / w, ((double)imgHeight) / h);
          g2d.scale(scale, scale);
        }
        synchronized (crt.getTreeLock()) {
          synchronized (crt) {
            Color oldBG = crt.getDefBackground();
            //int oldBS = crt.getDefFieldsBorderStyle();
            try {
              //crt.setDefFieldsBorderStyle(XI5250Field.NO_BORDER);
              crt.setDefBackground(SystemColor.white);
              crt.printAll(g2d);
            }
            finally {
              //crt.setDefFieldsBorderStyle(oldBS);
              crt.setDefBackground(oldBG);
            }
          }
        }
        return PAGE_EXISTS;      }
    });
    boolean doPrint = job.printDialog();
    if (doPrint) {
      try {
        job.print();
      } 
      catch (final PrinterException ex) {
        LOGGER.log(Level.SEVERE, "catchedException()", ex);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            JOptionPane.showMessageDialog(getCrt(), 
                ex.getMessage() + "\nSee the log for details ",
                "ERROR", JOptionPane.ERROR_MESSAGE);
          }
        });
      }
    }    
  }

  
  //////////////////////////////////////////////////////////////////////////////

  /**
   * Usata per sincronizzare i comandi con le property dell' emulatore.
   */
  class PropertyListener implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {
      emulatorPropertyChanged(e);
    }
  }
}


