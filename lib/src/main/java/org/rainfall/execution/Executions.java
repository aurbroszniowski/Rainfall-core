package org.rainfall.execution;

import org.rainfall.Unit;
import org.rainfall.unit.TimeMeasurement;

/**
 * @author Aurelien Broszniowski
 */

public class Executions {

  public static AtOnce atOnce(int nb, Unit users) {
    return new AtOnce(nb, users);
  }

  public static Times times(long occurrences) {
    return new Times(occurrences);
  }

  public static InParallel inParallel(int nb, Unit unit, TimeMeasurement every, TimeMeasurement during) {
    return new InParallel(nb, unit, every, during);
  }

  public static ConstantUsersPerSec constantUsersPerSec(int nbUsers, TimeMeasurement timeMeasurement) {
    return new ConstantUsersPerSec(nbUsers, timeMeasurement);
  }
}
