/*
!!V 09/09/97 rel. 1.04c- creation.
    24/09/97 rel. 1.05 - DNCX project.
    ***
    10/07/98 rel. _.___- Swing, JBuilder2 e VSS.
*/


package net.infordata.em.tn5250ext;


import java.beans.*;


public class XI5250EmulatorExtBeanInfo extends SimpleBeanInfo {
  Class beanClass = XI5250EmulatorExt.class;
  String iconColor16x16Filename;
  String iconColor32x32Filename;
  String iconMono16x16Filename;
  String iconMono32x32Filename;


  public XI5250EmulatorExtBeanInfo() {
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    try  {
      PropertyDescriptor _hintOnActiveField = 
          new PropertyDescriptor("hintOnActiveField", beanClass,
                                 "isHintOnActiveField", "setHintOnActiveField");

      PropertyDescriptor _showHints =
          new PropertyDescriptor("showHints", beanClass, "getShowHints",
                                 "setShowHints");

      PropertyDescriptor[] pds = new PropertyDescriptor[] {
        _hintOnActiveField,
        _showHints,
      };
      return pds;
    }
    catch (IntrospectionException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public java.awt.Image getIcon(int iconKind) {
    switch (iconKind) {
    case BeanInfo.ICON_COLOR_16x16:
      return iconColor16x16Filename != null ? loadImage(iconColor16x16Filename) : null;
    case BeanInfo.ICON_COLOR_32x32:
      return iconColor32x32Filename != null ? loadImage(iconColor32x32Filename) : null;
    case BeanInfo.ICON_MONO_16x16:
      return iconMono16x16Filename != null ? loadImage(iconMono16x16Filename) : null;
    case BeanInfo.ICON_MONO_32x32:
      return iconMono32x32Filename != null ? loadImage(iconMono32x32Filename) : null;
    }
    return null;
  }

  public BeanInfo[] getAdditionalBeanInfo() {
    Class superclass = beanClass.getSuperclass();
    try  {
      BeanInfo superBeanInfo = Introspector.getBeanInfo(superclass);
      return new BeanInfo[] { superBeanInfo };
    }
    catch (IntrospectionException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}

