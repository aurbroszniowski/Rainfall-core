package org.rainfall.web;

import org.rainfall.web.execution.AtOnce;
import org.rainfall.web.execution.NothingFor;
import org.rainfall.web.unit.User;

import java.util.concurrent.TimeUnit;

/**
 * @author Aurelien Broszniowski
 */

public class WebExecutions {

  public static NothingFor nothingFor(int nb, TimeUnit timeUnit) {
    return new NothingFor(nb, timeUnit);
  }

  public static AtOnce atOnce(int nb, User users) {
    return new AtOnce(nb, users);
  }

}
