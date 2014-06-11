package org.rainfall.gatling;

import org.rainfall.gatling.operation.HttpOperation;

/**
 * @author Aurelien Broszniowski
 */

public class GatlingOperations {

  public static HttpOperation http(final String description) {
    return new HttpOperation(description);
  }

}
