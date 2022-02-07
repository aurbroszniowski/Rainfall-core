/*
 * Copyright (c) 2014-2022 Aurélien Broszniowski
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

package io.rainfall.execution;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.Execution;
import io.rainfall.Scenario;
import io.rainfall.TestException;
import io.rainfall.statistics.StatisticsHolder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Aurelien Broszniowski
 */

public class RepeatTest {

  @Test
  public void testRepeat() throws TestException {
    StatisticsHolder statisticsHolder = mock(StatisticsHolder.class);
    Scenario scenario = mock(Scenario.class);
    Map<Class<? extends Configuration>, Configuration> configurations = new HashMap<>();
    List<AssertionEvaluator> assertions = new ArrayList<>();

    Execution execution1 = mock(Execution.class);
    Execution execution2 = mock(Execution.class);
    Repeat repeat = new Repeat(3, new Execution[] { execution1, execution2 });
    repeat.execute(statisticsHolder, scenario, configurations, assertions);

    verify(execution1, times(3)).execute(statisticsHolder, scenario, configurations, assertions);
    verify(execution2, times(3)).execute(statisticsHolder, scenario, configurations, assertions);
  }
}
