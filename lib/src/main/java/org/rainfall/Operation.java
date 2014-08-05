package org.rainfall;

import java.util.List;
import java.util.Map;

/**
 * A step executed in the {@link Scenario}
 *
 * @author Aurelien Broszniowski
 */

public abstract class Operation {

  public abstract void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions);
}
