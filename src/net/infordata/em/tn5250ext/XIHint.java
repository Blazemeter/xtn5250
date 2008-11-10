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
import java.util.*;

import javax.swing.*;


/**
 * XIHint è la classe che descrive le caratteristiche di un Hint.
 * Utilizzando i caratteri di controllo è possibile andare a capo inserire
 * parole in grasseto, corsivo o entrambi.
 * I caratteri di controllo sono:
 * - \n : per andare a capo;
 * -  # : per inserire il testo in grassetto;
 * -  @ : per inserire il testo in corsivo;
 * Per esempio questo testo è: #grassetto grassetto# @corsivo corsivo corsivo@\n
 * a capo #@grassetto corsivo# corsivo@ normale.
 *
 * @see XIPanel
 * @see XIHintWindow
 */
public class XIHint extends JComponent {

  private static final long serialVersionUID = 1L;

  // !!CM 06/08/97
//  private final static int DEFAULT_MAX_WIDTH = 300;

  private String      ivText = null;

  // !!CM 06/08/97
  private int         ivMaxWidth = 0;

  private Font        ivFont            = null;
//  private FontMetrics ivFontMetrics     = null;

  private int ivSpaceLine  = 1;
  private int ivSpaceUp    = 1;
  private int ivSpaceDown  = 2;
  private int ivSpaceRight = 2;
  private int ivSpaceLeft  = 4;
  private int ivHeightShortText;

  private ArrayList<String> ivVectorLines;

  // !!0.83/CM - 30/04/97
  private Font ivFBold;
  private Font ivFItalic;
  private Font ivFBoldItalic;


  /**
   */
  public XIHint(String aText) {
    this(aText, 0);
  }


  /**
   */
  public XIHint(String aText, int aWidth) {
    ivText   = aText;

    ivFont    = new Font("Helvetica", Font.PLAIN, 11);

    ivFBold   = new Font(ivFont.getName(), Font.BOLD, ivFont.getSize());
    ivFItalic = new Font(ivFont.getName(), Font.ITALIC, ivFont.getSize());
    ivFBoldItalic = new Font(ivFont.getName(), (Font.BOLD|Font.ITALIC),
                             ivFont.getSize());

//    ivFontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(ivFont);

    ivMaxWidth = aWidth;

    ivVectorLines = calculateLines(ivText, ivFont);
  }


  /**
   */
  public Dimension getPreferredSize() {
    int x = ivSpaceLeft;
    int y = ivSpaceUp;

    x += getTextSize(ivFont).width + 2;
    y  = getTextSize(ivFont).height + 2;

    x += ivSpaceRight;
    y += ivSpaceDown;

    return new Dimension(x, y);
  }


  /**
   */
  public String getText() {
    return ivText;
  }


  /**
   */
  protected void paintComponent(Graphics aGraphics) {
//    Dimension vDim  = getSize();
//    String    vText;
    int       vXPos = 0;

    aGraphics.setFont(ivFont);
    aGraphics.setColor(Color.black);

    drawMultiLineString(aGraphics, ivVectorLines,
                        ivFont, vXPos + ivSpaceLeft, ivSpaceUp);
  }


  //!!CM 06/08/97 BEGIN
  @SuppressWarnings("deprecation")
  private int strWidth(String str, Font f) {

    return Toolkit.getDefaultToolkit().getFontMetrics(f).stringWidth(str);
  }


  @SuppressWarnings("deprecation")
  private int strHeight(Font f) {

    return Toolkit.getDefaultToolkit().getFontMetrics(f).getHeight();
  }


  @SuppressWarnings("deprecation")
  private FontMetrics fontMetrics(Font f) {
  
    return Toolkit.getDefaultToolkit().getFontMetrics(f);
  }


  private Dimension textDim(ArrayList<String> v, Font f) {
    
    int maxW = 0;
    int lineW = 0;
    int h = 0;

    boolean bold = false;
    boolean italic = false;
//    boolean boldItalic = false;

    
    for (int i = 0; i < v.size(); i++) {
      
      String str = v.get(i);
      
      if (isEscapeChar(str, "#") || 
          isEscapeChar(str, "@")) {
        
        StringTokenizer st = new StringTokenizer(str, "#@", true);
        
        while (st.hasMoreElements()) {
          String token = st.nextToken();

          if ("#".equals(token)) {
            if (bold) {
              bold = false;
            }
            else {
              bold = true;
            }
          }
          else if ("@".equals(token)) {
            if (italic) {
              italic = false;
            }
            else {
              italic = true;
            }
          }
          else {
            if (bold && italic) {
              lineW += strWidth(token, ivFBoldItalic);
            }
            else if (bold) {          
              lineW += strWidth(token, ivFBold);
            }
            else if (italic) {
              lineW += strWidth(token, ivFItalic);
            }
            else {
              lineW += strWidth(token, f);
            }
          }
        }
      }
      else {
        lineW = strWidth(str, f);
      }
      
      maxW = Math.max(maxW, lineW);
      lineW = 0;
      h += strHeight(f) - fontMetrics(f).getDescent() + ivSpaceLine;
    }    
  
    return new Dimension(maxW, h);
  }


  private boolean isEscapeChar(String text, String escChar) {
  
    if (escChar == null) {
      return false;
    }
    
    StringTokenizer st = new StringTokenizer(text, escChar, true);
        
    while (st.hasMoreTokens()) {
      if (escChar.equals(st.nextToken())) {
        return true;
      }
    }
    
    return false;
  }


  public Dimension getTextSize(Font f) {
    
    return new Dimension(textDim(ivVectorLines, f));
  }


  private void drawMultiLineString(Graphics g, ArrayList<String> v,
                                   Font f,
                                   int x, int y) {
  
    int xBegin = x;

    boolean bold = false;
    boolean italic = false;
//    boolean boldItalic = false;

    for (int i = 0; i < v.size(); i++) {
      y += ivHeightShortText  - fontMetrics(f).getDescent();
      StringTokenizer st = new StringTokenizer(v.get(i), "#@", true);
      
      while (st.hasMoreElements()) {
        String token = st.nextToken();
        
        if ("#".equals(token)) {
          if (bold) {
            bold = false;
          }
          else {
            bold = true;
          }
        }
        else if ("@".equals(token)) {
          if (italic) {
            italic = false;
          }
          else {
            italic = true;
          }
        }
        else {
          if (bold && italic) {
            g.setFont(ivFBoldItalic);
          }
          else if (bold) {          
            g.setFont(ivFBold);
          }
          else if (italic) {
            g.setFont(ivFItalic);
          }
          else {
            g.setFont(f);
          }
          
          g.drawString(token, x, y);
          x += strWidth(token, g.getFont());
        }  
      }

      x = xBegin;
      y += ivSpaceLine;
    }
  }


  private ArrayList<String> calculateLines(String text, Font f) {

    StringTokenizer st = new StringTokenizer(text, "\n");
    ArrayList<String> v = new ArrayList<String>(10);

    ivHeightShortText = strHeight(f);  
    
    while (st.hasMoreTokens()) {
      
      String token = st.nextToken();
      
      if ((ivMaxWidth > 0) && (strWidth(token, f) > ivMaxWidth)) {
        StringTokenizer subSt = new StringTokenizer(token);
        String subToken = "";
        
        while (subSt.hasMoreTokens()) {
          String str1 = subSt.nextToken();
          
          if (strWidth((subToken + str1), f) < ivMaxWidth) {
            subToken += str1 + " ";
          }
          else {
            v.add(trimRight(subToken));
            subToken = str1 + " ";
          }
        }

        v.add(trimRight(subToken));
      }
      else {
        v.add(trimRight(token));
      }
    }
    
    return v;
  }


  private String trimRight(String str) {
  
    String str1 = str;
    
    while (str.endsWith(" ")) {
      str1 = str.substring(0, str.length() - 1);
      str = str1;
    }

    return str1;
  }
  //!!CM 06/08/97 END
}
