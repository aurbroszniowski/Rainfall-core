package org.rainfall;

import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Broszniowski
 */

public abstract class Operation {

  public abstract void exec(final Map<Class<? extends Configuration>, Configuration> configurations, final List<Assertion> assertions);
}
