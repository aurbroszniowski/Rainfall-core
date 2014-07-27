package org.rainfall;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.rainfall.configuration.ConcurrencyConfig;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doAnswer;
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

  @Test
  public void testTimeoutOnScenario() {
    Runner runner = mock(Runner.class);
    Scenario scenario = mock(Scenario.class);
    ScenarioRun scenarioRun = new ScenarioRun(runner, scenario);
//    ConcurrencyConfig concurrencyConfig = mock(ConcurrencyConfig.class);
//    when(concurrencyConfig.getTimeoutInSeconds()).thenReturn(4L);
    ((ConcurrencyConfig)scenarioRun.getConfiguration(ConcurrencyConfig.class)).timeout(4, SECONDS);
    Execution execution = mock(Execution.class);
    scenarioRun.executed(execution);

    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(final InvocationOnMock invocationOnMock) throws Throwable {
        System.out.println("gonna sleep");
        Thread.sleep(SECONDS.toMillis(10));
        System.out.println("end of sleep");
        return null;
      }
    }).when(execution).execute(anyInt(), any(Scenario.class), anyMap(), anyList());

    try {
      scenarioRun.start();
      fail("The timeout should have occured");
    } catch (RuntimeException e) {
      // expected
    }

  }
}
