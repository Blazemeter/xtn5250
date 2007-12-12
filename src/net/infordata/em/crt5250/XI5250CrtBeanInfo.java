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


package net.infordata.em.crt5250;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class XI5250CrtBeanInfo extends SimpleBeanInfo {
  Class<XI5250Crt> beanClass = XI5250Crt.class;
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

      PropertyDescriptor _codePage = new PropertyDescriptor("codePage", beanClass, "getCodePage", "setCodePage");
      
      PropertyDescriptor[] pds = new PropertyDescriptor[] {
        _defBackground,
        _defFieldsBorderStyle,
        _font,
        _insertState,
        _referenceCursor,
        _codePage,
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
    Class<?> superclass = beanClass.getSuperclass();
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

 