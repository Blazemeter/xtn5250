package net.infordata.em;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;

import com.google.common.io.Resources;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.infordata.em.crt.XICrt;
import org.assertj.swing.exception.WaitTimedOutError;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.Test;

@Category(XICrtIT.class)
public class XICrtIT {

  private static final Dimension M5_SIZE = new Dimension(132, 27);
  private static final Dimension M2_SIZE = new Dimension(80, 24);
  private static final long TIMEOUT_MILLIS = 5000;
  private static final String M5_FILE_NAME = "user-menu-screen-m5.txt";
  private static final String M2_FILE_NAME = "user-menu-screen.txt";
  private FrameFixture frame;
  private XICrt crt;
  private int m5FontSize;
  private int m2FontSize;

  @Before
  public void setup() {
    crt = new XICrt();
    frame = showInFrame(crt);
    initializeFontSizes();
  }

  private void initializeFontSizes() {
    Dimension currentScreenSize = frame.target().getToolkit().getScreenSize();
    String stringScreenSize = currentScreenSize.width + "x" + currentScreenSize.height;
    switch (stringScreenSize) {
      case "1280x720":
        m5FontSize = 15;
        m2FontSize = 25;
        break;
      case "1920x1080":
        m5FontSize = 24;
        m2FontSize = 38;
        break;
      case "2560x1440":
        m5FontSize = 32;
        m2FontSize = 50;
        break;
      case "3840x2160":
        m5FontSize = 48;
        m2FontSize = 50;
        break;
      default:
        throw new UnsupportedOperationException(String.format("Size [%s] is not supported",
            stringScreenSize));
    }
  }

  @Test
  public void shouldProperlyRecFontSizeWhenResizeAndCrtSizeChanges() throws IOException {
    setScreenText(M5_SIZE, M5_FILE_NAME);
    Dimension screenSize = frame.target().getToolkit().getScreenSize();
    frame.moveTo(new Point(0, 0));
    frame.resizeTo(screenSize);
    waitForFontSize(m5FontSize);
    setScreenText(M2_SIZE, M2_FILE_NAME);
    waitForFontSize(m2FontSize);
  }

  private void setScreenText(Dimension crtSize, String fileName) throws IOException {
    String[] screenText =
        Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8).split("\n");
    crt.setCrtSize(crtSize.width, crtSize.height);
    for (int i = 0; i < crtSize.height; i++) {
      crt.drawString(screenText[i], 1, i + 1);
    }
  }

  private void waitForFontSize(int expectedSize) {
    try {
      pause(new Condition("waiting for font size to be " + expectedSize) {
        @Override
        public boolean test() {
          return crt.getFont().getSize() == expectedSize;
        }
      }, TIMEOUT_MILLIS);
    } catch (WaitTimedOutError e) {
      throw new WaitTimedOutError(
          e.getMessage() + String.format(" instead of %s", crt.getFont().getSize()));
    }
  }

}
