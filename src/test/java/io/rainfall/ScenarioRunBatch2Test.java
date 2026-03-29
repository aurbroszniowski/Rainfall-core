/*
 * Copyright (c) 2026 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScenarioRunBatch2Test {

  private enum Result {
    OK
  }

  @Test
  public void firstPeriodicReportShouldWaitForTheFullInterval() {
    ScenarioRun<Result> scenarioRun = new ScenarioRun<Result>(Scenario.scenario("batch2"));

    assertThat(scenarioRun.initialReportDelayInMillis(250L), is(250L));
  }
}
