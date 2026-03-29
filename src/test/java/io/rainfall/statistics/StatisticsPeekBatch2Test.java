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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StatisticsPeekBatch2Test {

  private enum Result {
    OK
  }

  @Test
  public void addAllShouldUseWeightedLatencyFromRawTotals() {
    StatisticsPeek<Result> firstPeek = new StatisticsPeek<Result>("first", Result.values(), 1L);
    firstPeek.setPeriodicValues(1_000_000_000L, Result.values(), new long[] {1L}, new long[] {10_000_000L});
    firstPeek.setCumulativeValues(1_000_000_000L, Result.values(), new long[] {1L}, new long[] {10_000_000L});

    StatisticsPeek<Result> secondPeek = new StatisticsPeek<Result>("second", Result.values(), 1L);
    secondPeek.setPeriodicValues(1_000_000_000L, Result.values(), new long[] {9L}, new long[] {9_000_000L});
    secondPeek.setCumulativeValues(1_000_000_000L, Result.values(), new long[] {9L}, new long[] {9_000_000L});

    Map<String, StatisticsPeek<Result>> peeks = new LinkedHashMap<String, StatisticsPeek<Result>>();
    peeks.put(firstPeek.getName(), firstPeek);
    peeks.put(secondPeek.getName(), secondPeek);

    StatisticsPeek<Result> totalPeek = new StatisticsPeek<Result>("ALL", Result.values(), 1L);
    totalPeek.addAll(peeks);

    assertThat(totalPeek.getSumOfPeriodicCounters(), is(10L));
    assertThat(totalPeek.getSumOfCumulativeCounters(), is(10L));
    assertThat(totalPeek.getAverageOfPeriodicAverageLatencies(), is(1.9d));
    assertThat(totalPeek.getAverageOfCumulativeAverageLatencies(), is(1.9d));
    assertThat(totalPeek.getPeriodicAverageLatencyInMs(Result.OK), is(1.9d));
    assertThat(totalPeek.getCumulativeAverageLatencyInMs(Result.OK), is(1.9d));
  }
}
