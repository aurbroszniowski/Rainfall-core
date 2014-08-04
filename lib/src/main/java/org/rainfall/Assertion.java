package org.rainfall;

/**
 * This is an assertion to be verified on the {@link Scenario} during the {@link ScenarioRun}
 *
 * @author Aurelien Broszniowski
 */

public abstract class Assertion {

  public abstract void evaluate(final Assertion assertion) throws AssertionError;

}
