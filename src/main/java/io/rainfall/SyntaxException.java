package io.rainfall;

/**
 * Generic framework exception
 *
 * @author Aurelien Broszniowski
 */
public class SyntaxException extends Exception {

  public SyntaxException(Throwable cause) {
    super(cause);
  }

  public SyntaxException(final String message) {
    super("Test syntax problem. " + message);
  }

  public SyntaxException(final String message, final Throwable cause) {
    super("Test syntax problem. " + message, cause);
  }
}
