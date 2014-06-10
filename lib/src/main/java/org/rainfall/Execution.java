package org.rainfall;

import java.util.List;

/**
 * This executes a {@link Scenario}, with the specific {@link Configuration}, and {@link Assertion}
 *
 * @author Aurelien Broszniowski
 */

public abstract class Execution {

  public abstract void execute(final Scenario scenario, final List<Configuration> configurations, final List<Assertion> assertions);

}
