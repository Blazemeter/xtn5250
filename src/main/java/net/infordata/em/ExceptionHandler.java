package net.infordata.em;

/**
 * Interface to be invoked when some exceptional case arises.
 */
public interface ExceptionHandler {

  /**
   * Method invoked when an {@link Exception} is thrown.
   *
   * @param ex Exception thrown while connected to the terminal server.
   */
  void onException(Throwable ex);

  /**
   * Method invoked when connection is closed by terminal server.
   */
  void onConnectionClosed();

}
