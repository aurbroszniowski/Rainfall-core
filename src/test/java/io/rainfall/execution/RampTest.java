/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
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
import io.rainfall.Scenario;
import io.rainfall.TestException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.unit.From;
import io.rainfall.unit.Instance;
import io.rainfall.unit.Over;
import io.rainfall.unit.TimeDivision;
import io.rainfall.unit.To;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Aurelien Broszniowski
 */

public class RampTest {

  @Test
  public void testNormalRampup() throws TestException {
    int startThreadCount = 2;
    int maxThreadCount = startThreadCount + 4;
    int executionLength = 8;

    final Ramp ramp = new Ramp(From.from(startThreadCount, Instance.instances), To.to(maxThreadCount, Instance.instances), Over
        .over(executionLength, TimeDivision.seconds));

    StatisticsHolder statisticsHolder = mock(StatisticsHolder.class);
    Scenario scenario = mock(Scenario.class);
    Map<Class<? extends Configuration>, Configuration> configurations = new HashMap<Class<? extends Configuration>, Configuration>();
    ConcurrencyConfig concurrencyConfig = mock(ConcurrencyConfig.class);
    Map<String, ScheduledExecutorService> schedulers = new HashMap<>();
    ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    schedulers.put(ConcurrencyConfig.defaultThreadpoolname, scheduler);
    when(concurrencyConfig.createScheduledExecutorService()).thenReturn(schedulers);
    configurations.put(ConcurrencyConfig.class, concurrencyConfig);

    List<AssertionEvaluator> assertions = new ArrayList<AssertionEvaluator>();

    ramp.scheduleThreads(statisticsHolder, scenario, configurations, assertions, new AtomicBoolean(), schedulers);

    verify(scheduler).schedule(any(Callable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
    verify(scheduler).schedule(any(Callable.class), eq(2000L), eq(TimeUnit.MILLISECONDS));
    verify(scheduler).schedule(any(Callable.class), eq(4000L), eq(TimeUnit.MILLISECONDS));
    verify(scheduler).schedule(any(Callable.class), eq(6000L), eq(TimeUnit.MILLISECONDS));

  }


}
