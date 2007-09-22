/*
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */


package net.infordata.em.tn5250ext;


import java.awt.*;

import javax.swing.*;


/**
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIImage extends JComponent {  

  private Image ivImage;


  /**
   */
	public XIImage(Image anImage) {
    setImage(anImage);
 	}


  /**
   */
  public void setImage(Image anImage)	{
	  if (anImage == ivImage)
	    return;

	  ivImage = anImage;
		repaint();
	}


  /**
	 */
  public void update(Graphics gr) {
    paint(gr);
  }


  /**
	 */
  public void paintComponent(Graphics gr) {
    Dimension dim = getSize();

    if (ivImage != null)
      gr.drawImage(ivImage, 0, 0, dim.width, dim.height, this);
 	}
}