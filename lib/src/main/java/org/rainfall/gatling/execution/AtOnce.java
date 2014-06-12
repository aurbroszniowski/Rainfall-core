package org.rainfall.gatling.execution;

import org.rainfall.Assertion;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Operation;
import org.rainfall.Scenario;
import org.rainfall.Unit;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.gatling.unit.User;

import java.util.List;
import java.util.Map;

/**
 * This will execute the {@link Scenario} with {@link AtOnce#nb} occurrences of a specified {@link Unit}
 *
 * @author Aurelien Broszniowski
 */

public class AtOnce extends Execution {
  private final int nb;
  private final User users;

  public AtOnce(final int nb, final User users) {
    this.nb = nb;
    this.users = users;
  }

  public void execute(final int threadNb, final Scenario scenario, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<Assertion> assertions) {
    List<Operation> operations = scenario.getOperations();

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);
    int max = concurrencyConfig.getNbIterationsForThread(threadNb, nb);
    for (int i = 0; i < max; i++) {
      for (Operation operation : operations) {
        operation.exec(configurations, assertions);
      }
    }
  }
}
