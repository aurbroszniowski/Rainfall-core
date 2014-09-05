package org.rainfall.web;

import org.rainfall.web.execution.NothingFor;
import org.rainfall.unit.TimeDivision;

/**
 * @author Aurelien Broszniowski
 */

public class WebExecutions {

  public static NothingFor nothingFor(int nb, TimeDivision timeDivision) {
    return new NothingFor(nb, timeDivision);
  }

}
