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

package io.rainfall.statistics;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Aurelien Broszniowski
 */
public class StatisticsPeekTest {

  @Test
  public void periodicAverageLatency() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(5L);

    assertThat(peek.getPeriodicAverageLatencyInMs(two), is((105d + 34d + 97d) / 3));
  }

  @Test
  public void cumulativeAverageLatency() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(5L);

    assertThat(peek.getCumulativeAverageLatencyInMs(two), is((105d + 34d + 97d) / 3));
  }

  @Test
  public void periodicAverageLatencyAfterTwoPeeks() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    statistics.peek(5L);

    statistics.increaseCounterAndSetLatencyInNs(two, 25 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 94 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 37 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(8L);

    assertThat(peek.getPeriodicAverageLatencyInMs(two), is((25d + 94d + 37d) / 3));
  }

  @Test
  public void cumulativeAverageLatencyAfterTwoPeeks() {
    Result one = Result.ONE;
    Result two = Result.TWO;
    Result three = Result.THREE;
    final Result[] keys = new Result[] { one, two, three };
    Statistics<Result> statistics = new Statistics<Result>("test", keys, 100);

    statistics.increaseCounterAndSetLatencyInNs(two, 105 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 34 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 97 * 1000000L);

    statistics.peek(5L);

    statistics.increaseCounterAndSetLatencyInNs(two, 25 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 94 * 1000000L);
    statistics.increaseCounterAndSetLatencyInNs(two, 37 * 1000000L);

    StatisticsPeek<Result> peek = statistics.peek(8L);

    assertThat(round(peek.getCumulativeAverageLatencyInMs(two)), is(round((105d + 34d + 97d + 25d + 94d + 37d) / 6)));
  }

  @Test
  public void addAllShouldUseWeightedLatencyFromRawTotals() {
    StatisticsPeek<Result> firstPeek = new StatisticsPeek<Result>("first", Result.values(), 1L);
    firstPeek.setPeriodicValues(1_000_000_000L, Result.values(), new long[] {1L, 0L, 0L},
        new long[] {10_000_000L, 0L, 0L});
    firstPeek.setCumulativeValues(1_000_000_000L, Result.values(), new long[] {1L, 0L, 0L},
        new long[] {10_000_000L, 0L, 0L});

    StatisticsPeek<Result> secondPeek = new StatisticsPeek<Result>("second", Result.values(), 1L);
    secondPeek.setPeriodicValues(1_000_000_000L, Result.values(), new long[] {9L, 0L, 0L},
        new long[] {9_000_000L, 0L, 0L});
    secondPeek.setCumulativeValues(1_000_000_000L, Result.values(), new long[] {9L, 0L, 0L},
        new long[] {9_000_000L, 0L, 0L});

    Map<String, StatisticsPeek<Result>> peeks = new LinkedHashMap<String, StatisticsPeek<Result>>();
    peeks.put(firstPeek.getName(), firstPeek);
    peeks.put(secondPeek.getName(), secondPeek);

    StatisticsPeek<Result> totalPeek = new StatisticsPeek<Result>("ALL", Result.values(), 1L);
    totalPeek.addAll(peeks);

    assertThat(totalPeek.getSumOfPeriodicCounters(), is(10L));
    assertThat(totalPeek.getSumOfCumulativeCounters(), is(10L));
    assertThat(totalPeek.getAverageOfPeriodicAverageLatencies(), is(1.9d));
    assertThat(totalPeek.getAverageOfCumulativeAverageLatencies(), is(1.9d));
    assertThat(totalPeek.getPeriodicAverageLatencyInMs(Result.ONE), is(1.9d));
    assertThat(totalPeek.getCumulativeAverageLatencyInMs(Result.ONE), is(1.9d));
  }

  private Double round(final Double value) {
    return Math.round(value * 10000.0) / 10000.0;
  }


  enum Result {ONE, TWO, THREE}
}
