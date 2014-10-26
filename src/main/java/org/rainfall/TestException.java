package org.rainfall;

/**
 * Generic framework exception
 *
 * @author Aurelien Broszniowski
 */
public class TestException extends Exception {

  public TestException(Throwable cause) {
    super(cause);
  }

  public TestException(final String message) {
    super(message);
  }

  public TestException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
