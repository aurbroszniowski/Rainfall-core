package org.rainfall.unit;

import org.rainfall.Unit;

import java.util.concurrent.TimeUnit;

/**
 * @author Aurelien Broszniowski
 */

public class TimeDivision extends Unit {

  private TimeUnit timeUnit;

  public TimeDivision(final TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }
}
