package org.rainfall.configuration;

import org.rainfall.Assertion;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Scenario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Aurelien Broszniowski
 */

public class ConcurrencyConfig extends Configuration {

  private int nbThreads = 1;
  private ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
  private final Map<Integer, AtomicInteger> nbIterationsPerThread = new HashMap<Integer, AtomicInteger>();

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

  public int getNbIterationsForThread(int threadNb, int nbIterations) {
    synchronized (nbIterationsPerThread) {
      if (nbIterationsPerThread.size() == 0) {
        for (int i = 0; i < nbThreads; i++)
          nbIterationsPerThread.put(i, new AtomicInteger());
        int i = 0;
        while (nbIterations > 0) {
          nbIterationsPerThread.get(i % nbThreads).incrementAndGet();
          i++;
          nbIterations--;
        }
      }
    }
    return nbIterationsPerThread.get(threadNb).intValue();
  }

  public void submit(final List<Execution> executions, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<Assertion> assertions) {
    for (final Execution execution : executions) {
      for (int i = 0; i < nbThreads; i++) {
        final int threadNb = i;
        executor.submit(new Runnable() {

          @Override
          public void run() {
            execution.execute(threadNb, scenario, configurations, assertions);
          }
        });
      }
    }
    executor.shutdown();
    try {
      executor.awaitTermination(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Execution of Scenario didn't stop correctly.", e);
    }
  }

  public ExecutorService getExecutor() {
    return executor;
  }
}
