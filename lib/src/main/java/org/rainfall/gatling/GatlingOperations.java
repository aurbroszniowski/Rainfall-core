package org.rainfall.gatling;

import org.rainfall.Unit;
import org.rainfall.gatling.execution.AtOnce;
import org.rainfall.gatling.execution.NothingFor;
import org.rainfall.gatling.operation.HttpOperation;
import org.rainfall.gatling.unit.User;

import java.util.concurrent.TimeUnit;

/**
 * @author Aurelien Broszniowski
 */

public class GatlingOperations {

  public static HttpOperation http(final String description) {
    return new HttpOperation(description);
  }

  public static NothingFor nothingFor(int nb, TimeUnit timeUnit) {
    return new NothingFor(nb, timeUnit);
  }

  public static AtOnce atOnce(int nb, User users) {
    return new AtOnce(nb, users);
  }

}
