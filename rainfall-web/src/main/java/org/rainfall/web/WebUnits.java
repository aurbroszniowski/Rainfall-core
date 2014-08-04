package org.rainfall.web;

import org.rainfall.web.unit.TimeDivision;
import org.rainfall.web.unit.User;

import java.util.concurrent.TimeUnit;

/**
 * This bookkeeping class contains the instances of Gatling {@link org.rainfall.Unit} classes.
 * A Unit class defines the execution of an {@link org.rainfall.Operation} according to its parameters
 *
 * @author Aurelien Broszniowski
 */

public class WebUnits {

  public static User users = new User();

  public static TimeDivision seconds = new TimeDivision(TimeUnit.SECONDS);

}
