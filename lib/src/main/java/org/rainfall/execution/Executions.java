package org.rainfall.execution;

import org.rainfall.execution.AtOnce;
import org.rainfall.unit.User;

/**
 * @author Aurelien Broszniowski
 */

public class Executions {

  public static AtOnce atOnce(int nb, User users) {
    return new AtOnce(nb, users);
  }

  public static Times times(long occurrences) {
    return new Times(occurrences);
  }
}
