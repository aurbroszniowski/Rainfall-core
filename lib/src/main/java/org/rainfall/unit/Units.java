package org.rainfall.unit;

import org.rainfall.unit.TimeDivision;
import org.rainfall.unit.User;

import java.util.concurrent.TimeUnit;

/**
 * This bookkeeping class contains the instances of Gatling {@link org.rainfall.Unit} classes.
 * A Unit class defines the execution of an {@link org.rainfall.Operation} according to its parameters
 *
 * @author Aurelien Broszniowski
 */

public class Units {

  public static User users = new User();

  public static TimeDivision seconds = new TimeDivision(TimeUnit.SECONDS);

}
