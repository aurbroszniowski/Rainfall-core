package org.rainfall.web.assertion;

import org.rainfall.Assertion;
import org.rainfall.Unit;

/**
 * @author Aurelien Broszniowski
 */

public class LessThanComparator extends Assertion {

  private final long value;
  private final Unit unit;

  public LessThanComparator(final long value, final Unit unit) {
    this.value = value;
    this.unit = unit;
  }

  @Override
  public void evaluate(final Assertion assertion) throws AssertionError {
    //TODO : implement? or reuse existing assertion evaluators ?
    throw new UnsupportedOperationException();
  }

}
