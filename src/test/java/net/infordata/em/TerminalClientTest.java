package net.infordata.em;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.wiresham.Flow;
import us.abstracta.wiresham.VirtualTcpService;

public class TerminalClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(TerminalClientTest.class);
  private static final long TIMEOUT_MILLIS = 10000;
  private static final String SERVICE_HOST = "localhost";

  private VirtualTcpService service = new VirtualTcpService();
  private TerminalClient client;

  @Before
  public void setup() throws IOException {
    service.setSslEnabled(false);
    service.setFlow(Flow.fromYml(new File(getResourceFilePath("/login.yml"))));
    service.start();
    client = new TerminalClient();
    client.setTerminalType("IBM-3477-FC");
    client.connect(SERVICE_HOST, service.getPort());
  }

  private String getResourceFilePath(String resourcePath) {
    return getClass().getResource(resourcePath).getFile();
  }

  @After
  public void teardown() throws Exception {
    client.disconnect();
    service.stop(TIMEOUT_MILLIS);
  }

  @Test
  public void shouldGetUnlockedKeyboardWhenConnect() throws Exception {
    awaitKeyboardUnlock();
    assertThat(client.isKeyboardLocked()).isFalse();
  }

  private void awaitKeyboardUnlock() throws InterruptedException, TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    client.addEmulatorListener(new EmulatorListener() {
      @Override
      public void stateChanged(XI5250EmulatorEvent e) {
        if (!client.isKeyboardLocked()) {
          latch.countDown();
        }
      }
    });
    if (!client.isKeyboardLocked()) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException();
    }
  }

  private static abstract class EmulatorListener implements XI5250EmulatorListener {

    @Override
    public void connecting(XI5250EmulatorEvent e) {
    }

    @Override
    public void connected(XI5250EmulatorEvent e) {
    }

    @Override
    public void disconnected(XI5250EmulatorEvent e) {
    }

    @Override
    public void stateChanged(XI5250EmulatorEvent e) {
    }

    @Override
    public void newPanelReceived(XI5250EmulatorEvent e) {
    }

    @Override
    public void fieldsRemoved(XI5250EmulatorEvent e) {
    }

    @Override
    public void dataSended(XI5250EmulatorEvent e) {
    }

  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    awaitLoginScreen();
    assertThat(client.getScreenText())
        .isEqualTo(getWelcomeScreen());
  }

  private void awaitLoginScreen() throws TimeoutException, InterruptedException {
    awaitScreenContains("Sign On");
  }

  private void awaitScreenContains(String text) throws InterruptedException, TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    client.addEmulatorListener(new EmulatorListener() {
      @Override
      public void stateChanged(XI5250EmulatorEvent e) {
        String screen = client.getScreenText();
        LOG.debug("Received screen {}", screen);
        if (screen.contains(text)) {
          latch.countDown();
        }
      }
    });
    if (client.getScreenText().contains(text)) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException();
    }
  }

  private String getWelcomeScreen() throws IOException {
    return getFileContent("login-welcome-screen.txt");
  }

  private String getFileContent(String resourceFile) throws IOException {
    return Resources.toString(Resources.getResource(resourceFile),
        Charsets.UTF_8);
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUserField() throws Exception {
    awaitLoginScreen();
    sendUserField();
    awaitMenuScreen();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void sendUserField() {
    client.setFieldText(6, 53, "TESTUSR");
    client.setFieldText(7, 53, "TESTPSW");
    client.sendKeyEvent(KeyEvent.VK_ENTER, 0);
  }

  private void awaitMenuScreen() throws InterruptedException, TimeoutException {
    awaitScreenContains("IBM i Main Menu");
  }

  @Test
  public void shouldGetFieldPositionWhenGetCursorPositionAfterConnect() throws Exception {
    Point fieldPosition = new Point(53, 6);
    awaitCursorPosition(fieldPosition);
    assertThat(client.getCursorPosition()).isEqualTo(Optional.of(fieldPosition));
  }

  private void awaitCursorPosition(Point position) throws InterruptedException, TimeoutException {
    CountDownLatch latch = new CountDownLatch(1);
    client.addEmulatorListener(new EmulatorListener() {
      @Override
      public void stateChanged(XI5250EmulatorEvent e) {
        LOG.debug("Cursor is at {}", client.getCursorPosition());
        if (position.equals(client.getCursorPosition().orElse(null))) {
          latch.countDown();
        }
      }
    });
    if (!client.isKeyboardLocked()) {
      latch.countDown();
    }
    if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendIncorrectFieldPosition()
      throws Exception {
    awaitLoginScreen();
    client.setFieldText(0, 1, "test");
  }

}
