package org.rainfall.gatling.operation;

import org.rainfall.Assertion;
import org.rainfall.Configuration;
import org.rainfall.Operation;
import org.rainfall.gatling.configuration.HttpConfig;

import java.util.List;

/**
 * @author Aurelien Broszniowski
 */

public class HttpOperation extends Operation {
  private String description;
  private String path = null;

  public HttpOperation(final String description) {
    this.description = description;
  }

  public Operation get(final String path) {
    this.path = path;
    return this;
  }

  @Override
  public void exec(final List<Configuration> configurations, final List<Assertion> assertions) {
    String url = null;
    for (Configuration configuration : configurations) {
      if (configuration instanceof HttpConfig) {
        if (url != null) {
          throw new RuntimeException("baseURL of org.rainfall.gatling.HttpConfig has already been defined");
        }
        url = ((HttpConfig)configuration).getUrl();
      }
    }
    if (url == null) {
      throw new RuntimeException("baseURL of org.rainfall.gatling.HttpConfig is missing");
    }

    if (path != null) {
      url += path;
    }
    System.out.println(">>> Get page for URL  = " + url + " (" + description + ")");
  }
}
