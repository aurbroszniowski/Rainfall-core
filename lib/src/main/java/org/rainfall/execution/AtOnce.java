package org.rainfall.execution;

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Operation;
import org.rainfall.Scenario;
import org.rainfall.Unit;
import org.rainfall.configuration.ConcurrencyConfig;

import java.util.List;
import java.util.Map;

/**
 * This will execute the {@link Scenario} with {@link AtOnce#nb} occurrences of a specified {@link org.rainfall.Unit}
 *
 * @author Aurelien Broszniowski
 */

public class AtOnce extends Execution {
  private final int nb;
  private final Unit unit;

  public AtOnce(final int nb, final Unit unit) {
    this.nb = nb;
    this.unit = unit;
  }

  public void execute(final int threadNb, final Scenario scenario,
                      final Map<Class<? extends Configuration>, Configuration> configurations,
                      final List<AssertionEvaluator> assertions) {

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int max = concurrencyConfig.getNbIterationsForThread(threadNb, nb);
    for (int i = 0; i < max; i++) {
      for (Operation operation : scenario.getOperations()) {
        operation.exec(configurations, assertions);
      }
    }
  }
}
