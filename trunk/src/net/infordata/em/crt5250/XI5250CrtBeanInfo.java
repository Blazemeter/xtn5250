
package net.infordata.em.crt5250;

import java.beans.*;

public class XI5250CrtBeanInfo extends SimpleBeanInfo {
  Class beanClass = XI5250Crt.class;
  String iconColor16x16Filename;
  String iconColor32x32Filename;
  String iconMono16x16Filename;
  String iconMono32x32Filename;

  
  public XI5250CrtBeanInfo() {
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    try  {
      PropertyDescriptor _defBackground = new PropertyDescriptor("defBackground", beanClass, "getDefBackground", "setDefBackground");
      
      PropertyDescriptor _defFieldsBorderStyle = new PropertyDescriptor("defFieldsBorderStyle", beanClass, "getDefFieldsBorderStyle", "setDefFieldsBorderStyle");
      
      PropertyDescriptor _font = new PropertyDescriptor("font", beanClass, null, "setFont");
      
      PropertyDescriptor _insertState = new PropertyDescriptor("insertState", beanClass, "isInsertState", "setInsertState");
      
      PropertyDescriptor _referenceCursor = new PropertyDescriptor("referenceCursor", beanClass, "isReferenceCursor", "setReferenceCursor");
      
      PropertyDescriptor[] pds = new PropertyDescriptor[] {
        _defBackground,
        _defFieldsBorderStyle,
        _font,
        _insertState,
        _referenceCursor,
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

 