package org.rainfall;

/**
 * This is the Scenario runner
 * It takes the scenario(s), the configuration(s) and the assertion(s) and executes them
 *
 * @author Aurelien Broszniowski
 */

public class Runner {

  // add scenario beExecuted
  public static ScenarioRun setUp(Scenario scenario) {
    return new ScenarioRun(new Runner(), scenario);
  }


  public void start(final ScenarioRun run) {
  }
}
