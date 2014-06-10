package org.rainfall;

import java.util.List;

/**
 * @author Aurelien Broszniowski
 */

public abstract class Operation {

  public abstract void exec(final List<Configuration> configurations, final List<Assertion> assertions);
}
