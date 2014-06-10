package org.rainfall.gatling;

import org.rainfall.gatling.unit.User;

import java.util.concurrent.TimeUnit;

/**
 * This bookkeeping class contains the instances of Gatling {@link org.rainfall.Unit} classes.
 * A Unit class defines the execution of an {@link org.rainfall.Operation} according to its parameters
 *
 * @author Aurelien Broszniowski
 */

public class GatlingUnits {

  public static User users = new User();

  public static TimeUnit seconds = TimeUnit.SECONDS;

}
