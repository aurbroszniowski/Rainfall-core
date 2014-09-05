package org.rainfall.unit;

import org.rainfall.Unit;

import java.util.concurrent.TimeUnit;

/**
 * @author Aurelien Broszniowski
 */

public class TimeMeasurement extends Unit {

  private final int nb;
  private final TimeDivision timeDivision;

  public TimeMeasurement(final int nb, final TimeDivision timeDivision) {
    this.nb = nb;
    this.timeDivision = timeDivision;
  }

  public static TimeMeasurement during(int nb, TimeDivision timeDivision) {
    return new TimeMeasurement(nb, timeDivision);
  }

  public static TimeMeasurement every(int nb, TimeDivision timeDivision) {
    return new TimeMeasurement(nb, timeDivision);
  }

  public int getNb() {
    return nb;
  }

  public TimeDivision getTimeDivision() {
    return timeDivision;
  }
}
