package org.rainfall;

/**
 * @author Aurelien Broszniowski
 */

public class AssertionEvaluator {

  private Assertion actual;
  private Assertion expected;

  public AssertionEvaluator(final Assertion actual, final Assertion expected) {
    this.actual = actual;
    this.expected = expected;
  }

  public void evaluate() {
    actual.evaluate(expected);
  }
}
