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
!!V 29/07/99 rel. 1.15 - creation.
 */


package net.infordata.em.util;


import java.awt.*;
import java.util.*;


/**
 */
public class XIRatioLayout implements LayoutManager2, java.io.Serializable {

  private static final long serialVersionUID = 1L;

  public static final int LEFT 	 = 0;   

  public static final int CENTER = 1;

  public static final int RIGHT  = 2;

  private int ivHGap;

  private Hashtable ivConstraints = new Hashtable();


  /**
	 */
	public XIRatioLayout() {
	  this(0);
  }


  /**
	 */
  public XIRatioLayout(int hgap) {
	  ivHGap = hgap;
  }


  /**
   */
  public final int getHGap() {
    return ivHGap;
  }


  /**
   */
  public void addLayoutComponent(String aDescriptor, Component comp) {
    Float fl = Float.valueOf(aDescriptor);
    addLayoutComponent(comp, new Constraints(fl.floatValue(), LEFT));
  }


  /**
   */
  public void addLayoutComponent(Component comp, Object constraints) {
    if (constraints != null && !(constraints instanceof Constraints))
      throw new IllegalArgumentException("XIRatioLayout.Constraints expected");
    ivConstraints.put(comp, constraints);
  }


  /**
   */
  public void removeLayoutComponent(Component comp) {
    ivConstraints.remove(comp);
  }


  private static final int PREFERRED = 0;
  private static final int MINIMUM = 1;
  private static final int MAXIMUM = 2;

  private Dimension layoutSize(Container parent, int type) {
    Insets insets;
    int[] nComps = new int[3];
    int maxW, maxH;
        
    synchronized (parent.getTreeLock()) {
      insets = parent.getInsets();
      maxW = insets.left + insets.right;
      maxH = insets.top + insets.bottom;

      {
        int n = parent.getComponentCount();
        Component comp;
        Constraints constr;
        for (int i = 0; i < n; i++) {
          comp = parent.getComponent(i);
          if (comp.isVisible()) {
            constr = (Constraints)ivConstraints.get(comp);
            if (constr != null) {
              ++nComps[constr.getAlignment()];
            }
            else {
              Dimension dim;
              switch (type) {
                case PREFERRED:
                  dim = comp.getPreferredSize();
                  break;
                case MINIMUM:
                  dim = comp.getMinimumSize();
                  break;
                case MAXIMUM:
                  dim = comp.getMaximumSize();
                  break;
                default:
                  throw new IllegalStateException();
              }
              Point loc = comp.getLocation();
              maxW = Math.max(maxW, loc.x + dim.width - 1);
              maxH = Math.max(maxH, loc.y + dim.height - 1);
            }
          }
        }
      }
    }

    int w = insets.left + insets.right;
    for (int i = LEFT; i <= RIGHT; i++)
      if (nComps[i] > 0)
        w -= (nComps[i] - 1) * ivHGap;

    maxW = Math.max(maxW, w);

    return new Dimension(maxW, maxH);
  }


  /**
   */
  public Dimension preferredLayoutSize(Container parent) {
    return layoutSize(parent, PREFERRED);
  }


  /**
   */
  public Dimension minimumLayoutSize(Container parent) {
    return layoutSize(parent, MINIMUM);
  }


  /**
   */
  public Dimension maximumLayoutSize(Container parent) {
    return layoutSize(parent, MAXIMUM);
  }


  /**
   */
  public void layoutContainer(Container parent) {
    Insets insets;
    Vector[] comps = new Vector[3];  // LEFT, CENTER and RIGHT
    Vector[] constrs = new Vector[3];  // LEFT, CENTER and RIGHT
    Dimension parentDim;
    
    synchronized (parent.getTreeLock()) {
      insets  = parent.getInsets();
      comps   = new Vector[3];  // LEFT, CENTER and RIGHT
      constrs = new Vector[3];  // LEFT, CENTER and RIGHT

      {
        int nComps = parent.getComponentCount();
        for (int i = LEFT; i <= RIGHT; i++) {
          comps[i] = new Vector(nComps);
          constrs[i] = new Vector(nComps);
        }
        Component comp;
        Constraints constr;
        for (int i = 0; i < nComps; i++) {
          comp = parent.getComponent(i);
          if (comp.isVisible()) {
            constr = (Constraints)ivConstraints.get(comp);
            if (constr != null) {
              comps[constr.getAlignment()].addElement(comp);
              constrs[constr.getAlignment()].addElement(constr);
            }
          }
        }
      }

      parentDim = parent.getSize();
    }

    if (comps[LEFT].size() == 0 && comps[CENTER].size() == 0 &&
        comps[RIGHT].size() == 0)
	    return;

	  int maxW = parentDim.width - (insets.left + insets.right);
	  int maxH = parentDim.height - (insets.top + insets.bottom);
    for (int i = LEFT; i <= RIGHT; i++)
      if (comps[i].size() > 0)
        maxW -= (comps[i].size() - 1) * ivHGap;

    Component   comp;
    Constraints constr;
    int[] totW = new int[3]; 

    for (int i = LEFT; i <= RIGHT; i++) {
      for (int j = 0; j < comps[i].size(); j++) {
        comp = (Component)comps[i].elementAt(j);
        constr = (Constraints)constrs[i].elementAt(j);
        comp.setSize(Math.round(maxW * constr.getHRatio()), maxH); 
        totW[i] += comp.getSize().width;
      }
    }

    if (comps[LEFT].size() > 0) { //LEFT ALIGNED
      int x = insets.left;
      Dimension dim;
      for (int j = 0; j < comps[LEFT].size(); j++) {
        comp = (Component)comps[LEFT].elementAt(j);
        dim = comp.getSize();
        comp.setLocation(x, insets.top);
        x += (dim.width + ivHGap);
      }
    }

    if (comps[RIGHT].size() > 0) { //RIGHT ALIGNED
      int x = parentDim.width - insets.right;
      Dimension dim;
      for (int j = 0; j < comps[RIGHT].size(); j++) {
        comp = (Component)comps[RIGHT].elementAt(j);
        dim = comp.getSize();
        comp.setLocation(x - dim.width, insets.top);
        x -= (dim.width + ivHGap);
      }
    }

    if (comps[CENTER].size() > 0) { //CENTERED
      int x = (parentDim.width - totW[CENTER] -
               (comps[CENTER].size() - 1) * ivHGap) / 2;
      Dimension dim;
      for (int j = 0; j < comps[CENTER].size(); j++) {
        comp = (Component)comps[CENTER].elementAt(j);
        dim = comp.getSize();
        comp.setLocation(x, insets.top);
        x += (dim.width + ivHGap);
      }
    }
  }


  /**
   */
  public float getLayoutAlignmentX(Container target) {
    return 0.5f;
  }


  /**
   */
  public float getLayoutAlignmentY(Container target) {
    return 0.5f;
  }


  /**
   */
  public void invalidateLayout(Container target) {
  }


  /**
   */
  public static void main(String[] args) {
    Frame frame = new Frame("TEST XIRatioLayout");
    Panel panel = new Panel(new XIRatioLayout(4));
    panel.add(new Button("L1"), new Constraints(0.1F, LEFT));
    panel.add(new Button("L2"), new Constraints(0.2F, LEFT));
    panel.add(new Button("R1"), new Constraints(0.1F, RIGHT));
    panel.add(new Button("R2"), new Constraints(0.2F, RIGHT));
    panel.add(new Button("C1"), new Constraints(0.2F, CENTER));
    panel.add(new Button("C2"), new Constraints(0.2F, CENTER));
    frame.add(panel);
    frame.setBounds(0, 0, 200, 200);
    frame.show();
  }


  //////////////////////////////////////////////////////////////////////////////

  public static class Constraints implements java.io.Serializable {

    private float ivHRatio;
    private int   ivAlign;

    public Constraints(float hRatio) {
      this(hRatio, LEFT);
    }

    public Constraints(float hRatio, int alignment) {
      if (alignment < LEFT || alignment > RIGHT)
        throw new IllegalArgumentException();
      ivHRatio = hRatio;
      ivAlign = alignment;
    }

    public final float getHRatio() {
      return ivHRatio;
    }

    public final int getAlignment() {
      return ivAlign;
    }
  }
}