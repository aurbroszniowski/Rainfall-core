package org.rainfall.gatling.operation;

import org.rainfall.Assertion;
import org.rainfall.Configuration;
import org.rainfall.Operation;
import org.rainfall.gatling.configuration.HttpConfig;
import org.rainfall.gatling.statistics.HttpResult;
import org.rainfall.statistics.StatisticsManager;
import org.rainfall.statistics.StatisticsObserver;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Aurelien Broszniowski
 */

public class HttpOperation extends Operation {
  private String description;
  private String path = null;
  private StatisticsObserver httpObserver = StatisticsManager.getStatisticObserver("http", HttpResult.class);

  public HttpOperation(final String description) {
    this.description = description;
  }

  public Operation get(final String path) {
    this.path = path;
    return this;
  }

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<Assertion> assertions) {
    String url = null;
    HttpConfig httpConfig = (HttpConfig)configurations.get(HttpConfig.class);
    if (httpConfig != null) {
      url = httpConfig.getUrl();
    }
    if (url == null) {
      throw new RuntimeException("baseURL of org.rainfall.gatling.HttpConfig is missing");
    }

    if (path != null) {
      url += path;
    }
    long start = httpObserver.start();
    System.out.println(">>> Get page for URL  = " + url + " (" + description + ")");
    if (new Random(System.currentTimeMillis()).nextBoolean())
      httpObserver.end(start, HttpResult.OK);
    else
      httpObserver.end(start, HttpResult.KO);
  }

}
