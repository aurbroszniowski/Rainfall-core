package org.rainfall.web;

import org.rainfall.web.execution.AtOnce;
import org.rainfall.web.execution.NothingFor;
import org.rainfall.web.unit.TimeDivision;
import org.rainfall.web.unit.User;

/**
 * @author Aurelien Broszniowski
 */

public class WebExecutions {

  public static NothingFor nothingFor(int nb, TimeDivision timeDivision) {
    return new NothingFor(nb, timeDivision);
  }

  public static AtOnce atOnce(int nb, User users) {
    return new AtOnce(nb, users);
  }

}
