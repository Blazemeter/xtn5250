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
!!V 10/04/97 rel. 0.93 - moved to crt5250 package.
    16/04/97 rel. 0.94 - toPacked method added.
    28/04/97 rel. 0.94c- corrected problem with underscore.
    27/05/97 rel. 1.00 - first release.
    25/07/97 rel. 1.03a- '|' char mapping.
    03/03/98 rel. _.___- SWING and reorganization.
    ***
    30/06/98 rel. _.___- Swing, JBuilder2 e VSS.
 */
 
 
package net.infordata.em.crt5250;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public abstract class XIEbcdicTranslator {


  private static final Map<String,XIEbcdicTranslator> cvRegistry = 
      Collections.synchronizedMap(new LinkedHashMap<String,XIEbcdicTranslator>());
  private static final Map<String,XIEbcdicTranslator> cvRORegistry = 
      Collections.unmodifiableMap(cvRegistry);

  
  static {
    registerTranslator("CP37", XIEbcdicNTranslator.TRANSLATOR_CP37);
    registerTranslator("CP273", XIEbcdicNTranslator.TRANSLATOR_CP273);
    registerTranslator("CP277", XIEbcdicNTranslator.TRANSLATOR_CP277);
    registerTranslator("CP278", XIEbcdicNTranslator.TRANSLATOR_CP278);
    registerTranslator("CP280", XIEbcdicNTranslator.TRANSLATOR_CP280);
    registerTranslator("CP284", XIEbcdicNTranslator.TRANSLATOR_CP284);
    registerTranslator("CP285", XIEbcdicNTranslator.TRANSLATOR_CP285);
    registerTranslator("CP297", XIEbcdicNTranslator.TRANSLATOR_CP297);
    registerTranslator("CP424", XIEbcdicNTranslator.TRANSLATOR_CP424);
    registerTranslator("CP500", XIEbcdicNTranslator.TRANSLATOR_CP500);
    registerTranslator("CP850", XIEbcdicNTranslator.TRANSLATOR_CP850);
    registerTranslator("CP870", XIEbcdicNTranslator.TRANSLATOR_CP870);
    registerTranslator("CP838", XIEbcdicNTranslator.TRANSLATOR_CP838);
    registerTranslator("CP1140", XIEbcdicNTranslator.TRANSLATOR_CP1140);
    registerTranslator("CP1141", XIEbcdicNTranslator.TRANSLATOR_CP1141);
    registerTranslator("CP1144", XIEbcdicNTranslator.TRANSLATOR_CP1144);
    registerTranslator("CP1147", XIEbcdicNTranslator.TRANSLATOR_CP1147);
    registerTranslator("CP1153", XIEbcdicNTranslator.TRANSLATOR_CP1153);
    registerTranslator("CP1160", XIEbcdicNTranslator.TRANSLATOR_CP1160);
    //
    registerTranslator("CP1025", XIEbcdicNTranslator.TRANSLATOR_CP1025);
    registerTranslator("CP1026", XIEbcdicNTranslator.TRANSLATOR_CP1026);
    
    registerTranslator("CP1112", XIEbcdicNTranslator.TRANSLATOR_CP1112);
    registerTranslator("CP1122", XIEbcdicNTranslator.TRANSLATOR_CP1122);
    registerTranslator("CP1123", XIEbcdicNTranslator.TRANSLATOR_CP1123);
    registerTranslator("CP1125", XIEbcdicNTranslator.TRANSLATOR_CP1125);
    
    registerTranslator("CP1130", XIEbcdicNTranslator.TRANSLATOR_CP1130);
    registerTranslator("CP1132", XIEbcdicNTranslator.TRANSLATOR_CP1132);
    registerTranslator("CP1137", XIEbcdicNTranslator.TRANSLATOR_CP1137);
    
    registerTranslator("CP1142", XIEbcdicNTranslator.TRANSLATOR_CP1142);
    registerTranslator("CP1145", XIEbcdicNTranslator.TRANSLATOR_CP1145);
    registerTranslator("CP1146", XIEbcdicNTranslator.TRANSLATOR_CP1146);
    registerTranslator("CP1148", XIEbcdicNTranslator.TRANSLATOR_CP1148);
    registerTranslator("CP1149", XIEbcdicNTranslator.TRANSLATOR_CP1149);
    
    registerTranslator("CP1154", XIEbcdicNTranslator.TRANSLATOR_CP1154);
    registerTranslator("CP1155", XIEbcdicNTranslator.TRANSLATOR_CP1155);
    registerTranslator("CP1156", XIEbcdicNTranslator.TRANSLATOR_CP1156);
    registerTranslator("CP1157", XIEbcdicNTranslator.TRANSLATOR_CP1157);
    registerTranslator("CP1158", XIEbcdicNTranslator.TRANSLATOR_CP1158);
    
    registerTranslator("CP1164", XIEbcdicNTranslator.TRANSLATOR_CP1164);

    registerTranslator("CP420", XIEbcdicNTranslator.TRANSLATOR_CP420);
    registerTranslator("CP871", XIEbcdicNTranslator.TRANSLATOR_CP871);
    registerTranslator("CP875", XIEbcdicNTranslator.TRANSLATOR_CP875);
  }
  
  
  /**
   */
  protected XIEbcdicTranslator() {
  }
  
  
  /**
   */
  public static synchronized void registerTranslator(String id, 
      XIEbcdicTranslator tr) {
    if (id == null)
      throw new NullPointerException("id is null");
    if (tr == null)
      throw new NullPointerException("tr is null");
    if (cvRegistry.containsKey(id.toLowerCase()))
      throw new IllegalArgumentException("Translator " + id.toLowerCase() + " already registered");
    cvRegistry.put(id.toLowerCase(), tr);
  }
  
  
  /**
   * @return a read-only map of registered translators
   */
  public static Map<String,XIEbcdicTranslator> getRegisteredTranslators() {
    return cvRORegistry;
  }

  
  /**
   * Returns the XIEbcdicTranslator.
   */
  public static XIEbcdicTranslator getTranslator(String id) {
    return cvRegistry.get(id.toLowerCase());
  }

  
  /**
   * Converts byte to int without sign.
   */
  public static final int toInt(byte bb) {
    return ((int)bb & 0xff);
  }


  /**
   * Converts byte to hex rapresentation
   */
  public static final String toHex(byte bb) {
    String hex = Integer.toString(toInt(bb), 16);
    return "00".substring(hex.length()) + hex;
  }


  /**
   * From ebcdic code to char
   */
  public abstract char toChar(byte aEbcdicChar);


  /**
   * From char to ebcdic code
   */
  public abstract byte toEBCDIC(char aChar);


  /**
   * From String to ebcdic string
   */
  public byte[] toText(String aString, int aLen) {
    byte[] bb = new byte[aLen];
    int    i;
    int    len = Math.min(aLen, aString.length());

    for (i = 0; i < len; i++)
      bb[i] = toEBCDIC(aString.charAt(i));
    // fill with space
    for (i = len; i < aLen; i++)
      bb[i] = toEBCDIC(' ');

    return bb;
  }


  /**
   * From int to ebcdic numeric not packed
   */
  public byte[] toNumeric(int aNum, int aLen) {
    byte[] bb = new byte[aLen];
    int    i;

    for (i = aLen - 1; i >= 0; i--) {
      bb[i] = toEBCDIC((char)(((int)'0') + (aNum % 10)));
      aNum /= 10;
    }

    return bb;
  }


  /**
   * From int to packed
   */
  public byte[] toPacked(int aNum, int aLen) {
    byte[] res = new byte[(aLen + 1) / 2];
    int    x;

    for (int i = res.length - 1; i >= 0; i--) {
      if (i == res.length - 1 && aNum < 0) {
        res[i] = (byte)0x0D;
      }
      else {
        x = aNum % 10;
        aNum /= 10;
        res[i] = (byte)x;
      }

      x = aNum % 10;
      aNum /= 10;
      res[i] |= (byte)(x << 4);
    }

    return res;
  }


  /**
   * From ebcdic string to String
   */
  public String toString(byte[] aBuf, int aOfs, int aLen) {
    String str = "";
    int    i;

    for (i = aOfs; i < (aOfs + aLen); i++)
      str += toChar(aBuf[i]);

    // strip trailing blanks
    for (i = str.length() - 1; (i >= 0) && (str.charAt(i) == ' '); i--)
      ;

    return str.substring(0, Math.max(0, Math.min(str.length(), i + 1)));
  }


  /**
   * From ebcdic numeric not packed to int
   */
  public int toInt(byte[] aBuf, int aOfs, int aLen) {
    String str = toString(aBuf, aOfs, aLen);
    return Integer.parseInt(str);
  }


  /**
   * From ebcdic packed to int
   */
  public int toIntFromPacked(byte[] aBuf, int aOfs, int aLen) {
    int res = 0;

    for (int i = aOfs; i < (aOfs + aLen); i++) {
      res = (res * 100) + ((aBuf[i] >> 4 & 0x0F) * 10);

      if ((aBuf[i] & 0x0F) == 0x0D)
        res /= -10;
      else if ((aBuf[i] & 0x0F) == 0x0F)
        res /= 10;
      else
        res += (aBuf[i] & 0x0F);
    }

    return res;
  }
}