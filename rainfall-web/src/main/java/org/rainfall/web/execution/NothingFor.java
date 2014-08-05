package org.rainfall.web.execution;

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Execution;
import org.rainfall.Scenario;
import org.rainfall.unit.TimeDivision;

import java.util.List;
import java.util.Map;

/**
 * This will do nothing for a certain amount of time.
 *
 * @author Aurelien Broszniowski
 */

public class NothingFor extends Execution {
  private final int nb;
  private final TimeDivision timeDivision;

  public NothingFor(final int nb, final TimeDivision timeDivision) {
    this.nb = nb;
    this.timeDivision = timeDivision;
  }

  @Override
  public void execute(final int threadNb, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) {
    System.out.println(">>> Sleep " + nb);
    try {
      Thread.sleep(timeDivision.getTimeUnit().toMillis(nb));
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}
