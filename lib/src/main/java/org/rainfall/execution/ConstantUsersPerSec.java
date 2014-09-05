package org.rainfall.execution;

import org.rainfall.unit.TimeMeasurement;

import static org.rainfall.unit.TimeMeasurement.every;
import static org.rainfall.unit.Units.seconds;
import static org.rainfall.unit.Units.users;

/**
 * Schedule a Scenario execution for nbUsers every timeMeasurement
 *
 * @author Aurelien Broszniowski
 */

public class ConstantUsersPerSec extends InParallel {

  public ConstantUsersPerSec(final int nbUsers, final TimeMeasurement during) {
    super(nbUsers, users, every(1, seconds), during);
  }

}
