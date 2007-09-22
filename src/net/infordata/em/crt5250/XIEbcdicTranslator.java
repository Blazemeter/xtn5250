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


/**
 * Singleton class used to translate chars to EBCDIC and vice-versa.
 * Future releases will support multiple convertion tables.
 *
 * @version
 * @author   Valentino Proietti - Infordata S.p.A.
 */
public class XIEbcdicTranslator {

  /**
   * EBCDIC to char translation table.
   */
  protected static final char[] EBCDIC2CHAR =
    //     0        1        2        3        4        5        6        7        8        9        A        B        C        D        E        F
    {'\u0000','\u0001','\u0002','\u0003','\u0004','\u0005','\u0006','\u0007','\u0008','\u0009',    '\n','\u000B','\u000C',    '\r','\u000E','\u000F',
     '\u0010','\u0011','\u0012','\u0013','\u0014','\u0015','\u0016','\u0017','\u0018','\u0019','\u001A','\u001B','\u001C','\u001D','\u001E','\u001F',
     '\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000',
     '\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000','\u0000',
          ' ','\u0000','\u00e2','\u00e4',     '{','\u00e1','\u00e3','\u00e5',    '\\','\u00f1','\u00ba',     '.',     '<',     '(',     '+',     '!',
          '&',     ']','\u00ea','\u00eb',     '}','\u00ed','\u00ee','\u00ef',     '~','\u00df','\u00e8',     '$',     '*',     ')',     ';',     '^',
          '-',     '/','\u00c2','\u00c4','\u00c0','\u00c1','\u00c3','\u00c5','\u00c7','\u00d1','\u00f2',     ',',     '%',     '_',     '>',     '?',
     '\u00f8','\u00c9','\u00ca','\u00cb','\u00c8','\u00cd','\u00ce','\u00cf','\u00cc','\u00f9',     ':','\u00a3','\u00a7',    '\'',     '=',    '\"',
     '\u00d8',     'a',     'b',     'c',     'd',     'e',     'f',     'g',     'h',     'i','\u00ab','\u00bb','\u00f0','\u0000','\u00de','\u00b1',
          '[',     'j',     'k',     'l',     'm',     'n',     'o',     'p',     'q',     'r','\u0000','\u0000','\u00e6','\u0000','\u00c6','\u00a4',
     '\u00b5','\u00ec',     's',     't',     'u',     'v',     'w',     'x',     'y',     'z',     '¡','\u00bf','\u00d0','\u0000','\u00fe','\u00ae',
     '\u00a2',     '#','\u00a5','\u0000','\u0000',     '@','\u00b6','\u00bc','\u00bd','\u00be','\u00ac',     '|','\u0000','\u00a8','\u00b4','\u0000',
     '\u00e0',     'A',     'B',     'C',     'D',     'E',     'F',     'G',     'H',     'I','\u00ad','\u00f4','\u00f6','\u00a6','\u00f3','\u00f5',
     '\u00e8',     'J',     'K',     'L',     'M',     'N',     'O',     'P',     'Q',     'R','\u0000','\u00fb','\u00fc',     '`','\u00fa','\u00ff',
     '\u00e7','\u0000',     'S',     'T',     'U',     'V',     'W',     'X',     'Y',     'Z','\u00b2','\u00d4','\u00d6','\u00d2','\u00d3','\u00d5',
          '0',     '1',     '2',     '3',     '4',     '5',     '6',     '7',     '8',     '9','\u00b3','\u00db','\u00dc','\u00d9','\u00da','\u0000'};

  /**
   * Char to EBCDIC translation table.
   */
  protected static int[] CHAR2EBCDIC = new int[512];


  private static XIEbcdicTranslator cvTranslator;


  /**
   */
  static {
    // inverts conversion table
    for (int i = EBCDIC2CHAR.length - 1; i >=0; i--)
      CHAR2EBCDIC[(int)EBCDIC2CHAR[i]] = i;
  }


  /**
   */
  private XIEbcdicTranslator() {
  }


  /**
   * Returns the XIEbcdicTranslator single instance.
   */
  public static XIEbcdicTranslator getTranslator() {
    if (cvTranslator == null)
      cvTranslator = new XIEbcdicTranslator();
    return cvTranslator;
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
  public char toChar(byte aEbcdicChar) {
    return EBCDIC2CHAR[toInt(aEbcdicChar)];
  }


  /**
   * From char to ebcdic code
   */
  public byte toEBCDIC(char aChar) {
    return (byte)CHAR2EBCDIC[Math.max(0, Math.min(CHAR2EBCDIC.length - 1, (int)aChar))];
  }


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