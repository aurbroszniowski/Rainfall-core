package org.rainfall;

import org.junit.Test;
import org.rainfall.configuration.ConcurrencyConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Aurelien Broszniowski
 */

public class ScenarioRunTest {

  @Test
  public void testCorrectInstantiation() {
    Runner runner = mock(Runner.class);
    Scenario scenario = mock(Scenario.class);
    ScenarioRun scenarioRun = new ScenarioRun(runner, scenario);

    assertThat(scenarioRun.getConfiguration(ConcurrencyConfig.class), is(notNullValue()));
  }
}
