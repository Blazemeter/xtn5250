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
    30/07/99 rel. 1.14b- removed statusbar.* sub package. 
 */

package net.infordata.em.tn5250; 

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.infordata.em.util.XIRatioLayout;


 
/**
 * The 5250 status bar.
 */
public class XI5250StatusBar extends JPanel {

  private static final long serialVersionUID = 1L;

  //
  public final static int SHIFT_UP             =  0;
  public final static int SHIFT_DOWN           =  1;

  //
  public final static int CAPS_LOCK_UP         =  0;
  public final static int CAPS_LOCK_DOWN       =  1;

  //
  public final static int MESSAGE_OFF          =  0;
  public final static int MESSAGE_ON           =  1;

  // images
  static XIImagesBdl cvImagesBdl = XIImagesBdl.getImagesBdl();

  private static Image
      cvTemporaryLockImage  = cvImagesBdl.getImage("TemporaryLock");
  private static Image
      cvNormalLockImage     = cvImagesBdl.getImage("NormalLock");
  private static Image
      cvHelpImage           = cvImagesBdl.getImage("Help");
  private static Image
      cvShiftDownImage      = cvImagesBdl.getImage("ShiftDown");
  private static Image
      cvCapsLockImage       = cvImagesBdl.getImage("CapsLock");
  private static Image
      cvMessageImage        = cvImagesBdl.getImage("Message");
  private static Image
      cvFlashImage          = cvImagesBdl.getImage("Flash");

  // status bar components
  private TextAndImage ivFlashArea;
  private TextAndImage ivStateArea;
  private TextAndImage ivMessageArea;
	private TextAndImage ivShiftArea;
	private TextAndImage ivCapsLockArea;
  private JLabel       ivCoordArea;

  private boolean ivFlashOn;
  private int     ivShiftAreaState = -1;
  private int     ivState          = XI5250Emulator.ST_NULL;


  /**
   */
  public XI5250StatusBar() {
    super(new XIRatioLayout(4));
    setBorder(BorderFactory.createRaisedBevelBorder());

    // add components
    addComponents();
  }


  /**
   */
  @Override
  public boolean isValidateRoot() {
    return true;
  }


  /**
	 */
  public void addComponents() {
    Border border = BorderFactory.createEtchedBorder();

    ivFlashArea = new TextAndImage(TextAndImage.CENTER);
    ivFlashArea.setBorder(border);
    add(ivFlashArea, new XIRatioLayout.Constraints(0.03f));

    JComponent dummyArea = new TextAndImage(TextAndImage.CENTER);
    dummyArea.setBorder(border);
    add(dummyArea, new XIRatioLayout.Constraints(0.03f));

    // STATE AREA
    ivStateArea = new TextAndImage();
    ivStateArea.setBorder(border);
    add(ivStateArea, new XIRatioLayout.Constraints(0.3f));

    // MESSAGE AREA
    ivMessageArea = new TextAndImage(TextAndImage.CENTER);
    ivMessageArea.setBorder(border);
    add(ivMessageArea, new XIRatioLayout.Constraints(0.03f));

    // SHIFT AREA
    ivShiftArea = new TextAndImage(TextAndImage.CENTER);
    ivShiftArea.setBorder(border);
    add(ivShiftArea, new XIRatioLayout.Constraints(0.03f));

    // CAPS LOCK  AREA
    ivCapsLockArea = new TextAndImage(TextAndImage.CENTER);
    ivCapsLockArea.setBorder(border);
    add(ivCapsLockArea, new XIRatioLayout.Constraints(0.03f));

    // COORD AREA
    ivCoordArea = new JLabel(null, null, JLabel.RIGHT);
    ivCoordArea.setFont(null);
    ivCoordArea.setBorder(border);
    add(ivCoordArea, new XIRatioLayout.Constraints(0.15f, XIRatioLayout.RIGHT));
  }


  /**
	 */
	public void setCoordArea(int aCol, int aRow) {
	  ivCoordArea.setText(aRow + " / " + aCol + " ");
	}


  /**
	 */
	public void setFlashArea(boolean flag) {
	  if (flag == ivFlashOn)
	    return;

	  ivFlashOn = flag;

	  if (flag)
	    ivFlashArea.setImage(cvFlashImage);
	  else
	    ivFlashArea.setImage(null);
	}


  /**
	 */
	public void setShiftArea(int aState) {
	  if (aState == ivShiftAreaState)
	    return;

	  ivShiftAreaState = aState;

	  if (aState == SHIFT_DOWN)
	    ivShiftArea.setImage(cvShiftDownImage);
	  else
	    ivShiftArea.setImage(null);
	}


	/**
	 */
	public void setCapsLockArea(int aState)	{
	  if (aState == CAPS_LOCK_DOWN) {
	    ivCapsLockArea.setImage(cvCapsLockImage);
	  }
	  else {
	    ivCapsLockArea.setImage(null);
    }
	}


	/**
	 */
	public void setMessageArea(int aState) {
	  if (aState == MESSAGE_ON) {
	    ivMessageArea.setImage(cvMessageImage);
	  }
	  else {
	    ivMessageArea.setImage(null);
    }
  }


	/**
	 */
	public void setStateArea(int aState) {
	  if (aState == ivState)
	    return;

		switch (aState) {

      case XI5250Emulator.ST_HARDWARE_ERROR:
        ivStateArea.setImage(null);
        ivStateArea.setText("HARDWARE_ERROR");
        break;

      case XI5250Emulator.ST_NORMAL_LOCKED:
        ivStateArea.setImage(cvNormalLockImage);
        ivStateArea.setText("SYSTEM");
        break;

      case XI5250Emulator.ST_NORMAL_UNLOCKED:
        ivStateArea.setImage(null);
        ivStateArea.setText("");
        break;

      case XI5250Emulator.ST_POST_HELP:
        ivStateArea.setImage(null);
        ivStateArea.setText("POST_HELP");
        break;

      case XI5250Emulator.ST_POWER_ON:
        ivStateArea.setImage(null);
        ivStateArea.setText("");
        break;

      case XI5250Emulator.ST_PRE_HELP:
        ivStateArea.setImage(cvHelpImage);
        ivStateArea.setText("");
        break;

      case XI5250Emulator.ST_SS_MESSAGE:
        ivStateArea.setImage(null);
        ivStateArea.setText("SS_MESSAGE");
        break;

      case XI5250Emulator.ST_SYSTEM_REQUEST:
        ivStateArea.setImage(null);
        ivStateArea.setText("SYSTEM_REQUEST");
        break;

      case XI5250Emulator.ST_TEMPORARY_LOCK:
        ivStateArea.setImage(cvTemporaryLockImage);
        ivStateArea.setText("");
        break;
    }
  }


  /**
  public void revalidate() {
    // otherwise causes interferences with the emulator when it tries to
    // force the position and the size of the status bar
  }
   */


  //////////////////////////////////////////////////////////////////////////////

  /**
   */
  protected static class TextAndImage extends JComponent {

    private static final long serialVersionUID = 1L;

    public static final int LEFT   = 0;
    public static final int CENTER = 1;

    private Image  ivImage = null;
    private String ivText = null;

    private int    ivAlignment;

    private final int ivHGap = 2;


    public TextAndImage(int alignment) {
      ivAlignment = alignment;
    }


    public TextAndImage() {
      this(LEFT);
    }


    public void setText(String aText) {
      ivText = aText;
      repaint();
    }


    public void setImage(Image aImage) {
      ivImage = aImage;
      repaint();
    }


    @Override
    public void paintComponent(Graphics gr) {

      Insets    insets = getInsets();
      int       imageWidth = 0;
      int       imageHeight = 0;
      int       textWidth = 0;
      Dimension dim = getSize();
      FontMetrics fm = null;      

      if ((ivImage != null)) {
        int w = ivImage.getWidth(this);
        int h = ivImage.getHeight(this);
        imageHeight = dim.height - insets.top - insets.bottom;
        imageWidth = w * imageHeight / h;
      }

      if (ivText != null && ivText.length() != 0) {
        fm = gr.getFontMetrics();
        textWidth = fm.stringWidth(ivText);
      }

      int width = imageWidth + textWidth +
                  ((ivImage != null && fm != null) ? ivHGap : 0);
      int x = insets.left;
      if (ivAlignment == CENTER)
        x = (dim.width - width) / 2;

      if ((ivImage != null)) {
        gr.drawImage(ivImage, x, insets.top,
                     imageWidth, imageHeight, this);
        x += imageWidth + ivHGap;
      }

      if (fm != null) {
        gr.setColor(getForeground());
        int y = (dim.height + fm.getHeight()) / 2;
        gr.drawString(ivText, x, y - fm.getDescent());
      }
    }
  }
}
