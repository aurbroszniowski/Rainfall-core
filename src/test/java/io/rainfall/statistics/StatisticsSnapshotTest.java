/*
 * Copyright (c) 2026 Aurélien Broszniowski
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

package io.rainfall.statistics;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StatisticsSnapshotTest {

  private enum Result {
    OK
  }

  @Test
  public void peekShouldDrainPeriodicCountersWithoutLosingCumulativeData() {
    Statistics<Result> statistics = new Statistics<Result>("op", Result.values());

    statistics.increaseCounterAndSetLatencyInNs(Result.OK, 1_000_000L);
    statistics.increaseCounterAndSetLatencyInNs(Result.OK, 2_000_000L);
    statistics.increaseCounterAndSetLatencyInNs(Result.OK, 3_000_000L);

    StatisticsPeek<Result> firstPeek = statistics.peek(1L);
    assertThat(firstPeek.getPeriodicCounters(Result.OK), is(3L));
    assertThat(firstPeek.getCumulativeCounters(Result.OK), is(3L));
    assertThat(firstPeek.getPeriodicAverageLatencyInMs(Result.OK), is(2.0d));
    assertThat(firstPeek.getCumulativeAverageLatencyInMs(Result.OK), is(2.0d));

    statistics.increaseCounterAndSetLatencyInNs(Result.OK, 4_000_000L);
    statistics.increaseCounterAndSetLatencyInNs(Result.OK, 6_000_000L);

    StatisticsPeek<Result> secondPeek = statistics.peek(2L);
    assertThat(secondPeek.getPeriodicCounters(Result.OK), is(2L));
    assertThat(secondPeek.getCumulativeCounters(Result.OK), is(5L));
    assertThat(secondPeek.getPeriodicAverageLatencyInMs(Result.OK), is(5.0d));
    assertThat(secondPeek.getCumulativeAverageLatencyInMs(Result.OK), is(3.2d));

    StatisticsPeek<Result> emptyPeek = statistics.peek(3L);
    assertThat(emptyPeek.getPeriodicCounters(Result.OK), is(0L));
    assertThat(emptyPeek.getCumulativeCounters(Result.OK), is(5L));
    assertThat(emptyPeek.getPeriodicAverageLatencyInMs(Result.OK), is(0.0d));
    assertThat(emptyPeek.getCumulativeAverageLatencyInMs(Result.OK), is(3.2d));
  }
}
