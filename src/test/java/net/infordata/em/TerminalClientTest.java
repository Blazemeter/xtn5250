package net.infordata.em;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import net.infordata.em.crt5250.XI5250Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.wiresham.Flow;
import us.abstracta.wiresham.VirtualTcpService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TerminalClientTest {

  private static final Logger LOG = LoggerFactory.getLogger(TerminalClientTest.class);
  private static final long TIMEOUT_MILLIS = 10000;
  private static final String SERVICE_HOST = "localhost";
  private static final String TERMINAL_TYPE = "IBM-3477-FC";

  @Rule
  public TestRule watchman = new TestWatcher() {
    @Override
    public void starting(Description description) {
      LOG.debug("Starting {}", description.getMethodName());
    }

    @Override
    public void finished(Description description) {
      LOG.debug("Finished {}", description.getMethodName());
    }
  };
  private VirtualTcpService service = new VirtualTcpService();
  private TerminalClient client;
  private ExceptionWaiter exceptionWaiter;
  private ScheduledExecutorService stableTimeoutExecutor = Executors
      .newSingleThreadScheduledExecutor();

  @Before
  public void setup() throws IOException {
    service.setSslEnabled(false);
    service.setFlow(Flow.fromYml(new File(getResourceFilePath("/login.yml"))));
    service.start();
    client = new TerminalClient();
    client.setTerminalType(TERMINAL_TYPE);
    client.setConnectionTimeoutMillis(5000);
    exceptionWaiter = new ExceptionWaiter();
    client.setExceptionHandler(exceptionWaiter);
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
    new UnlockWaiter(client, stableTimeoutExecutor).await(TIMEOUT_MILLIS);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getWelcomeScreen());
  }

  private String getWelcomeScreen() throws IOException {
    return getFileContent("login-welcome-screen.txt");
  }

  private String getFileContent(String resourceFile) throws IOException {
    return Resources.toString(Resources.getResource(resourceFile),
        Charsets.UTF_8);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectWithSsl() throws Exception {
    awaitKeyboardUnlock();
    teardown();

    service.setSslEnabled(true);
    System.setProperty("javax.net.ssl.keyStore", getResourceFilePath("/keystore.jks"));
    System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
    service.start();

    client = new TerminalClient();
    client.setTerminalType(TERMINAL_TYPE);
    client.setSocketFactory(buildSslContext().getSocketFactory());
    client.connect(SERVICE_HOST, service.getPort());

    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getWelcomeScreen());
  }

  private SSLContext buildSslContext() throws GeneralSecurityException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    TrustManager trustManager = new X509TrustManager() {

      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }

      public void checkClientTrusted(
          X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(
          X509Certificate[] certs, String authType) {
      }
    };
    sslContext.init(null, new TrustManager[]{trustManager},
        new SecureRandom());
    return sslContext;
  }

  @Test
  public void shouldGetUserMenuScreenWhenLoginByCoord() throws Exception {
    awaitKeyboardUnlock();
    loginByCoord();
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  @Test
  public void shouldGetUserMenuScreenWhenLoginByLabel() throws Exception {
    awaitKeyboardUnlock();
    loginByLabel();
    awaitKeyboardUnlock();
    assertThat(client.getScreenText())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void loginByCoord() {
    client.setFieldTextByCoord(6, 53, "TESTUSR");
    client.setFieldTextByCoord(7, 53, "TESTPSW");
    client.sendKeyEvent(KeyEvent.VK_ENTER, 0);
  }

  private void loginByLabel() {
    client.setFieldTextByLabel("User", "TESTUSR");
    client.setFieldTextByLabel("Password", "TESTPSW");
    client.sendKeyEvent(KeyEvent.VK_ENTER, 0);
  }

  @Test
  public void shouldGetNotSoundedAlarmWhenWhenConnect() throws Exception {
    awaitKeyboardUnlock();
    assertThat(client.resetAlarm()).isFalse();
  }

  @Test
  public void shouldGetSoundedAlarmWhenWhenLoginByCoord() throws Exception {
    awaitKeyboardUnlock();
    loginByCoord();
    awaitKeyboardUnlock();
    assertThat(client.resetAlarm()).isTrue();
  }

  @Test
  public void shouldGetSoundedAlarmWhenWhenLoginByLabel() throws Exception {
    awaitKeyboardUnlock();
    loginByLabel();
    awaitKeyboardUnlock();
    assertThat(client.resetAlarm()).isTrue();
  }

  @Test
  public void shouldGetNotSoundedAlarmWhenWhenLoginAndResetAlarmByCoord() throws Exception {
    awaitKeyboardUnlock();
    loginByCoord();
    awaitKeyboardUnlock();
    client.resetAlarm();
    assertThat(client.resetAlarm()).isFalse();
  }

  @Test
  public void shouldGetNotSoundedAlarmWhenWhenLoginAndResetAlarmByLabel() throws Exception {
    awaitKeyboardUnlock();
    loginByLabel();
    awaitKeyboardUnlock();
    client.resetAlarm();
    assertThat(client.resetAlarm()).isFalse();
  }

  @Test
  public void shouldGetFieldPositionWhenGetCursorPositionAfterConnect() throws Exception {
    Point fieldPosition = new Point(53, 6);
    awaitKeyboardUnlock();
    assertThat(client.getCursorPosition()).isEqualTo(Optional.of(fieldPosition));
  }

  @Test
  public void shouldSendExceptionToExceptionHandlerWhenConnectWithInvalidPort() throws Exception {
    client.connect(SERVICE_HOST, 1);
    exceptionWaiter.awaitException();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendIncorrectFieldPosition()
      throws Exception {
    awaitKeyboardUnlock();
    client.setFieldTextByCoord(0, 1, "test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendIncorrectFieldLabel()
      throws Exception {
    awaitKeyboardUnlock();
    client.setFieldTextByLabel("test", "test");
  }

  @Test
  public void shouldSendCloseToExceptionHandlerWhenServerDown() throws Exception {
    awaitKeyboardUnlock();
    service.stop(TIMEOUT_MILLIS);
    exceptionWaiter.awaitClose();
  }

  @Test
  public void shouldSendExceptionToExceptionHandlerWhenLoginAndServerDownByCoord()
      throws Exception {
    awaitKeyboardUnlock();
    service.stop(TIMEOUT_MILLIS);
    loginByCoord();
    exceptionWaiter.awaitException();
  }

  @Test
  public void shouldSendExceptionToExceptionHandlerWhenLoginAndServerDownByLabel()
      throws Exception {
    awaitKeyboardUnlock();
    service.stop(TIMEOUT_MILLIS);
    loginByLabel();
    exceptionWaiter.awaitException();
  }

  @Test
  public void shouldGetCorrectFieldsWhenGetFields() throws Exception {
    awaitKeyboardUnlock();
    List<TestField> actualFields = client.getFields().stream()
        .map(TestField::new)
        .collect(Collectors.toList());
    assertEquals(buildExpectedFields(), actualFields);
  }

  private List<TestField> buildExpectedFields() {
    List<TestField> testFieldList = new ArrayList<>();
    int fieldsColumn = 52;
    String fieldsText = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    testFieldList.add(new TestField.Builder(5, fieldsColumn, fieldsText).withMonocase().build());
    testFieldList.add(
        new TestField.Builder(6, fieldsColumn, fieldsText).withMonocase().withHighIntensity()
            .withReverseImage()
            .build());
    testFieldList.add(new TestField.Builder(7, fieldsColumn, fieldsText).build());
    testFieldList.add(new TestField.Builder(8, fieldsColumn, fieldsText).build());
    testFieldList.add(new TestField.Builder(9, fieldsColumn, fieldsText).build());
    return testFieldList;
  }

  private static class TestField {

    private static final byte FIELD_FORMAT_MASK = 0x40;
    private static final byte MONOCASE_MASK = 0x20;
    private static final byte SCREEN_ATTRIBUTE_MASK = 0x20;
    private static final byte UNDERSCORE_MASK = 0x04;
    private static final byte HIGH_INTENSITY_MASK = 0x02;
    private static final byte REVERSE_IMAGE_MASK = 0x01;

    private final int row;
    private final int column;
    private final String text;
    private final byte[] ffw;
    private final byte[] fcw;
    private final int attr;

    private TestField(Builder builder) {
      row = builder.row;
      column = builder.column;
      attr = builder.attr;
      text = builder.text;
      ffw = builder.ffw;
      fcw = builder.fcw;
    }

    private TestField(XI5250Field field) {
      column = field.getCol();
      row = field.getRow();
      attr = field.getAttr();
      text = field.getString();
      ffw = field.getFFW();
      fcw = field.getFCW();
    }

    private static final class Builder {

      private final int row;
      private final int column;
      private final String text;
      private final byte[] fcw = {0, 0};
      private byte[] ffw = {FIELD_FORMAT_MASK, 0};
      private byte attr = SCREEN_ATTRIBUTE_MASK | UNDERSCORE_MASK;

      private Builder(int row, int column, String text) {
        this.row = row;
        this.column = column;
        this.text = text;
      }

      private Builder withMonocase() {
        ffw[1] |= MONOCASE_MASK;
        return this;
      }

      private Builder withHighIntensity() {
        attr |= HIGH_INTENSITY_MASK;
        return this;
      }

      private Builder withReverseImage() {
        attr |= REVERSE_IMAGE_MASK;
        return this;
      }

      private TestField build() {
        return new TestField(this);
      }

    }

    @Override
    public String toString() {
      return "TestField{" +
          "row=" + row +
          ", column=" + column +
          ", ffw=" + Arrays.toString(ffw) +
          ", fcw=" + Arrays.toString(fcw) +
          ", attr=" + attr +
          ", text='" + text + '\'' +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestField testField = (TestField) o;
      return row == testField.row &&
          column == testField.column &&
          attr == testField.attr &&
          Arrays.equals(ffw, testField.ffw) &&
          Arrays.equals(fcw, testField.fcw) &&
          Objects.equals(text, testField.text);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(row, column, attr, text);
      result = 31 * result + Arrays.hashCode(ffw);
      result = 31 * result + Arrays.hashCode(fcw);
      return result;
    }

  }

  private static class ExceptionWaiter implements ExceptionHandler {

    private CountDownLatch exceptionLatch = new CountDownLatch(1);
    private CountDownLatch closeLatch = new CountDownLatch(1);

    @Override
    public void onException(Throwable ex) {
      exceptionLatch.countDown();
    }

    @Override
    public void onConnectionClosed() {
      closeLatch.countDown();
    }

    private void awaitException() throws InterruptedException {
      assertThat(exceptionLatch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue();
    }

    private void awaitClose() throws InterruptedException {
      assertThat(closeLatch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)).isTrue();
    }

  }

}
