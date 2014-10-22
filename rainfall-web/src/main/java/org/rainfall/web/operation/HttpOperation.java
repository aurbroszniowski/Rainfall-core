/*
 * Copyright 2014 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rainfall.web.operation;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Operation;
import org.rainfall.TestException;
import org.rainfall.statistics.StatisticsObserver;
import org.rainfall.statistics.StatisticsObserversFactory;
import org.rainfall.statistics.Task;
import org.rainfall.web.configuration.HttpConfig;
import org.rainfall.web.statistics.HttpResult;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Broszniowski
 */

public class HttpOperation extends Operation {
  private String description;
  private String path = null;
  private StatisticsObserver<HttpResult> httpObserver =
      StatisticsObserversFactory.<HttpResult>getInstance().getStatisticObserver("http", HttpResult.class);
  private HttpRequest operation;
  private List<NameValuePair> queryParams = new ArrayList<NameValuePair>();

  public HttpOperation(String description) {
    this.description = description;
  }

  public HttpOperation get(String path) {
    this.path = path;
    this.operation = HttpRequest.GET;
    return this;
  }

  public HttpOperation post(String path) {
    this.path = path;
    this.operation = HttpRequest.POST;
    return this;
  }

  public HttpOperation queryParam(String key, String value) {
    queryParams.add(new BasicNameValuePair(key, value));
    return this;
  }

  @Override
  public void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {
    String url = null;
    HttpConfig httpConfig = (HttpConfig)configurations.get(HttpConfig.class);
    if (httpConfig != null) {
      url = httpConfig.getUrl();
    }
    if (url == null) {
      throw new TestException("baseURL of org.rainfall.web.HttpConfig is missing");
    }
    final HttpClient client = HttpClientBuilder.create().build();

    if (path != null) {
      url += path;
    }

    final String finalUrl = url;
    this.httpObserver.measure(new Task<HttpResult>() {
      @Override
      public HttpResult definition() throws Exception {

        HttpResponse response = client.execute(httpRequest(finalUrl));
        if (response.getStatusLine().getStatusCode() == 200)
          return HttpResult.OK;
        else
          return HttpResult.KO;
      }
    });

    //TODO : evaluate assertions
  }

  private HttpRequestBase httpRequest(final String finalUrl) {
    try {
      if (HttpRequest.GET.equals(this.operation)) {
        HttpGet request = new HttpGet(new URIBuilder(finalUrl).setParameters(this.queryParams).build());
        return request;
      } else if (HttpRequest.POST.equals(this.operation)) {
        HttpPost request = new HttpPost(new URIBuilder(finalUrl).setParameters(this.queryParams).build());
        return request;
      }
    } catch (URISyntaxException e) {
      return null;
    }
    return null;
  }

  public enum HttpRequest {
    GET, POST
  }
}
