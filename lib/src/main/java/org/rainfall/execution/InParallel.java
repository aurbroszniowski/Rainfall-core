package org.rainfall.execution;

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Operation;
import org.rainfall.Scenario;
import org.rainfall.Unit;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.unit.TimeMeasurement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * TODO : Unit test
 * @author Aurelien Broszniowski
 */

public class InParallel extends Execution {
  protected final int nb;
  protected final Unit unit;
  protected final TimeMeasurement every;
  protected final TimeMeasurement during;

  public InParallel(final int nb, final Unit unit, final TimeMeasurement every, final TimeMeasurement during) {
    this.nb = nb;
    this.unit = unit;
    this.every = every;
    this.during = during;
  }

  @Override
  public void execute(final int threadNb, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) {
    final ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    // Use a scheduled thread pool in order to execute concurrent Scenarios
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(concurrencyConfig.getNbThreads());

    // Schedule the scenario every second, until
    final ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        int max = concurrencyConfig.getNbIterationsForThread(threadNb, nb);
        for (int i = 0; i < max; i++) {
          for (Operation operation : scenario.getOperations()) {
            operation.exec(configurations, assertions);
          }
        }
      }
    }, 0, every.getNb(), every.getTimeDivision().getTimeUnit());
    // Schedule the end of the execution after the time entered as parameter
    scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        future.cancel(true);
      }
    }, during.getNb(), during.getTimeDivision().getTimeUnit());
    try {
      future.get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
