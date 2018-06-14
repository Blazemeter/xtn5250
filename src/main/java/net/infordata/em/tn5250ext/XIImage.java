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
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
 */

package net.infordata.em.tn5250ext;

import java.awt.*;

import javax.swing.*;

/**
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIImage extends JComponent {  

  private static final long serialVersionUID = 1L;

  private Image ivImage;

  public XIImage(Image anImage) {
    setImage(anImage);
  }

  public void setImage(Image anImage)	{
    if (anImage == ivImage)
      return;

    ivImage = anImage;
    repaint();
  }

  @Override
  public void update(Graphics gr) {
    paint(gr);
  }

  @Override
  public void paintComponent(Graphics gr) {
    Dimension dim = getSize();

    if (ivImage != null)
      gr.drawImage(ivImage, 0, 0, dim.width, dim.height, this);
  }

}
