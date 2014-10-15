package org.rainfall.web.operation;

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Operation;
import org.rainfall.statistics.StatisticsObserversFactory;
import org.rainfall.statistics.StatisticsObserver;
import org.rainfall.statistics.Task;
import org.rainfall.web.configuration.HttpConfig;
import org.rainfall.web.statistics.HttpResult;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Aurelien Broszniowski
 */

public class HttpOperation extends Operation {
  private String description;
  private String path = null;
  private StatisticsObserver<HttpResult> httpObserver = StatisticsObserversFactory.<HttpResult>getInstance().getStatisticObserver("http", HttpResult.class);

  public HttpOperation(final String description) {
    this.description = description;
  }

  public Operation get(final String path) {
    this.path = path;
    return this;
  }

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) {
    String url = null;
    HttpConfig httpConfig = (HttpConfig)configurations.get(HttpConfig.class);
    if (httpConfig != null) {
      url = httpConfig.getUrl();
    }
    if (url == null) {
      throw new RuntimeException("baseURL of org.rainfall.web.HttpConfig is missing");
    }

    if (path != null) {
      url += path;
    }

    this.httpObserver.measure(new Task<HttpResult>() {
      @Override
      public HttpResult definition() throws Exception {
        Thread.sleep(new Random(System.currentTimeMillis()).nextInt(500));
        if (new Random(System.currentTimeMillis()).nextBoolean())
          return HttpResult.OK;
        else
          return HttpResult.KO;
      }
    });

    //TODO : evaluate assertions
  }

}
