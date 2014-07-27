package org.rainfall.web;

import org.rainfall.web.operation.HttpOperation;

/**
 * @author Aurelien Broszniowski
 */

public class WebOperations {

  public static HttpOperation http(final String description) {
    return new HttpOperation(description);
  }

}
