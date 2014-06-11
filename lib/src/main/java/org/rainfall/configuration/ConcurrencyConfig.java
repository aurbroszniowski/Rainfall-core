package org.rainfall.configuration;

import org.rainfall.Assertion;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Scenario;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Aurelien Broszniowski
 */

public class ConcurrencyConfig extends Configuration {

  private int nbThreads = 1;
  private ExecutorService executor = Executors.newFixedThreadPool(nbThreads);

  public static ConcurrencyConfig concurrencyConfig() {
    return new ConcurrencyConfig();
  }

  public ConcurrencyConfig threads(final int nbThreads) {
    this.nbThreads = nbThreads;
    this.executor = Executors.newFixedThreadPool(nbThreads);
    return this;
  }

  public int getNbThreads() {
    return nbThreads;
  }

  public void submit(final List<Execution> executions, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<Assertion> assertions) {
    for (final Execution execution : executions) {
      for (int i = 0; i < nbThreads; i++) {
        executor.submit(new Runnable() {

          @Override
          public void run() {
            execution.execute(scenario, configurations, assertions);
          }
        });
      }
    }
    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Execution of Scenario didn't stop correctly", e);
    }
  }
}
