package net.infordata.em;

import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class UnlockWaiter implements XI5250EmulatorListener {

  private static final Logger LOG = LoggerFactory.getLogger(UnlockWaiter.class);
  private static final int STABLE_PERIOD_MILLIS = 1000;

  private final CountDownLatch lock = new CountDownLatch(1);
  private final TerminalClient client;
  private final ScheduledExecutorService stableTimeoutExecutor;
  private ScheduledFuture stableTimeoutTask;
  private boolean ended;
  private boolean isInputInhibited;


  public UnlockWaiter(TerminalClient client, ScheduledExecutorService stableTimeoutExecutor) {
    this.client = client;
    this.stableTimeoutExecutor = stableTimeoutExecutor;
    client.addEmulatorListener(this);
    isInputInhibited = client.isKeyboardLocked();
    if (!isInputInhibited) {
      LOG.debug("Start stable period since input is not inhibited");
      startStablePeriod();
    }
  }

  private synchronized void startStablePeriod() {
    if (ended) {
      return;
    }
    endStablePeriod();
    stableTimeoutTask = stableTimeoutExecutor
            .schedule(lock::countDown, STABLE_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
  }

  private synchronized void endStablePeriod() {
    if (stableTimeoutTask != null) {
      stableTimeoutTask.cancel(false);
    }
  }

  public void await(long timeoutMillis) throws InterruptedException, TimeoutException {
    try {
      if (!lock.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
        throw new TimeoutException();
      }
    } finally {
      cancelWait();
      client.removeEmulatorListener(this);
    }
  }

  private synchronized void cancelWait() {
    ended = true;
    lock.countDown();
    endStablePeriod();
  }

  @Override
  public void connecting(XI5250EmulatorEvent event) {
  }

  @Override
  public void connected(XI5250EmulatorEvent event) {
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    boolean wasInputInhibited = isInputInhibited;
    isInputInhibited = client.isKeyboardLocked();
    if (isInputInhibited != wasInputInhibited) {
      if (isInputInhibited) {
        LOG.debug("Cancel stable period since input has been inhibited");
        endStablePeriod();
      } else {
        LOG.debug("Start stable period since input is no longer inhibited");
        startStablePeriod();
      }
    }
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent event) {
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent event) {
  }

  @Override
  public void dataSended(XI5250EmulatorEvent event) {
  }

}