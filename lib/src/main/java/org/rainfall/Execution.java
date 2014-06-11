package org.rainfall;

import java.util.List;
import java.util.Map;

/**
 * This executes a {@link Scenario}, with the specific {@link Configuration}, and {@link Assertion}
 *
 * @author Aurelien Broszniowski
 */

public abstract class Execution {

  public abstract void execute(final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<Assertion> assertions);

}
