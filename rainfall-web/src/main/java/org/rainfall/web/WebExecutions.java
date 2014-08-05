package org.rainfall.web;

import org.rainfall.execution.AtOnce;
import org.rainfall.web.execution.NothingFor;
import org.rainfall.unit.TimeDivision;
import org.rainfall.unit.User;

/**
 * @author Aurelien Broszniowski
 */

public class WebExecutions {

  public static NothingFor nothingFor(int nb, TimeDivision timeDivision) {
    return new NothingFor(nb, timeDivision);
  }

}
