package org.rainfall.gatling.execution;

import org.rainfall.Assertion;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Scenario;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This will do nothing for a certain amount of time.
 *
 * @author Aurelien Broszniowski
 */

public class NothingFor extends Execution {
  private final int nb;
  private final TimeUnit timeUnit;

  public NothingFor(final int nb, final TimeUnit timeUnit) {
    this.nb = nb;
    this.timeUnit = timeUnit;
  }

  @Override
  public void execute(final int threadNb, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<Assertion> assertions) {
    System.out.println(">>> Sleep " + nb);
    try {
      Thread.sleep(timeUnit.toMillis(nb));
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
