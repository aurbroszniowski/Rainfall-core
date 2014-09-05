package org.rainfall.unit;

import org.rainfall.Unit;

import java.util.concurrent.TimeUnit;

/**
 * This bookkeeping class contains the instances of {@link org.rainfall.Unit} classes.
 * A Unit class defines the execution of an {@link org.rainfall.Operation} according to its parameters
 *
 * @author Aurelien Broszniowski
 */

public class Units {

  public static Unit users = new User();

  public static TimeDivision seconds = new TimeDivision(TimeUnit.SECONDS);

  public static TimeDivision minutes = new TimeDivision(TimeUnit.MINUTES);

}
