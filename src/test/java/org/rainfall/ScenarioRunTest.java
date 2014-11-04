/*
 * Copyright 2014 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rainfall;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.statistics.RuntimeStatisticsObserversHolder;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
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
    ScenarioRun scenarioRun = new ScenarioRun(scenario);

    assertThat(scenarioRun.getConfiguration(ConcurrencyConfig.class), is(notNullValue()));
  }

  @Test
  public void cantDefineExecutionsTwice() throws SyntaxException {
    Scenario scenario = mock(Scenario.class);
    ScenarioRun scenarioRun = new ScenarioRun(scenario);
    Execution execution1 = mock(Execution.class);
    Execution execution2 = mock(Execution.class);
    scenarioRun.executed(execution1);
    try {
      scenarioRun.executed(execution2);
      throw new AssertionError("Should have thrown an exception");
    } catch (SyntaxException e) {
      // expected
    }
  }

  @Test
  @Ignore
  public void testTimeoutOnScenario() throws TestException, SyntaxException {
    Runner runner = mock(Runner.class);
    Scenario scenario = mock(Scenario.class);
    ScenarioRun scenarioRun = new ScenarioRun(scenario);
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
    }).when(execution).execute(any(RuntimeStatisticsObserversHolder.class), any(Scenario.class), anyMap(), anyList());

    try {
      scenarioRun.start();
      fail("The timeout should have occured");
    } catch (RuntimeException e) {
      // expected
    }

  }
}
