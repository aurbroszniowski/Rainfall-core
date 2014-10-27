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

package org.rainfall.statistics;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Aurelien Broszniowski
 */

public class StatisticsObserverTest {

/* TODO : fix
  @Test
  public void testCounterAndLatency() throws Exception {
    Task task = mock(Task.class);
    StatisticsObserver observer = spy(new StatisticsObserver(Results.class));
    long start = 55 * 1000000L;
    long okEnd = 125 * 1000000L;
    long koEnd = 140 * 1000000L;
    when(observer.getTime()).thenReturn(start, okEnd, start, koEnd);

    when(task.definition()).thenReturn(Results.OK, Results.KO);
    observer.measure((Task)task);
    observer.measure((Task)task);

    StatisticsHolder<Results> holder = observer.peek();
    Statistics<Results> resultsStatistics = holder.getStatistics();
    double okLatency = (okEnd - start) / 1000000L;
    double koLatency = (koEnd - start) / 1000000L;
    double averageLatency = (okLatency + koLatency) / 2;
    assertThat(resultsStatistics.averageLatencyInMs(), is(equalTo(averageLatency)));
    assertThat(resultsStatistics.sumOfCounters(), is(equalTo(2L)));
    assertThat(resultsStatistics.getLatency(Results.OK), is(equalTo(okLatency)));
    assertThat(resultsStatistics.getCounter(Results.OK), is(equalTo(1L)));
    assertThat(resultsStatistics.getLatency(Results.KO), is(equalTo(koLatency)));
    assertThat(resultsStatistics.getCounter(Results.KO), is(equalTo(1L)));
  }

  //TODO statisticsMap merge

*/

  public enum Results {
    OK, KO
  }
}
