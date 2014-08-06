package org.rainfall.execution;

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Operation;
import org.rainfall.Scenario;
import org.rainfall.configuration.ConcurrencyConfig;

import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Broszniowski
 */

public class Times extends Execution {

  private final long occurrences;

  public Times(final long occurrences) {
    this.occurrences = occurrences;
  }

  @Override
  public void execute(final int threadNb, final Scenario scenario, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) {
    List<Operation> operations = scenario.getOperations();

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int max = concurrencyConfig.getNbIterationsForThread(threadNb, occurrences);
    for (int i = 0; i < max; i++) {
      for (Operation operation : operations) {
        operation.exec(configurations, assertions);
      }
    }
  }
}
