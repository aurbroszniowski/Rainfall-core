package org.rainfall.gatling.configuration;

import org.rainfall.Configuration;

/**
 * @author Aurelien Broszniowski
 */

public class HttpConfig extends Configuration {

  private String url;

  public static HttpConfig httpConfig() {
    return new HttpConfig();
  }

  public HttpConfig baseURL(final String url) {
    this.url = url;
    return this;
  }

  public String getUrl() {
    return url;
  }
}
