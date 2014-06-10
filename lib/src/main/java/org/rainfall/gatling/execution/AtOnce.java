package org.rainfall.gatling.execution;

import org.rainfall.Assertion;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Operation;
import org.rainfall.Scenario;
import org.rainfall.Unit;
import org.rainfall.gatling.unit.User;

import java.util.List;

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

  public void execute(final Scenario scenario, final List<Configuration> configurations, final List<Assertion> assertions) {
    List<Operation> operations = scenario.getOperations();
    for (Operation operation : operations) {
      for (int i=0; i< nb; i++) {
        operation.exec(configurations, assertions);
      }
    }
  }

}
